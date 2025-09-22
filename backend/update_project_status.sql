-- Update existing ADMIN_APPROVED projects to follow the new workflow
-- Since admin approval should lead to SUPER_ADMIN_REVIEWING, not ADMIN_APPROVED
-- But if projects were already approved by admin, we need to decide:
-- Option 1: Move them to SUPER_ADMIN_REVIEWING for proper workflow
-- Option 2: Move them to SUPER_ADMIN_APPROVED if we consider admin approval as complete

-- Let's check which projects have ADMIN_APPROVED status
SELECT id, name, approval_status, admin_reviewer_id, super_admin_reviewer_id 
FROM projects 
WHERE approval_status = 'ADMIN_APPROVED';

-- Update ADMIN_APPROVED to SUPER_ADMIN_APPROVED since they were already approved
UPDATE projects 
SET approval_status = 'SUPER_ADMIN_APPROVED', 
    super_admin_reviewer_id = admin_reviewer_id,  -- Copy admin reviewer as super admin reviewer
    updated_at = CURRENT_TIMESTAMP
WHERE approval_status = 'ADMIN_APPROVED';

-- Verify the update
SELECT id, name, approval_status, admin_reviewer_id, super_admin_reviewer_id 
FROM projects 
WHERE id = 55;