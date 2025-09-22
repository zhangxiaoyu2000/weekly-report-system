-- 修复projects表缺失字段问题
-- 这个脚本确保projects表有所有必要的字段

USE qr_auth_dev;

-- 检查并添加stop_loss字段
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
              WHERE TABLE_NAME = 'projects' 
              AND COLUMN_NAME = 'stop_loss' 
              AND TABLE_SCHEMA = DATABASE()) = 0, 
              'ALTER TABLE projects ADD COLUMN stop_loss TEXT COMMENT "止损点"', 
              'SELECT "stop_loss column already exists"');
PREPARE stmt FROM @sql; 
EXECUTE stmt; 
DEALLOCATE PREPARE stmt;

-- 检查并添加timeline字段
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
              WHERE TABLE_NAME = 'projects' 
              AND COLUMN_NAME = 'timeline' 
              AND TABLE_SCHEMA = DATABASE()) = 0, 
              'ALTER TABLE projects ADD COLUMN timeline TEXT COMMENT "时间线"', 
              'SELECT "timeline column already exists"');
PREPARE stmt FROM @sql; 
EXECUTE stmt; 
DEALLOCATE PREPARE stmt;

-- 检查并添加expected_results字段
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
              WHERE TABLE_NAME = 'projects' 
              AND COLUMN_NAME = 'expected_results' 
              AND TABLE_SCHEMA = DATABASE()) = 0, 
              'ALTER TABLE projects ADD COLUMN expected_results TEXT COMMENT "预期结果"', 
              'SELECT "expected_results column already exists"');
PREPARE stmt FROM @sql; 
EXECUTE stmt; 
DEALLOCATE PREPARE stmt;

-- 检查并添加ai_analysis_id字段
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
              WHERE TABLE_NAME = 'projects' 
              AND COLUMN_NAME = 'ai_analysis_id' 
              AND TABLE_SCHEMA = DATABASE()) = 0, 
              'ALTER TABLE projects ADD COLUMN ai_analysis_id BIGINT COMMENT "AI分析结果ID"', 
              'SELECT "ai_analysis_id column already exists"');
PREPARE stmt FROM @sql; 
EXECUTE stmt; 
DEALLOCATE PREPARE stmt;

-- 检查并添加admin_reviewer_id字段
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
              WHERE TABLE_NAME = 'projects' 
              AND COLUMN_NAME = 'admin_reviewer_id' 
              AND TABLE_SCHEMA = DATABASE()) = 0, 
              'ALTER TABLE projects ADD COLUMN admin_reviewer_id BIGINT COMMENT "管理员审批人ID"', 
              'SELECT "admin_reviewer_id column already exists"');
PREPARE stmt FROM @sql; 
EXECUTE stmt; 
DEALLOCATE PREPARE stmt;

-- 检查并添加super_admin_reviewer_id字段
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
              WHERE TABLE_NAME = 'projects' 
              AND COLUMN_NAME = 'super_admin_reviewer_id' 
              AND TABLE_SCHEMA = DATABASE()) = 0, 
              'ALTER TABLE projects ADD COLUMN super_admin_reviewer_id BIGINT COMMENT "超级管理员审批人ID"', 
              'SELECT "super_admin_reviewer_id column already exists"');
PREPARE stmt FROM @sql; 
EXECUTE stmt; 
DEALLOCATE PREPARE stmt;

-- 检查并添加rejection_reason字段
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
              WHERE TABLE_NAME = 'projects' 
              AND COLUMN_NAME = 'rejection_reason' 
              AND TABLE_SCHEMA = DATABASE()) = 0, 
              'ALTER TABLE projects ADD COLUMN rejection_reason TEXT COMMENT "拒绝理由"', 
              'SELECT "rejection_reason column already exists"');
PREPARE stmt FROM @sql; 
EXECUTE stmt; 
DEALLOCATE PREPARE stmt;

-- 检查并添加approval_status字段
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
              WHERE TABLE_NAME = 'projects' 
              AND COLUMN_NAME = 'approval_status' 
              AND TABLE_SCHEMA = DATABASE()) = 0, 
              'ALTER TABLE projects ADD COLUMN approval_status VARCHAR(30) NOT NULL DEFAULT "DRAFT" COMMENT "审批状态"', 
              'SELECT "approval_status column already exists"');
PREPARE stmt FROM @sql; 
EXECUTE stmt; 
DEALLOCATE PREPARE stmt;

-- 显示当前projects表结构
DESCRIBE projects;