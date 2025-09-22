-- Force delete existing weekly reports to avoid conflicts

-- Disable foreign key checks temporarily
SET FOREIGN_KEY_CHECKS = 0;

-- Delete specific weekly reports and their tasks
DELETE FROM tasks WHERE weekly_report_id IN (1, 2);
DELETE FROM weekly_reports WHERE id IN (1, 2);

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;