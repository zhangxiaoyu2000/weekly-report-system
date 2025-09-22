-- Remove task_templates table and related references
-- This table was created as an over-engineered feature and is not needed for the business requirements

-- Remove foreign key constraint from tasks table if it exists
-- Using a safe approach that won't fail if constraint doesn't exist
SET @constraint_exists = (SELECT COUNT(*) 
                         FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
                         WHERE TABLE_NAME = 'tasks' 
                         AND CONSTRAINT_NAME = 'fk_tasks_task_template' 
                         AND TABLE_SCHEMA = DATABASE());

SET @sql = IF(@constraint_exists > 0, 
              'ALTER TABLE tasks DROP FOREIGN KEY fk_tasks_task_template', 
              'SELECT "Foreign key constraint does not exist" as message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Remove task_template_id column from tasks table if it exists
SET @column_exists = (SELECT COUNT(*) 
                     FROM INFORMATION_SCHEMA.COLUMNS 
                     WHERE TABLE_NAME = 'tasks' 
                     AND COLUMN_NAME = 'task_template_id' 
                     AND TABLE_SCHEMA = DATABASE());

SET @sql = IF(@column_exists > 0, 
              'ALTER TABLE tasks DROP COLUMN task_template_id', 
              'SELECT "Column task_template_id does not exist" as message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Drop task_templates table if it exists
DROP TABLE IF EXISTS task_templates;