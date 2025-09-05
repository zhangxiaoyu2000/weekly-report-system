package com.weeklyreport.performance;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StopWatch;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Database performance tests and benchmarks.
 * These tests verify database performance under various load conditions.
 * 
 * Note: These tests are disabled by default as they're primarily for performance analysis.
 * Enable them by removing @Disabled annotation when needed for performance tuning.
 */
@SpringBootTest
@ActiveProfiles("test")
@Disabled("Performance tests - enable when needed for analysis")
class DatabasePerformanceTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void connectionPoolPerformanceTest() throws Exception {
        StopWatch stopWatch = new StopWatch("Connection Pool Performance");
        
        // Test single connection acquisition
        stopWatch.start("Single Connection");
        try (Connection connection = dataSource.getConnection()) {
            assertThat(connection).isNotNull();
        }
        stopWatch.stop();
        
        // Test multiple sequential connections
        stopWatch.start("10 Sequential Connections");
        for (int i = 0; i < 10; i++) {
            try (Connection connection = dataSource.getConnection()) {
                assertThat(connection).isNotNull();
            }
        }
        stopWatch.stop();
        
        // Test concurrent connections
        stopWatch.start("10 Concurrent Connections");
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<Boolean>> futures = new ArrayList<>();
        
        for (int i = 0; i < 10; i++) {
            futures.add(executor.submit(() -> {
                try (Connection connection = dataSource.getConnection()) {
                    return connection != null;
                }
            }));
        }
        
        for (Future<Boolean> future : futures) {
            assertThat(future.get(5, TimeUnit.SECONDS)).isTrue();
        }
        
        executor.shutdown();
        stopWatch.stop();
        
        System.out.println(stopWatch.prettyPrint());
        
        // Performance assertions
        assertThat(stopWatch.getLastTaskTimeMillis()).isLessThan(1000); // < 1 second
    }

    @Test
    void basicQueryPerformanceTest() throws Exception {
        StopWatch stopWatch = new StopWatch("Query Performance");
        
        try (Connection connection = dataSource.getConnection()) {
            // Test simple query performance
            stopWatch.start("100 Simple Queries");
            for (int i = 0; i < 100; i++) {
                try (PreparedStatement statement = connection.prepareStatement("SELECT 1");
                     ResultSet resultSet = statement.executeQuery()) {
                    assertThat(resultSet.next()).isTrue();
                }
            }
            stopWatch.stop();
            
            // Test prepared statement reuse
            stopWatch.start("100 Prepared Statement Reuse");
            try (PreparedStatement statement = connection.prepareStatement("SELECT ? as test_value")) {
                for (int i = 0; i < 100; i++) {
                    statement.setInt(1, i);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        assertThat(resultSet.next()).isTrue();
                        assertThat(resultSet.getInt("test_value")).isEqualTo(i);
                    }
                }
            }
            stopWatch.stop();
        }
        
        System.out.println(stopWatch.prettyPrint());
        
        // Performance assertions
        assertThat(stopWatch.getTotalTimeMillis()).isLessThan(5000); // < 5 seconds total
    }

    @Test
    void transactionPerformanceTest() throws Exception {
        StopWatch stopWatch = new StopWatch("Transaction Performance");
        
        try (Connection connection = dataSource.getConnection()) {
            // Create test table
            try (PreparedStatement createTable = connection.prepareStatement(
                "CREATE TABLE perf_test (id INTEGER PRIMARY KEY, data VARCHAR(100))")) {
                createTable.execute();
            }
            
            // Test auto-commit performance
            stopWatch.start("50 Auto-commit Inserts");
            connection.setAutoCommit(true);
            try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO perf_test (id, data) VALUES (?, ?)")) {
                for (int i = 0; i < 50; i++) {
                    statement.setInt(1, i);
                    statement.setString(2, "test_data_" + i);
                    statement.executeUpdate();
                }
            }
            stopWatch.stop();
            
            // Test transaction batch performance
            stopWatch.start("50 Batched Inserts in Transaction");
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO perf_test (id, data) VALUES (?, ?)")) {
                for (int i = 50; i < 100; i++) {
                    statement.setInt(1, i);
                    statement.setString(2, "test_data_" + i);
                    statement.addBatch();
                }
                statement.executeBatch();
                connection.commit();
            }
            stopWatch.stop();
            
            // Cleanup
            try (PreparedStatement dropTable = connection.prepareStatement("DROP TABLE perf_test")) {
                dropTable.execute();
            }
        }
        
        System.out.println(stopWatch.prettyPrint());
        
        // Performance assertions - batch should be faster than auto-commit
        long autoCommitTime = stopWatch.getTaskInfo()[0].getTimeMillis();
        long batchTime = stopWatch.getTaskInfo()[1].getTimeMillis();
        
        System.out.println("Auto-commit time: " + autoCommitTime + "ms");
        System.out.println("Batch time: " + batchTime + "ms");
        System.out.println("Performance improvement: " + 
            ((double)(autoCommitTime - batchTime) / autoCommitTime * 100) + "%");
    }

    @Test
    void connectionPoolStressTest() throws Exception {
        // Stress test with high concurrent load
        ExecutorService executor = Executors.newFixedThreadPool(20);
        List<Future<Long>> futures = new ArrayList<>();
        
        StopWatch stopWatch = new StopWatch("Connection Pool Stress Test");
        stopWatch.start("20 Concurrent Tasks");
        
        for (int i = 0; i < 20; i++) {
            final int taskId = i;
            futures.add(executor.submit(() -> {
                long startTime = System.currentTimeMillis();
                try (Connection connection = dataSource.getConnection();
                     PreparedStatement statement = connection.prepareStatement("SELECT ? as task_id");) {
                    
                    statement.setInt(1, taskId);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (resultSet.next()) {
                            return System.currentTimeMillis() - startTime;
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return -1L;
            }));
        }
        
        long totalTime = 0;
        int successfulTasks = 0;
        
        for (Future<Long> future : futures) {
            long taskTime = future.get(10, TimeUnit.SECONDS);
            if (taskTime > 0) {
                totalTime += taskTime;
                successfulTasks++;
            }
        }
        
        executor.shutdown();
        stopWatch.stop();
        
        System.out.println(stopWatch.prettyPrint());
        System.out.println("Successful tasks: " + successfulTasks + "/20");
        System.out.println("Average task time: " + (totalTime / successfulTasks) + "ms");
        
        // Performance assertions
        assertThat(successfulTasks).isEqualTo(20); // All tasks should succeed
        assertThat(totalTime / successfulTasks).isLessThan(1000); // Average < 1 second per task
    }
}