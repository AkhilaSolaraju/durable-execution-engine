package com.durable.engine;

import java.sql.*;
import java.util.Optional;

public class StepRepository {

    public static Optional<String> get(Connection conn,
                                       String workflowId,
                                       String stepKey) throws Exception {

        String sql = "SELECT output FROM steps WHERE workflow_id=? AND step_key=? AND status='COMPLETED'";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, workflowId);
            ps.setString(2, stepKey);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(rs.getString("output"));
            }
        }

        return Optional.empty();
    }

    public static void insertRunning(Connection conn,
                                     String workflowId,
                                     String stepKey) throws Exception {

        String sql = "INSERT OR IGNORE INTO steps(workflow_id, step_key, status) VALUES(?,?,?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, workflowId);
            ps.setString(2, stepKey);
            ps.setString(3, "RUNNING");
            ps.executeUpdate();
        }
    }

    public static void markCompleted(Connection conn,
                                     String workflowId,
                                     String stepKey,
                                     String output) throws Exception {

        String sql = "UPDATE steps SET status='COMPLETED', output=? WHERE workflow_id=? AND step_key=?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, output);
            ps.setString(2, workflowId);
            ps.setString(3, stepKey);
            ps.executeUpdate();
        }
    }
}
