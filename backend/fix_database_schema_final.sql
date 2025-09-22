-- 数据库Schema修正脚本 - 修正与设计文档的不一致问题
-- 执行日期：2025-09-20
-- 修正内容：
-- 1. 删除冗余表 weekly_reports_v2
-- 2. 为 projects 表添加缺失字段
-- 3. 为 project_phases 表添加时间戳字段  
-- 4. 为 tasks 表添加时间戳字段

USE qr_auth_dev;

-- 1. 删除冗余表 weekly_reports_v2
DROP TABLE IF EXISTS weekly_reports_v2;

-- 2. 为 projects 表添加缺失字段
ALTER TABLE projects 
ADD COLUMN super_admin_reviewer_id BIGINT NULL COMMENT '超级管理员审批人ID',
ADD COLUMN approval_status ENUM(
    'DRAFT',
    'AI_ANALYZING', 
    'AI_APPROVED',
    'AI_REJECTED',
    'ADMIN_REVIEWING',
    'ADMIN_APPROVED', 
    'ADMIN_REJECTED',
    'SUPER_ADMIN_REVIEWING',
    'SUPER_ADMIN_APPROVED',
    'SUPER_ADMIN_REJECTED',
    'FINAL_APPROVED'
) NOT NULL DEFAULT 'DRAFT' COMMENT '审批状态',
ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

-- 3. 为 project_phases 表添加时间戳字段
ALTER TABLE project_phases
ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

-- 4. 为 tasks 表添加时间戳字段  
ALTER TABLE tasks
ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

-- 验证修改结果
SELECT 'Schema修正完成' AS status;