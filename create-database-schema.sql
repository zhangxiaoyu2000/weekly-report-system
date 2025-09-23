-- 周报管理系统数据库创建脚本
-- 基于数据库设计.md文档生成
-- 生成日期: 2025-09-23

-- 如果数据库不存在则创建
CREATE DATABASE IF NOT EXISTS weekly_report_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE weekly_report_system;

-- 1. AI分析结果表
CREATE TABLE ai_analysis_results (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'AI分析结果ID',
    report_id BIGINT NOT NULL COMMENT '关联报告ID',
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
    completed_at TIMESTAMP NULL COMMENT '完成时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI分析结果表';

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
        'AI_APPROVED',             -- AI分析通过
        'AI_REJECTED',             -- AI分析拒绝
        'ADMIN_REVIEWING',         -- 管理员审核中
        'ADMIN_APPROVED',          -- 管理员审核通过
        'ADMIN_REJECTED',          -- 管理员审核拒绝
        'SUPER_ADMIN_REVIEWING',   -- 超级管理员审核中
        'SUPER_ADMIN_APPROVED',    -- 超级管理员审核通过
        'SUPER_ADMIN_REJECTED',    -- 超级管理员审核拒绝
        'FINAL_APPROVED'           -- 最终批准
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
    quantitative_metrics VARCHAR(300) COMMENT '量化指标',
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
        'AI_APPROVED',             -- AI分析通过
        'AI_REJECTED',             -- AI分析拒绝
        'ADMIN_REVIEWING',         -- 管理员审核中
        'ADMIN_APPROVED',          -- 管理员审核通过
        'ADMIN_REJECTED',          -- 管理员审核拒绝
        'SUPER_ADMIN_REVIEWING',   -- 超级管理员审核中
        'SUPER_ADMIN_APPROVED',    -- 超级管理员审核通过
        'SUPER_ADMIN_REJECTED',    -- 超级管理员审核拒绝
        'FINAL_APPROVED'           -- 最终批准
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
    actual_results TEXT COMMENT '实际结果',
    result_difference_analysis TEXT COMMENT '结果差异分析',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    PRIMARY KEY (weekly_report_id, task_id),
    FOREIGN KEY (weekly_report_id) REFERENCES weekly_reports(id) ON DELETE CASCADE,
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    
    INDEX idx_task_reports_weekly_report (weekly_report_id),
    INDEX idx_task_reports_task (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='日常任务与周报关联表';

-- 8. 发展任务与周报关联表
CREATE TABLE dev_task_reports (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    weekly_report_id BIGINT NOT NULL COMMENT '周报ID',
    project_id BIGINT NOT NULL COMMENT '项目ID',
    phases_id BIGINT NOT NULL COMMENT '项目阶段ID',
    actual_results TEXT COMMENT '实际结果',
    result_difference_analysis TEXT COMMENT '结果差异分析',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    UNIQUE KEY uk_dev_task_reports_unique (weekly_report_id, project_id, phases_id),
    FOREIGN KEY (weekly_report_id) REFERENCES weekly_reports(id) ON DELETE CASCADE,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (phases_id) REFERENCES project_phases(id) ON DELETE CASCADE,
    
    INDEX idx_dev_task_reports_weekly_report (weekly_report_id),
    INDEX idx_dev_task_reports_project (project_id),
    INDEX idx_dev_task_reports_phases (phases_id)
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

-- 显示创建的表
SHOW TABLES;

-- 显示表结构
DESCRIBE users;
DESCRIBE projects;
DESCRIBE project_phases;
DESCRIBE tasks;
DESCRIBE weekly_reports;
DESCRIBE task_reports;
DESCRIBE dev_task_reports;
DESCRIBE ai_analysis_results;