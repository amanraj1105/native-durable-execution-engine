package com.aman.durable.engine;

public class DurableContext {

    private final String workflowId;
    private final Storage storage;
    private final SequenceManager sequenceManager;
    private final long crashAt;

    public DurableContext(String workflowId, Storage storage, long crashAt) {
        this.workflowId = workflowId;
        this.storage = storage;
        this.sequenceManager = new SequenceManager();
        this.crashAt = crashAt;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public Storage getStorage() {
        return storage;
    }

    public SequenceManager getSequenceManager() {
        return sequenceManager;
    }

    public long getCrashAt() {
        return crashAt;
    }
}