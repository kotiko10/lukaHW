package com.example.dao;

import com.example.util.TestDatabaseManager;
import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.connection.ConnectionHolder;
import com.github.database.rider.junit5.DBUnitExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

@ExtendWith(DBUnitExtension.class)
@DBUnit(qualifiedTableNames = true)
public abstract class BaseDaoTest {

    protected TestDatabaseManager testDatabaseManager;

    protected ConnectionHolder connectionHolder = () -> {
        Connection conn = DriverManager.getConnection(
                "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
                "sa",
                ""
        );
        createTablesIfNotExist(conn);
        return conn;
    };

    @BeforeEach
    void setUpTestDatabase() throws SQLException {
        testDatabaseManager = new TestDatabaseManager(connectionHolder);

        cleanDatabase();
    }

    void cleanDatabase() throws SQLException {
        Connection conn = connectionHolder.getConnection();
        Statement stmt = conn.createStatement();

        stmt.execute("DELETE FROM users;");
        stmt.execute("DELETE FROM roles;");
        stmt.close();
    }

    private void createTablesIfNotExist(@NotNull Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();

        stmt.execute(
                "CREATE TABLE IF NOT EXISTS roles (" +
                        "    id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                        "    name VARCHAR(50) NOT NULL UNIQUE" +
                        ")"
        );

        stmt.execute(
                "CREATE TABLE IF NOT EXISTS users (" +
                        "    id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                        "    login VARCHAR(50) NOT NULL UNIQUE," +
                        "    password VARCHAR(255) NOT NULL," +
                        "    email VARCHAR(100) NOT NULL UNIQUE," +
                        "    first_name VARCHAR(50)," +
                        "    last_name VARCHAR(50)," +
                        "    birthday DATE," +
                        "    role_id BIGINT," +
                        "    FOREIGN KEY (role_id) REFERENCES roles(id)" +
                        ")"
        );
        stmt.close();
    }

    protected TestDatabaseManager getTestDatabaseManager() {
        return testDatabaseManager;
    }
}