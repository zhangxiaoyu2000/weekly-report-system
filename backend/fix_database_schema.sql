-- Fix missing columns in weekly_reports table (ignore errors if columns exist)
SET @sql = 'ALTER TABLE weekly_reports ADD COLUMN manager_reviewer_id BIGINT';
SET @sql = CONCAT(@sql, ', ADD COLUMN manager_review_comment TEXT');
SET @sql = CONCAT(@sql, ', ADD COLUMN manager_reviewed_at TIMESTAMP NULL');
SET @sql = CONCAT(@sql, ', ADD COLUMN admin_reviewer_id BIGINT');
SET @sql = CONCAT(@sql, ', ADD COLUMN admin_review_comment TEXT');
SET @sql = CONCAT(@sql, ', ADD COLUMN admin_reviewed_at TIMESTAMP NULL');
SET @sql = CONCAT(@sql, ', ADD COLUMN super_admin_reviewer_id BIGINT');
SET @sql = CONCAT(@sql, ', ADD COLUMN super_admin_review_comment TEXT');
SET @sql = CONCAT(@sql, ', ADD COLUMN super_admin_reviewed_at TIMESTAMP NULL');

-- Check if columns exist before adding
SELECT COUNT(*) INTO @col_exists FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'qr_auth_dev' AND TABLE_NAME = 'weekly_reports' AND COLUMN_NAME = 'manager_reviewer_id';

-- Execute only if columns don't exist
SET @sql = IF(@col_exists > 0, 'SELECT "Columns already exist"', @sql);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add missing columns to users table (ignore errors if columns exist)
SELECT COUNT(*) INTO @user_col_exists FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'qr_auth_dev' AND TABLE_NAME = 'users' AND COLUMN_NAME = 'employee_id';

SET @user_sql = 'ALTER TABLE users ADD COLUMN employee_id VARCHAR(50)';
SET @user_sql = CONCAT(@user_sql, ', ADD COLUMN position VARCHAR(100)');
SET @user_sql = CONCAT(@user_sql, ', ADD COLUMN phone VARCHAR(20)');
SET @user_sql = CONCAT(@user_sql, ', ADD COLUMN avatar_url VARCHAR(500)');
SET @user_sql = CONCAT(@user_sql, ', ADD COLUMN full_name VARCHAR(101)');
SET @user_sql = CONCAT(@user_sql, ', ADD COLUMN last_login_time TIMESTAMP NULL');

SET @user_sql = IF(@user_col_exists > 0, 'SELECT "User columns already exist"', @user_sql);
PREPARE user_stmt FROM @user_sql;
EXECUTE user_stmt;
DEALLOCATE PREPARE user_stmt;

-- Update existing users to populate full_name
UPDATE users 
SET full_name = CONCAT(COALESCE(first_name, ''), ' ', COALESCE(last_name, ''))
WHERE full_name IS NULL;

-- Update last_login_time from last_login
UPDATE users 
SET last_login_time = last_login
WHERE last_login_time IS NULL AND last_login IS NOT NULL;