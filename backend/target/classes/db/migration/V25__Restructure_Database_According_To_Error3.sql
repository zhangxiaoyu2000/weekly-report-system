-- V25__Restructure_Database_According_To_Error3.sql
-- 根据error3.md的最新要求重构数据库结构
-- 目标：简化设计，使用关联表模式，移除复杂的审批字段

-- ========================================
-- 第一步：备份重要数据
-- ========================================

-- 备份当前的周报数据
CREATE TEMPORARY TABLE temp_weekly_reports_backup AS 
SELECT * FROM weekly_reports WHERE id IS NOT NULL;

-- 备份tasks表数据
CREATE TEMPORARY TABLE temp_tasks_backup AS 
SELECT * FROM tasks WHERE id IS NOT NULL;

-- ========================================
-- 第二步：修改tasks表结构
-- ========================================

-- 首先删除所有相关的外键约束
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE WHERE TABLE_NAME = 'tasks' AND CONSTRAINT_NAME = 'tasks_ibfk_1' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE tasks DROP FOREIGN KEY tasks_ibfk_1', 'SELECT "tasks_ibfk_1 constraint does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE WHERE TABLE_NAME = 'tasks' AND CONSTRAINT_NAME = 'tasks_ibfk_2' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE tasks DROP FOREIGN KEY tasks_ibfk_2', 'SELECT "tasks_ibfk_2 constraint does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE WHERE TABLE_NAME = 'tasks' AND CONSTRAINT_NAME = 'tasks_ibfk_3' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE tasks DROP FOREIGN KEY tasks_ibfk_3', 'SELECT "tasks_ibfk_3 constraint does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE WHERE TABLE_NAME = 'tasks' AND CONSTRAINT_NAME = 'tasks_ibfk_4' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE tasks DROP FOREIGN KEY tasks_ibfk_4', 'SELECT "tasks_ibfk_4 constraint does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 移除不需要的project关联字段（因为发展性任务已分离到project_phases）
-- 使用兼容的语法
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'tasks' AND COLUMN_NAME = 'project_id' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE tasks DROP COLUMN project_id', 'SELECT "project_id column does not exist"');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'tasks' AND COLUMN_NAME = 'simple_project_id' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE tasks DROP COLUMN simple_project_id', 'SELECT "simple_project_id column does not exist"');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'tasks' AND COLUMN_NAME = 'project_phase_id' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE tasks DROP COLUMN project_phase_id', 'SELECT "project_phase_id column does not exist"');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'tasks' AND COLUMN_NAME = 'weekly_report_id' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE tasks DROP COLUMN weekly_report_id', 'SELECT "weekly_report_id column does not exist"');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'tasks' AND COLUMN_NAME = 'report_section' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE tasks DROP COLUMN report_section', 'SELECT "report_section column does not exist"');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 确保tasks表包含error3.md要求的字段
ALTER TABLE tasks 
MODIFY COLUMN task_name VARCHAR(200) NOT NULL COMMENT '任务名称';

-- 添加字段（使用兼容语法）
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'tasks' AND COLUMN_NAME = 'personnel_assignment' AND TABLE_SCHEMA = DATABASE()) = 0, 'ALTER TABLE tasks ADD COLUMN personnel_assignment VARCHAR(200) COMMENT "人员分配"', 'SELECT "personnel_assignment column already exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'tasks' AND COLUMN_NAME = 'timeline' AND TABLE_SCHEMA = DATABASE()) = 0, 'ALTER TABLE tasks ADD COLUMN timeline VARCHAR(300) COMMENT "时间线"', 'SELECT "timeline column already exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'tasks' AND COLUMN_NAME = 'quantitative_metrics' AND TABLE_SCHEMA = DATABASE()) = 0, 'ALTER TABLE tasks ADD COLUMN quantitative_metrics VARCHAR(500) COMMENT "量化指标"', 'SELECT "quantitative_metrics column already exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'tasks' AND COLUMN_NAME = 'expected_results' AND TABLE_SCHEMA = DATABASE()) = 0, 'ALTER TABLE tasks ADD COLUMN expected_results TEXT COMMENT "预期结果"', 'SELECT "expected_results column already exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'tasks' AND COLUMN_NAME = 'actual_results' AND TABLE_SCHEMA = DATABASE()) = 0, 'ALTER TABLE tasks ADD COLUMN actual_results TEXT COMMENT "实际结果"', 'SELECT "actual_results column already exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'tasks' AND COLUMN_NAME = 'result_difference_analysis' AND TABLE_SCHEMA = DATABASE()) = 0, 'ALTER TABLE tasks ADD COLUMN result_difference_analysis TEXT COMMENT "结果差异分析"', 'SELECT "result_difference_analysis column already exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'tasks' AND COLUMN_NAME = 'created_by' AND TABLE_SCHEMA = DATABASE()) = 0, 'ALTER TABLE tasks ADD COLUMN created_by BIGINT COMMENT "创建者ID"', 'SELECT "created_by column already exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 首先更新DEVELOPMENT类型的任务为DAILY类型（因为发展性任务已移到project_phases）
UPDATE tasks SET task_type = 'DAILY' WHERE task_type = 'DEVELOPMENT';

-- 修改task_type字段
ALTER TABLE tasks 
MODIFY COLUMN task_type ENUM('DAILY', 'WEEKLY', 'MONTHLY') NOT NULL DEFAULT 'DAILY' COMMENT '任务类型：DAILY=每日，WEEKLY=每周，MONTHLY=每月';

-- 添加外键约束（如果不存在）
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE WHERE TABLE_NAME = 'tasks' AND CONSTRAINT_NAME = 'fk_tasks_created_by' AND TABLE_SCHEMA = DATABASE()) = 0, 'ALTER TABLE tasks ADD CONSTRAINT fk_tasks_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL', 'SELECT "fk_tasks_created_by constraint already exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ========================================
-- 第三步：修改project_phases表结构  
-- ========================================

-- 确保project_phases表包含error3.md要求的字段
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'project_phases' AND COLUMN_NAME = 'phase_name' AND TABLE_SCHEMA = DATABASE()) = 0, 'ALTER TABLE project_phases ADD COLUMN phase_name VARCHAR(200) COMMENT "任务名称"', 'SELECT "phase_name column already exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'project_phases' AND COLUMN_NAME = 'description' AND TABLE_SCHEMA = DATABASE()) = 0, 'ALTER TABLE project_phases ADD COLUMN description TEXT COMMENT "阶段描述"', 'SELECT "description column already exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'project_phases' AND COLUMN_NAME = 'assigned_members' AND TABLE_SCHEMA = DATABASE()) = 0, 'ALTER TABLE project_phases ADD COLUMN assigned_members VARCHAR(500) COMMENT "负责成员"', 'SELECT "assigned_members column already exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'project_phases' AND COLUMN_NAME = 'schedule' AND TABLE_SCHEMA = DATABASE()) = 0, 'ALTER TABLE project_phases ADD COLUMN schedule VARCHAR(300) COMMENT "时间安排"', 'SELECT "schedule column already exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'project_phases' AND COLUMN_NAME = 'expected_results' AND TABLE_SCHEMA = DATABASE()) = 0, 'ALTER TABLE project_phases ADD COLUMN expected_results TEXT COMMENT "预期结果"', 'SELECT "expected_results column already exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'project_phases' AND COLUMN_NAME = 'actual_results' AND TABLE_SCHEMA = DATABASE()) = 0, 'ALTER TABLE project_phases ADD COLUMN actual_results TEXT COMMENT "实际结果"', 'SELECT "actual_results column already exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'project_phases' AND COLUMN_NAME = 'result_difference_analysis' AND TABLE_SCHEMA = DATABASE()) = 0, 'ALTER TABLE project_phases ADD COLUMN result_difference_analysis TEXT COMMENT "结果差异分析"', 'SELECT "result_difference_analysis column already exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 确保有项目关联
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'project_phases' AND COLUMN_NAME = 'project_id' AND TABLE_SCHEMA = DATABASE()) = 0, 'ALTER TABLE project_phases ADD COLUMN project_id BIGINT NOT NULL COMMENT "关联项目ID"', 'SELECT "project_id column already exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 添加外键约束
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE WHERE TABLE_NAME = 'project_phases' AND CONSTRAINT_NAME = 'fk_project_phases_project' AND TABLE_SCHEMA = DATABASE()) = 0, 'ALTER TABLE project_phases ADD CONSTRAINT fk_project_phases_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE', 'SELECT "fk_project_phases_project constraint already exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ========================================
-- 第四步：简化weekly_reports表结构
-- ========================================

-- 移除复杂的审批和AI分析字段，保留核心字段
-- 使用兼容语法逐个删除字段
SET @columns_to_drop = 'content,next_week_plan,summary,status,week_start,week_end,submitted_at,reviewed_at,reviewed_by,review_comments,priority,tags,attachments,view_count,manager_reviewer_id,manager_review_comment,manager_reviewed_at,admin_reviewer_id,admin_review_comment,admin_reviewed_at,super_admin_reviewer_id,super_admin_review_comment,super_admin_reviewed_at,ai_confidence,ai_quality_score,ai_risk_level,ai_provider_used,ai_processing_time_ms,ai_analyzed_at,ai_key_issues,ai_recommendations,ai_analysis_id,project_id,template_id,deleted_at';

-- 删除content字段
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'weekly_reports' AND COLUMN_NAME = 'content' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE weekly_reports DROP COLUMN content', 'SELECT "content column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 删除一些主要的复杂字段
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'weekly_reports' AND COLUMN_NAME = 'next_week_plan' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE weekly_reports DROP COLUMN next_week_plan', 'SELECT "next_week_plan column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'weekly_reports' AND COLUMN_NAME = 'summary' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE weekly_reports DROP COLUMN summary', 'SELECT "summary column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'weekly_reports' AND COLUMN_NAME = 'status' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE weekly_reports DROP COLUMN status', 'SELECT "status column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'weekly_reports' AND COLUMN_NAME = 'project_id' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE weekly_reports DROP COLUMN project_id', 'SELECT "project_id column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'weekly_reports' AND COLUMN_NAME = 'template_id' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE weekly_reports DROP COLUMN template_id', 'SELECT "template_id column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 重新定义weekly_reports表的核心字段
ALTER TABLE weekly_reports 
MODIFY COLUMN title VARCHAR(200) NOT NULL COMMENT '周报标题';

-- 添加user_id字段
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'weekly_reports' AND COLUMN_NAME = 'user_id' AND TABLE_SCHEMA = DATABASE()) = 0, 'ALTER TABLE weekly_reports ADD COLUMN user_id BIGINT NOT NULL COMMENT "提交周报的用户ID"', 'SELECT "user_id column already exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 修改existing字段
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'weekly_reports' AND COLUMN_NAME = 'additional_notes' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE weekly_reports MODIFY COLUMN additional_notes TEXT COMMENT "其他备注"', 'SELECT "additional_notes column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'weekly_reports' AND COLUMN_NAME = 'development_opportunities' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE weekly_reports MODIFY COLUMN development_opportunities TEXT COMMENT "可发展性清单"', 'SELECT "development_opportunities column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 更新author字段为user_id（如果存在author字段）
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'weekly_reports' AND COLUMN_NAME = 'author_id' AND TABLE_SCHEMA = DATABASE()) > 0, 'UPDATE weekly_reports wr INNER JOIN users u ON wr.author_id = u.id SET wr.user_id = u.id WHERE wr.author_id IS NOT NULL', 'SELECT "author_id column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 删除旧的author相关字段
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'weekly_reports' AND COLUMN_NAME = 'author_id' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE weekly_reports DROP COLUMN author_id', 'SELECT "author_id column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 添加外键约束
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE WHERE TABLE_NAME = 'weekly_reports' AND CONSTRAINT_NAME = 'fk_weekly_reports_user' AND TABLE_SCHEMA = DATABASE()) = 0, 'ALTER TABLE weekly_reports ADD CONSTRAINT fk_weekly_reports_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE', 'SELECT "fk_weekly_reports_user constraint already exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ========================================
-- 第五步：创建task_reports关联表
-- ========================================

-- 创建日常性任务与周报的关联表
CREATE TABLE IF NOT EXISTS task_reports (
    weekly_report_id BIGINT NOT NULL COMMENT '周报ID',
    task_id BIGINT NOT NULL COMMENT '任务ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    PRIMARY KEY (weekly_report_id, task_id),
    
    CONSTRAINT fk_task_reports_weekly_report 
    FOREIGN KEY (weekly_report_id) REFERENCES weekly_reports(id) ON DELETE CASCADE,
    
    CONSTRAINT fk_task_reports_task 
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE
) COMMENT = '日常性任务与周报关联表';

-- 创建索引优化查询（使用兼容语法）
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_NAME = 'task_reports' AND INDEX_NAME = 'idx_task_reports_weekly_report' AND TABLE_SCHEMA = DATABASE()) = 0, 'CREATE INDEX idx_task_reports_weekly_report ON task_reports(weekly_report_id)', 'SELECT "idx_task_reports_weekly_report index already exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_NAME = 'task_reports' AND INDEX_NAME = 'idx_task_reports_task' AND TABLE_SCHEMA = DATABASE()) = 0, 'CREATE INDEX idx_task_reports_task ON task_reports(task_id)', 'SELECT "idx_task_reports_task index already exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ========================================
-- 第六步：创建dev_task_reports关联表
-- ========================================

-- 创建发展性项目阶段任务与周报关联表
CREATE TABLE IF NOT EXISTS dev_task_reports (
    weekly_report_id BIGINT NOT NULL COMMENT '周报ID',
    project_id BIGINT NOT NULL COMMENT '项目ID', 
    task_id BIGINT NOT NULL COMMENT '阶段任务ID（对应project_phases表）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    PRIMARY KEY (weekly_report_id, project_id, task_id),
    
    CONSTRAINT fk_dev_task_reports_weekly_report 
    FOREIGN KEY (weekly_report_id) REFERENCES weekly_reports(id) ON DELETE CASCADE,
    
    CONSTRAINT fk_dev_task_reports_project 
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    
    CONSTRAINT fk_dev_task_reports_task 
    FOREIGN KEY (task_id) REFERENCES project_phases(id) ON DELETE CASCADE
) COMMENT = '发展性项目阶段任务与周报关联表';

-- 创建索引优化查询（使用兼容语法）
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_NAME = 'dev_task_reports' AND INDEX_NAME = 'idx_dev_task_reports_weekly_report' AND TABLE_SCHEMA = DATABASE()) = 0, 'CREATE INDEX idx_dev_task_reports_weekly_report ON dev_task_reports(weekly_report_id)', 'SELECT "idx_dev_task_reports_weekly_report index already exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_NAME = 'dev_task_reports' AND INDEX_NAME = 'idx_dev_task_reports_project' AND TABLE_SCHEMA = DATABASE()) = 0, 'CREATE INDEX idx_dev_task_reports_project ON dev_task_reports(project_id)', 'SELECT "idx_dev_task_reports_project index already exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_NAME = 'dev_task_reports' AND INDEX_NAME = 'idx_dev_task_reports_task' AND TABLE_SCHEMA = DATABASE()) = 0, 'CREATE INDEX idx_dev_task_reports_task ON dev_task_reports(task_id)', 'SELECT "idx_dev_task_reports_task index already exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ========================================
-- 第七步：简化projects表结构（可选）
-- ========================================

-- 首先删除projects表的外键约束
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE WHERE TABLE_NAME = 'projects' AND CONSTRAINT_NAME = 'fk_projects_ai_analysis' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE projects DROP FOREIGN KEY fk_projects_ai_analysis', 'SELECT "fk_projects_ai_analysis constraint does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE WHERE TABLE_NAME = 'projects' AND CONSTRAINT_NAME = 'fk_simple_projects_admin_reviewer' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE projects DROP FOREIGN KEY fk_simple_projects_admin_reviewer', 'SELECT "fk_simple_projects_admin_reviewer constraint does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE WHERE TABLE_NAME = 'projects' AND CONSTRAINT_NAME = 'fk_simple_projects_manager_reviewer' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE projects DROP FOREIGN KEY fk_simple_projects_manager_reviewer', 'SELECT "fk_simple_projects_manager_reviewer constraint does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE WHERE TABLE_NAME = 'projects' AND CONSTRAINT_NAME = 'fk_simple_projects_super_admin_reviewer' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE projects DROP FOREIGN KEY fk_simple_projects_super_admin_reviewer', 'SELECT "fk_simple_projects_super_admin_reviewer constraint does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 根据error3.md，projects表只需要基础字段，移除复杂的审批字段
-- 使用兼容语法逐个删除字段
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'projects' AND COLUMN_NAME = 'ai_analysis_id' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE projects DROP COLUMN ai_analysis_id', 'SELECT "ai_analysis_id column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'projects' AND COLUMN_NAME = 'admin_reviewer_id' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE projects DROP COLUMN admin_reviewer_id', 'SELECT "admin_reviewer_id column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'projects' AND COLUMN_NAME = 'super_admin_reviewer_id' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE projects DROP COLUMN super_admin_reviewer_id', 'SELECT "super_admin_reviewer_id column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'projects' AND COLUMN_NAME = 'rejection_reason' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE projects DROP COLUMN rejection_reason', 'SELECT "rejection_reason column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'projects' AND COLUMN_NAME = 'approval_status' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE projects DROP COLUMN approval_status', 'SELECT "approval_status column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 确保projects表包含必要字段
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'projects' AND COLUMN_NAME = 'name' AND TABLE_SCHEMA = DATABASE()) = 0, 'ALTER TABLE projects ADD COLUMN name VARCHAR(200) NOT NULL COMMENT "项目名称"', 'SELECT "name column already exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'projects' AND COLUMN_NAME = 'description' AND TABLE_SCHEMA = DATABASE()) = 0, 'ALTER TABLE projects ADD COLUMN description TEXT COMMENT "项目内容"', 'SELECT "description column already exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'projects' AND COLUMN_NAME = 'members' AND TABLE_SCHEMA = DATABASE()) = 0, 'ALTER TABLE projects ADD COLUMN members TEXT COMMENT "项目成员"', 'SELECT "members column already exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'projects' AND COLUMN_NAME = 'expected_results' AND TABLE_SCHEMA = DATABASE()) = 0, 'ALTER TABLE projects ADD COLUMN expected_results TEXT COMMENT "预期结果"', 'SELECT "expected_results column already exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'projects' AND COLUMN_NAME = 'timeline' AND TABLE_SCHEMA = DATABASE()) = 0, 'ALTER TABLE projects ADD COLUMN timeline VARCHAR(500) COMMENT "时间线"', 'SELECT "timeline column already exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'projects' AND COLUMN_NAME = 'stop_loss' AND TABLE_SCHEMA = DATABASE()) = 0, 'ALTER TABLE projects ADD COLUMN stop_loss TEXT COMMENT "止损点"', 'SELECT "stop_loss column already exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ========================================
-- 第八步：清理不需要的表和数据
-- ========================================

-- 清空关联表数据（准备重新建立关联）
TRUNCATE TABLE task_reports;
TRUNCATE TABLE dev_task_reports;

-- 删除一些示例数据中可能存在的错误关联
DELETE FROM tasks WHERE task_name IN ('系统维护', '周会准备', '月度报告') AND created_at > '2024-01-01';

-- ========================================
-- 第九步：更新表注释
-- ========================================

ALTER TABLE projects COMMENT = '项目表：存储项目基础信息（简化版）';
ALTER TABLE project_phases COMMENT = '项目阶段表：存储项目的各个阶段任务信息';
ALTER TABLE tasks COMMENT = '任务表：存储日常性任务（DAILY/WEEKLY/MONTHLY）';
ALTER TABLE weekly_reports COMMENT = '周报表：存储周报基础信息，通过关联表连接任务';
ALTER TABLE ai_analysis_results COMMENT = 'AI分析结果表：存储AI分析结果（保留备用）';
ALTER TABLE users COMMENT = '用户表：存储系统用户信息';

-- ========================================
-- 第十步：验证数据完整性
-- ========================================

-- 检查必要的表是否存在
SELECT 
    CASE WHEN COUNT(*) = 7 THEN 'SUCCESS' ELSE 'MISSING_TABLES' END AS table_check_result
FROM information_schema.tables 
WHERE table_schema = DATABASE() 
AND table_name IN ('users', 'projects', 'project_phases', 'tasks', 'weekly_reports', 'task_reports', 'dev_task_reports');

-- 检查关联表的外键约束
SELECT 
    CONSTRAINT_NAME, 
    TABLE_NAME, 
    REFERENCED_TABLE_NAME 
FROM information_schema.REFERENTIAL_CONSTRAINTS 
WHERE CONSTRAINT_SCHEMA = DATABASE() 
AND TABLE_NAME IN ('task_reports', 'dev_task_reports');

COMMIT;