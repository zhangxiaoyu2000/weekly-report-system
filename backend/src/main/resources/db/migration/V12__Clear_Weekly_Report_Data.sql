-- Clear all weekly report related data to resolve "Weekly report already exists" issue

-- Disable foreign key checks temporarily
SET FOREIGN_KEY_CHECKS = 0;

-- Clear tasks first (due to foreign key constraints)
DELETE FROM tasks WHERE id > 0;

-- Clear weekly reports
DELETE FROM weekly_reports WHERE id > 0;

-- Clear simple weekly reports if exists
DELETE FROM simple_weekly_reports WHERE id > 0;

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;