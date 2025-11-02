package graph.dagsp;

import graph.utils.*;
import graph.topo.TopologicalSort;
import java.util.*;

/**
 * Implementation of shortest and longest path algorithms for Directed Acyclic Graphs (DAGs).
 * Uses dynamic programming over topological order.
 * Time Complexity: O(V + E)
 * Space Complexity: O(V)
 */
public class DAGShortestPath {
    private final Graph graph;
    private final Metrics metrics;
    
    /**
     * Constructor for DAG shortest/longest path algorithms.
     * @param graph Input directed acyclic graph
     * @param metrics Metrics tracker
     */
    public DAGShortestPath(Graph graph, Metrics metrics) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException("DAG algorithms require a directed graph");
        }
        this.graph = graph;
        this.metrics = metrics;
    }
    
    /**
     * Compute single-source shortest paths in a DAG.
     * @param source Source vertex
     * @return PathResult containing distances and paths
     */
    public PathResult shortestPaths(int source) {
        metrics.startTimer();
        
        // Get topological order
        TopologicalSort topoSort = new TopologicalSort(graph, new graph.utils.MetricsImpl());
        TopologicalSort.TopoResult topoResult = topoSort.kahnSort();
        
        if (!topoResult.isDAG()) {
            metrics.stopTimer();
            throw new IllegalArgumentException("Graph contains cycles - not a DAG");
        }
        
        List<Integer> topoOrder = topoResult.getOrder();
        int n = graph.getVertices();
        
        // Initialize distances
        int[] dist = new int[n];
        int[] parent = new int[n];
        Arrays.fill(dist, Integer.MAX_VALUE);
        Arrays.fill(parent, -1);
        dist[source] = 0;
        
        String weightModel = graph.getWeightModel();
        
        // Process vertices in topological order
        for (int u : topoOrder) {
            if (dist[u] != Integer.MAX_VALUE) {
                metrics.incrementCounter("vertices_processed");
                
                for (Edge edge : graph.getEdges(u)) {
                    int v = edge.to;
                    metrics.incrementCounter("relaxations");
                    
                    // Calculate edge weight based on model
                    int edgeWeight = weightModel.equals("edge") ? edge.weight : graph.getNodeWeight(v);
                    
                    if (dist[u] + edgeWeight < dist[v]) {
                        dist[v] = dist[u] + edgeWeight;
                        parent[v] = u;
                        metrics.incrementCounter("successful_relaxations");
                    }
                }
            }
        }
        
        metrics.stopTimer();
        
        return new PathResult(dist, parent, source, false, metrics);
    }
    
    /**
     * Compute longest paths in a DAG (critical path).
     * @param source Source vertex
     * @return PathResult containing longest distances and paths
     */
    public PathResult longestPaths(int source) {
        metrics.startTimer();
        
        // Get topological order
        TopologicalSort topoSort = new TopologicalSort(graph, new graph.utils.MetricsImpl());
        TopologicalSort.TopoResult topoResult = topoSort.kahnSort();
        
        if (!topoResult.isDAG()) {
            metrics.stopTimer();
            throw new IllegalArgumentException("Graph contains cycles - not a DAG");
        }
        
        List<Integer> topoOrder = topoResult.getOrder();
        int n = graph.getVertices();
        
        // Initialize distances (use negative infinity for longest path)
        int[] dist = new int[n];
        int[] parent = new int[n];
        Arrays.fill(dist, Integer.MIN_VALUE);
        Arrays.fill(parent, -1);
        dist[source] = 0;
        
        String weightModel = graph.getWeightModel();
        
        // Process vertices in topological order
        for (int u : topoOrder) {
            if (dist[u] != Integer.MIN_VALUE) {
                metrics.incrementCounter("vertices_processed");
                
                for (Edge edge : graph.getEdges(u)) {
                    int v = edge.to;
                    metrics.incrementCounter("relaxations");
                    
                    // Calculate edge weight based on model
                    int edgeWeight = weightModel.equals("edge") ? edge.weight : graph.getNodeWeight(v);
                    
                    if (dist[u] + edgeWeight > dist[v]) {
                        dist[v] = dist[u] + edgeWeight;
                        parent[v] = u;
                        metrics.incrementCounter("successful_relaxations");
                    }
                }
            }
        }
        
        metrics.stopTimer();
        
        return new PathResult(dist, parent, source, true, metrics);
    }
    
    /**
     * Find critical path (longest path) in the entire DAG.
     * @return PathResult for the critical path
     */
    public PathResult criticalPath() {
        metrics.startTimer();
        
        int n = graph.getVertices();
        PathResult bestResult = null;
        int maxLength = Integer.MIN_VALUE;
        
        // Try each vertex as potential start of critical path
        for (int source = 0; source < n; source++) {
            Metrics tempMetrics = new graph.utils.MetricsImpl();
            DAGShortestPath tempDAGSP = new DAGShortestPath(graph, tempMetrics);
            PathResult result = tempDAGSP.longestPaths(source);
            
            // Find maximum distance from this source
            int maxDist = Integer.MIN_VALUE;
            for (int i = 0; i < n; i++) {
                if (result.getDistance(i) != Integer.MIN_VALUE && result.getDistance(i) > maxDist) {
                    maxDist = result.getDistance(i);
                }
            }
            
            if (maxDist > maxLength) {
                maxLength = maxDist;
                bestResult = result;
            }
        }
        
        metrics.stopTimer();
        
        return bestResult;
    }
    
    /**
     * Result container for path algorithms.
     */
    public static class PathResult {
        private final int[] distances;
        private final int[] parent;
        private final int source;
        private final boolean isLongestPath;
        private final Metrics metrics;
        
        public PathResult(int[] distances, int[] parent, int source, boolean isLongestPath, Metrics metrics) {
            this.distances = distances;
            this.parent = parent;
            this.source = source;
            this.isLongestPath = isLongestPath;
            this.metrics = metrics;
        }
        
        public int getDistance(int vertex) {
            return distances[vertex];
        }
        
        public List<Integer> getPath(int destination) {
            if (parent[destination] == -1 && destination != source) {
                return new ArrayList<>(); // No path exists
            }
            
            List<Integer> path = new ArrayList<>();
            int current = destination;
            
            while (current != -1) {
                path.add(current);
                current = parent[current];
            }
            
            Collections.reverse(path);
            return path;
        }
        
        public int getSource() {
            return source;
        }
        
        public boolean isLongestPath() {
            return isLongestPath;
        }
        
        public Metrics getMetrics() {
            return metrics;
        }
        
        public int getCriticalPathLength() {
            int maxDist = isLongestPath ? Integer.MIN_VALUE : Integer.MAX_VALUE;
            for (int dist : distances) {
                if (isLongestPath) {
                    if (dist != Integer.MIN_VALUE && dist > maxDist) {
                        maxDist = dist;
                    }
                } else {
                    if (dist != Integer.MAX_VALUE && dist < maxDist) {
                        maxDist = dist;
                    }
                }
            }
            return maxDist;
        }
        
        public int getCriticalPathDestination() {
            int maxDist = isLongestPath ? Integer.MIN_VALUE : Integer.MAX_VALUE;
            int destination = -1;
            
            for (int i = 0; i < distances.length; i++) {
                if (isLongestPath) {
                    if (distances[i] != Integer.MIN_VALUE && distances[i] > maxDist) {
                        maxDist = distances[i];
                        destination = i;
                    }
                } else {
                    if (distances[i] != Integer.MAX_VALUE && distances[i] < maxDist) {
                        maxDist = distances[i];
                        destination = i;
                    }
                }
            }
            
            return destination;
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== ").append(isLongestPath ? "Longest" : "Shortest").append(" Path Results ===\n");
            sb.append("Source: ").append(source).append("\n\n");
            
            sb.append("Distances:\n");
            for (int i = 0; i < distances.length; i++) {
                if (distances[i] != Integer.MAX_VALUE && distances[i] != Integer.MIN_VALUE) {
                    sb.append("  Vertex ").append(i).append(": ").append(distances[i]);
                    List<Integer> path = getPath(i);
                    if (!path.isEmpty()) {
                        sb.append(" (Path: ").append(path).append(")");
                    }
                    sb.append("\n");
                } else if (i != source) {
                    sb.append("  Vertex ").append(i).append(": unreachable\n");
                }
            }
            
            if (isLongestPath) {
                int dest = getCriticalPathDestination();
                if (dest != -1) {
                    sb.append("\nCritical Path: ").append(getPath(dest));
                    sb.append("\nCritical Path Length: ").append(getCriticalPathLength()).append("\n");
                }
            }
            
            sb.append("\n").append(metrics.getSummary());
            return sb.toString();
        }
    }
}
