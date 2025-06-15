package com.example.util;

import com.example.exceptions.LoadDatabasePropertiesException;
import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseManager {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private static DatabaseManager instance;
    private static BasicDataSource dataSource;

    protected DatabaseManager() {
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    public static synchronized BasicDataSource getDataSource() {
        if (dataSource == null) {
            Properties props = loadDatabaseProperties();

            BasicDataSource basicDataSource = new BasicDataSource();
            basicDataSource.setDriverClassName(props.getProperty("db.driver"));
            basicDataSource.setUrl(props.getProperty("db.url"));
            basicDataSource.setUsername(props.getProperty("db.username"));
            basicDataSource.setPassword(props.getProperty("db.password"));

            basicDataSource.setMinIdle(Integer.parseInt(props.getProperty("db.pool.minIdle", "5")));
            basicDataSource.setMaxIdle(Integer.parseInt(props.getProperty("db.pool.maxIdle", "10")));
            basicDataSource.setMaxTotal(Integer.parseInt(props.getProperty("db.pool.maxTotal", "20")));
            basicDataSource.setMaxOpenPreparedStatements(10);
            basicDataSource.setDefaultAutoCommit(false);

            basicDataSource.setMaxWait(java.time.Duration.ofMillis(Long.parseLong(props.getProperty("db.pool.maxWaitMillis", "30000"))));
            basicDataSource.setValidationQuery(props.getProperty("db.pool.validationQuery", "SELECT 1"));
            basicDataSource.setTestOnBorrow(true);
            basicDataSource.setTestWhileIdle(true);

            dataSource = basicDataSource;
            logger.info("Database connection pool initialized with URL: {}", props.getProperty("db.url"));
        }
        return dataSource;
    }

    public Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    private static Properties loadDatabaseProperties() {
        Properties props = new Properties();
        try (InputStream input = DatabaseManager.class.getClassLoader().getResourceAsStream("database.properties")) {
            if (input == null) {
                throw new RuntimeException("Unable to find database.properties");
            }
            props.load(input);
        } catch (IOException e) {
            throw new LoadDatabasePropertiesException("Error loading database properties", e);
        }
        return props;
    }

    public static void shutdown() {
        try {
            if (dataSource != null) {
                dataSource.close();
                logger.info("Database connection pool shut down");
                dataSource = null;
            }
        } catch (SQLException e) {
            logger.error("Error shutting down database connection pool", e);
        }
    }
}