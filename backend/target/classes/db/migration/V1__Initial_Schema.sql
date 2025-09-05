-- =============================================
-- 周报系统数据库初始化脚本
-- Version: 1.0
-- Author: System
-- Date: 2025-09-05
-- Description: 创建周报系统的核心数据表
-- =============================================

-- 设置字符集和排序规则
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =============================================
-- 1. 部门表 (Departments)
-- =============================================
CREATE TABLE `departments` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '部门ID',
    `name` VARCHAR(100) NOT NULL COMMENT '部门名称',
    `description` TEXT COMMENT '部门描述',
    `parent_id` BIGINT COMMENT '父部门ID',
    `manager_id` BIGINT COMMENT '部门经理ID',
    `level` INT NOT NULL DEFAULT 1 COMMENT '部门层级',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `status` ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_at` TIMESTAMP NULL COMMENT '删除时间',
    
    PRIMARY KEY (`id`),
    INDEX `idx_departments_parent_id` (`parent_id`),
    INDEX `idx_departments_manager_id` (`manager_id`),
    INDEX `idx_departments_level` (`level`),
    INDEX `idx_departments_status` (`status`),
    INDEX `idx_departments_deleted_at` (`deleted_at`),
    
    CONSTRAINT `chk_departments_level` CHECK (`level` > 0)
) ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='部门表';

-- =============================================
-- 2. 用户表 (Users)
-- =============================================
CREATE TABLE `users` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `email` VARCHAR(100) NOT NULL COMMENT '邮箱',
    `password` VARCHAR(255) NOT NULL COMMENT '密码哈希',
    `first_name` VARCHAR(50) NOT NULL COMMENT '姓',
    `last_name` VARCHAR(50) NOT NULL COMMENT '名',
    `role` ENUM('ADMIN', 'MANAGER', 'EMPLOYEE') NOT NULL DEFAULT 'EMPLOYEE' COMMENT '角色',
    `department_id` BIGINT COMMENT '部门ID',
    `status` ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED') NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
    `last_login` TIMESTAMP NULL COMMENT '最后登录时间',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_at` TIMESTAMP NULL COMMENT '删除时间',
    
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_users_username` (`username`),
    UNIQUE KEY `uk_users_email` (`email`),
    INDEX `idx_users_department_id` (`department_id`),
    INDEX `idx_users_status` (`status`),
    INDEX `idx_users_role` (`role`),
    INDEX `idx_users_created_at` (`created_at`),
    INDEX `idx_users_deleted_at` (`deleted_at`),
    
    CONSTRAINT `fk_users_department_id` FOREIGN KEY (`department_id`) REFERENCES `departments` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=10000 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- =============================================
-- 3. 模板表 (Templates)
-- =============================================
CREATE TABLE `templates` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '模板ID',
    `name` VARCHAR(100) NOT NULL COMMENT '模板名称',
    `description` TEXT COMMENT '模板描述',
    `content` LONGTEXT NOT NULL COMMENT '模板内容',
    `fields` JSON COMMENT '模板字段配置',
    `is_default` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否默认模板',
    `is_public` BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否公开模板',
    `created_by` BIGINT NOT NULL COMMENT '创建者ID',
    `department_id` BIGINT COMMENT '适用部门ID',
    `status` ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `usage_count` INT NOT NULL DEFAULT 0 COMMENT '使用次数',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_at` TIMESTAMP NULL COMMENT '删除时间',
    
    PRIMARY KEY (`id`),
    INDEX `idx_templates_created_by` (`created_by`),
    INDEX `idx_templates_department_id` (`department_id`),
    INDEX `idx_templates_status` (`status`),
    INDEX `idx_templates_is_default` (`is_default`),
    INDEX `idx_templates_is_public` (`is_public`),
    INDEX `idx_templates_deleted_at` (`deleted_at`),
    
    CONSTRAINT `fk_templates_created_by` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_templates_department_id` FOREIGN KEY (`department_id`) REFERENCES `departments` (`id`) ON DELETE SET NULL,
    CONSTRAINT `chk_templates_usage_count` CHECK (`usage_count` >= 0)
) ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='周报模板表';

-- =============================================
-- 4. 周报表 (WeeklyReports)
-- =============================================
CREATE TABLE `weekly_reports` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '周报ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `template_id` BIGINT COMMENT '模板ID',
    `title` VARCHAR(200) NOT NULL COMMENT '标题',
    `content` LONGTEXT NOT NULL COMMENT '内容',
    `summary` TEXT COMMENT '摘要',
    `status` ENUM('DRAFT', 'SUBMITTED', 'REVIEWED', 'PUBLISHED') NOT NULL DEFAULT 'DRAFT' COMMENT '状态',
    `week_start` DATE NOT NULL COMMENT '周开始日期',
    `week_end` DATE NOT NULL COMMENT '周结束日期',
    `submitted_at` TIMESTAMP NULL COMMENT '提交时间',
    `reviewed_at` TIMESTAMP NULL COMMENT '审核时间',
    `reviewed_by` BIGINT COMMENT '审核人ID',
    `review_comments` TEXT COMMENT '审核意见',
    `priority` ENUM('LOW', 'NORMAL', 'HIGH', 'URGENT') NOT NULL DEFAULT 'NORMAL' COMMENT '优先级',
    `tags` JSON COMMENT '标签',
    `attachments` JSON COMMENT '附件',
    `view_count` INT NOT NULL DEFAULT 0 COMMENT '查看次数',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_at` TIMESTAMP NULL COMMENT '删除时间',
    
    PRIMARY KEY (`id`),
    INDEX `idx_reports_user_id` (`user_id`),
    INDEX `idx_reports_template_id` (`template_id`),
    INDEX `idx_reports_status` (`status`),
    INDEX `idx_reports_week_start` (`week_start`),
    INDEX `idx_reports_week_end` (`week_end`),
    INDEX `idx_reports_priority` (`priority`),
    INDEX `idx_reports_created_at` (`created_at`),
    INDEX `idx_reports_reviewed_by` (`reviewed_by`),
    INDEX `idx_reports_deleted_at` (`deleted_at`),
    INDEX `idx_reports_user_week` (`user_id`, `week_start`),
    INDEX `idx_reports_status_date` (`status`, `created_at`),
    
    CONSTRAINT `fk_reports_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_reports_template_id` FOREIGN KEY (`template_id`) REFERENCES `templates` (`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_reports_reviewed_by` FOREIGN KEY (`reviewed_by`) REFERENCES `users` (`id`) ON DELETE SET NULL,
    CONSTRAINT `chk_reports_week_dates` CHECK (`week_end` >= `week_start`),
    CONSTRAINT `chk_reports_view_count` CHECK (`view_count` >= 0)
) ENGINE=InnoDB AUTO_INCREMENT=100000 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='周报表';

-- =============================================
-- 5. 评论表 (Comments)
-- =============================================
CREATE TABLE `comments` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评论ID',
    `report_id` BIGINT NOT NULL COMMENT '周报ID',
    `user_id` BIGINT NOT NULL COMMENT '评论用户ID',
    `parent_id` BIGINT COMMENT '父评论ID',
    `content` TEXT NOT NULL COMMENT '评论内容',
    `type` ENUM('COMMENT', 'SUGGESTION', 'APPROVAL', 'REJECTION') NOT NULL DEFAULT 'COMMENT' COMMENT '评论类型',
    `is_private` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否私密评论',
    `status` ENUM('ACTIVE', 'HIDDEN', 'DELETED') NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted_at` TIMESTAMP NULL COMMENT '删除时间',
    
    PRIMARY KEY (`id`),
    INDEX `idx_comments_report_id` (`report_id`),
    INDEX `idx_comments_user_id` (`user_id`),
    INDEX `idx_comments_parent_id` (`parent_id`),
    INDEX `idx_comments_type` (`type`),
    INDEX `idx_comments_status` (`status`),
    INDEX `idx_comments_created_at` (`created_at`),
    INDEX `idx_comments_deleted_at` (`deleted_at`),
    INDEX `idx_comments_report_status` (`report_id`, `status`),
    
    CONSTRAINT `fk_comments_report_id` FOREIGN KEY (`report_id`) REFERENCES `weekly_reports` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_comments_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_comments_parent_id` FOREIGN KEY (`parent_id`) REFERENCES `comments` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=100000 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- =============================================
-- 添加部门表的外键约束 (需要在用户表创建后)
-- =============================================
ALTER TABLE `departments` 
ADD CONSTRAINT `fk_departments_parent_id` FOREIGN KEY (`parent_id`) REFERENCES `departments` (`id`) ON DELETE SET NULL,
ADD CONSTRAINT `fk_departments_manager_id` FOREIGN KEY (`manager_id`) REFERENCES `users` (`id`) ON DELETE SET NULL;

-- =============================================
-- 创建视图用于常用查询
-- =============================================

-- 活跃用户视图
CREATE VIEW `v_active_users` AS
SELECT 
    u.id,
    u.username,
    u.email,
    u.first_name,
    u.last_name,
    u.role,
    d.name as department_name,
    u.last_login,
    u.created_at
FROM `users` u
LEFT JOIN `departments` d ON u.department_id = d.id
WHERE u.deleted_at IS NULL 
AND u.status = 'ACTIVE';

-- 部门层级视图
CREATE VIEW `v_department_hierarchy` AS
SELECT 
    d.id,
    d.name,
    d.level,
    pd.name as parent_name,
    CONCAT(REPEAT('  ', d.level - 1), d.name) as hierarchy_name,
    u.username as manager_name,
    d.status
FROM `departments` d
LEFT JOIN `departments` pd ON d.parent_id = pd.id
LEFT JOIN `users` u ON d.manager_id = u.id
WHERE d.deleted_at IS NULL
ORDER BY d.level, d.sort_order;

-- 最新周报视图
CREATE VIEW `v_latest_reports` AS
SELECT 
    wr.id,
    wr.title,
    wr.status,
    wr.week_start,
    wr.week_end,
    u.username as author,
    d.name as department,
    wr.view_count,
    wr.created_at,
    wr.updated_at
FROM `weekly_reports` wr
INNER JOIN `users` u ON wr.user_id = u.id
LEFT JOIN `departments` d ON u.department_id = d.id
WHERE wr.deleted_at IS NULL
ORDER BY wr.created_at DESC;

-- =============================================
-- 创建存储过程
-- =============================================

-- 获取部门下所有用户（包括子部门）
DELIMITER //
CREATE PROCEDURE `GetDepartmentUsers`(IN dept_id BIGINT)
BEGIN
    WITH RECURSIVE dept_tree AS (
        SELECT id, parent_id, name, level
        FROM departments 
        WHERE id = dept_id AND deleted_at IS NULL
        
        UNION ALL
        
        SELECT d.id, d.parent_id, d.name, d.level
        FROM departments d
        INNER JOIN dept_tree dt ON d.parent_id = dt.id
        WHERE d.deleted_at IS NULL
    )
    SELECT 
        u.id,
        u.username,
        u.email,
        u.first_name,
        u.last_name,
        u.role,
        d.name as department_name
    FROM users u
    INNER JOIN dept_tree d ON u.department_id = d.id
    WHERE u.deleted_at IS NULL AND u.status = 'ACTIVE'
    ORDER BY d.level, u.username;
END //
DELIMITER ;

-- 获取用户周报统计
DELIMITER //
CREATE PROCEDURE `GetUserReportStats`(IN user_id BIGINT, IN start_date DATE, IN end_date DATE)
BEGIN
    SELECT 
        COUNT(*) as total_reports,
        COUNT(CASE WHEN status = 'PUBLISHED' THEN 1 END) as published_reports,
        COUNT(CASE WHEN status = 'DRAFT' THEN 1 END) as draft_reports,
        AVG(view_count) as avg_views,
        MIN(week_start) as first_report_date,
        MAX(week_start) as last_report_date
    FROM weekly_reports
    WHERE user_id = user_id 
    AND deleted_at IS NULL
    AND week_start BETWEEN start_date AND end_date;
END //
DELIMITER ;

-- =============================================
-- 创建触发器
-- =============================================

-- 更新模板使用次数
DELIMITER //
CREATE TRIGGER `trg_update_template_usage` 
AFTER INSERT ON `weekly_reports`
FOR EACH ROW
BEGIN
    IF NEW.template_id IS NOT NULL THEN
        UPDATE `templates` 
        SET `usage_count` = `usage_count` + 1,
            `updated_at` = CURRENT_TIMESTAMP
        WHERE `id` = NEW.template_id;
    END IF;
END //
DELIMITER ;

-- 自动设置周结束日期（基于周开始日期）
DELIMITER //
CREATE TRIGGER `trg_set_week_end_date`
BEFORE INSERT ON `weekly_reports`
FOR EACH ROW
BEGIN
    IF NEW.week_end IS NULL OR NEW.week_end = '0000-00-00' THEN
        SET NEW.week_end = DATE_ADD(NEW.week_start, INTERVAL 6 DAY);
    END IF;
END //
DELIMITER ;

-- =============================================
-- 恢复外键检查
-- =============================================
SET FOREIGN_KEY_CHECKS = 1;

-- =============================================
-- 插入系统配置数据
-- =============================================

-- 插入根部门
INSERT INTO `departments` (`id`, `name`, `description`, `parent_id`, `level`, `sort_order`, `status`) 
VALUES (1, '总公司', '公司根部门', NULL, 1, 0, 'ACTIVE');

-- 插入系统管理员用户
INSERT INTO `users` (`id`, `username`, `email`, `password`, `first_name`, `last_name`, `role`, `department_id`, `status`) 
VALUES (1, 'admin', 'admin@company.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9P/wcs2EWOYKm2u', 'System', 'Admin', 'ADMIN', 1, 'ACTIVE');

-- 插入默认模板
INSERT INTO `templates` (`id`, `name`, `description`, `content`, `fields`, `is_default`, `is_public`, `created_by`, `status`) 
VALUES (1, '标准周报模板', '公司标准周报模板', 
'# 周报 - {{week_start}} 至 {{week_end}}

## 本周工作总结
{{work_summary}}

## 本周完成的主要工作
{{completed_tasks}}

## 遇到的问题和解决方案
{{issues_and_solutions}}

## 下周工作计划
{{next_week_plan}}

## 需要的支持和帮助
{{support_needed}}
', 
'{"fields": [{"name": "work_summary", "label": "工作总结", "type": "textarea", "required": true}, {"name": "completed_tasks", "label": "完成工作", "type": "textarea", "required": true}, {"name": "issues_and_solutions", "label": "问题解决", "type": "textarea", "required": false}, {"name": "next_week_plan", "label": "下周计划", "type": "textarea", "required": true}, {"name": "support_needed", "label": "需要支持", "type": "textarea", "required": false}]}',
TRUE, TRUE, 1, 'ACTIVE');