package graph.scc;

import graph.utils.*;
import java.util.*;

/**
 * Implementation of Kosaraju's algorithm for finding Strongly Connected Components (SCC).
 * Time Complexity: O(V + E)
 * Space Complexity: O(V)
 */
public class KosarajuSCC {
    private final Graph graph;
    private final Metrics metrics;
    
    private boolean[] visited;
    private List<List<Integer>> sccs;
    
    /**
     * Constructor for Kosaraju's SCC algorithm.
     * @param graph Input directed graph
     * @param metrics Metrics tracker
     */
    public KosarajuSCC(Graph graph, Metrics metrics) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException("Kosaraju's algorithm requires a directed graph");
        }
        this.graph = graph;
        this.metrics = metrics;
    }
    
    /**
     * Find all strongly connected components in the graph.
     * @return SCCResult containing components and condensation graph
     */
    public TarjanSCC.SCCResult findSCCs() {
        metrics.startTimer();
        
        int n = graph.getVertices();
        visited = new boolean[n];
        sccs = new ArrayList<>();
        
        // Step 1: Fill order using DFS on original graph
        Stack<Integer> finishOrder = new Stack<>();
        for (int i = 0; i < n; i++) {
            if (!visited[i]) {
                fillOrder(i, finishOrder);
            }
        }
        
        // Step 2: Create reversed graph
        Graph reversedGraph = graph.reverse();
        
        // Step 3: Process vertices in reverse finish order on reversed graph
        Arrays.fill(visited, false);
        while (!finishOrder.isEmpty()) {
            int v = finishOrder.pop();
            if (!visited[v]) {
                List<Integer> scc = new ArrayList<>();
                dfsReversed(v, reversedGraph, scc);
                Collections.sort(scc);
                sccs.add(scc);
                metrics.incrementCounter("sccs_found");
            }
        }
        
        metrics.stopTimer();
        
        // Build condensation graph
        Graph condensation = buildCondensation();
        
        return new TarjanSCC.SCCResult(sccs, condensation, metrics);
    }
    
    /**
     * DFS to fill vertices in order of finish time.
     * @param u Current vertex
     * @param finishOrder Stack to store finish order
     */
    private void fillOrder(int u, Stack<Integer> finishOrder) {
        visited[u] = true;
        metrics.incrementCounter("dfs_visits");
        
        for (Edge edge : graph.getEdges(u)) {
            int v = edge.to;
            metrics.incrementCounter("edges_explored");
            if (!visited[v]) {
                fillOrder(v, finishOrder);
            }
        }
        
        finishOrder.push(u);
    }
    
    /**
     * DFS on reversed graph to find one SCC.
     * @param u Current vertex
     * @param reversedGraph Reversed graph
     * @param scc Current SCC being built
     */
    private void dfsReversed(int u, Graph reversedGraph, List<Integer> scc) {
        visited[u] = true;
        scc.add(u);
        metrics.incrementCounter("dfs_visits");
        metrics.incrementCounter("nodes_in_sccs");
        
        for (Edge edge : reversedGraph.getEdges(u)) {
            int v = edge.to;
            metrics.incrementCounter("edges_explored");
            if (!visited[v]) {
                dfsReversed(v, reversedGraph, scc);
            }
        }
    }
    
    /**
     * Build the condensation graph (DAG of SCCs).
     * @return Condensation graph
     */
    private Graph buildCondensation() {
        int numSCCs = sccs.size();
        Graph condensation = new Graph(numSCCs, true, graph.getWeightModel());
        
        // Map each vertex to its SCC index
        Map<Integer, Integer> vertexToSCC = new HashMap<>();
        for (int i = 0; i < sccs.size(); i++) {
            for (int vertex : sccs.get(i)) {
                vertexToSCC.put(vertex, i);
            }
        }
        
        // Add edges between different SCCs
        Set<String> addedEdges = new HashSet<>();
        for (int u = 0; u < graph.getVertices(); u++) {
            int sccU = vertexToSCC.get(u);
            for (Edge edge : graph.getEdges(u)) {
                int v = edge.to;
                int sccV = vertexToSCC.get(v);
                
                if (sccU != sccV) {
                    String edgeKey = sccU + "->" + sccV;
                    if (!addedEdges.contains(edgeKey)) {
                        condensation.addEdge(sccU, sccV, edge.weight);
                        addedEdges.add(edgeKey);
                    }
                }
            }
        }
        
        // Set node weights for condensation
        for (int i = 0; i < sccs.size(); i++) {
            int totalWeight = 0;
            for (int vertex : sccs.get(i)) {
                totalWeight += graph.getNodeWeight(vertex);
            }
            condensation.setNodeWeight(i, totalWeight);
        }
        
        return condensation;
    }
}
