-- V32__Fix_TaskReport_Primary_Key.sql
-- 修复TaskReport表的复合主键，添加is_week字段到主键中
-- 解决同一任务在本周汇报和下周规划中的主键冲突问题

-- 1. 备份现有数据
CREATE TABLE task_reports_backup AS SELECT * FROM task_reports;

-- 2. 删除外键约束（如果存在）
-- ALTER TABLE task_reports DROP FOREIGN KEY IF EXISTS fk_task_reports_weekly_report;
-- ALTER TABLE task_reports DROP FOREIGN KEY IF EXISTS fk_task_reports_task;

-- 3. 删除原主键约束
ALTER TABLE task_reports DROP PRIMARY KEY;

-- 4. 重新创建包含is_week字段的复合主键
ALTER TABLE task_reports ADD PRIMARY KEY (weekly_report_id, task_id, is_week);

-- 5. 重新创建外键约束（如果需要）
-- ALTER TABLE task_reports 
-- ADD CONSTRAINT fk_task_reports_weekly_report 
-- FOREIGN KEY (weekly_report_id) REFERENCES weekly_reports(id) ON DELETE CASCADE;

-- ALTER TABLE task_reports 
-- ADD CONSTRAINT fk_task_reports_task 
-- FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE;

-- 6. 确保is_week字段有正确的默认值和非空约束
ALTER TABLE task_reports MODIFY COLUMN is_week BOOLEAN NOT NULL DEFAULT TRUE;

-- 7. 添加索引以提高查询性能
CREATE INDEX idx_task_reports_weekly_report_is_week ON task_reports(weekly_report_id, is_week);
CREATE INDEX idx_task_reports_task_is_week ON task_reports(task_id, is_week);

-- 8. 清理备份表（可选，生产环境建议保留一段时间）
-- DROP TABLE task_reports_backup;