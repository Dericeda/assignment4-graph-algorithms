package graph.utils;

/**
 * Interface for tracking algorithm performance metrics.
 * Provides operation counters and timing capabilities.
 */
public interface Metrics {
    
    /**
     * Start timing an operation.
     */
    void startTimer();
    
    /**
     * Stop timing and return elapsed time in nanoseconds.
     * @return Elapsed time in nanoseconds
     */
    long stopTimer();
    
    /**
     * Get elapsed time in milliseconds.
     * @return Elapsed time in milliseconds
     */
    double getElapsedTimeMs();
    
    /**
     * Increment a named counter.
     * @param counterName Name of the counter
     */
    void incrementCounter(String counterName);
    
    /**
     * Increment a named counter by a specific amount.
     * @param counterName Name of the counter
     * @param amount Amount to increment
     */
    void incrementCounter(String counterName, int amount);
    
    /**
     * Get the value of a named counter.
     * @param counterName Name of the counter
     * @return Counter value
     */
    long getCounter(String counterName);
    
    /**
     * Reset all metrics.
     */
    void reset();
    
    /**
     * Get a summary of all metrics.
     * @return String representation of metrics
     */
    String getSummary();
    
    /**
     * Get all counter names.
     * @return Set of counter names
     */
    java.util.Set<String> getCounterNames();
}
