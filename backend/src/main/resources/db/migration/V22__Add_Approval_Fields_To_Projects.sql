-- =============================================
-- 为项目表添加审批相关字段
-- Version: V22
-- Description: 为simple_projects表添加AI分析和审批流程字段
-- =============================================

-- 为simple_projects表添加审批相关字段
ALTER TABLE simple_projects 
ADD COLUMN ai_analysis_passed BOOLEAN DEFAULT NULL COMMENT 'AI分析是否通过',
ADD COLUMN ai_analysis_result TEXT COMMENT 'AI分析结果',
ADD COLUMN admin_reviewer_id BIGINT COMMENT '管理员审批人ID',
ADD COLUMN super_admin_reviewer_id BIGINT COMMENT '超级管理员审批人ID',
ADD COLUMN rejection_reason TEXT COMMENT '拒绝理由',
ADD COLUMN approval_status ENUM(
    'PENDING_AI', 
    'PENDING_ADMIN', 
    'PENDING_SUPER_ADMIN', 
    'APPROVED', 
    'REJECTED'
) NOT NULL DEFAULT 'PENDING_AI' COMMENT '审批状态',
ADD COLUMN submitted_at TIMESTAMP NULL COMMENT '提交时间',
ADD COLUMN reviewed_at TIMESTAMP NULL COMMENT '审核时间';

-- 添加外键约束
ALTER TABLE simple_projects 
ADD CONSTRAINT fk_simple_projects_admin_reviewer 
    FOREIGN KEY (admin_reviewer_id) REFERENCES users(id) ON DELETE SET NULL,
ADD CONSTRAINT fk_simple_projects_super_admin_reviewer 
    FOREIGN KEY (super_admin_reviewer_id) REFERENCES users(id) ON DELETE SET NULL;

-- 添加索引
ALTER TABLE simple_projects 
ADD INDEX idx_simple_projects_approval_status (approval_status),
ADD INDEX idx_simple_projects_admin_reviewer (admin_reviewer_id),
ADD INDEX idx_simple_projects_super_admin_reviewer (super_admin_reviewer_id),
ADD INDEX idx_simple_projects_ai_analysis_passed (ai_analysis_passed),
ADD INDEX idx_simple_projects_submitted_at (submitted_at);

-- 更新现有项目的状态
UPDATE simple_projects 
SET approval_status = CASE 
    WHEN status = 'PENDING_AI_ANALYSIS' THEN 'PENDING_AI'
    WHEN status = 'APPROVED' THEN 'APPROVED'
    ELSE 'PENDING_AI'
END;