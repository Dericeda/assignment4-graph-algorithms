package graph.utils;

import java.io.*;
import java.nio.file.*;
import java.util.regex.*;

/**
 * Utility class for loading graphs from JSON files.
 * Supports the format specified in the assignment.
 */
public class GraphLoader {
    
    /**
     * Load a graph from a JSON file.
     * @param filepath Path to the JSON file
     * @return Loaded graph
     * @throws IOException If file cannot be read
     */
    public static GraphData loadFromJson(String filepath) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(filepath)));
        return parseJson(content);
    }
    
    /**
     * Parse JSON content into GraphData.
     * Simple parser for the specific JSON format used in assignment.
     * @param json JSON string
     * @return Parsed graph data
     */
    private static GraphData parseJson(String json) {
        GraphData data = new GraphData();
        
        // Parse directed
        Pattern directedPattern = Pattern.compile("\"directed\"\\s*:\\s*(true|false)");
        Matcher directedMatcher = directedPattern.matcher(json);
        if (directedMatcher.find()) {
            data.directed = Boolean.parseBoolean(directedMatcher.group(1));
        }
        
        // Parse n (number of vertices)
        Pattern nPattern = Pattern.compile("\"n\"\\s*:\\s*(\\d+)");
        Matcher nMatcher = nPattern.matcher(json);
        if (nMatcher.find()) {
            data.n = Integer.parseInt(nMatcher.group(1));
        }
        
        // Parse weight_model
        Pattern weightModelPattern = Pattern.compile("\"weight_model\"\\s*:\\s*\"([^\"]+)\"");
        Matcher weightModelMatcher = weightModelPattern.matcher(json);
        if (weightModelMatcher.find()) {
            data.weightModel = weightModelMatcher.group(1);
        } else {
            data.weightModel = "edge"; // Default
        }
        
        // Parse source
        Pattern sourcePattern = Pattern.compile("\"source\"\\s*:\\s*(\\d+)");
        Matcher sourceMatcher = sourcePattern.matcher(json);
        if (sourceMatcher.find()) {
            data.source = Integer.parseInt(sourceMatcher.group(1));
        }
        
        // Parse edges
        Pattern edgePattern = Pattern.compile("\\{\\s*\"u\"\\s*:\\s*(\\d+)\\s*,\\s*\"v\"\\s*:\\s*(\\d+)\\s*,\\s*\"w\"\\s*:\\s*(\\d+)\\s*\\}");
        Matcher edgeMatcher = edgePattern.matcher(json);
        
        while (edgeMatcher.find()) {
            int u = Integer.parseInt(edgeMatcher.group(1));
            int v = Integer.parseInt(edgeMatcher.group(2));
            int w = Integer.parseInt(edgeMatcher.group(3));
            data.edges.add(new EdgeData(u, v, w));
        }
        
        return data;
    }
    
    /**
     * Create a Graph object from GraphData.
     * @param data Graph data
     * @return Constructed graph
     */
    public static Graph createGraph(GraphData data) {
        Graph graph = new Graph(data.n, data.directed, data.weightModel);
        
        for (EdgeData edge : data.edges) {
            graph.addEdge(edge.u, edge.v, edge.w);
        }
        
        return graph;
    }
    
    /**
     * Container for parsed graph data.
     */
    public static class GraphData {
        public boolean directed = true;
        public int n = 0;
        public String weightModel = "edge";
        public int source = 0;
        public java.util.List<EdgeData> edges = new java.util.ArrayList<>();
        
        @Override
        public String toString() {
            return String.format("GraphData{n=%d, directed=%s, weightModel=%s, source=%d, edges=%d}",
                    n, directed, weightModel, source, edges.size());
        }
    }
    
    /**
     * Container for edge data.
     */
    public static class EdgeData {
        public final int u;
        public final int v;
        public final int w;
        
        public EdgeData(int u, int v, int w) {
            this.u = u;
            this.v = v;
            this.w = w;
        }
        
        @Override
        public String toString() {
            return String.format("(%d -> %d, w=%d)", u, v, w);
        }
    }
}
