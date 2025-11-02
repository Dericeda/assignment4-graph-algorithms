package graph.utils;

/**
 * Represents an edge in a graph with a destination vertex and weight.
 */
public class Edge {
    public final int to;
    public final int weight;
    
    /**
     * Constructor for an edge.
     * @param to Destination vertex
     * @param weight Edge weight
     */
    public Edge(int to, int weight) {
        this.to = to;
        this.weight = weight;
    }
    
    @Override
    public String toString() {
        return "Edge{to=" + to + ", weight=" + weight + "}";
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Edge edge = (Edge) o;
        return to == edge.to && weight == edge.weight;
    }
    
    @Override
    public int hashCode() {
        return 31 * to + weight;
    }
}
