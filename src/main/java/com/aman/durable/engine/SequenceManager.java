package com.aman.durable.engine;

import java.util.concurrent.atomic.AtomicLong;

public class SequenceManager {

    private final AtomicLong logicalClock = new AtomicLong(0);

    public long next() {
        return logicalClock.incrementAndGet();
    }

    // Automatic ID using caller method name
    public String autoStepId() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        // stack[3] usually refers to caller method
        return stack[3].getMethodName();
    }
}