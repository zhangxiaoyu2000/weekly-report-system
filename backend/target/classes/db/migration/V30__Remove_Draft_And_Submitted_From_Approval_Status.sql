-- V30__Remove_Draft_And_Submitted_From_Approval_Status.sql
-- 从approval_status枚举中移除DRAFT和SUBMITTED状态
-- 只保留AI分析和审批相关的状态

-- 首先更新所有DRAFT状态的记录为AI_ANALYZING (如果有的话)
UPDATE weekly_reports 
SET approval_status = 'AI_ANALYZING' 
WHERE approval_status = 'DRAFT';

UPDATE projects 
SET approval_status = 'AI_ANALYZING' 
WHERE approval_status = 'DRAFT';

-- 更新所有SUBMITTED状态的记录为AI_ANALYZING (如果有的话)
UPDATE weekly_reports 
SET approval_status = 'AI_ANALYZING' 
WHERE approval_status = 'SUBMITTED';

UPDATE projects 
SET approval_status = 'AI_ANALYZING' 
WHERE approval_status = 'SUBMITTED';

-- 修改weekly_reports表的approval_status字段，移除DRAFT和SUBMITTED
ALTER TABLE weekly_reports 
MODIFY COLUMN approval_status ENUM(
    'AI_ANALYZING',             -- AI分析中
    'AI_APPROVED',              -- AI分析通过
    'AI_REJECTED',              -- AI分析不通过
    'ADMIN_REVIEWING',          -- 管理员审核中
    'ADMIN_APPROVED',           -- 管理员审核通过
    'ADMIN_REJECTED',           -- 管理员审核不通过
    'SUPER_ADMIN_REVIEWING',    -- 超级管理员审核中
    'SUPER_ADMIN_APPROVED',     -- 超级管理员审核通过
    'SUPER_ADMIN_REJECTED',     -- 超级管理员审核不通过
    'REJECTED',                 -- 已拒绝
    'FINAL_APPROVED'            -- 最终通过
) DEFAULT 'AI_ANALYZING' 
COMMENT '审批状态 - 移除了DRAFT和SUBMITTED状态';

-- 同样修改projects表的approval_status字段 (保持一致性)
ALTER TABLE projects 
MODIFY COLUMN approval_status ENUM(
    'AI_ANALYZING',             -- AI分析中
    'AI_APPROVED',              -- AI分析通过
    'AI_REJECTED',              -- AI分析不通过
    'ADMIN_REVIEWING',          -- 管理员审核中
    'ADMIN_APPROVED',           -- 管理员审核通过
    'ADMIN_REJECTED',           -- 管理员审核不通过
    'SUPER_ADMIN_REVIEWING',    -- 超级管理员审核中
    'SUPER_ADMIN_APPROVED',     -- 超级管理员审核通过
    'SUPER_ADMIN_REJECTED',     -- 超级管理员审核不通过
    'REJECTED',                 -- 已拒绝
    'FINAL_APPROVED'            -- 最终通过
) DEFAULT 'AI_ANALYZING' 
COMMENT '审批状态 - 移除了DRAFT和SUBMITTED状态';

-- 验证枚举值是否正确更新
SELECT 
    TABLE_NAME,
    COLUMN_NAME,
    DATA_TYPE,
    COLUMN_TYPE,
    COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME IN ('weekly_reports', 'projects') 
    AND COLUMN_NAME = 'approval_status' 
    AND TABLE_SCHEMA = DATABASE();