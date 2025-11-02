package graph.scc;

import graph.utils.*;
import java.util.*;

/**
 * Implementation of Tarjan's algorithm for finding Strongly Connected Components (SCC).
 * Time Complexity: O(V + E)
 * Space Complexity: O(V)
 */
public class TarjanSCC {
    private final Graph graph;
    private final Metrics metrics;
    
    private int[] ids;           // Node IDs (discovery time)
    private int[] low;           // Low-link values
    private boolean[] onStack;   // Track if node is on stack
    private Stack<Integer> stack;
    private int id;              // Current ID counter
    private List<List<Integer>> sccs; // Result SCCs
    
    /**
     * Constructor for Tarjan's SCC algorithm.
     * @param graph Input directed graph
     * @param metrics Metrics tracker
     */
    public TarjanSCC(Graph graph, Metrics metrics) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException("Tarjan's algorithm requires a directed graph");
        }
        this.graph = graph;
        this.metrics = metrics;
    }
    
    /**
     * Find all strongly connected components in the graph.
     * @return SCCResult containing components and condensation graph
     */
    public SCCResult findSCCs() {
        metrics.startTimer();
        
        int n = graph.getVertices();
        ids = new int[n];
        low = new int[n];
        onStack = new boolean[n];
        stack = new Stack<>();
        sccs = new ArrayList<>();
        id = 0;
        
        Arrays.fill(ids, -1);
        
        // Visit all unvisited nodes
        for (int i = 0; i < n; i++) {
            if (ids[i] == -1) {
                dfs(i);
            }
        }
        
        metrics.stopTimer();
        
        // Build condensation graph
        Graph condensation = buildCondensation();
        
        return new SCCResult(sccs, condensation, metrics);
    }
    
    /**
     * Depth-first search for Tarjan's algorithm.
     * @param u Current vertex
     */
    private void dfs(int u) {
        metrics.incrementCounter("dfs_visits");
        
        // Initialize discovery time and low-link value
        ids[u] = low[u] = id++;
        stack.push(u);
        onStack[u] = true;
        
        // Visit all neighbors
        for (Edge edge : graph.getEdges(u)) {
            int v = edge.to;
            metrics.incrementCounter("edges_explored");
            
            if (ids[v] == -1) {
                // Tree edge - recurse
                dfs(v);
                low[u] = Math.min(low[u], low[v]);
            } else if (onStack[v]) {
                // Back edge to node on stack
                low[u] = Math.min(low[u], ids[v]);
            }
        }
        
        // If u is a root node, pop the SCC from stack
        if (ids[u] == low[u]) {
            List<Integer> scc = new ArrayList<>();
            int v;
            do {
                v = stack.pop();
                onStack[v] = false;
                scc.add(v);
                metrics.incrementCounter("nodes_in_sccs");
            } while (v != u);
            
            // Sort SCC for consistent output
            Collections.sort(scc);
            sccs.add(scc);
            metrics.incrementCounter("sccs_found");
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
                        // Use minimum weight for edges between SCCs
                        condensation.addEdge(sccU, sccV, edge.weight);
                        addedEdges.add(edgeKey);
                    }
                }
            }
        }
        
        // Set node weights for condensation (sum or max of component nodes)
        for (int i = 0; i < sccs.size(); i++) {
            int totalWeight = 0;
            for (int vertex : sccs.get(i)) {
                totalWeight += graph.getNodeWeight(vertex);
            }
            condensation.setNodeWeight(i, totalWeight);
        }
        
        return condensation;
    }
    
    /**
     * Result container for SCC computation.
     */
    public static class SCCResult {
        private final List<List<Integer>> components;
        private final Graph condensation;
        private final Metrics metrics;
        
        public SCCResult(List<List<Integer>> components, Graph condensation, Metrics metrics) {
            this.components = components;
            this.condensation = condensation;
            this.metrics = metrics;
        }
        
        public List<List<Integer>> getComponents() {
            return components;
        }
        
        public Graph getCondensation() {
            return condensation;
        }
        
        public Metrics getMetrics() {
            return metrics;
        }
        
        public int getComponentCount() {
            return components.size();
        }
        
        public List<Integer> getComponentSizes() {
            List<Integer> sizes = new ArrayList<>();
            for (List<Integer> component : components) {
                sizes.add(component.size());
            }
            return sizes;
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== SCC Results ===\n");
            sb.append("Number of SCCs: ").append(components.size()).append("\n");
            sb.append("Component Sizes: ").append(getComponentSizes()).append("\n\n");
            
            for (int i = 0; i < components.size(); i++) {
                sb.append("SCC ").append(i).append(": ").append(components.get(i)).append("\n");
            }
            
            sb.append("\n").append(metrics.getSummary());
            return sb.toString();
        }
    }
}
