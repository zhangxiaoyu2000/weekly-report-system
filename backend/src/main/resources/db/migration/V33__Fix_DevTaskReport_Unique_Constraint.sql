-- V33__Fix_DevTaskReport_Unique_Constraint.sql
-- 修复DevTaskReport表的唯一约束，添加is_week字段
-- 解决同一项目阶段在本周汇报和下周规划中的唯一约束冲突问题

-- 1. 删除原有的唯一约束
ALTER TABLE dev_task_reports DROP INDEX uk_dev_task_reports_unique;

-- 2. 重新创建包含is_week字段的唯一约束
ALTER TABLE dev_task_reports 
ADD CONSTRAINT uk_dev_task_reports_unique 
UNIQUE (weekly_report_id, project_id, phases_id, is_week);

-- 3. 确保is_week字段有正确的默认值和非空约束
ALTER TABLE dev_task_reports MODIFY COLUMN is_week BOOLEAN NOT NULL DEFAULT TRUE;

-- 4. 添加索引以提高查询性能
CREATE INDEX idx_dev_task_reports_weekly_report_is_week ON dev_task_reports(weekly_report_id, is_week);
CREATE INDEX idx_dev_task_reports_project_is_week ON dev_task_reports(project_id, is_week);