package com.weeklyreport.util;

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
            // Add attachments column to comments table if it doesn't exist
            jdbcTemplate.execute("ALTER TABLE comments ADD COLUMN IF NOT EXISTS attachments JSON");
            System.out.println("Added attachments column to comments table (if not exists)");
            
            // Add weekly_report_id column to comments table if it doesn't exist
            jdbcTemplate.execute("ALTER TABLE comments ADD COLUMN IF NOT EXISTS weekly_report_id BIGINT");
            System.out.println("Added weekly_report_id column to comments table (if not exists)");
            
            // Add foreign key constraint if it doesn't exist
            try {
                jdbcTemplate.execute(
                    "ALTER TABLE comments ADD CONSTRAINT fk_comments_weekly_report " +
                    "FOREIGN KEY (weekly_report_id) REFERENCES weekly_reports(id)"
                );
                System.out.println("Added foreign key constraint for weekly_report_id");
            } catch (Exception e) {
                System.out.println("Foreign key constraint already exists or failed to add: " + e.getMessage());
            }
            
        } catch (Exception e) {
            System.err.println("Simple comments table fix failed: " + e.getMessage());
        }
        
        System.out.println("=== SIMPLE COMMENTS TABLE FIX COMPLETE ===");
    }
}