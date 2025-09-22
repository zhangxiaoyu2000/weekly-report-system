-- Fix users table schema to match simplified entity design
-- Add missing created_at and updated_at columns

USE qr_auth_dev;

-- Show current table structure
DESCRIBE users;

-- Add created_at column (ignore error if already exists)
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = 'qr_auth_dev' 
     AND TABLE_NAME = 'users' 
     AND COLUMN_NAME = 'created_at') = 0,
    'ALTER TABLE users ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP',
    'SELECT "created_at column already exists" as result'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add updated_at column (ignore error if already exists)
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = 'qr_auth_dev' 
     AND TABLE_NAME = 'users' 
     AND COLUMN_NAME = 'updated_at') = 0,
    'ALTER TABLE users ADD COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP',
    'SELECT "updated_at column already exists" as result'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Show the final table structure
DESCRIBE users;