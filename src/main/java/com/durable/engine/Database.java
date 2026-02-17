package com.durable.engine;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class Database {

    public static Connection connect() throws Exception {
        Connection conn = DriverManager.getConnection("jdbc:sqlite:steps.db");

        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA journal_mode=WAL;");
            stmt.execute("PRAGMA busy_timeout = 5000;");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS steps (
                    workflow_id TEXT,
                    step_key TEXT,
                    status TEXT,
                    output TEXT,
                    PRIMARY KEY(workflow_id, step_key)
                );
            """);
        }

        return conn;
    }
}
