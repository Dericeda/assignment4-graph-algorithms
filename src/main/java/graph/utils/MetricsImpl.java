package graph.utils;

import java.util.*;

/**
 * Implementation of Metrics interface for tracking algorithm performance.
 */
public class MetricsImpl implements Metrics {
    private final Map<String, Long> counters;
    private long startTime;
    private long endTime;
    private boolean timerRunning;
    
    /**
     * Constructor initializes empty metrics.
     */
    public MetricsImpl() {
        this.counters = new HashMap<>();
        this.startTime = 0;
        this.endTime = 0;
        this.timerRunning = false;
    }
    
    @Override
    public void startTimer() {
        startTime = System.nanoTime();
        timerRunning = true;
    }
    
    @Override
    public long stopTimer() {
        if (timerRunning) {
            endTime = System.nanoTime();
            timerRunning = false;
            return endTime - startTime;
        }
        return 0;
    }
    
    @Override
    public double getElapsedTimeMs() {
        long elapsed = timerRunning ? System.nanoTime() - startTime : endTime - startTime;
        return elapsed / 1_000_000.0;
    }
    
    @Override
    public void incrementCounter(String counterName) {
        counters.put(counterName, counters.getOrDefault(counterName, 0L) + 1);
    }
    
    @Override
    public void incrementCounter(String counterName, int amount) {
        counters.put(counterName, counters.getOrDefault(counterName, 0L) + amount);
    }
    
    @Override
    public long getCounter(String counterName) {
        return counters.getOrDefault(counterName, 0L);
    }
    
    @Override
    public void reset() {
        counters.clear();
        startTime = 0;
        endTime = 0;
        timerRunning = false;
    }
    
    @Override
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Metrics Summary:\n");
        sb.append(String.format("  Elapsed Time: %.3f ms\n", getElapsedTimeMs()));
        
        List<String> sortedKeys = new ArrayList<>(counters.keySet());
        Collections.sort(sortedKeys);
        
        for (String key : sortedKeys) {
            sb.append(String.format("  %s: %d\n", key, counters.get(key)));
        }
        return sb.toString();
    }
    
    @Override
    public Set<String> getCounterNames() {
        return new HashSet<>(counters.keySet());
    }
    
    @Override
    public String toString() {
        return getSummary();
    }
}
