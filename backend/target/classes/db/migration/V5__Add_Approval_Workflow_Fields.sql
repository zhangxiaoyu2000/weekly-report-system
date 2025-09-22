-- V5__Add_Approval_Workflow_Fields.sql
-- Add approval workflow fields to simple_projects and weekly_reports tables

-- Add approval workflow fields to simple_projects table
ALTER TABLE simple_projects 
ADD COLUMN manager_reviewer_id BIGINT,
ADD COLUMN manager_review_comment TEXT,
ADD COLUMN manager_reviewed_at TIMESTAMP,
ADD COLUMN admin_reviewer_id BIGINT,
ADD COLUMN admin_review_comment TEXT,
ADD COLUMN admin_reviewed_at TIMESTAMP,
ADD COLUMN super_admin_reviewer_id BIGINT,
ADD COLUMN super_admin_review_comment TEXT,
ADD COLUMN super_admin_reviewed_at TIMESTAMP;

-- Add foreign key constraints
ALTER TABLE simple_projects
ADD CONSTRAINT fk_simple_projects_manager_reviewer 
FOREIGN KEY (manager_reviewer_id) REFERENCES users(id),
ADD CONSTRAINT fk_simple_projects_admin_reviewer 
FOREIGN KEY (admin_reviewer_id) REFERENCES users(id),
ADD CONSTRAINT fk_simple_projects_super_admin_reviewer 
FOREIGN KEY (super_admin_reviewer_id) REFERENCES users(id);

-- Add approval workflow fields to weekly_reports table
ALTER TABLE weekly_reports 
ADD COLUMN manager_reviewer_id BIGINT,
ADD COLUMN manager_review_comment TEXT,
ADD COLUMN manager_reviewed_at TIMESTAMP,
ADD COLUMN admin_reviewer_id BIGINT,
ADD COLUMN admin_review_comment TEXT,
ADD COLUMN admin_reviewed_at TIMESTAMP,
ADD COLUMN super_admin_reviewer_id BIGINT,
ADD COLUMN super_admin_review_comment TEXT,
ADD COLUMN super_admin_reviewed_at TIMESTAMP;

-- Add foreign key constraints
ALTER TABLE weekly_reports
ADD CONSTRAINT fk_weekly_reports_manager_reviewer 
FOREIGN KEY (manager_reviewer_id) REFERENCES users(id),
ADD CONSTRAINT fk_weekly_reports_admin_reviewer 
FOREIGN KEY (admin_reviewer_id) REFERENCES users(id),
ADD CONSTRAINT fk_weekly_reports_super_admin_reviewer 
FOREIGN KEY (super_admin_reviewer_id) REFERENCES users(id);

-- Update project status enum values
-- First, update existing records to use new status values
UPDATE simple_projects 
SET status = 'SUBMITTED' 
WHERE status = 'PENDING_AI_ANALYSIS';

-- Update weekly report status enum values
-- First, update existing records to use new status values
UPDATE weekly_reports 
SET status = 'PENDING_MANAGER_REVIEW' 
WHERE status = 'SUBMITTED';

-- Create indexes for better performance
CREATE INDEX idx_simple_projects_manager_reviewer ON simple_projects(manager_reviewer_id);
CREATE INDEX idx_simple_projects_admin_reviewer ON simple_projects(admin_reviewer_id);
CREATE INDEX idx_simple_projects_super_admin_reviewer ON simple_projects(super_admin_reviewer_id);
CREATE INDEX idx_weekly_reports_manager_reviewer ON weekly_reports(manager_reviewer_id);
CREATE INDEX idx_weekly_reports_admin_reviewer ON weekly_reports(admin_reviewer_id);
CREATE INDEX idx_weekly_reports_super_admin_reviewer ON weekly_reports(super_admin_reviewer_id);