-- Update test reports to AI_REJECTED and ADMIN_REJECTED status for testing
-- Run this script in your MySQL database

-- Update report 113 to AI_REJECTED status
UPDATE weekly_reports SET approval_status = 'AI_REJECTED' WHERE id = 113;

-- Update report 114 to ADMIN_REJECTED status  
UPDATE weekly_reports SET approval_status = 'ADMIN_REJECTED' WHERE id = 114;

-- Check the results
SELECT id, title, approval_status FROM weekly_reports WHERE id IN (113, 114);