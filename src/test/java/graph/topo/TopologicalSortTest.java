package graph.topo;

import graph.utils.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

/**
 * JUnit tests for Topological Sort algorithms.
 */
public class TopologicalSortTest {
    
    @Test
    @DisplayName("Test simple DAG topological sort")
    public void testSimpleDAG() {
        // Graph: 0->1->2->3 (simple chain)
        Graph graph = new Graph(4, true, "edge");
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(2, 3, 1);
        
        Metrics metrics = new MetricsImpl();
        TopologicalSort topoSort = new TopologicalSort(graph, metrics);
        TopologicalSort.TopoResult result = topoSort.kahnSort();
        
        assertTrue(result.isDAG(), "Should be a valid DAG");
        assertEquals(4, result.getOrder().size(), "Should have all 4 vertices");
        
        List<Integer> order = result.getOrder();
        assertTrue(order.indexOf(0) < order.indexOf(1), "0 should come before 1");
        assertTrue(order.indexOf(1) < order.indexOf(2), "1 should come before 2");
        assertTrue(order.indexOf(2) < order.indexOf(3), "2 should come before 3");
    }
    
    @Test
    @DisplayName("Test DAG with multiple valid orderings")
    public void testMultipleValidOrderings() {
        // Graph: 0->2, 1->2
        Graph graph = new Graph(3, true, "edge");
        graph.addEdge(0, 2, 1);
        graph.addEdge(1, 2, 1);
        
        Metrics metrics = new MetricsImpl();
        TopologicalSort topoSort = new TopologicalSort(graph, metrics);
        TopologicalSort.TopoResult result = topoSort.kahnSort();
        
        assertTrue(result.isDAG(), "Should be a valid DAG");
        List<Integer> order = result.getOrder();
        
        // Both 0 and 1 can come first, but both must come before 2
        assertTrue(order.indexOf(0) < order.indexOf(2), "0 should come before 2");
        assertTrue(order.indexOf(1) < order.indexOf(2), "1 should come before 2");
    }
    
    @Test
    @DisplayName("Test cycle detection")
    public void testCycleDetection() {
        // Graph: 0->1->2->0 (cycle)
        Graph graph = new Graph(3, true, "edge");
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(2, 0, 1);
        
        Metrics metrics = new MetricsImpl();
        TopologicalSort topoSort = new TopologicalSort(graph, metrics);
        TopologicalSort.TopoResult result = topoSort.kahnSort();
        
        assertFalse(result.isDAG(), "Should detect cycle");
    }
    
    @Test
    @DisplayName("Test DFS-based topological sort")
    public void testDFSBasedSort() {
        Graph graph = new Graph(4, true, "edge");
        graph.addEdge(0, 1, 1);
        graph.addEdge(0, 2, 1);
        graph.addEdge(1, 3, 1);
        graph.addEdge(2, 3, 1);
        
        Metrics metrics = new MetricsImpl();
        TopologicalSort topoSort = new TopologicalSort(graph, metrics);
        TopologicalSort.TopoResult result = topoSort.dfsSort();
        
        assertTrue(result.isDAG(), "Should be a valid DAG");
        List<Integer> order = result.getOrder();
        
        assertTrue(order.indexOf(0) < order.indexOf(1), "0 should come before 1");
        assertTrue(order.indexOf(0) < order.indexOf(2), "0 should come before 2");
        assertTrue(order.indexOf(1) < order.indexOf(3), "1 should come before 3");
        assertTrue(order.indexOf(2) < order.indexOf(3), "2 should come before 3");
    }
    
    @Test
    @DisplayName("Test Kahn vs DFS - both detect cycles")
    public void testKahnVsDFSCycleDetection() {
        Graph graph = new Graph(3, true, "edge");
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(2, 0, 1);
        
        Metrics kahnMetrics = new MetricsImpl();
        TopologicalSort kahnSort = new TopologicalSort(graph, kahnMetrics);
        TopologicalSort.TopoResult kahnResult = kahnSort.kahnSort();
        
        Metrics dfsMetrics = new MetricsImpl();
        TopologicalSort dfsSort = new TopologicalSort(graph, dfsMetrics);
        TopologicalSort.TopoResult dfsResult = dfsSort.dfsSort();
        
        assertFalse(kahnResult.isDAG(), "Kahn should detect cycle");
        assertFalse(dfsResult.isDAG(), "DFS should detect cycle");
    }
    
    @Test
    @DisplayName("Test empty graph")
    public void testEmptyGraph() {
        Graph graph = new Graph(3, true, "edge");
        
        Metrics metrics = new MetricsImpl();
        TopologicalSort topoSort = new TopologicalSort(graph, metrics);
        TopologicalSort.TopoResult result = topoSort.kahnSort();
        
        assertTrue(result.isDAG(), "Empty graph is a DAG");
        assertEquals(3, result.getOrder().size(), "Should have all vertices");
    }
    
    @Test
    @DisplayName("Test single vertex")
    public void testSingleVertex() {
        Graph graph = new Graph(1, true, "edge");
        
        Metrics metrics = new MetricsImpl();
        TopologicalSort topoSort = new TopologicalSort(graph, metrics);
        TopologicalSort.TopoResult result = topoSort.kahnSort();
        
        assertTrue(result.isDAG(), "Single vertex is a DAG");
        assertEquals(Arrays.asList(0), result.getOrder());
    }
    
    @Test
    @DisplayName("Test complex DAG")
    public void testComplexDAG() {
        // More complex dependency graph
        Graph graph = new Graph(6, true, "edge");
        graph.addEdge(5, 2, 1);
        graph.addEdge(5, 0, 1);
        graph.addEdge(4, 0, 1);
        graph.addEdge(4, 1, 1);
        graph.addEdge(2, 3, 1);
        graph.addEdge(3, 1, 1);
        
        Metrics metrics = new MetricsImpl();
        TopologicalSort topoSort = new TopologicalSort(graph, metrics);
        TopologicalSort.TopoResult result = topoSort.kahnSort();
        
        assertTrue(result.isDAG(), "Should be a valid DAG");
        assertEquals(6, result.getOrder().size());
        
        List<Integer> order = result.getOrder();
        assertTrue(order.indexOf(5) < order.indexOf(2), "5 before 2");
        assertTrue(order.indexOf(2) < order.indexOf(3), "2 before 3");
        assertTrue(order.indexOf(3) < order.indexOf(1), "3 before 1");
    }
    
    @Test
    @DisplayName("Test metrics tracking")
    public void testMetrics() {
        Graph graph = new Graph(3, true, "edge");
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);
        
        Metrics metrics = new MetricsImpl();
        TopologicalSort topoSort = new TopologicalSort(graph, metrics);
        topoSort.kahnSort();
        
        assertTrue(metrics.getCounter("queue_pushes") > 0, "Should track queue operations");
        assertTrue(metrics.getCounter("vertices_processed") > 0, "Should track vertices");
        assertTrue(metrics.getElapsedTimeMs() >= 0, "Should track time");
    }
}
