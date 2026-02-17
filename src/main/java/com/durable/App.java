package com.durable;

import com.durable.engine.*;
import com.durable.examples.onboarding.OnboardingWorkflow;

import java.sql.Connection;

public class App {

    public static void main(String[] args) throws Exception {

        Connection conn = Database.connect();
        DurableContext ctx =
                new DurableContext("workflow-1", conn);

        int crashAfter = 0;

        if (args.length > 0) {
            crashAfter = Integer.parseInt(args[0]);
        }

        OnboardingWorkflow.run(ctx, crashAfter);

        conn.close();
    }
}
