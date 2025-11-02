package graph.utils;

import java.util.*;

/**
 * Represents a directed weighted graph.
 * Supports both edge weights and node weights (durations).
 */
public class Graph {
    private final int vertices;
    private final List<List<Edge>> adjacencyList;
    private final boolean directed;
    private final Map<Integer, Integer> nodeWeights; // For node duration model
    private final String weightModel; // "edge" or "node"
    
    /**
     * Constructor for graph with specified weight model.
     * @param vertices Number of vertices
     * @param directed Whether graph is directed
     * @param weightModel "edge" or "node" - determines weight model
     */
    public Graph(int vertices, boolean directed, String weightModel) {
        this.vertices = vertices;
        this.directed = directed;
        this.adjacencyList = new ArrayList<>(vertices);
        this.nodeWeights = new HashMap<>();
        this.weightModel = weightModel;
        
        for (int i = 0; i < vertices; i++) {
            adjacencyList.add(new ArrayList<>());
        }
    }
    
    /**
     * Add an edge to the graph.
     * @param u Source vertex
     * @param v Destination vertex
     * @param weight Edge weight
     */
    public void addEdge(int u, int v, int weight) {
        if (u < 0 || u >= vertices || v < 0 || v >= vertices) {
            throw new IllegalArgumentException("Invalid vertex: " + u + " or " + v);
        }
        adjacencyList.get(u).add(new Edge(v, weight));
        if (!directed) {
            adjacencyList.get(v).add(new Edge(u, weight));
        }
    }
    
    /**
     * Set node weight (duration) for a vertex.
     * @param vertex Vertex index
     * @param weight Node weight/duration
     */
    public void setNodeWeight(int vertex, int weight) {
        nodeWeights.put(vertex, weight);
    }
    
    /**
     * Get node weight for a vertex.
     * @param vertex Vertex index
     * @return Node weight, or 1 if not set
     */
    public int getNodeWeight(int vertex) {
        return nodeWeights.getOrDefault(vertex, 1);
    }
    
    /**
     * Get all edges from a vertex.
     * @param u Source vertex
     * @return List of edges from u
     */
    public List<Edge> getEdges(int u) {
        if (u < 0 || u >= vertices) {
            return new ArrayList<>();
        }
        return adjacencyList.get(u);
    }
    
    /**
     * Get number of vertices.
     * @return Number of vertices
     */
    public int getVertices() {
        return vertices;
    }
    
    /**
     * Check if graph is directed.
     * @return true if directed, false otherwise
     */
    public boolean isDirected() {
        return directed;
    }
    
    /**
     * Get weight model used by this graph.
     * @return "edge" or "node"
     */
    public String getWeightModel() {
        return weightModel;
    }
    
    /**
     * Get total number of edges in the graph.
     * @return Total edge count
     */
    public int getEdgeCount() {
        int count = 0;
        for (int i = 0; i < vertices; i++) {
            count += adjacencyList.get(i).size();
        }
        return directed ? count : count / 2;
    }
    
    /**
     * Create a reversed graph (transpose).
     * @return Reversed graph
     */
    public Graph reverse() {
        Graph reversed = new Graph(vertices, true, weightModel);
        for (int u = 0; u < vertices; u++) {
            for (Edge edge : adjacencyList.get(u)) {
                reversed.addEdge(edge.to, u, edge.weight);
            }
            if (nodeWeights.containsKey(u)) {
                reversed.setNodeWeight(u, nodeWeights.get(u));
            }
        }
        return reversed;
    }
    
    /**
     * Get all vertices as a list.
     * @return List of vertex indices
     */
    public List<Integer> getAllVertices() {
        List<Integer> vertices = new ArrayList<>();
        for (int i = 0; i < this.vertices; i++) {
            vertices.add(i);
        }
        return vertices;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Graph{vertices=").append(vertices)
          .append(", directed=").append(directed)
          .append(", weightModel=").append(weightModel)
          .append(", edges=").append(getEdgeCount())
          .append("}\n");
        
        for (int u = 0; u < vertices; u++) {
            if (!adjacencyList.get(u).isEmpty()) {
                sb.append(u).append(" -> ");
                for (Edge edge : adjacencyList.get(u)) {
                    sb.append(edge.to).append("(").append(edge.weight).append(") ");
                }
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}
