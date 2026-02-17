package com.aman.durable.examples.onboarding;

import com.aman.durable.engine.StepExecutor;

import java.util.concurrent.*;

public class EmployeeOnboardingWorkflow {

    private final StepExecutor executor;

    public EmployeeOnboardingWorkflow(StepExecutor executor) {
        this.executor = executor;
    }

    public void run() throws Exception {

        // Step 1: Sequential
        executor.step("create_record", () -> {
            System.out.println("Creating employee record...");
            Thread.sleep(1000);
            return "EMPLOYEE_CREATED";
        }, String.class);

        ExecutorService pool = Executors.newFixedThreadPool(2);

        // Step 2: Parallel
        CompletableFuture<Void> laptop =
                CompletableFuture.runAsync(() -> {
                    try {
                        executor.step("provision_laptop", () -> {
                            System.out.println("Provisioning laptop...");
                            Thread.sleep(2000);
                            return "LAPTOP_PROVISIONED";
                        }, String.class);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }, pool);

        // Step 3: Parallel
        CompletableFuture<Void> access =
                CompletableFuture.runAsync(() -> {
                    try {
                        executor.step("provision_access", () -> {
                            System.out.println("Provisioning access...");
                            Thread.sleep(2000);
                            return "ACCESS_GRANTED";
                        }, String.class);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }, pool);

        CompletableFuture.allOf(laptop, access).join();

        // Step 4: Sequential
        executor.step("send_email", () -> {
            System.out.println("Sending welcome email...");
            return "EMAIL_SENT";
        }, String.class);

        pool.shutdown();
    }
}