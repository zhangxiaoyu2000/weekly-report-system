-- V31__Update_Approval_Status_Enum_To_Five_States.sql
-- 将approval_status枚举从7个状态简化为5个状态
-- 新状态: AI_ANALYZING, AI_REJECTED, ADMIN_REVIEWING, ADMIN_APPROVED, ADMIN_REJECTED

-- 1. 首先更新现有数据，将旧状态映射到新状态
UPDATE weekly_reports 
SET approval_status = 'ADMIN_REVIEWING' 
WHERE approval_status IN ('AI_APPROVED', 'PENDING_ADMIN_REVIEW');

UPDATE weekly_reports 
SET approval_status = 'ADMIN_REJECTED' 
WHERE approval_status IN ('REJECTED', 'ADMIN_REJECTED');

UPDATE weekly_reports 
SET approval_status = 'ADMIN_APPROVED' 
WHERE approval_status IN ('APPROVED', 'ADMIN_APPROVED');

-- 2. 删除现有的CHECK约束（兼容多版本MySQL）
-- 先尝试删除可能存在的约束，忽略错误
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM information_schema.table_constraints 
     WHERE table_schema = DATABASE() 
     AND table_name = 'weekly_reports' 
     AND constraint_name LIKE '%approval_status%' 
     LIMIT 1) > 0,
    'ALTER TABLE weekly_reports DROP CHECK chk_approval_status',
    'SELECT "No constraint to drop"'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3. 添加新的CHECK约束，只允许5个状态
ALTER TABLE weekly_reports 
ADD CONSTRAINT chk_approval_status_five_states 
CHECK (approval_status IN ('AI_ANALYZING', 'AI_REJECTED', 'ADMIN_REVIEWING', 'ADMIN_APPROVED', 'ADMIN_REJECTED'));

-- 4. 验证数据迁移结果
-- 显示更新后的状态分布
SELECT approval_status, COUNT(*) as count 
FROM weekly_reports 
GROUP BY approval_status 
ORDER BY approval_status;