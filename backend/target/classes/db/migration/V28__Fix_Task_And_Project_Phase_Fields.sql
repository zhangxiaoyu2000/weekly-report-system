-- V28__Fix_Task_And_Project_Phase_Fields.sql
-- 修复任务表和项目阶段表的字段结构问题
-- 1. 从tasks表移除实际结果和结果差异分析字段
-- 2. 从project_phases表移除实际结果和结果差异分析字段  
-- 3. 确保task_reports表有实际结果和结果差异分析字段
-- 4. 修正dev_task_reports表，将task_id改为phases_id，并添加实际结果和结果差异分析字段

-- ========================================
-- 第一步：修复tasks表结构
-- ========================================

-- 移除tasks表中不合理的字段：实际结果和结果差异分析
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'tasks' AND COLUMN_NAME = 'actual_results' AND TABLE_SCHEMA = DATABASE()) > 0, 
    'ALTER TABLE tasks DROP COLUMN actual_results', 
    'SELECT "actual_results column does not exist in tasks table"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'tasks' AND COLUMN_NAME = 'result_difference_analysis' AND TABLE_SCHEMA = DATABASE()) > 0, 
    'ALTER TABLE tasks DROP COLUMN result_difference_analysis', 
    'SELECT "result_difference_analysis column does not exist in tasks table"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 移除task_type字段（如果存在），因为任务表不应该有任务类型字段
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'tasks' AND COLUMN_NAME = 'task_type' AND TABLE_SCHEMA = DATABASE()) > 0, 
    'ALTER TABLE tasks DROP COLUMN task_type', 
    'SELECT "task_type column does not exist in tasks table"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ========================================
-- 第二步：修复project_phases表结构
-- ========================================

-- 移除project_phases表中不合理的字段：实际结果和结果差异分析
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'project_phases' AND COLUMN_NAME = 'actual_results' AND TABLE_SCHEMA = DATABASE()) > 0, 
    'ALTER TABLE project_phases DROP COLUMN actual_results', 
    'SELECT "actual_results column does not exist in project_phases table"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'project_phases' AND COLUMN_NAME = 'result_difference_analysis' AND TABLE_SCHEMA = DATABASE()) > 0, 
    'ALTER TABLE project_phases DROP COLUMN result_difference_analysis', 
    'SELECT "result_difference_analysis column does not exist in project_phases table"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ========================================
-- 第三步：修复task_reports表结构
-- ========================================

-- 确保task_reports表有实际结果和结果差异分析字段
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'task_reports' AND COLUMN_NAME = 'actual_results' AND TABLE_SCHEMA = DATABASE()) = 0, 
    'ALTER TABLE task_reports ADD COLUMN actual_results TEXT COMMENT "实际结果"', 
    'SELECT "actual_results column already exists in task_reports table"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'task_reports' AND COLUMN_NAME = 'result_difference_analysis' AND TABLE_SCHEMA = DATABASE()) = 0, 
    'ALTER TABLE task_reports ADD COLUMN result_difference_analysis TEXT COMMENT "结果差异分析"', 
    'SELECT "result_difference_analysis column already exists in task_reports table"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ========================================
-- 第四步：重建dev_task_reports表结构
-- ========================================

-- 删除现有的dev_task_reports表并重建
DROP TABLE IF EXISTS dev_task_reports;

-- 重新创建dev_task_reports表，task_id改名为phases_id，并添加实际结果和结果差异分析字段
CREATE TABLE dev_task_reports (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    weekly_report_id BIGINT NOT NULL COMMENT '周报ID',
    project_id BIGINT NOT NULL COMMENT '项目ID',
    phases_id BIGINT NOT NULL COMMENT '项目阶段ID（关联project_phases表）',
    actual_results TEXT COMMENT '实际结果',
    result_difference_analysis TEXT COMMENT '结果差异分析',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX idx_dev_task_reports_weekly_report (weekly_report_id),
    INDEX idx_dev_task_reports_project (project_id),
    INDEX idx_dev_task_reports_phases (phases_id),
    
    CONSTRAINT fk_dev_task_reports_weekly_report 
        FOREIGN KEY (weekly_report_id) REFERENCES weekly_reports(id) ON DELETE CASCADE,
    
    CONSTRAINT fk_dev_task_reports_project 
        FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    
    CONSTRAINT fk_dev_task_reports_phases 
        FOREIGN KEY (phases_id) REFERENCES project_phases(id) ON DELETE CASCADE,
        
    -- 确保同一周报、项目、阶段的组合唯一
    UNIQUE KEY uk_dev_task_reports_unique (weekly_report_id, project_id, phases_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='发展性任务报告表：存储周报中发展性任务的实际结果和差异分析';

-- ========================================
-- 第五步：验证表结构
-- ========================================

-- 检查tasks表结构（不应该有actual_results和result_difference_analysis字段）
SELECT 
    'tasks_table_check' as check_type,
    CASE 
        WHEN COUNT(*) = 0 THEN 'SUCCESS: tasks表字段结构正确'
        ELSE CONCAT('ERROR: tasks表仍有', COUNT(*), '个不应存在的字段')
    END as result
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'tasks' 
    AND TABLE_SCHEMA = DATABASE()
    AND COLUMN_NAME IN ('actual_results', 'result_difference_analysis', 'task_type');

-- 检查project_phases表结构（不应该有actual_results和result_difference_analysis字段）
SELECT 
    'project_phases_table_check' as check_type,
    CASE 
        WHEN COUNT(*) = 0 THEN 'SUCCESS: project_phases表字段结构正确'
        ELSE CONCAT('ERROR: project_phases表仍有', COUNT(*), '个不应存在的字段')
    END as result
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'project_phases' 
    AND TABLE_SCHEMA = DATABASE()
    AND COLUMN_NAME IN ('actual_results', 'result_difference_analysis');

-- 检查task_reports表结构（应该有actual_results和result_difference_analysis字段）
SELECT 
    'task_reports_table_check' as check_type,
    CASE 
        WHEN COUNT(*) = 2 THEN 'SUCCESS: task_reports表字段结构正确'
        ELSE CONCAT('ERROR: task_reports表缺少必要字段，当前只有', COUNT(*), '个字段')
    END as result
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'task_reports' 
    AND TABLE_SCHEMA = DATABASE()
    AND COLUMN_NAME IN ('actual_results', 'result_difference_analysis');

-- 检查dev_task_reports表结构（应该有phases_id、actual_results和result_difference_analysis字段）
SELECT 
    'dev_task_reports_table_check' as check_type,
    CASE 
        WHEN COUNT(*) = 3 THEN 'SUCCESS: dev_task_reports表字段结构正确'
        ELSE CONCAT('ERROR: dev_task_reports表字段不完整，当前只有', COUNT(*), '个必要字段')
    END as result
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'dev_task_reports' 
    AND TABLE_SCHEMA = DATABASE()
    AND COLUMN_NAME IN ('phases_id', 'actual_results', 'result_difference_analysis');

-- 显示所有表的字段数量概览
SELECT 
    table_name,
    COUNT(*) as field_count
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
    AND table_name IN ('tasks', 'project_phases', 'task_reports', 'dev_task_reports')
GROUP BY table_name
ORDER BY table_name;

COMMIT;