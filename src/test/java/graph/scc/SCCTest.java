package graph.scc;

import graph.utils.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

/**
 * JUnit tests for Strongly Connected Components algorithms.
 */
public class SCCTest {
    
    @Test
    @DisplayName("Test simple SCC - single cycle")
    public void testSimpleCycle() {
        // Graph: 0->1->2->0 (one SCC)
        Graph graph = new Graph(3, true, "edge");
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(2, 0, 1);
        
        Metrics metrics = new MetricsImpl();
        TarjanSCC tarjan = new TarjanSCC(graph, metrics);
        TarjanSCC.SCCResult result = tarjan.findSCCs();
        
        assertEquals(1, result.getComponentCount(), "Should have 1 SCC");
        assertEquals(3, result.getComponents().get(0).size(), "SCC should contain 3 vertices");
    }
    
    @Test
    @DisplayName("Test multiple SCCs")
    public void testMultipleSCCs() {
        // Graph: 0->1->2->0, 3->4->3 (two SCCs)
        Graph graph = new Graph(5, true, "edge");
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(2, 0, 1);
        graph.addEdge(3, 4, 1);
        graph.addEdge(4, 3, 1);
        
        Metrics metrics = new MetricsImpl();
        TarjanSCC tarjan = new TarjanSCC(graph, metrics);
        TarjanSCC.SCCResult result = tarjan.findSCCs();
        
        assertEquals(2, result.getComponentCount(), "Should have 2 SCCs");
    }
    
    @Test
    @DisplayName("Test pure DAG - each vertex is its own SCC")
    public void testPureDAG() {
        // Graph: 0->1->2->3 (pure DAG, 4 SCCs)
        Graph graph = new Graph(4, true, "edge");
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(2, 3, 1);
        
        Metrics metrics = new MetricsImpl();
        TarjanSCC tarjan = new TarjanSCC(graph, metrics);
        TarjanSCC.SCCResult result = tarjan.findSCCs();
        
        assertEquals(4, result.getComponentCount(), "Each vertex should be its own SCC");
        for (List<Integer> scc : result.getComponents()) {
            assertEquals(1, scc.size(), "Each SCC should have size 1");
        }
    }
    
    @Test
    @DisplayName("Test condensation graph is DAG")
    public void testCondensationIsDAG() {
        // Graph with cycles
        Graph graph = new Graph(6, true, "edge");
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(2, 0, 1);
        graph.addEdge(2, 3, 1);
        graph.addEdge(3, 4, 1);
        graph.addEdge(4, 5, 1);
        graph.addEdge(5, 3, 1);
        
        Metrics metrics = new MetricsImpl();
        TarjanSCC tarjan = new TarjanSCC(graph, metrics);
        TarjanSCC.SCCResult result = tarjan.findSCCs();
        
        Graph condensation = result.getCondensation();
        
        // Verify condensation is a DAG using topological sort
        graph.topo.TopologicalSort topoSort = new graph.topo.TopologicalSort(condensation, new MetricsImpl());
        graph.topo.TopologicalSort.TopoResult topoResult = topoSort.kahnSort();
        
        assertTrue(topoResult.isDAG(), "Condensation graph should be a DAG");
    }
    
    @Test
    @DisplayName("Test Tarjan vs Kosaraju - same results")
    public void testTarjanVsKosaraju() {
        // Create test graph
        Graph graph = new Graph(5, true, "edge");
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(2, 0, 1);
        graph.addEdge(3, 4, 1);
        graph.addEdge(4, 3, 1);
        graph.addEdge(2, 3, 1);
        
        Metrics tarjanMetrics = new MetricsImpl();
        TarjanSCC tarjan = new TarjanSCC(graph, tarjanMetrics);
        TarjanSCC.SCCResult tarjanResult = tarjan.findSCCs();
        
        Metrics kosarajuMetrics = new MetricsImpl();
        KosarajuSCC kosaraju = new KosarajuSCC(graph, kosarajuMetrics);
        TarjanSCC.SCCResult kosarajuResult = kosaraju.findSCCs();
        
        assertEquals(tarjanResult.getComponentCount(), kosarajuResult.getComponentCount(),
                    "Both algorithms should find same number of SCCs");
    }
    
    @Test
    @DisplayName("Test empty graph")
    public void testEmptyGraph() {
        Graph graph = new Graph(3, true, "edge");
        
        Metrics metrics = new MetricsImpl();
        TarjanSCC tarjan = new TarjanSCC(graph, metrics);
        TarjanSCC.SCCResult result = tarjan.findSCCs();
        
        assertEquals(3, result.getComponentCount(), "Each isolated vertex is its own SCC");
    }
    
    @Test
    @DisplayName("Test single vertex")
    public void testSingleVertex() {
        Graph graph = new Graph(1, true, "edge");
        
        Metrics metrics = new MetricsImpl();
        TarjanSCC tarjan = new TarjanSCC(graph, metrics);
        TarjanSCC.SCCResult result = tarjan.findSCCs();
        
        assertEquals(1, result.getComponentCount(), "Single vertex is one SCC");
        assertEquals(1, result.getComponents().get(0).size());
    }
    
    @Test
    @DisplayName("Test metrics tracking")
    public void testMetrics() {
        Graph graph = new Graph(3, true, "edge");
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(2, 0, 1);
        
        Metrics metrics = new MetricsImpl();
        TarjanSCC tarjan = new TarjanSCC(graph, metrics);
        tarjan.findSCCs();
        
        assertTrue(metrics.getCounter("dfs_visits") > 0, "Should track DFS visits");
        assertTrue(metrics.getCounter("edges_explored") > 0, "Should track edges explored");
        assertTrue(metrics.getElapsedTimeMs() >= 0, "Should track elapsed time");
    }
}
