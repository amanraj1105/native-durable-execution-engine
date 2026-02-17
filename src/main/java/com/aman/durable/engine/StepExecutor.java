package com.aman.durable.engine;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;
import java.util.concurrent.Callable;

public class StepExecutor {

    private final DurableContext ctx;
    private final ObjectMapper mapper = new ObjectMapper();

    public StepExecutor(DurableContext ctx) {
        this.ctx = ctx;
    }

    public <T> T step(String id, Callable<T> fn, Class<T> clazz) throws Exception {

        SequenceManager seqManager = ctx.getSequenceManager();

        if (id == null || id.isBlank()) {
            id = seqManager.autoStepId(); // Bonus feature
        }

        long seq = seqManager.next();
        String stepKey = id + "_" + seq;

        // 1️⃣ Check if already completed
        Optional<String> cached =
                ctx.getStorage().getCompletedStep(ctx.getWorkflowId(), stepKey);

        if (cached.isPresent()) {
            System.out.println("Skipping completed step: " + stepKey);
            return mapper.readValue(cached.get(), clazz);
        }

        // 2️⃣ Detect zombie step
        if (ctx.getStorage().isInProgress(ctx.getWorkflowId(), stepKey)) {
            System.out.println("Zombie step detected. Re-executing: " + stepKey);
        }

        ctx.getStorage().markInProgress(ctx.getWorkflowId(), stepKey);

        // 3️⃣ Crash simulation
        if (ctx.getCrashAt() == seq) {
            System.out.println("Simulating crash at step: " + stepKey);
            System.exit(1);
        }

        // 4️⃣ Execute actual function
        T result = fn.call();

        // 5️⃣ Serialize
        String output = mapper.writeValueAsString(result);

        // 6️⃣ Mark completed
        ctx.getStorage().markCompleted(ctx.getWorkflowId(), stepKey, output);

        return result;
    }
}