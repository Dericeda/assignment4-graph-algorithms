package graph.topo;

import graph.utils.*;
import java.util.*;

/**
 * Implementation of Topological Sort using Kahn's algorithm (BFS-based).
 * Works on Directed Acyclic Graphs (DAGs).
 * Time Complexity: O(V + E)
 * Space Complexity: O(V)
 */
public class TopologicalSort {
    private final Graph graph;
    private final Metrics metrics;
    
    /**
     * Constructor for topological sort.
     * @param graph Input directed graph (must be DAG)
     * @param metrics Metrics tracker
     */
    public TopologicalSort(Graph graph, Metrics metrics) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException("Topological sort requires a directed graph");
        }
        this.graph = graph;
        this.metrics = metrics;
    }
    
    /**
     * Perform topological sort using Kahn's algorithm (BFS-based).
     * @return TopoResult containing the topological order
     */
    public TopoResult kahnSort() {
        metrics.startTimer();
        
        int n = graph.getVertices();
        int[] inDegree = new int[n];
        
        // Calculate in-degrees
        for (int u = 0; u < n; u++) {
            for (Edge edge : graph.getEdges(u)) {
                inDegree[edge.to]++;
            }
        }
        
        // Initialize queue with vertices having 0 in-degree
        Queue<Integer> queue = new LinkedList<>();
        for (int i = 0; i < n; i++) {
            if (inDegree[i] == 0) {
                queue.offer(i);
                metrics.incrementCounter("queue_pushes");
            }
        }
        
        List<Integer> topoOrder = new ArrayList<>();
        
        // Process vertices
        while (!queue.isEmpty()) {
            int u = queue.poll();
            topoOrder.add(u);
            metrics.incrementCounter("queue_pops");
            metrics.incrementCounter("vertices_processed");
            
            // Reduce in-degree for neighbors
            for (Edge edge : graph.getEdges(u)) {
                int v = edge.to;
                inDegree[v]--;
                metrics.incrementCounter("edges_relaxed");
                
                if (inDegree[v] == 0) {
                    queue.offer(v);
                    metrics.incrementCounter("queue_pushes");
                }
            }
        }
        
        metrics.stopTimer();
        
        // Check if all vertices were processed (DAG validation)
        boolean isDAG = topoOrder.size() == n;
        
        return new TopoResult(topoOrder, isDAG, metrics);
    }
    
    /**
     * Perform topological sort using DFS-based approach.
     * @return TopoResult containing the topological order
     */
    public TopoResult dfsSort() {
        metrics.startTimer();
        
        int n = graph.getVertices();
        boolean[] visited = new boolean[n];
        boolean[] recursionStack = new boolean[n];
        Stack<Integer> stack = new Stack<>();
        boolean hasCycle = false;
        
        // Visit all vertices
        for (int i = 0; i < n; i++) {
            if (!visited[i]) {
                if (dfsVisit(i, visited, recursionStack, stack)) {
                    hasCycle = true;
                    break;
                }
            }
        }
        
        metrics.stopTimer();
        
        // Build topological order from stack
        List<Integer> topoOrder = new ArrayList<>();
        while (!stack.isEmpty()) {
            topoOrder.add(stack.pop());
        }
        
        return new TopoResult(topoOrder, !hasCycle, metrics);
    }
    
    /**
     * DFS visit for topological sort.
     * @param u Current vertex
     * @param visited Visited array
     * @param recursionStack Recursion stack for cycle detection
     * @param stack Stack for topological order
     * @return true if cycle detected, false otherwise
     */
    private boolean dfsVisit(int u, boolean[] visited, boolean[] recursionStack, Stack<Integer> stack) {
        visited[u] = true;
        recursionStack[u] = true;
        metrics.incrementCounter("dfs_visits");
        
        for (Edge edge : graph.getEdges(u)) {
            int v = edge.to;
            metrics.incrementCounter("edges_explored");
            
            if (!visited[v]) {
                if (dfsVisit(v, visited, recursionStack, stack)) {
                    return true; // Cycle found
                }
            } else if (recursionStack[v]) {
                return true; // Back edge (cycle) found
            }
        }
        
        recursionStack[u] = false;
        stack.push(u);
        return false;
    }
    
    /**
     * Result container for topological sort.
     */
    public static class TopoResult {
        private final List<Integer> order;
        private final boolean isDAG;
        private final Metrics metrics;
        
        public TopoResult(List<Integer> order, boolean isDAG, Metrics metrics) {
            this.order = order;
            this.isDAG = isDAG;
            this.metrics = metrics;
        }
        
        public List<Integer> getOrder() {
            return order;
        }
        
        public boolean isDAG() {
            return isDAG;
        }
        
        public Metrics getMetrics() {
            return metrics;
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== Topological Sort Results ===\n");
            sb.append("Is DAG: ").append(isDAG).append("\n");
            
            if (isDAG) {
                sb.append("Topological Order: ").append(order).append("\n");
            } else {
                sb.append("Graph contains cycles - no valid topological order\n");
                if (!order.isEmpty()) {
                    sb.append("Partial order: ").append(order).append("\n");
                }
            }
            
            sb.append("\n").append(metrics.getSummary());
            return sb.toString();
        }
    }
}
