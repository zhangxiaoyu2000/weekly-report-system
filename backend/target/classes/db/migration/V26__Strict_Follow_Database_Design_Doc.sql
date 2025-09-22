-- V26__Strict_Follow_Database_Design_Doc.sql
-- 严格按照doc/数据库设计.md的要求重构数据库
-- 移除所有非必要字段，只保留error3.md指定的核心字段

-- ========================================
-- 第一步：删除不需要的表
-- ========================================

-- 根据设计文档，需要删除以下表（如果存在）
DROP TABLE IF EXISTS departments;
DROP TABLE IF EXISTS simple_weekly_reports;
DROP TABLE IF EXISTS task_templates;
DROP TABLE IF EXISTS templates;
DROP TABLE IF EXISTS comments;

-- ========================================
-- 第二步：清理users表 - 只保留核心字段
-- ========================================

-- 先删除users表相关的外键约束
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE WHERE TABLE_NAME = 'users' AND CONSTRAINT_NAME = 'fk_users_department_id' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE users DROP FOREIGN KEY fk_users_department_id', 'SELECT "fk_users_department_id constraint does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 根据设计文档，users表只保留：id, username, email, password, role, status
-- 删除非必要字段
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'users' AND COLUMN_NAME = 'first_name' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE users DROP COLUMN first_name', 'SELECT "first_name column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'users' AND COLUMN_NAME = 'last_name' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE users DROP COLUMN last_name', 'SELECT "last_name column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'users' AND COLUMN_NAME = 'department_id' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE users DROP COLUMN department_id', 'SELECT "department_id column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'users' AND COLUMN_NAME = 'employee_id' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE users DROP COLUMN employee_id', 'SELECT "employee_id column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'users' AND COLUMN_NAME = 'position' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE users DROP COLUMN position', 'SELECT "position column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'users' AND COLUMN_NAME = 'phone' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE users DROP COLUMN phone', 'SELECT "phone column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'users' AND COLUMN_NAME = 'avatar_url' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE users DROP COLUMN avatar_url', 'SELECT "avatar_url column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'users' AND COLUMN_NAME = 'full_name' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE users DROP COLUMN full_name', 'SELECT "full_name column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'users' AND COLUMN_NAME = 'last_login' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE users DROP COLUMN last_login', 'SELECT "last_login column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'users' AND COLUMN_NAME = 'last_login_time' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE users DROP COLUMN last_login_time', 'SELECT "last_login_time column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'users' AND COLUMN_NAME = 'deleted_at' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE users DROP COLUMN deleted_at', 'SELECT "deleted_at column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'users' AND COLUMN_NAME = 'created_at' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE users DROP COLUMN created_at', 'SELECT "created_at column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'users' AND COLUMN_NAME = 'updated_at' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE users DROP COLUMN updated_at', 'SELECT "updated_at column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ========================================
-- 第三步：清理projects表 - 字段重命名和删除
-- ========================================

-- 根据设计文档，projects表字段映射：
-- project_name → name, project_content → description, project_members → members
-- 保留：id, name, description, members, expected_results, timeline, stop_loss, created_by
-- 保留审批字段：ai_analysis_id, admin_reviewer_id, super_admin_reviewer_id, rejection_reason, approval_status

-- 删除冗余字段（保留新增的标准字段name, description, members）
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'projects' AND COLUMN_NAME = 'project_name' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE projects DROP COLUMN project_name', 'SELECT "project_name column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'projects' AND COLUMN_NAME = 'project_content' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE projects DROP COLUMN project_content', 'SELECT "project_content column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'projects' AND COLUMN_NAME = 'project_members' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE projects DROP COLUMN project_members', 'SELECT "project_members column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 删除设计文档标记为删除的字段
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'projects' AND COLUMN_NAME = 'actual_results' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE projects DROP COLUMN actual_results', 'SELECT "actual_results column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'projects' AND COLUMN_NAME = 'status' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE projects DROP COLUMN status', 'SELECT "status column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'projects' AND COLUMN_NAME = 'ai_analysis_result' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE projects DROP COLUMN ai_analysis_result', 'SELECT "ai_analysis_result column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'projects' AND COLUMN_NAME = 'manager_reviewer_id' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE projects DROP COLUMN manager_reviewer_id', 'SELECT "manager_reviewer_id column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'projects' AND COLUMN_NAME = 'manager_review_comment' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE projects DROP COLUMN manager_review_comment', 'SELECT "manager_review_comment column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'projects' AND COLUMN_NAME = 'manager_reviewed_at' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE projects DROP COLUMN manager_reviewed_at', 'SELECT "manager_reviewed_at column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'projects' AND COLUMN_NAME = 'admin_review_comment' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE projects DROP COLUMN admin_review_comment', 'SELECT "admin_review_comment column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'projects' AND COLUMN_NAME = 'admin_reviewed_at' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE projects DROP COLUMN admin_reviewed_at', 'SELECT "admin_reviewed_at column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'projects' AND COLUMN_NAME = 'super_admin_review_comment' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE projects DROP COLUMN super_admin_review_comment', 'SELECT "super_admin_review_comment column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'projects' AND COLUMN_NAME = 'super_admin_reviewed_at' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE projects DROP COLUMN super_admin_reviewed_at', 'SELECT "super_admin_reviewed_at column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'projects' AND COLUMN_NAME = 'ai_confidence' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE projects DROP COLUMN ai_confidence', 'SELECT "ai_confidence column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'projects' AND COLUMN_NAME = 'ai_feasibility_score' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE projects DROP COLUMN ai_feasibility_score', 'SELECT "ai_feasibility_score column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'projects' AND COLUMN_NAME = 'ai_risk_level' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE projects DROP COLUMN ai_risk_level', 'SELECT "ai_risk_level column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'projects' AND COLUMN_NAME = 'ai_provider_used' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE projects DROP COLUMN ai_provider_used', 'SELECT "ai_provider_used column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'projects' AND COLUMN_NAME = 'ai_processing_time_ms' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE projects DROP COLUMN ai_processing_time_ms', 'SELECT "ai_processing_time_ms column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'projects' AND COLUMN_NAME = 'ai_analyzed_at' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE projects DROP COLUMN ai_analyzed_at', 'SELECT "ai_analyzed_at column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'projects' AND COLUMN_NAME = 'ai_key_issues' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE projects DROP COLUMN ai_key_issues', 'SELECT "ai_key_issues column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'projects' AND COLUMN_NAME = 'ai_recommendations' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE projects DROP COLUMN ai_recommendations', 'SELECT "ai_recommendations column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'projects' AND COLUMN_NAME = 'created_at' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE projects DROP COLUMN created_at', 'SELECT "created_at column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'projects' AND COLUMN_NAME = 'updated_at' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE projects DROP COLUMN updated_at', 'SELECT "updated_at column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 添加缺失的字段
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'projects' AND COLUMN_NAME = 'ai_analysis_id' AND TABLE_SCHEMA = DATABASE()) = 0, 'ALTER TABLE projects ADD COLUMN ai_analysis_id BIGINT COMMENT "AI分析结果ID"', 'SELECT "ai_analysis_id column already exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'projects' AND COLUMN_NAME = 'admin_reviewer_id' AND TABLE_SCHEMA = DATABASE()) = 0, 'ALTER TABLE projects ADD COLUMN admin_reviewer_id BIGINT COMMENT "管理员审批人ID"', 'SELECT "admin_reviewer_id column already exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'projects' AND COLUMN_NAME = 'super_admin_reviewer_id' AND TABLE_SCHEMA = DATABASE()) = 0, 'ALTER TABLE projects ADD COLUMN super_admin_reviewer_id BIGINT COMMENT "超级管理员审批人ID"', 'SELECT "super_admin_reviewer_id column already exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'projects' AND COLUMN_NAME = 'rejection_reason' AND TABLE_SCHEMA = DATABASE()) = 0, 'ALTER TABLE projects ADD COLUMN rejection_reason TEXT COMMENT "拒绝理由"', 'SELECT "rejection_reason column already exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'projects' AND COLUMN_NAME = 'approval_status' AND TABLE_SCHEMA = DATABASE()) = 0, 'ALTER TABLE projects ADD COLUMN approval_status ENUM("DRAFT","AI_ANALYZING","AI_APPROVED","AI_REJECTED","ADMIN_REVIEWING","ADMIN_APPROVED","ADMIN_REJECTED","SUPER_ADMIN_REVIEWING","SUPER_ADMIN_APPROVED","SUPER_ADMIN_REJECTED","FINAL_APPROVED") DEFAULT "DRAFT" COMMENT "审批状态"', 'SELECT "approval_status column already exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ========================================
-- 第四步：清理project_phases表
-- ========================================

-- 根据设计文档，project_phases表删除时间管理字段，保留核心业务字段
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'project_phases' AND COLUMN_NAME = 'phase_description' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE project_phases DROP COLUMN phase_description', 'SELECT "phase_description column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'project_phases' AND COLUMN_NAME = 'timeline' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE project_phases DROP COLUMN timeline', 'SELECT "timeline column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'project_phases' AND COLUMN_NAME = 'estimated_results' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE project_phases DROP COLUMN estimated_results', 'SELECT "estimated_results column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'project_phases' AND COLUMN_NAME = 'status' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE project_phases DROP COLUMN status', 'SELECT "status column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'project_phases' AND COLUMN_NAME = 'start_date' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE project_phases DROP COLUMN start_date', 'SELECT "start_date column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'project_phases' AND COLUMN_NAME = 'end_date' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE project_phases DROP COLUMN end_date', 'SELECT "end_date column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'project_phases' AND COLUMN_NAME = 'completion_date' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE project_phases DROP COLUMN completion_date', 'SELECT "completion_date column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'project_phases' AND COLUMN_NAME = 'phase_order' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE project_phases DROP COLUMN phase_order', 'SELECT "phase_order column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'project_phases' AND COLUMN_NAME = 'created_at' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE project_phases DROP COLUMN created_at', 'SELECT "created_at column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'project_phases' AND COLUMN_NAME = 'updated_at' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE project_phases DROP COLUMN updated_at', 'SELECT "updated_at column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ========================================
-- 第五步：清理tasks表
-- ========================================

-- 根据设计文档，tasks表删除管理字段，只保留核心业务字段
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'tasks' AND COLUMN_NAME = 'stop_loss_point' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE tasks DROP COLUMN stop_loss_point', 'SELECT "stop_loss_point column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'tasks' AND COLUMN_NAME = 'progress' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE tasks DROP COLUMN progress', 'SELECT "progress column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'tasks' AND COLUMN_NAME = 'start_date' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE tasks DROP COLUMN start_date', 'SELECT "start_date column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'tasks' AND COLUMN_NAME = 'due_date' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE tasks DROP COLUMN due_date', 'SELECT "due_date column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'tasks' AND COLUMN_NAME = 'completion_date' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE tasks DROP COLUMN completion_date', 'SELECT "completion_date column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'tasks' AND COLUMN_NAME = 'budget' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE tasks DROP COLUMN budget', 'SELECT "budget column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'tasks' AND COLUMN_NAME = 'is_completed' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE tasks DROP COLUMN is_completed', 'SELECT "is_completed column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'tasks' AND COLUMN_NAME = 'is_overdue' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE tasks DROP COLUMN is_overdue', 'SELECT "is_overdue column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'tasks' AND COLUMN_NAME = 'priority' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE tasks DROP COLUMN priority', 'SELECT "priority column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'tasks' AND COLUMN_NAME = 'task_template_id' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE tasks DROP COLUMN task_template_id', 'SELECT "task_template_id column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'tasks' AND COLUMN_NAME = 'created_at' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE tasks DROP COLUMN created_at', 'SELECT "created_at column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'tasks' AND COLUMN_NAME = 'updated_at' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE tasks DROP COLUMN updated_at', 'SELECT "updated_at column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ========================================
-- 第六步：清理weekly_reports表
-- ========================================

-- 先删除依赖week_start字段的检查约束
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS WHERE CONSTRAINT_NAME = 'chk_reports_week_dates' AND TABLE_NAME = 'weekly_reports' AND CONSTRAINT_TYPE = 'CHECK' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE weekly_reports DROP CHECK chk_reports_week_dates', 'SELECT "chk_reports_week_dates constraint does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 删除其他检查约束
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS WHERE CONSTRAINT_NAME = 'weekly_reports_chk_1' AND TABLE_NAME = 'weekly_reports' AND CONSTRAINT_TYPE = 'CHECK' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE weekly_reports DROP CHECK weekly_reports_chk_1', 'SELECT "weekly_reports_chk_1 constraint does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 删除阻塞字段删除的外键约束
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE WHERE TABLE_NAME = 'weekly_reports' AND CONSTRAINT_NAME = 'fk_reports_reviewed_by' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE weekly_reports DROP FOREIGN KEY fk_reports_reviewed_by', 'SELECT "fk_reports_reviewed_by constraint does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE WHERE TABLE_NAME = 'weekly_reports' AND CONSTRAINT_NAME = 'fk_weekly_reports_user' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE weekly_reports DROP FOREIGN KEY fk_weekly_reports_user', 'SELECT "fk_weekly_reports_user constraint does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE WHERE TABLE_NAME = 'weekly_reports' AND CONSTRAINT_NAME = 'fk_weekly_reports_ai_analysis' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE weekly_reports DROP FOREIGN KEY fk_weekly_reports_ai_analysis', 'SELECT "fk_weekly_reports_ai_analysis constraint does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 根据设计文档，weekly_reports表删除大量字段，只保留核心字段
-- 删除时间管理字段
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'weekly_reports' AND COLUMN_NAME = 'week_start' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE weekly_reports DROP COLUMN week_start', 'SELECT "week_start column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'weekly_reports' AND COLUMN_NAME = 'week_end' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE weekly_reports DROP COLUMN week_end', 'SELECT "week_end column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'weekly_reports' AND COLUMN_NAME = 'submitted_at' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE weekly_reports DROP COLUMN submitted_at', 'SELECT "submitted_at column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'weekly_reports' AND COLUMN_NAME = 'reviewed_at' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE weekly_reports DROP COLUMN reviewed_at', 'SELECT "reviewed_at column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'weekly_reports' AND COLUMN_NAME = 'reviewed_by' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE weekly_reports DROP COLUMN reviewed_by', 'SELECT "reviewed_by column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'weekly_reports' AND COLUMN_NAME = 'review_comments' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE weekly_reports DROP COLUMN review_comments', 'SELECT "review_comments column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'weekly_reports' AND COLUMN_NAME = 'created_at' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE weekly_reports DROP COLUMN created_at', 'SELECT "created_at column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'weekly_reports' AND COLUMN_NAME = 'updated_at' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE weekly_reports DROP COLUMN updated_at', 'SELECT "updated_at column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 删除复杂审批字段
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'weekly_reports' AND COLUMN_NAME = 'manager_reviewer_id' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE weekly_reports DROP COLUMN manager_reviewer_id', 'SELECT "manager_reviewer_id column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'weekly_reports' AND COLUMN_NAME = 'manager_review_comment' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE weekly_reports DROP COLUMN manager_review_comment', 'SELECT "manager_review_comment column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'weekly_reports' AND COLUMN_NAME = 'manager_reviewed_at' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE weekly_reports DROP COLUMN manager_reviewed_at', 'SELECT "manager_reviewed_at column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'weekly_reports' AND COLUMN_NAME = 'admin_review_comment' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE weekly_reports DROP COLUMN admin_review_comment', 'SELECT "admin_review_comment column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'weekly_reports' AND COLUMN_NAME = 'admin_reviewed_at' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE weekly_reports DROP COLUMN admin_reviewed_at', 'SELECT "admin_reviewed_at column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'weekly_reports' AND COLUMN_NAME = 'super_admin_reviewer_id' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE weekly_reports DROP COLUMN super_admin_reviewer_id', 'SELECT "super_admin_reviewer_id column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'weekly_reports' AND COLUMN_NAME = 'super_admin_review_comment' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE weekly_reports DROP COLUMN super_admin_review_comment', 'SELECT "super_admin_review_comment column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'weekly_reports' AND COLUMN_NAME = 'super_admin_reviewed_at' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE weekly_reports DROP COLUMN super_admin_reviewed_at', 'SELECT "super_admin_reviewed_at column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 删除AI分析详细字段（通过ai_analysis_id关联）
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'weekly_reports' AND COLUMN_NAME = 'ai_confidence' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE weekly_reports DROP COLUMN ai_confidence', 'SELECT "ai_confidence column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'weekly_reports' AND COLUMN_NAME = 'ai_quality_score' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE weekly_reports DROP COLUMN ai_quality_score', 'SELECT "ai_quality_score column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'weekly_reports' AND COLUMN_NAME = 'ai_risk_level' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE weekly_reports DROP COLUMN ai_risk_level', 'SELECT "ai_risk_level column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'weekly_reports' AND COLUMN_NAME = 'ai_provider_used' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE weekly_reports DROP COLUMN ai_provider_used', 'SELECT "ai_provider_used column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'weekly_reports' AND COLUMN_NAME = 'ai_processing_time_ms' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE weekly_reports DROP COLUMN ai_processing_time_ms', 'SELECT "ai_processing_time_ms column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'weekly_reports' AND COLUMN_NAME = 'ai_analyzed_at' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE weekly_reports DROP COLUMN ai_analyzed_at', 'SELECT "ai_analyzed_at column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'weekly_reports' AND COLUMN_NAME = 'ai_key_issues' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE weekly_reports DROP COLUMN ai_key_issues', 'SELECT "ai_key_issues column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'weekly_reports' AND COLUMN_NAME = 'ai_recommendations' AND TABLE_SCHEMA = DATABASE()) > 0, 'ALTER TABLE weekly_reports DROP COLUMN ai_recommendations', 'SELECT "ai_recommendations column does not exist"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 添加必要的审批字段（按照设计文档的要求）
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'weekly_reports' AND COLUMN_NAME = 'ai_analysis_id' AND TABLE_SCHEMA = DATABASE()) = 0, 'ALTER TABLE weekly_reports ADD COLUMN ai_analysis_id BIGINT COMMENT "AI分析结果ID"', 'SELECT "ai_analysis_id column already exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'weekly_reports' AND COLUMN_NAME = 'admin_reviewer_id' AND TABLE_SCHEMA = DATABASE()) = 0, 'ALTER TABLE weekly_reports ADD COLUMN admin_reviewer_id BIGINT COMMENT "管理员审批人ID"', 'SELECT "admin_reviewer_id column already exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'weekly_reports' AND COLUMN_NAME = 'rejection_reason' AND TABLE_SCHEMA = DATABASE()) = 0, 'ALTER TABLE weekly_reports ADD COLUMN rejection_reason TEXT COMMENT "拒绝理由"', 'SELECT "rejection_reason column already exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'weekly_reports' AND COLUMN_NAME = 'approval_status' AND TABLE_SCHEMA = DATABASE()) = 0, 'ALTER TABLE weekly_reports ADD COLUMN approval_status ENUM("DRAFT","AI_ANALYZING","AI_APPROVED","AI_REJECTED","ADMIN_REVIEWING","ADMIN_APPROVED","ADMIN_REJECTED","SUPER_ADMIN_REVIEWING","SUPER_ADMIN_APPROVED","SUPER_ADMIN_REJECTED","FINAL_APPROVED") DEFAULT "DRAFT" COMMENT "审批状态"', 'SELECT "approval_status column already exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 重新添加必要的外键约束
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE WHERE TABLE_NAME = 'weekly_reports' AND CONSTRAINT_NAME = 'fk_weekly_reports_user_id' AND TABLE_SCHEMA = DATABASE()) = 0, 'ALTER TABLE weekly_reports ADD CONSTRAINT fk_weekly_reports_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE', 'SELECT "fk_weekly_reports_user_id constraint already exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ========================================
-- 第七步：验证最终结构
-- ========================================

-- 检查关键表的最终结构
SELECT 'users' as table_name, COUNT(*) as field_count FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'users' AND TABLE_SCHEMA = DATABASE()
UNION ALL
SELECT 'projects', COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'projects' AND TABLE_SCHEMA = DATABASE()
UNION ALL
SELECT 'project_phases', COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'project_phases' AND TABLE_SCHEMA = DATABASE()
UNION ALL
SELECT 'tasks', COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'tasks' AND TABLE_SCHEMA = DATABASE()
UNION ALL
SELECT 'weekly_reports', COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'weekly_reports' AND TABLE_SCHEMA = DATABASE()
UNION ALL
SELECT 'task_reports', COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'task_reports' AND TABLE_SCHEMA = DATABASE()
UNION ALL
SELECT 'dev_task_reports', COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'dev_task_reports' AND TABLE_SCHEMA = DATABASE()
UNION ALL
SELECT 'ai_analysis_results', COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'ai_analysis_results' AND TABLE_SCHEMA = DATABASE();

COMMIT;