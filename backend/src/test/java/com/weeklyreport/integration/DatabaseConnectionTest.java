package com.weeklyreport.integration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for database connectivity and basic operations.
 * These tests verify that the database configuration works correctly.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DatabaseConnectionTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void canConnectToDatabase() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            assertThat(connection).isNotNull();
            assertThat(connection.isClosed()).isFalse();
        }
    }

    @Test
    void canExecuteBasicQuery() throws Exception {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT 1 as test_value")) {
            
            assertThat(resultSet.next()).isTrue();
            assertThat(resultSet.getInt("test_value")).isEqualTo(1);
        }
    }

    @Test
    void canCreateAndDropTable() throws Exception {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            
            // Create test table
            statement.execute("CREATE TABLE test_table (id INTEGER PRIMARY KEY, name VARCHAR(50))");
            
            // Insert test data
            statement.execute("INSERT INTO test_table (id, name) VALUES (1, 'test')");
            
            // Query test data
            try (ResultSet resultSet = statement.executeQuery("SELECT * FROM test_table WHERE id = 1")) {
                assertThat(resultSet.next()).isTrue();
                assertThat(resultSet.getString("name")).isEqualTo("test");
            }
            
            // Drop test table
            statement.execute("DROP TABLE test_table");
        }
    }

    @Test
    void connectionPoolIsWorking() throws Exception {
        // Test multiple simultaneous connections
        Connection[] connections = new Connection[3];
        
        try {
            for (int i = 0; i < connections.length; i++) {
                connections[i] = dataSource.getConnection();
                assertThat(connections[i]).isNotNull();
                assertThat(connections[i].isClosed()).isFalse();
            }
        } finally {
            for (Connection connection : connections) {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
            }
        }
    }
}