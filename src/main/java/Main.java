import graph.utils.*;
import graph.scc.*;
import graph.topo.*;
import graph.dagsp.*;
import java.io.IOException;
import java.util.*;

/**
 * Main class demonstrating SCC, Topological Sort, and DAG Shortest/Longest Path algorithms.
 */
public class Main {
    
    public static void main(String[] args) {
        System.out.println("=".repeat(80));
        System.out.println("Assignment 4: Graph Algorithms - Smart City Task Scheduling");
        System.out.println("=".repeat(80));
        System.out.println();
        
        // Default file path
        String filepath = args.length > 0 ? args[0] : "data/tasks.json";
        
        try {
            // Load graph from JSON
            System.out.println("Loading graph from: " + filepath);
            GraphLoader.GraphData graphData = GraphLoader.loadFromJson(filepath);
            System.out.println(graphData);
            System.out.println();
            
            Graph originalGraph = GraphLoader.createGraph(graphData);
            System.out.println("Original Graph:");
            System.out.println(originalGraph);
            System.out.println();
            
            // ===== PART 1: SCC Analysis =====
            System.out.println("█".repeat(80));
            System.out.println("PART 1: STRONGLY CONNECTED COMPONENTS (SCC)");
            System.out.println("█".repeat(80));
            System.out.println();
            
            // Run Tarjan's algorithm
            System.out.println("--- Using Tarjan's Algorithm ---");
            Metrics tarjanMetrics = new MetricsImpl();
            TarjanSCC tarjan = new TarjanSCC(originalGraph, tarjanMetrics);
            TarjanSCC.SCCResult tarjanResult = tarjan.findSCCs();
            System.out.println(tarjanResult);
            System.out.println();
            
            // Run Kosaraju's algorithm for comparison
            System.out.println("--- Using Kosaraju's Algorithm (for comparison) ---");
            Metrics kosarajuMetrics = new MetricsImpl();
            KosarajuSCC kosaraju = new KosarajuSCC(originalGraph, kosarajuMetrics);
            TarjanSCC.SCCResult kosarajuResult = kosaraju.findSCCs();
            System.out.println(kosarajuResult);
            System.out.println();
            
            // Get condensation graph
            Graph condensation = tarjanResult.getCondensation();
            System.out.println("Condensation Graph (DAG of SCCs):");
            System.out.println(condensation);
            System.out.println();
            
            // ===== PART 2: Topological Sort =====
            System.out.println("█".repeat(80));
            System.out.println("PART 2: TOPOLOGICAL SORT");
            System.out.println("█".repeat(80));
            System.out.println();
            
            // Topological sort on condensation (which is a DAG)
            System.out.println("--- Kahn's Algorithm (BFS-based) ---");
            Metrics kahnMetrics = new MetricsImpl();
            TopologicalSort kahnSort = new TopologicalSort(condensation, kahnMetrics);
            TopologicalSort.TopoResult kahnResult = kahnSort.kahnSort();
            System.out.println(kahnResult);
            System.out.println();
            
            System.out.println("--- DFS-based Algorithm ---");
            Metrics dfsTopoMetrics = new MetricsImpl();
            TopologicalSort dfsSort = new TopologicalSort(condensation, dfsTopoMetrics);
            TopologicalSort.TopoResult dfsResult = dfsSort.dfsSort();
            System.out.println(dfsResult);
            System.out.println();
            
            // Derive task order from SCC order
            if (kahnResult.isDAG()) {
                System.out.println("--- Derived Task Order ---");
                List<Integer> sccOrder = kahnResult.getOrder();
                List<Integer> taskOrder = new ArrayList<>();
                
                for (int sccIndex : sccOrder) {
                    List<Integer> sccTasks = tarjanResult.getComponents().get(sccIndex);
                    taskOrder.addAll(sccTasks);
                }
                
                System.out.println("SCC Processing Order: " + sccOrder);
                System.out.println("Derived Task Order: " + taskOrder);
                System.out.println("(Tasks within each SCC can be executed in any order or in parallel)");
                System.out.println();
            }
            
            // ===== PART 3: Shortest and Longest Paths in DAG =====
            System.out.println("█".repeat(80));
            System.out.println("PART 3: SHORTEST AND LONGEST PATHS IN DAG");
            System.out.println("█".repeat(80));
            System.out.println();
            
            System.out.println("Weight Model: " + condensation.getWeightModel());
            System.out.println();
            
            // Find a valid source (vertex with no incoming edges preferred)
            int source = findBestSource(condensation);
            if (graphData.source >= 0 && graphData.source < condensation.getVertices()) {
                // Map original source to SCC index
                source = mapVertexToSCC(graphData.source, tarjanResult.getComponents());
            }
            System.out.println("Source SCC: " + source);
            System.out.println();
            
            // Shortest paths
            System.out.println("--- Shortest Paths from Source ---");
            Metrics shortestMetrics = new MetricsImpl();
            DAGShortestPath dagsp = new DAGShortestPath(condensation, shortestMetrics);
            DAGShortestPath.PathResult shortestResult = dagsp.shortestPaths(source);
            System.out.println(shortestResult);
            System.out.println();
            
            // Longest paths (Critical Path)
            System.out.println("--- Longest Paths from Source (Critical Path) ---");
            Metrics longestMetrics = new MetricsImpl();
            DAGShortestPath dagspLongest = new DAGShortestPath(condensation, longestMetrics);
            DAGShortestPath.PathResult longestResult = dagspLongest.longestPaths(source);
            System.out.println(longestResult);
            System.out.println();
            
            // ===== SUMMARY =====
            System.out.println("█".repeat(80));
            System.out.println("SUMMARY");
            System.out.println("█".repeat(80));
            System.out.println();
            
            printSummary(originalGraph, tarjanResult, kahnResult, shortestResult, longestResult);
            
        } catch (IOException e) {
            System.err.println("Error loading graph: " + e.getMessage());
            System.err.println("Usage: java Main [path/to/tasks.json]");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Find the best source vertex (preferably one with no incoming edges).
     */
    private static int findBestSource(Graph graph) {
        int n = graph.getVertices();
        boolean[] hasIncoming = new boolean[n];
        
        for (int u = 0; u < n; u++) {
            for (Edge edge : graph.getEdges(u)) {
                hasIncoming[edge.to] = true;
            }
        }
        
        // Return first vertex with no incoming edges
        for (int i = 0; i < n; i++) {
            if (!hasIncoming[i]) {
                return i;
            }
        }
        
        // If all have incoming edges, return 0
        return 0;
    }
    
    /**
     * Map a vertex from original graph to its SCC index.
     */
    private static int mapVertexToSCC(int vertex, List<List<Integer>> sccs) {
        for (int i = 0; i < sccs.size(); i++) {
            if (sccs.get(i).contains(vertex)) {
                return i;
            }
        }
        return 0;
    }
    
    /**
     * Print comprehensive summary of results.
     */
    private static void printSummary(Graph originalGraph, 
                                    TarjanSCC.SCCResult sccResult,
                                    TopologicalSort.TopoResult topoResult,
                                    DAGShortestPath.PathResult shortestResult,
                                    DAGShortestPath.PathResult longestResult) {
        
        System.out.println("Graph Statistics:");
        System.out.println("  Vertices: " + originalGraph.getVertices());
        System.out.println("  Edges: " + originalGraph.getEdgeCount());
        System.out.println("  Weight Model: " + originalGraph.getWeightModel());
        System.out.println();
        
        System.out.println("SCC Analysis:");
        System.out.println("  Number of SCCs: " + sccResult.getComponentCount());
        System.out.println("  Component Sizes: " + sccResult.getComponentSizes());
        System.out.println("  Largest SCC: " + Collections.max(sccResult.getComponentSizes()));
        System.out.println("  Condensation Vertices: " + sccResult.getCondensation().getVertices());
        System.out.println("  Condensation Edges: " + sccResult.getCondensation().getEdgeCount());
        System.out.println();
        
        System.out.println("Topological Sort:");
        System.out.println("  Valid DAG: " + topoResult.isDAG());
        System.out.println("  Processing Order: " + topoResult.getOrder());
        System.out.println();
        
        System.out.println("Path Analysis:");
        int criticalDest = longestResult.getCriticalPathDestination();
        if (criticalDest != -1) {
            System.out.println("  Critical Path: " + longestResult.getPath(criticalDest));
            System.out.println("  Critical Path Length: " + longestResult.getCriticalPathLength());
        }
        System.out.println();
        
        System.out.println("Performance Metrics:");
        System.out.printf("  SCC (Tarjan): %.3f ms\n", sccResult.getMetrics().getElapsedTimeMs());
        System.out.printf("  Topological Sort: %.3f ms\n", topoResult.getMetrics().getElapsedTimeMs());
        System.out.printf("  Shortest Path: %.3f ms\n", shortestResult.getMetrics().getElapsedTimeMs());
        System.out.printf("  Longest Path: %.3f ms\n", longestResult.getMetrics().getElapsedTimeMs());
        System.out.println();
    }
}
