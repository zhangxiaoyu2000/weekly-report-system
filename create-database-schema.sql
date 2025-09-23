-- 周报管理系统数据库创建脚本
-- 基于数据库设计.md文档生成，结合V26-V33迁移文件优化
-- 生成日期: 2025-09-23
-- 更新日期: 2025-09-23 - 集成V26-V33迁移优化

-- 如果数据库不存在则创建
CREATE DATABASE IF NOT EXISTS weekly_report_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE weekly_report_system;

-- 1. AI分析结果表
CREATE TABLE ai_analysis_results (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'AI分析结果ID',
    report_id BIGINT NOT NULL COMMENT '关联的实体ID (项目ID或周报ID，取决于analysis_type)',
    entity_type ENUM('PROJECT', 'WEEKLY_REPORT') NOT NULL DEFAULT 'WEEKLY_REPORT' COMMENT '关联的实体类型: PROJECT-项目, WEEKLY_REPORT-周报',
    analysis_type VARCHAR(50) NOT NULL COMMENT '分析类型',
    result TEXT NOT NULL COMMENT '分析结果',
    confidence DECIMAL(3,2) COMMENT '置信度',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态',
    processing_time_ms BIGINT COMMENT '处理时间(毫秒)',
    model_version VARCHAR(100) COMMENT '模型版本',
    parameters TEXT COMMENT '分析参数',
    error_message TEXT COMMENT '错误信息',
    metadata JSON COMMENT '元数据',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    completed_at TIMESTAMP NULL COMMENT '完成时间',
    
    INDEX idx_ai_analysis_entity (entity_type, report_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI分析结果表，支持项目和周报的AI分析结果存储';

-- 2. 用户表
CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    role ENUM('MANAGER', 'ADMIN', 'SUPER_ADMIN') NOT NULL DEFAULT 'MANAGER' COMMENT '角色',
    status ENUM('ACTIVE', 'INACTIVE', 'DELETED') NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_users_username (username),
    INDEX idx_users_email (email),
    INDEX idx_users_role (role),
    INDEX idx_users_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 3. 项目表
CREATE TABLE projects (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '项目ID',
    name VARCHAR(200) NOT NULL COMMENT '项目名称',
    description TEXT COMMENT '项目内容',
    members TEXT COMMENT '项目成员',
    expected_results TEXT COMMENT '预期结果',
    timeline TEXT COMMENT '时间线',
    stop_loss TEXT COMMENT '止损点',
    created_by BIGINT NOT NULL COMMENT '创建者ID',
    ai_analysis_id BIGINT NULL COMMENT 'AI分析结果ID',
    admin_reviewer_id BIGINT NULL COMMENT '管理员审批人ID',
    super_admin_reviewer_id BIGINT NULL COMMENT '超级管理员审批人ID',
    rejection_reason TEXT COMMENT '拒绝理由',
    approval_status ENUM(
        'AI_ANALYZING',            -- AI分析中
        'AI_REJECTED',             -- AI分析拒绝
        'ADMIN_REVIEWING',         -- 管理员审核中
        'ADMIN_APPROVED',          -- 管理员审核通过
        'ADMIN_REJECTED'           -- 管理员审核拒绝
    ) NOT NULL DEFAULT 'AI_ANALYZING' COMMENT '审批状态',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE RESTRICT,
    FOREIGN KEY (ai_analysis_id) REFERENCES ai_analysis_results(id) ON DELETE SET NULL,
    FOREIGN KEY (admin_reviewer_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (super_admin_reviewer_id) REFERENCES users(id) ON DELETE SET NULL,
    
    INDEX idx_projects_created_by (created_by),
    INDEX idx_projects_approval_status (approval_status),
    INDEX idx_projects_name (name),
    INDEX idx_projects_ai_analysis (ai_analysis_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目表';

-- 4. 项目阶段表
CREATE TABLE project_phases (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '阶段任务ID',
    phase_name VARCHAR(200) NOT NULL COMMENT '任务名称',
    description TEXT COMMENT '阶段描述',
    assigned_members VARCHAR(500) COMMENT '负责成员',
    schedule VARCHAR(300) COMMENT '时间安排',
    expected_results TEXT COMMENT '预期结果',
    project_id BIGINT NOT NULL COMMENT '关联项目ID',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    
    INDEX idx_project_phases_project_id (project_id),
    INDEX idx_project_phases_name (phase_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目阶段表';

-- 5. 任务表
CREATE TABLE tasks (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '任务ID',
    task_name VARCHAR(200) NOT NULL COMMENT '任务名称',
    personnel_assignment VARCHAR(100) COMMENT '人员分配',
    timeline VARCHAR(200) COMMENT '时间线',
    expected_results VARCHAR(500) COMMENT '预期结果',
    created_by BIGINT NOT NULL COMMENT '创建者ID',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE RESTRICT,
    
    INDEX idx_tasks_created_by (created_by),
    INDEX idx_tasks_name (task_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务表';

-- 6. 周报表
CREATE TABLE weekly_reports (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '周报ID',
    user_id BIGINT NOT NULL COMMENT '提交周报的用户ID',
    title VARCHAR(200) NOT NULL COMMENT '周报标题',
    report_week VARCHAR(50) NOT NULL COMMENT '周报周期',
    additional_notes TEXT COMMENT '其他备注',
    development_opportunities TEXT COMMENT '可发展性清单',
    ai_analysis_id BIGINT NULL COMMENT 'AI分析结果ID',
    admin_reviewer_id BIGINT NULL COMMENT '管理员审批人ID',
    rejection_reason TEXT COMMENT '拒绝理由',
    approval_status ENUM(
        'AI_ANALYZING',            -- AI分析中
        'AI_REJECTED',             -- AI分析拒绝
        'ADMIN_REVIEWING',         -- 管理员审核中
        'ADMIN_APPROVED',          -- 管理员审核通过
        'ADMIN_REJECTED'           -- 管理员审核拒绝
    ) NOT NULL DEFAULT 'AI_ANALYZING' COMMENT '审批状态',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    FOREIGN KEY (ai_analysis_id) REFERENCES ai_analysis_results(id) ON DELETE SET NULL,
    FOREIGN KEY (admin_reviewer_id) REFERENCES users(id) ON DELETE SET NULL,
    
    INDEX idx_weekly_reports_user_id (user_id),
    INDEX idx_weekly_reports_report_week (report_week),
    INDEX idx_weekly_reports_approval_status (approval_status),
    INDEX idx_weekly_reports_ai_analysis (ai_analysis_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='周报表';

-- 7. 日常任务与周报关联表
CREATE TABLE task_reports (
    weekly_report_id BIGINT NOT NULL COMMENT '周报ID',
    task_id BIGINT NOT NULL COMMENT '任务ID',
    is_week BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否为本周汇报 (TRUE: 本周汇报, FALSE: 下周规划)',
    actual_results TEXT COMMENT '实际结果',
    result_difference_analysis TEXT COMMENT '结果差异分析',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    PRIMARY KEY (weekly_report_id, task_id, is_week),
    FOREIGN KEY (weekly_report_id) REFERENCES weekly_reports(id) ON DELETE CASCADE,
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    
    INDEX idx_task_reports_weekly_report (weekly_report_id),
    INDEX idx_task_reports_task (task_id),
    INDEX idx_task_reports_weekly_report_is_week (weekly_report_id, is_week),
    INDEX idx_task_reports_task_is_week (task_id, is_week)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='日常任务与周报关联表';

-- 8. 发展任务与周报关联表
CREATE TABLE dev_task_reports (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    weekly_report_id BIGINT NOT NULL COMMENT '周报ID',
    project_id BIGINT NOT NULL COMMENT '项目ID',
    phases_id BIGINT NOT NULL COMMENT '项目阶段ID',
    is_week BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否为本周汇报 (TRUE: 本周汇报, FALSE: 下周规划)',
    actual_results TEXT COMMENT '实际结果',
    result_difference_analysis TEXT COMMENT '结果差异分析',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    UNIQUE KEY uk_dev_task_reports_unique (weekly_report_id, project_id, phases_id, is_week),
    FOREIGN KEY (weekly_report_id) REFERENCES weekly_reports(id) ON DELETE CASCADE,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (phases_id) REFERENCES project_phases(id) ON DELETE CASCADE,
    
    INDEX idx_dev_task_reports_weekly_report (weekly_report_id),
    INDEX idx_dev_task_reports_project (project_id),
    INDEX idx_dev_task_reports_phases (phases_id),
    INDEX idx_dev_task_reports_weekly_report_is_week (weekly_report_id, is_week),
    INDEX idx_dev_task_reports_project_is_week (project_id, is_week)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='发展任务与周报关联表';

-- 插入默认数据

-- 插入默认用户（密码为明文，实际使用时应使用加密）
INSERT INTO users (username, email, password, role, status) VALUES
('admin', 'admin@company.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'ADMIN', 'ACTIVE'),
('super_admin', 'super@company.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'SUPER_ADMIN', 'ACTIVE'),
('manager1', 'manager1@company.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'MANAGER', 'ACTIVE'),
('manager2', 'manager2@company.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'MANAGER', 'ACTIVE');

-- 提交事务
COMMIT;

-- ========================================
-- 数据库结构验证
-- ========================================

-- 显示创建的表
SHOW TABLES;

-- 验证表字段数量（基于V26-V33迁移要求）
SELECT 
    'Table Structure Validation' as validation_type,
    table_name,
    COUNT(*) as field_count
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'weekly_report_system'
    AND table_name IN ('users', 'projects', 'project_phases', 'tasks', 'weekly_reports', 'task_reports', 'dev_task_reports', 'ai_analysis_results')
GROUP BY table_name
ORDER BY table_name;

-- 验证approval_status枚举值（应为5个状态）
SELECT 
    'Approval Status Validation' as validation_type,
    TABLE_NAME,
    COLUMN_TYPE
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME IN ('weekly_reports', 'projects') 
    AND COLUMN_NAME = 'approval_status' 
    AND TABLE_SCHEMA = 'weekly_report_system';

-- 验证关联表主键和唯一约束
SELECT 
    'Primary Key Validation' as validation_type,
    TABLE_NAME,
    CONSTRAINT_NAME,
    CONSTRAINT_TYPE
FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS 
WHERE TABLE_SCHEMA = 'weekly_report_system'
    AND TABLE_NAME IN ('task_reports', 'dev_task_reports')
    AND CONSTRAINT_TYPE IN ('PRIMARY KEY', 'UNIQUE')
ORDER BY TABLE_NAME, CONSTRAINT_TYPE;

-- 验证ai_analysis_results的entity_type字段
SELECT 
    'AI Analysis Entity Type Validation' as validation_type,
    COLUMN_NAME,
    COLUMN_TYPE,
    COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'ai_analysis_results' 
    AND COLUMN_NAME = 'entity_type' 
    AND TABLE_SCHEMA = 'weekly_report_system';

-- 显示表结构详情
DESCRIBE users;
DESCRIBE projects;
DESCRIBE project_phases;
DESCRIBE tasks;
DESCRIBE weekly_reports;
DESCRIBE task_reports;
DESCRIBE dev_task_reports;
DESCRIBE ai_analysis_results;

-- ========================================
-- 结构优化总结
-- ========================================
/*
本SQL脚本集成了V26-V33迁移文件的所有优化：

V26: 清理表结构，只保留核心字段
V27: AI分析结果表增加entity_type字段，支持项目和周报分析
V28: 任务和项目阶段表移除实际结果字段，关联表增加结果字段
V29: 增加approval_status的SUBMITTED状态
V30: 移除DRAFT和SUBMITTED状态
V31: 简化为5个approval_status状态
V32: task_reports表主键增加is_week字段
V33: dev_task_reports表唯一约束增加is_week字段

最终数据库结构特点：
1. 8张核心表，删除6张冗余表
2. approval_status简化为5个状态：AI_ANALYZING, AI_REJECTED, ADMIN_REVIEWING, ADMIN_APPROVED, ADMIN_REJECTED
3. 关联表支持本周汇报和下周规划的区分（is_week字段）
4. AI分析支持项目和周报两种实体类型
5. 完整的外键约束和索引优化
*/