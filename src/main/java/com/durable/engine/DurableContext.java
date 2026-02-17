package com.durable.engine;

import java.sql.Connection;
import java.util.concurrent.atomic.AtomicInteger;

public class DurableContext {

    private final String workflowId;
    private final Connection connection;
    private final AtomicInteger sequence = new AtomicInteger(0);
    private final Object lock = new Object();

    public DurableContext(String workflowId, Connection connection) {
        this.workflowId = workflowId;
        this.connection = connection;
    }

    public int nextSequence() {
        return sequence.incrementAndGet();
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public Connection getConnection() {
        return connection;
    }

    public Object getLock() {
        return lock;
    }
}
