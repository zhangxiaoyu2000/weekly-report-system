-- Allow NULL weekly_report_id and report_section in tasks table for routine task templates
ALTER TABLE tasks 
MODIFY COLUMN weekly_report_id BIGINT NULL,
MODIFY COLUMN report_section VARCHAR(20) NULL;

-- Update index comment to reflect new nullable constraint
ALTER TABLE tasks DROP INDEX idx_task_weekly_report;
CREATE INDEX idx_task_weekly_report ON tasks (weekly_report_id);