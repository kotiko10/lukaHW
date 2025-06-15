package com.example.util;

import com.github.database.rider.core.api.connection.ConnectionHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Test-specific DatabaseManager that uses the test ConnectionHolder
 * instead of a production connection pool.
 */
public class TestDatabaseManager extends DatabaseManager {
    private static final Logger logger = LoggerFactory.getLogger(TestDatabaseManager.class);
    private final ConnectionHolder connectionHolder;

    public TestDatabaseManager(ConnectionHolder connectionHolder) {
        super();
        this.connectionHolder = connectionHolder;
    }

    @Override
    public Connection getConnection() throws SQLException {
        try {
            Connection conn = connectionHolder.getConnection();
            logger.debug("Retrieved test database connection: {}", conn);
            return conn;
        } catch (Exception e) {
            logger.error("Error getting test database connection", e);
            throw new SQLException("Error getting test database connection", e);
        }
    }

    public static void shutdown() {
        logger.debug("Test database shutdown (no-op)");
    }
}