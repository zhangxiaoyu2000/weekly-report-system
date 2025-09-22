-- V29__Fix_Approval_Status_Enum_Add_Submitted.sql
-- 修复approval_status枚举类型，添加缺失的SUBMITTED状态
-- 解决 "Data truncated for column 'approval_status' at row 1" 错误

-- 修复weekly_reports表的approval_status字段
-- 当前枚举值不包含SUBMITTED，但实体代码尝试使用该状态
ALTER TABLE weekly_reports 
MODIFY COLUMN approval_status ENUM(
    'DRAFT',                    -- 草稿
    'SUBMITTED',                -- 已提交 (新增)
    'AI_ANALYZING',             -- AI分析中  
    'AI_APPROVED',              -- AI分析通过
    'AI_REJECTED',              -- AI分析不通过
    'ADMIN_REVIEWING',          -- 管理员审核中
    'ADMIN_APPROVED',           -- 管理员审核通过
    'ADMIN_REJECTED',           -- 管理员审核不通过
    'SUPER_ADMIN_REVIEWING',    -- 超级管理员审核中
    'SUPER_ADMIN_APPROVED',     -- 超级管理员审核通过
    'SUPER_ADMIN_REJECTED',     -- 超级管理员审核不通过
    'REJECTED',                 -- 已拒绝 (新增)
    'FINAL_APPROVED'            -- 最终通过
) DEFAULT 'DRAFT' 
COMMENT '审批状态';

-- 同样修复projects表的approval_status字段 (保持一致性)
ALTER TABLE projects 
MODIFY COLUMN approval_status ENUM(
    'DRAFT',                    -- 草稿
    'SUBMITTED',                -- 已提交 (新增)
    'AI_ANALYZING',             -- AI分析中
    'AI_APPROVED',              -- AI分析通过
    'AI_REJECTED',              -- AI分析不通过
    'ADMIN_REVIEWING',          -- 管理员审核中
    'ADMIN_APPROVED',           -- 管理员审核通过
    'ADMIN_REJECTED',           -- 管理员审核不通过
    'SUPER_ADMIN_REVIEWING',    -- 超级管理员审核中
    'SUPER_ADMIN_APPROVED',     -- 超级管理员审核通过
    'SUPER_ADMIN_REJECTED',     -- 超级管理员审核不通过
    'REJECTED',                 -- 已拒绝 (新增)
    'FINAL_APPROVED'            -- 最终通过
) DEFAULT 'DRAFT' 
COMMENT '审批状态';

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