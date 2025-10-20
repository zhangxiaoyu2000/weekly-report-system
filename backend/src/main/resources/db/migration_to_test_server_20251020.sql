-- ========================================
-- 测试服务器数据库结构同步脚本
-- ========================================
-- 目的: 将测试服务器数据库结构更新为与本地Docker一致
-- 原则: 只增不减，保留所有现有数据
-- 生成时间: 2025-10-20 17:40:00
-- 执行前必须: 备份数据库
-- ========================================

USE weekly_report_system;

-- ========================================
-- 第1部分: 修复projects表字段差异
-- ========================================

-- 问题: 测试服务器projects.name是VARCHAR(200)，本地是TEXT
-- 问题: 测试服务器approval_status包含'AI_APPROVED'，本地已移除
-- 操作: 修改字段类型（安全操作，数据会自动转换）

-- 步骤1: 先删除name字段的索引（TEXT字段不能直接作为索引）
-- 使用存储过程处理IF EXISTS逻辑
DROP PROCEDURE IF EXISTS drop_index_if_exists;

DELIMITER //
CREATE PROCEDURE drop_index_if_exists()
BEGIN
    DECLARE index_exists INT DEFAULT 0;

    SELECT COUNT(*) INTO index_exists
    FROM information_schema.statistics
    WHERE table_schema = 'weekly_report_system'
      AND table_name = 'projects'
      AND index_name = 'idx_projects_name';

    IF index_exists > 0 THEN
        ALTER TABLE projects DROP INDEX idx_projects_name;
    END IF;
END //
DELIMITER ;

CALL drop_index_if_exists();
DROP PROCEDURE drop_index_if_exists;

-- 步骤2: 修改name字段类型为TEXT
ALTER TABLE projects
  MODIFY COLUMN name TEXT NOT NULL COMMENT '项目名称 - 无长度限制';

-- 步骤3: 重新创建name字段的前缀索引
ALTER TABLE projects
  ADD INDEX idx_projects_name (name(100));

-- 步骤4: 修改approval_status枚举（移除AI_APPROVED）
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
  ) NOT NULL DEFAULT 'AI_ANALYZING' COMMENT '审批状态';

-- 说明:
-- 1. VARCHAR(200) → TEXT: 数据完全保留，只是允许更长内容
-- 2. 索引处理: TEXT字段索引需要指定前缀长度(100)
-- 3. 移除'AI_APPROVED'枚举值: 已确认无数据使用该值

-- ========================================
-- 第2部分: 创建缺失的表
-- ========================================

-- 2.1 文件附件表
CREATE TABLE IF NOT EXISTS file_attachments (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '文件ID',
    original_filename VARCHAR(255) NOT NULL COMMENT '原始文件名',
    stored_filename VARCHAR(255) NOT NULL COMMENT '存储文件名（UUID）',
    file_path VARCHAR(500) NOT NULL COMMENT 'MinIO中的文件路径',
    file_size BIGINT NOT NULL COMMENT '文件大小（字节）',
    mime_type VARCHAR(100) NOT NULL COMMENT 'MIME类型',
    file_extension VARCHAR(10) NOT NULL COMMENT '文件扩展名',
    bucket_name VARCHAR(100) NOT NULL DEFAULT 'weekly-reports' COMMENT 'MinIO桶名',
    uploaded_by BIGINT NOT NULL COMMENT '上传用户ID',
    upload_status ENUM('UPLOADING', 'COMPLETED', 'FAILED', 'DELETED') NOT NULL DEFAULT 'UPLOADING' COMMENT '上传状态',
    upload_progress INT DEFAULT 0 COMMENT '上传进度（0-100）',
    error_message TEXT COMMENT '错误信息',
    file_hash VARCHAR(64) COMMENT '文件哈希值（用于去重）',
    download_count INT DEFAULT 0 COMMENT '下载次数',
    is_public BOOLEAN DEFAULT FALSE COMMENT '是否公开访问',
    expires_at TIMESTAMP NULL COMMENT '过期时间',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at TIMESTAMP NULL COMMENT '删除时间（软删除）',

    FOREIGN KEY (uploaded_by) REFERENCES users(id) ON DELETE RESTRICT,

    INDEX idx_file_attachments_uploaded_by (uploaded_by),
    INDEX idx_file_attachments_upload_status (upload_status),
    INDEX idx_file_attachments_file_hash (file_hash),
    INDEX idx_file_attachments_created_at (created_at),
    INDEX idx_file_attachments_bucket_path (bucket_name, file_path),
    UNIQUE INDEX uk_file_path (bucket_name, file_path)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件附件表';

-- 2.2 周报附件关联表
CREATE TABLE IF NOT EXISTS weekly_report_attachments (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '关联ID',
    weekly_report_id BIGINT NOT NULL COMMENT '周报ID',
    file_attachment_id BIGINT NOT NULL COMMENT '文件ID',
    attachment_type ENUM(
        'ROUTINE_TASK_RESULT',
        'ROUTINE_TASK_ANALYSIS',
        'DEV_TASK_RESULT',
        'DEV_TASK_ANALYSIS',
        'ADDITIONAL_NOTES',
        'DEVELOPMENT_OPPORTUNITIES',
        'GENERAL'
    ) NOT NULL DEFAULT 'GENERAL' COMMENT '附件类型',
    related_task_id BIGINT NULL COMMENT '关联的任务ID（用于任务相关附件）',
    related_project_id BIGINT NULL COMMENT '关联的项目ID（用于发展任务附件）',
    related_phase_id BIGINT NULL COMMENT '关联的阶段ID（用于发展任务附件）',
    display_order INT DEFAULT 0 COMMENT '显示顺序',
    description TEXT COMMENT '附件描述',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '关联创建时间',

    FOREIGN KEY (weekly_report_id) REFERENCES weekly_reports(id) ON DELETE CASCADE,
    FOREIGN KEY (file_attachment_id) REFERENCES file_attachments(id) ON DELETE CASCADE,
    FOREIGN KEY (related_task_id) REFERENCES tasks(id) ON DELETE SET NULL,
    FOREIGN KEY (related_project_id) REFERENCES projects(id) ON DELETE SET NULL,
    FOREIGN KEY (related_phase_id) REFERENCES project_phases(id) ON DELETE SET NULL,

    INDEX idx_weekly_report_attachments_report (weekly_report_id),
    INDEX idx_weekly_report_attachments_file (file_attachment_id),
    INDEX idx_weekly_report_attachments_type (attachment_type),
    INDEX idx_weekly_report_attachments_task (related_task_id),
    INDEX idx_weekly_report_attachments_project (related_project_id, related_phase_id),
    UNIQUE INDEX uk_report_file_type (weekly_report_id, file_attachment_id, attachment_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='周报附件关联表';

-- 2.3 周报评论表
CREATE TABLE IF NOT EXISTS weekly_report_comments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评论ID',
    weekly_report_id BIGINT NOT NULL COMMENT '周报ID',
    user_id BIGINT NOT NULL COMMENT '评论用户ID',
    parent_comment_id BIGINT NULL COMMENT '父评论ID（回复功能）',
    content TEXT NOT NULL COMMENT '评论内容',
    comment_type ENUM('COMMENT', 'REPLY') NOT NULL DEFAULT 'COMMENT' COMMENT '评论类型（评论/回复）',
    status ENUM('ACTIVE', 'DELETED', 'HIDDEN') NOT NULL DEFAULT 'ACTIVE' COMMENT '评论状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    FOREIGN KEY (weekly_report_id) REFERENCES weekly_reports(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_comment_id) REFERENCES weekly_report_comments(id) ON DELETE CASCADE,

    INDEX idx_weekly_report_comments_report_id (weekly_report_id),
    INDEX idx_weekly_report_comments_user_id (user_id),
    INDEX idx_weekly_report_comments_parent_id (parent_comment_id),
    INDEX idx_weekly_report_comments_status (status),
    INDEX idx_weekly_report_comments_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='周报评论表';

-- 2.4 文件访问日志表
CREATE TABLE IF NOT EXISTS file_access_logs (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    file_attachment_id BIGINT NOT NULL COMMENT '文件ID',
    user_id BIGINT NULL COMMENT '访问用户ID',
    action ENUM('UPLOAD', 'DOWNLOAD', 'PREVIEW', 'DELETE') NOT NULL COMMENT '操作类型',
    ip_address VARCHAR(45) COMMENT 'IP地址',
    user_agent TEXT COMMENT '用户代理',
    access_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '访问时间',

    FOREIGN KEY (file_attachment_id) REFERENCES file_attachments(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,

    INDEX idx_file_access_logs_file (file_attachment_id),
    INDEX idx_file_access_logs_user (user_id),
    INDEX idx_file_access_logs_action (action),
    INDEX idx_file_access_logs_time (access_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件访问日志表';

-- ========================================
-- 第3部分: 验证迁移结果
-- ========================================

-- 检查新创建的表
SELECT
    'file_attachments' as table_name,
    COUNT(*) as row_count
FROM file_attachments
UNION ALL
SELECT
    'weekly_report_attachments' as table_name,
    COUNT(*) as row_count
FROM weekly_report_attachments
UNION ALL
SELECT
    'weekly_report_comments' as table_name,
    COUNT(*) as row_count
FROM weekly_report_comments
UNION ALL
SELECT
    'file_access_logs' as table_name,
    COUNT(*) as row_count
FROM file_access_logs;

-- 检查所有表
SHOW TABLES;

-- 检查projects表结构
DESCRIBE projects;

-- ========================================
-- 执行完成标记
-- ========================================
SELECT '✅ 测试服务器数据库结构同步完成' as status;
