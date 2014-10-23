package org.wildfly.metrics.scheduler.storage;

import org.wildfly.metrics.scheduler.polling.Task;

/**
 * @author Heiko Braun
 * @since 13/10/14
 */
public final class DataPoint {
    private Task task;
    private long timestamp;
    private double value;

    public DataPoint(Task task, double value) {
        this.task = task;
        this.timestamp = System.currentTimeMillis();
        this.value = value;
    }

    public Task getTask() {
        return task;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public double getValue() {
        return value;
    }
}
