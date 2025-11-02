package graph.dagsp;

import graph.utils.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

/**
 * JUnit tests for DAG Shortest Path algorithms.
 */
public class DAGShortestPathTest {
    
    @Test
    @DisplayName("Test shortest paths in simple DAG")
    public void testShortestPaths() {
        // Graph: 0->1(3), 1->2(2), 2->3(4)
        Graph graph = new Graph(4, true, "edge");
        graph.addEdge(0, 1, 3);
        graph.addEdge(1, 2, 2);
        graph.addEdge(2, 3, 4);
        
        Metrics metrics = new MetricsImpl();
        DAGShortestPath dagsp = new DAGShortestPath(graph, metrics);
        DAGShortestPath.PathResult result = dagsp.shortestPaths(0);
        
        assertEquals(0, result.getDistance(0), "Distance to source is 0");
        assertEquals(3, result.getDistance(1), "Distance to vertex 1");
        assertEquals(5, result.getDistance(2), "Distance to vertex 2");
        assertEquals(9, result.getDistance(3), "Distance to vertex 3");
    }
    
    @Test
    @DisplayName("Test longest paths in DAG")
    public void testLongestPaths() {
        Graph graph = new Graph(4, true, "edge");
        graph.addEdge(0, 1, 3);
        graph.addEdge(1, 2, 2);
        graph.addEdge(2, 3, 4);
        
        Metrics metrics = new MetricsImpl();
        DAGShortestPath dagsp = new DAGShortestPath(graph, metrics);
        DAGShortestPath.PathResult result = dagsp.longestPaths(0);
        
        assertEquals(0, result.getDistance(0), "Distance to source is 0");
        assertEquals(3, result.getDistance(1), "Longest distance to vertex 1");
        assertEquals(5, result.getDistance(2), "Longest distance to vertex 2");
        assertEquals(9, result.getDistance(3), "Longest distance to vertex 3");
        assertTrue(result.isLongestPath(), "Should be longest path");
    }
    
    @Test
    @DisplayName("Test critical path")
    public void testCriticalPath() {
        // Diamond graph
        Graph graph = new Graph(4, true, "edge");
        graph.addEdge(0, 1, 2);
        graph.addEdge(0, 2, 3);
        graph.addEdge(1, 3, 4);
        graph.addEdge(2, 3, 1);
        
        Metrics metrics = new MetricsImpl();
        DAGShortestPath dagsp = new DAGShortestPath(graph, metrics);
        DAGShortestPath.PathResult result = dagsp.longestPaths(0);
        
        // Critical path: 0->1->3 (2+4=6)
        assertEquals(6, result.getDistance(3), "Critical path length");
        List<Integer> criticalPath = result.getPath(3);
        assertEquals(Arrays.asList(0, 1, 3), criticalPath, "Critical path vertices");
    }
    
    @Test
    @DisplayName("Test path reconstruction")
    public void testPathReconstruction() {
        Graph graph = new Graph(4, true, "edge");
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(2, 3, 1);
        
        Metrics metrics = new MetricsImpl();
        DAGShortestPath dagsp = new DAGShortestPath(graph, metrics);
        DAGShortestPath.PathResult result = dagsp.shortestPaths(0);
        
        List<Integer> path = result.getPath(3);
        assertEquals(Arrays.asList(0, 1, 2, 3), path, "Complete path from source to destination");
    }
    
    @Test
    @DisplayName("Test unreachable vertices")
    public void testUnreachableVertices() {
        Graph graph = new Graph(4, true, "edge");
        graph.addEdge(0, 1, 1);
        graph.addEdge(2, 3, 1); // Disconnected component
        
        Metrics metrics = new MetricsImpl();
        DAGShortestPath dagsp = new DAGShortestPath(graph, metrics);
        DAGShortestPath.PathResult result = dagsp.shortestPaths(0);
        
        assertEquals(0, result.getDistance(0), "Source distance is 0");
        assertEquals(1, result.getDistance(1), "Reachable vertex");
        assertEquals(Integer.MAX_VALUE, result.getDistance(2), "Unreachable vertex");
        assertEquals(Integer.MAX_VALUE, result.getDistance(3), "Unreachable vertex");
    }
    
    @Test
    @DisplayName("Test multiple paths - shortest chosen")
    public void testMultiplePathsShort() {
        // Two paths: 0->1->3 (1+1=2) and 0->2->3 (5+1=6)
        Graph graph = new Graph(4, true, "edge");
        graph.addEdge(0, 1, 1);
        graph.addEdge(0, 2, 5);
        graph.addEdge(1, 3, 1);
        graph.addEdge(2, 3, 1);
        
        Metrics metrics = new MetricsImpl();
        DAGShortestPath dagsp = new DAGShortestPath(graph, metrics);
        DAGShortestPath.PathResult result = dagsp.shortestPaths(0);
        
        assertEquals(2, result.getDistance(3), "Should choose shorter path");
        List<Integer> path = result.getPath(3);
        assertEquals(Arrays.asList(0, 1, 3), path, "Shortest path through vertex 1");
    }
    
    @Test
    @DisplayName("Test multiple paths - longest chosen")
    public void testMultiplePathsLong() {
        // Two paths: 0->1->3 (1+1=2) and 0->2->3 (5+1=6)
        Graph graph = new Graph(4, true, "edge");
        graph.addEdge(0, 1, 1);
        graph.addEdge(0, 2, 5);
        graph.addEdge(1, 3, 1);
        graph.addEdge(2, 3, 1);
        
        Metrics metrics = new MetricsImpl();
        DAGShortestPath dagsp = new DAGShortestPath(graph, metrics);
        DAGShortestPath.PathResult result = dagsp.longestPaths(0);
        
        assertEquals(6, result.getDistance(3), "Should choose longer path");
        List<Integer> path = result.getPath(3);
        assertEquals(Arrays.asList(0, 2, 3), path, "Longest path through vertex 2");
    }
    
    @Test
    @DisplayName("Test single vertex")
    public void testSingleVertex() {
        Graph graph = new Graph(1, true, "edge");
        
        Metrics metrics = new MetricsImpl();
        DAGShortestPath dagsp = new DAGShortestPath(graph, metrics);
        DAGShortestPath.PathResult result = dagsp.shortestPaths(0);
        
        assertEquals(0, result.getDistance(0), "Distance to self is 0");
        assertEquals(Arrays.asList(0), result.getPath(0), "Path to self");
    }
    
    @Test
    @DisplayName("Test graph with cycle throws exception")
    public void testCycleThrowsException() {
        Graph graph = new Graph(3, true, "edge");
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(2, 0, 1); // Cycle
        
        Metrics metrics = new MetricsImpl();
        DAGShortestPath dagsp = new DAGShortestPath(graph, metrics);
        
        assertThrows(IllegalArgumentException.class, () -> {
            dagsp.shortestPaths(0);
        }, "Should throw exception for cyclic graph");
    }
    
    @Test
    @DisplayName("Test node weight model")
    public void testNodeWeightModel() {
        Graph graph = new Graph(3, true, "node");
        graph.addEdge(0, 1, 1); // Edge weight ignored in node model
        graph.addEdge(1, 2, 1);
        graph.setNodeWeight(0, 0);
        graph.setNodeWeight(1, 5);
        graph.setNodeWeight(2, 3);
        
        Metrics metrics = new MetricsImpl();
        DAGShortestPath dagsp = new DAGShortestPath(graph, metrics);
        DAGShortestPath.PathResult result = dagsp.shortestPaths(0);
        
        assertEquals(0, result.getDistance(0), "Source distance");
        assertEquals(5, result.getDistance(1), "Distance includes node weight of vertex 1");
        assertEquals(8, result.getDistance(2), "Distance includes node weight of vertex 2");
    }
    
    @Test
    @DisplayName("Test metrics tracking")
    public void testMetrics() {
        Graph graph = new Graph(3, true, "edge");
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);
        
        Metrics metrics = new MetricsImpl();
        DAGShortestPath dagsp = new DAGShortestPath(graph, metrics);
        dagsp.shortestPaths(0);
        
        assertTrue(metrics.getCounter("relaxations") > 0, "Should track relaxations");
        assertTrue(metrics.getCounter("vertices_processed") > 0, "Should track vertices");
        assertTrue(metrics.getElapsedTimeMs() >= 0, "Should track time");
    }
}
