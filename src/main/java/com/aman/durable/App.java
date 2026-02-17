package com.aman.durable;

import com.aman.durable.engine.*;
import com.aman.durable.examples.onboarding.EmployeeOnboardingWorkflow;

public class App {

    public static void main(String[] args) throws Exception {

        String workflowId = "employee_1";
        long crashAt = -1;

        for (String arg : args) {
            if (arg.startsWith("--workflow=")) {
                workflowId = arg.split("=")[1];
            }
            if (arg.startsWith("--crash-at=")) {
                crashAt = Long.parseLong(arg.split("=")[1]);
            }
        }

        Storage storage = new Storage("jdbc:sqlite:durable.db");

        DurableContext context =
                new DurableContext(workflowId, storage, crashAt);

        StepExecutor executor = new StepExecutor(context);

        EmployeeOnboardingWorkflow workflow =
                new EmployeeOnboardingWorkflow(executor);

        workflow.run();

        System.out.println("Workflow completed successfully!");
    }
}