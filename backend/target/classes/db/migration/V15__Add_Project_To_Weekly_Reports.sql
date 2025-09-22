-- V15: 为weekly_reports表添加project_id字段支持项目关联
-- 此迁移为统一双重架构做准备

-- 添加project_id字段到weekly_reports表
ALTER TABLE weekly_reports 
ADD COLUMN project_id BIGINT NULL,
ADD INDEX idx_weekly_report_project (project_id);

-- 添加外键约束（如果projects表存在）
-- 注意：这里使用IF EXISTS检查，因为可能存在不同的项目表
SET @table_exists = (SELECT COUNT(*) FROM information_schema.tables 
    WHERE table_schema = DATABASE() AND table_name = 'projects');

SET @sql = IF(@table_exists > 0, 
    'ALTER TABLE weekly_reports ADD CONSTRAINT fk_weekly_report_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE SET NULL',
    'SELECT "projects table not found, skipping foreign key constraint"');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 如果simple_projects表存在，也添加外键约束选项
SET @simple_table_exists = (SELECT COUNT(*) FROM information_schema.tables 
    WHERE table_schema = DATABASE() AND table_name = 'simple_projects');

-- 注释：由于可能存在多个项目表，实际的外键约束需要根据具体的项目表结构来决定
-- 这里只是为project_id字段预留了空间，具体的约束在数据迁移后再添加