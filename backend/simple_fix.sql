-- Simple approach: try to add columns, ignore errors if they exist
ALTER TABLE weekly_reports ADD COLUMN manager_reviewer_id BIGINT;
ALTER TABLE weekly_reports ADD COLUMN manager_review_comment TEXT;
ALTER TABLE weekly_reports ADD COLUMN manager_reviewed_at TIMESTAMP NULL;
ALTER TABLE weekly_reports ADD COLUMN admin_reviewer_id BIGINT;
ALTER TABLE weekly_reports ADD COLUMN admin_review_comment TEXT;
ALTER TABLE weekly_reports ADD COLUMN admin_reviewed_at TIMESTAMP NULL;
ALTER TABLE weekly_reports ADD COLUMN super_admin_reviewer_id BIGINT;
ALTER TABLE weekly_reports ADD COLUMN super_admin_review_comment TEXT;
ALTER TABLE weekly_reports ADD COLUMN super_admin_reviewed_at TIMESTAMP NULL;

-- Add missing user columns
ALTER TABLE users ADD COLUMN employee_id VARCHAR(50);
ALTER TABLE users ADD COLUMN position VARCHAR(100);
ALTER TABLE users ADD COLUMN phone VARCHAR(20);
ALTER TABLE users ADD COLUMN avatar_url VARCHAR(500);
ALTER TABLE users ADD COLUMN full_name VARCHAR(101);
ALTER TABLE users ADD COLUMN last_login_time TIMESTAMP NULL;