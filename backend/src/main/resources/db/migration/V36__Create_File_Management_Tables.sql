-- 文件管理系统表结构
-- 支持周报附件功能，文件存储在MinIO中
-- 创建日期: 2025-09-29

-- 1. 文件信息表
CREATE TABLE file_attachments (
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

-- 2. 周报文件关联表
CREATE TABLE weekly_report_attachments (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '关联ID',
    weekly_report_id BIGINT NOT NULL COMMENT '周报ID',
    file_attachment_id BIGINT NOT NULL COMMENT '文件ID',
    attachment_type ENUM(
        'ROUTINE_TASK_RESULT',           -- 日常任务实际结果附件
        'ROUTINE_TASK_ANALYSIS',         -- 日常任务差异分析附件
        'DEV_TASK_RESULT',               -- 发展任务实际结果附件
        'DEV_TASK_ANALYSIS',             -- 发展任务差异分析附件
        'ADDITIONAL_NOTES',              -- 其他备注附件
        'DEVELOPMENT_OPPORTUNITIES',     -- 可发展性清单附件
        'GENERAL'                        -- 通用附件
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
    INDEX idx_weekly_report_attachments_order (weekly_report_id, display_order),
    UNIQUE INDEX uk_report_file_unique (weekly_report_id, file_attachment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='周报文件关联表';

-- 3. 文件访问日志表（可选，用于审计）
CREATE TABLE file_access_logs (
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

-- 插入测试数据（可选）
-- 注意：实际环境中不要插入测试数据

-- 提交事务
COMMIT;

-- ========================================
-- 表结构说明
-- ========================================

/*
文件管理系统设计说明：

1. file_attachments表：
   - 存储文件的基本信息和MinIO路径
   - 支持文件去重（通过file_hash）
   - 支持上传进度跟踪
   - 支持软删除
   - 支持文件过期机制

2. weekly_report_attachments表：
   - 建立周报与文件的多对多关联
   - 支持不同类型的附件分类
   - 支持关联到具体的任务或项目阶段
   - 支持附件排序和描述

3. file_access_logs表：
   - 记录文件访问历史
   - 支持审计和统计
   - 可用于安全监控

设计优势：
- 文件信息与关联关系分离，支持文件复用
- 灵活的附件分类系统
- 完整的权限控制支持
- 支持异步上传和进度跟踪
- 良好的扩展性

使用场景：
1. 日常任务结果附件：截图、报告文档等
2. 发展任务附件：项目文档、设计图等
3. 其他备注附件：补充说明文档
4. 通用附件：任意相关文件

技术实现要点：
- MinIO作为对象存储
- 异步上传处理
- 文件预览支持
- 权限控制
- 文件去重
*/