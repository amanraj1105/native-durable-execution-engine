package com.aman.durable.engine;

import java.sql.SQLException;

public class RetryUtils {

    private static final int MAX_RETRIES = 5;
    private static final long BASE_DELAY_MS = 50;

    public static void retryOnBusy(SQLRunnable runnable) throws SQLException {
        int attempts = 0;

        while (true) {
            try {
                runnable.run();
                return;
            } catch (SQLException e) {
                if (!e.getMessage().contains("SQLITE_BUSY") || attempts >= MAX_RETRIES) {
                    throw e;
                }
                attempts++;
                try {
                    Thread.sleep(BASE_DELAY_MS * attempts);
                } catch (InterruptedException ignored) {}
            }
        }
    }

    @FunctionalInterface
    public interface SQLRunnable {
        void run() throws SQLException;
    }
}