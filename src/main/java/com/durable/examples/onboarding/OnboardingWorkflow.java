package com.durable.examples.onboarding;

import com.durable.engine.*;
import java.util.concurrent.CompletableFuture;

public class OnboardingWorkflow {

    public static void run(DurableContext ctx, int crashAfter) throws Exception {

        // Step 1
        StepExecutor.step(ctx, "create-record", () -> {
            Thread.sleep(1000);
            return "Employee Created";
        }, String.class);

        if (crashAfter == 1) {
            System.out.println("💥 Simulating Crash After Step 1...");
            System.exit(1);
        }

        // Step 2 & 3 (Parallel)
        CompletableFuture<String> laptop =
            CompletableFuture.supplyAsync(() -> {
                try {
                    return StepExecutor.step(ctx,
                            "provision-laptop",
                            () -> {
                                Thread.sleep(2000);
                                return "Laptop Ready";
                            },
                            String.class);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

        CompletableFuture<String> access =
            CompletableFuture.supplyAsync(() -> {
                try {
                    return StepExecutor.step(ctx,
                            "provision-access",
                            () -> {
                                Thread.sleep(2000);
                                return "Access Granted";
                            },
                            String.class);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

        CompletableFuture.allOf(laptop, access).join();

        if (crashAfter == 2) {
            System.out.println("💥 Simulating Crash After Step 2 & 3...");
            System.exit(1);
        }

        // Step 4
        StepExecutor.step(ctx, "send-email", () -> {
            Thread.sleep(1000);
            return "Email Sent";
        }, String.class);
    }
}
