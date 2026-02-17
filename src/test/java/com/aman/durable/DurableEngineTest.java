package com.aman.durable;

import com.aman.durable.engine.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DurableEngineTest {

    @Test
    public void testSimpleStepExecution() throws Exception {

        Storage storage = new Storage("jdbc:sqlite::memory:");
        DurableContext ctx = new DurableContext("test_workflow", storage, -1);
        StepExecutor executor = new StepExecutor(ctx);

        String result = executor.step("test_step", () -> "OK", String.class);

        assertEquals("OK", result);
    }
}