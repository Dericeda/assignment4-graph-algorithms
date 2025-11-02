import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Generator for creating diverse graph datasets for testing.
 * Generates 9 datasets: 3 small, 3 medium, 3 large.
 */
public class DatasetGenerator {
    
    private static final Random random = new Random(42); // Fixed seed for reproducibility
    
    public static void main(String[] args) {
        String outputDir = "data/";
        
        try {
            Files.createDirectories(Paths.get(outputDir));
            
            System.out.println("Generating datasets...\n");
            
            // Small datasets (6-10 vertices)
            generateDataset(outputDir + "small_dag.json", 8, 10, false, true, "Small pure DAG");
            generateDataset(outputDir + "small_cycle.json", 7, 12, true, true, "Small with 1-2 cycles");
            generateDataset(outputDir + "small_sparse.json", 10, 9, false, true, "Small sparse graph");
            
            // Medium datasets (10-20 vertices)
            generateDataset(outputDir + "medium_mixed.json", 15, 25, true, true, "Medium mixed structure");
            generateDataset(outputDir + "medium_multiple_sccs.json", 12, 20, true, true, "Medium with multiple SCCs");
            generateDataset(outputDir + "medium_dense.json", 18, 60, true, true, "Medium dense graph");
            
            // Large datasets (20-50 vertices)
            generateDataset(outputDir + "large_sparse.json", 30, 35, false, true, "Large sparse DAG");
            generateDataset(outputDir + "large_complex.json", 40, 80, true, true, "Large complex with SCCs");
            generateDataset(outputDir + "large_dense.json", 25, 100, true, true, "Large dense graph");
            
            System.out.println("\nAll datasets generated successfully in " + outputDir);
            generateDatasetSummary(outputDir);
            
        } catch (IOException e) {
            System.err.println("Error generating datasets: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Generate a single dataset with specified properties.
     */
    private static void generateDataset(String filename, int n, int numEdges, 
                                       boolean allowCycles, boolean directed,
                                       String description) throws IOException {
        
        System.out.println("Generating: " + filename);
        System.out.println("  Description: " + description);
        System.out.println("  Vertices: " + n + ", Target Edges: " + numEdges);
        
        List<Edge> edges = new ArrayList<>();
        Set<String> edgeSet = new HashSet<>();
        
        if (allowCycles) {
            // Generate graph that may contain cycles
            edges = generateGraphWithCycles(n, numEdges, edgeSet);
        } else {
            // Generate pure DAG
            edges = generateDAG(n, numEdges, edgeSet);
        }
        
        // Select random source
        int source = random.nextInt(n);
        
        System.out.println("  Actual Edges: " + edges.size());
        System.out.println("  Source: " + source);
        System.out.println("  Graph Type: " + (allowCycles ? "May contain cycles" : "Pure DAG"));
        
        // Write to JSON file
        writeToJson(filename, n, edges, source, directed);
        System.out.println("  ✓ Generated\n");
    }
    
    /**
     * Generate a DAG (no cycles).
     */
    private static List<Edge> generateDAG(int n, int targetEdges, Set<String> edgeSet) {
        List<Edge> edges = new ArrayList<>();
        
        // Create topological ordering
        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            order.add(i);
        }
        Collections.shuffle(order, random);
        
        // Map vertex to its position in topological order
        Map<Integer, Integer> position = new HashMap<>();
        for (int i = 0; i < n; i++) {
            position.put(order.get(i), i);
        }
        
        // Add edges only from earlier to later in topological order
        int attempts = 0;
        while (edges.size() < targetEdges && attempts < targetEdges * 10) {
            int u = random.nextInt(n);
            int v = random.nextInt(n);
            
            // Ensure u comes before v in topological order
            if (position.get(u) >= position.get(v)) {
                int temp = u;
                u = v;
                v = temp;
            }
            
            if (position.get(u) < position.get(v)) {
                String edgeKey = u + "->" + v;
                if (!edgeSet.contains(edgeKey) && u != v) {
                    int weight = 1 + random.nextInt(10);
                    edges.add(new Edge(u, v, weight));
                    edgeSet.add(edgeKey);
                }
            }
            attempts++;
        }
        
        // Ensure connectivity - add at least one path from start to end
        for (int i = 0; i < order.size() - 1; i++) {
            int u = order.get(i);
            int v = order.get(i + 1);
            String edgeKey = u + "->" + v;
            if (!edgeSet.contains(edgeKey)) {
                int weight = 1 + random.nextInt(10);
                edges.add(new Edge(u, v, weight));
                edgeSet.add(edgeKey);
            }
        }
        
        return edges;
    }
    
    /**
     * Generate a graph that may contain cycles and SCCs.
     */
    private static List<Edge> generateGraphWithCycles(int n, int targetEdges, Set<String> edgeSet) {
        List<Edge> edges = new ArrayList<>();
        
        // First create some SCCs
        int numSCCs = 1 + random.nextInt(Math.max(1, n / 4));
        List<List<Integer>> sccs = new ArrayList<>();
        List<Integer> vertices = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            vertices.add(i);
        }
        Collections.shuffle(vertices, random);
        
        // Distribute vertices into SCCs
        int verticesPerSCC = n / numSCCs;
        for (int i = 0; i < numSCCs; i++) {
            List<Integer> scc = new ArrayList<>();
            int start = i * verticesPerSCC;
            int end = (i == numSCCs - 1) ? n : (i + 1) * verticesPerSCC;
            for (int j = start; j < end; j++) {
                scc.add(vertices.get(j));
            }
            if (!scc.isEmpty()) {
                sccs.add(scc);
            }
        }
        
        // Create cycles within SCCs
        for (List<Integer> scc : sccs) {
            if (scc.size() > 1) {
                for (int i = 0; i < scc.size(); i++) {
                    int u = scc.get(i);
                    int v = scc.get((i + 1) % scc.size());
                    String edgeKey = u + "->" + v;
                    if (!edgeSet.contains(edgeKey)) {
                        int weight = 1 + random.nextInt(10);
                        edges.add(new Edge(u, v, weight));
                        edgeSet.add(edgeKey);
                    }
                }
            }
        }
        
        // Add random edges to reach target
        int attempts = 0;
        while (edges.size() < targetEdges && attempts < targetEdges * 10) {
            int u = random.nextInt(n);
            int v = random.nextInt(n);
            String edgeKey = u + "->" + v;
            
            if (!edgeSet.contains(edgeKey) && u != v) {
                int weight = 1 + random.nextInt(10);
                edges.add(new Edge(u, v, weight));
                edgeSet.add(edgeKey);
            }
            attempts++;
        }
        
        return edges;
    }
    
    /**
     * Write graph to JSON file.
     */
    private static void writeToJson(String filename, int n, List<Edge> edges, 
                                    int source, boolean directed) throws IOException {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"directed\": ").append(directed).append(",\n");
        json.append("  \"n\": ").append(n).append(",\n");
        json.append("  \"edges\": [\n");
        
        for (int i = 0; i < edges.size(); i++) {
            Edge e = edges.get(i);
            json.append("    {\"u\": ").append(e.u).append(", \"v\": ").append(e.v)
                .append(", \"w\": ").append(e.w).append("}");
            if (i < edges.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        
        json.append("  ],\n");
        json.append("  \"source\": ").append(source).append(",\n");
        json.append("  \"weight_model\": \"edge\"\n");
        json.append("}\n");
        
        Files.write(Paths.get(filename), json.toString().getBytes());
    }
    
    /**
     * Generate a summary document for all datasets.
     */
    private static void generateDatasetSummary(String outputDir) throws IOException {
        StringBuilder summary = new StringBuilder();
        summary.append("# Dataset Summary\n\n");
        summary.append("Generated: ").append(new Date()).append("\n\n");
        
        summary.append("## Small Datasets (6-10 vertices)\n");
        summary.append("1. **small_dag.json**: Pure DAG, 8 vertices, ~10 edges\n");
        summary.append("2. **small_cycle.json**: Contains 1-2 cycles, 7 vertices, ~12 edges\n");
        summary.append("3. **small_sparse.json**: Sparse structure, 10 vertices, ~9 edges\n\n");
        
        summary.append("## Medium Datasets (10-20 vertices)\n");
        summary.append("4. **medium_mixed.json**: Mixed structure with SCCs, 15 vertices, ~25 edges\n");
        summary.append("5. **medium_multiple_sccs.json**: Multiple distinct SCCs, 12 vertices, ~20 edges\n");
        summary.append("6. **medium_dense.json**: Dense connectivity, 18 vertices, ~60 edges\n\n");
        
        summary.append("## Large Datasets (20-50 vertices)\n");
        summary.append("7. **large_sparse.json**: Sparse DAG for performance testing, 30 vertices, ~35 edges\n");
        summary.append("8. **large_complex.json**: Complex structure with multiple SCCs, 40 vertices, ~80 edges\n");
        summary.append("9. **large_dense.json**: Dense graph for stress testing, 25 vertices, ~100 edges\n\n");
        
        summary.append("All datasets use edge weight model with weights ranging from 1 to 10.\n");
        
        Files.write(Paths.get(outputDir + "DATASETS.md"), summary.toString().getBytes());
        System.out.println("\nDataset summary written to " + outputDir + "DATASETS.md");
    }
    
    /**
     * Simple edge class for dataset generation.
     */
    private static class Edge {
        int u, v, w;
        
        Edge(int u, int v, int w) {
            this.u = u;
            this.v = v;
            this.w = w;
        }
    }
}
