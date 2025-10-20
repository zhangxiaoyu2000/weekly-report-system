-- 修复 approval_status enum：删除 AI_APPROVED，将现有数据更新为 ADMIN_REVIEWING

-- 步骤1：先将所有 AI_APPROVED 更新为 ADMIN_REVIEWING
UPDATE projects
SET approval_status = 'ADMIN_REVIEWING'
WHERE approval_status = 'AI_APPROVED';

-- 步骤2：修改 enum 定义，删除 AI_APPROVED
ALTER TABLE projects
MODIFY COLUMN approval_status ENUM(
    'AI_ANALYZING',
    'AI_REJECTED',
    'ADMIN_REVIEWING',
    'ADMIN_APPROVED',
    'ADMIN_REJECTED',
    'SUPER_ADMIN_REVIEWING',
    'SUPER_ADMIN_APPROVED',
    'SUPER_ADMIN_REJECTED',
    'FINAL_APPROVED'
) NOT NULL DEFAULT 'AI_ANALYZING';

-- 验证：检查是否还有 AI_APPROVED 状态的项目
-- SELECT COUNT(*) FROM projects WHERE approval_status = 'AI_APPROVED';
-- 应该返回 0
