package com.aman.durable.engine;

import java.sql.*;
import java.time.Instant;
import java.util.Optional;

public class Storage {

    private final Connection connection;

    public Storage(String dbUrl) throws SQLException {
        this.connection = DriverManager.getConnection(dbUrl);
        initialize();
    }

    private void initialize() throws SQLException {
        try (Statement stmt = connection.createStatement()) {

            stmt.execute("PRAGMA journal_mode=WAL;");
            stmt.execute("PRAGMA busy_timeout=3000;");

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS steps (
                        workflow_id TEXT NOT NULL,
                        step_key TEXT NOT NULL,
                        status TEXT NOT NULL,
                        output TEXT,
                        updated_at INTEGER,
                        PRIMARY KEY (workflow_id, step_key)
                    );
            """);
        }
    }

    public Optional<String> getCompletedStep(String workflowId, String stepKey) throws SQLException {

        String query = "SELECT status, output FROM steps WHERE workflow_id=? AND step_key=?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, workflowId);
            ps.setString(2, stepKey);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                if ("COMPLETED".equals(rs.getString("status"))) {
                    return Optional.ofNullable(rs.getString("output"));
                }
            }
        }
        return Optional.empty();
    }

    public boolean isInProgress(String workflowId, String stepKey) throws SQLException {

        String query = "SELECT status FROM steps WHERE workflow_id=? AND step_key=?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, workflowId);
            ps.setString(2, stepKey);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return "IN_PROGRESS".equals(rs.getString("status"));
            }
        }
        return false;
    }

    public void markInProgress(String workflowId, String stepKey) throws SQLException {

        RetryUtils.retryOnBusy(() -> {
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT OR REPLACE INTO steps(workflow_id, step_key, status, updated_at) VALUES(?,?,?,?)")) {

                ps.setString(1, workflowId);
                ps.setString(2, stepKey);
                ps.setString(3, "IN_PROGRESS");
                ps.setLong(4, Instant.now().toEpochMilli());
                ps.executeUpdate();
            }
        });
    }

    public void markCompleted(String workflowId, String stepKey, String output) throws SQLException {

        RetryUtils.retryOnBusy(() -> {
            try (PreparedStatement ps = connection.prepareStatement(
                    "UPDATE steps SET status=?, output=?, updated_at=? WHERE workflow_id=? AND step_key=?")) {

                ps.setString(1, "COMPLETED");
                ps.setString(2, output);
                ps.setLong(3, Instant.now().toEpochMilli());
                ps.setString(4, workflowId);
                ps.setString(5, stepKey);
                ps.executeUpdate();
            }
        });
    }
}