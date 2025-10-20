package com.weeklyreport.common.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class SimpleCommentsTableFix implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== SIMPLE COMMENTS TABLE FIX ===");

        try {
            // Check and add attachments column to comments table if it doesn't exist
            try {
                // Check if attachments column exists
                jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE " +
                    "TABLE_SCHEMA = 'weekly_report_system' AND TABLE_NAME = 'comments' AND COLUMN_NAME = 'attachments'",
                    Integer.class
                );
                System.out.println("Attachments column already exists");
            } catch (Exception e) {
                // Column doesn't exist, add it - use TEXT instead of JSON for broader compatibility
                jdbcTemplate.execute("ALTER TABLE comments ADD COLUMN attachments TEXT");
                System.out.println("Added attachments column to comments table");
            }

            // Check and add weekly_report_id column to comments table if it doesn't exist
            try {
                Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE " +
                    "TABLE_SCHEMA = 'weekly_report_system' AND TABLE_NAME = 'comments' AND COLUMN_NAME = 'weekly_report_id'",
                    Integer.class
                );
                if (count == 0) {
                    jdbcTemplate.execute("ALTER TABLE comments ADD COLUMN weekly_report_id BIGINT");
                    System.out.println("Added weekly_report_id column to comments table");
                } else {
                    System.out.println("weekly_report_id column already exists");
                }
            } catch (Exception e) {
                System.out.println("Failed to check/add weekly_report_id column: " + e.getMessage());
            }

            // Add foreign key constraint if it doesn't exist
            try {
                Integer fkCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.KEY_COLUMN_USAGE WHERE " +
                    "TABLE_SCHEMA = 'weekly_report_system' AND TABLE_NAME = 'comments' AND CONSTRAINT_NAME = 'fk_comments_weekly_report'",
                    Integer.class
                );
                if (fkCount == 0) {
                    jdbcTemplate.execute(
                        "ALTER TABLE comments ADD CONSTRAINT fk_comments_weekly_report " +
                        "FOREIGN KEY (weekly_report_id) REFERENCES weekly_reports(id)"
                    );
                    System.out.println("Added foreign key constraint for weekly_report_id");
                } else {
                    System.out.println("Foreign key constraint already exists");
                }
            } catch (Exception e) {
                System.out.println("Foreign key constraint already exists or failed to add: " + e.getMessage());
            }

        } catch (Exception e) {
            System.err.println("Simple comments table fix failed: " + e.getMessage());
        }

        System.out.println("=== SIMPLE COMMENTS TABLE FIX COMPLETE ===");
    }
}
