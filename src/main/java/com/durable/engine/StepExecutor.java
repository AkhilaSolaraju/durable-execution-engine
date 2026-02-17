package com.durable.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import java.util.concurrent.Callable;

public class StepExecutor {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static <T> T step(
            DurableContext ctx,
            String id,
            Callable<T> fn,
            Class<T> type
    ) throws Exception {

        int seq = ctx.nextSequence();
        String stepKey = id + "-" + seq;

        Optional<String> cached =
                StepRepository.get(ctx.getConnection(),
                                   ctx.getWorkflowId(),
                                   stepKey);

        if (cached.isPresent()) {
            System.out.println("[SKIPPED] " + stepKey);
            return mapper.readValue(cached.get(), type);
        }

        StepRepository.insertRunning(ctx.getConnection(),
                                     ctx.getWorkflowId(),
                                     stepKey);

        T result = fn.call();

        String json = mapper.writeValueAsString(result);

        synchronized (ctx.getLock()) {
            StepRepository.markCompleted(ctx.getConnection(),
                                         ctx.getWorkflowId(),
                                         stepKey,
                                         json);
        }

        System.out.println("[EXECUTED] " + stepKey);

        return result;
    }
}
