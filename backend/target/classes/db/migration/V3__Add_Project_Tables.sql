-- =============================================
-- 项目管理系统数据库迁移脚本
-- Version: 3.0
-- Author: System
-- Date: 2025-09-05
-- Description: 添加项目管理相关表
-- =============================================

-- 设置字符集和排序规则
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =============================================
-- 1. 项目表 (Projects)
-- =============================================
CREATE TABLE `projects` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '项目ID',
    `name` VARCHAR(100) NOT NULL COMMENT '项目名称',
    `description` VARCHAR(500) COMMENT '项目描述',
    `status` ENUM('PLANNING', 'ACTIVE', 'ON_HOLD', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'PLANNING' COMMENT '项目状态',
    `priority` ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') NOT NULL DEFAULT 'MEDIUM' COMMENT '项目优先级',
    `start_date` DATE COMMENT '计划开始日期',
    `end_date` DATE COMMENT '计划结束日期',
    `actual_start_date` DATE COMMENT '实际开始日期',
    `actual_end_date` DATE COMMENT '实际结束日期',
    `progress` INT NOT NULL DEFAULT 0 COMMENT '项目进度(0-100)',
    `owner_id` BIGINT NOT NULL COMMENT '项目负责人ID',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    PRIMARY KEY (`id`),
    INDEX `idx_projects_name` (`name`),
    INDEX `idx_projects_status` (`status`),
    INDEX `idx_projects_priority` (`priority`),
    INDEX `idx_projects_owner` (`owner_id`),
    INDEX `idx_projects_dates` (`start_date`, `end_date`),
    INDEX `idx_projects_created_at` (`created_at`),
    
    CONSTRAINT `fk_projects_owner_id` FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `chk_projects_progress` CHECK (`progress` >= 0 AND `progress` <= 100),
    CONSTRAINT `chk_projects_dates` CHECK (`end_date` IS NULL OR `start_date` IS NULL OR `end_date` >= `start_date`)
) ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目表';

-- =============================================
-- 2. 项目成员表 (Project Members)
-- =============================================
CREATE TABLE `project_members` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `project_id` BIGINT NOT NULL COMMENT '项目ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role` ENUM('PROJECT_MANAGER', 'TECH_LEAD', 'DEVELOPER', 'TESTER', 'DESIGNER', 'ANALYST', 'MEMBER', 'OBSERVER') NOT NULL DEFAULT 'MEMBER' COMMENT '项目角色',
    `status` ENUM('ACTIVE', 'INACTIVE', 'INVITED', 'REMOVED') NOT NULL DEFAULT 'ACTIVE' COMMENT '成员状态',
    `joined_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    `left_date` TIMESTAMP NULL COMMENT '离开时间',
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `invited_by` BIGINT COMMENT '邀请人ID',
    `notes` VARCHAR(500) COMMENT '备注',
    
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_project_member` (`project_id`, `user_id`),
    INDEX `idx_project_member_project` (`project_id`),
    INDEX `idx_project_member_user` (`user_id`),
    INDEX `idx_project_member_role` (`role`),
    INDEX `idx_project_member_status` (`status`),
    INDEX `idx_project_member_joined_date` (`joined_date`),
    
    CONSTRAINT `fk_project_members_project_id` FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_project_members_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_project_members_invited_by` FOREIGN KEY (`invited_by`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=10000 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目成员表';

-- =============================================
-- 3. 更新周报表，添加项目关联
-- =============================================
ALTER TABLE `weekly_reports` 
ADD COLUMN `project_id` BIGINT COMMENT '关联项目ID' AFTER `user_id`,
ADD INDEX `idx_reports_project_id` (`project_id`),
ADD CONSTRAINT `fk_reports_project_id` FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`) ON DELETE SET NULL;

-- =============================================
-- 4. 创建项目相关视图
-- =============================================

-- 活跃项目视图
CREATE VIEW `v_active_projects` AS
SELECT 
    p.id,
    p.name,
    p.description,
    p.status,
    p.priority,
    p.progress,
    p.start_date,
    p.end_date,
    u.username as owner_name,
    u.full_name as owner_full_name,
    COUNT(pm.id) as member_count,
    p.created_at,
    p.updated_at
FROM `projects` p
LEFT JOIN `users` u ON p.owner_id = u.id
LEFT JOIN `project_members` pm ON p.id = pm.project_id AND pm.status = 'ACTIVE'
WHERE p.status IN ('PLANNING', 'ACTIVE', 'ON_HOLD')
GROUP BY p.id, p.name, p.description, p.status, p.priority, p.progress, p.start_date, p.end_date, u.username, u.full_name, p.created_at, p.updated_at
ORDER BY p.priority DESC, p.created_at DESC;

-- 项目成员详情视图
CREATE VIEW `v_project_members_detail` AS
SELECT 
    pm.id,
    pm.project_id,
    p.name as project_name,
    pm.user_id,
    u.username,
    u.full_name,
    u.email,
    u.position,
    pm.role,
    pm.status,
    pm.joined_date,
    pm.left_date,
    pm.notes,
    inviter.username as invited_by_name
FROM `project_members` pm
INNER JOIN `projects` p ON pm.project_id = p.id
INNER JOIN `users` u ON pm.user_id = u.id
LEFT JOIN `users` inviter ON pm.invited_by = inviter.id
ORDER BY pm.project_id, pm.joined_date;

-- 用户项目参与视图
CREATE VIEW `v_user_project_participation` AS
SELECT 
    u.id as user_id,
    u.username,
    u.full_name,
    COUNT(CASE WHEN pm.status = 'ACTIVE' THEN 1 END) as active_projects,
    COUNT(CASE WHEN pm.status = 'ACTIVE' AND pm.role = 'PROJECT_MANAGER' THEN 1 END) as managed_projects,
    GROUP_CONCAT(CASE WHEN pm.status = 'ACTIVE' THEN p.name END SEPARATOR ', ') as active_project_names
FROM `users` u
LEFT JOIN `project_members` pm ON u.id = pm.user_id
LEFT JOIN `projects` p ON pm.project_id = p.id
WHERE u.deleted_at IS NULL
GROUP BY u.id, u.username, u.full_name
ORDER BY active_projects DESC, u.full_name;

-- =============================================
-- 5. 创建存储过程
-- =============================================

-- 获取项目统计信息
DELIMITER //
CREATE PROCEDURE `GetProjectStats`(IN project_id BIGINT)
BEGIN
    SELECT 
        p.id,
        p.name,
        p.status,
        p.priority,
        p.progress,
        COUNT(pm.id) as total_members,
        COUNT(CASE WHEN pm.status = 'ACTIVE' THEN 1 END) as active_members,
        COUNT(CASE WHEN pm.role = 'PROJECT_MANAGER' AND pm.status = 'ACTIVE' THEN 1 END) as managers,
        COUNT(CASE WHEN pm.role IN ('DEVELOPER', 'TECH_LEAD') AND pm.status = 'ACTIVE' THEN 1 END) as developers,
        COUNT(wr.id) as total_reports,
        COUNT(CASE WHEN wr.status = 'PUBLISHED' THEN 1 END) as published_reports
    FROM `projects` p
    LEFT JOIN `project_members` pm ON p.id = pm.project_id
    LEFT JOIN `weekly_reports` wr ON p.id = wr.project_id AND wr.deleted_at IS NULL
    WHERE p.id = project_id
    GROUP BY p.id, p.name, p.status, p.priority, p.progress;
END //
DELIMITER ;

-- 获取用户在项目中的权限
DELIMITER //
CREATE PROCEDURE `GetUserProjectPermissions`(IN user_id BIGINT, IN project_id BIGINT)
BEGIN
    SELECT 
        pm.role,
        pm.status,
        CASE 
            WHEN p.owner_id = user_id THEN 'OWNER'
            WHEN pm.role = 'PROJECT_MANAGER' AND pm.status = 'ACTIVE' THEN 'MANAGER'
            WHEN pm.role = 'TECH_LEAD' AND pm.status = 'ACTIVE' THEN 'LEAD'
            WHEN pm.status = 'ACTIVE' THEN 'MEMBER'
            ELSE 'NONE'
        END as permission_level,
        CASE 
            WHEN p.owner_id = user_id OR pm.role IN ('PROJECT_MANAGER', 'TECH_LEAD') AND pm.status = 'ACTIVE' THEN TRUE
            ELSE FALSE
        END as can_manage_members,
        CASE 
            WHEN p.owner_id = user_id OR pm.status = 'ACTIVE' THEN TRUE
            ELSE FALSE
        END as can_view_project
    FROM `projects` p
    LEFT JOIN `project_members` pm ON p.id = pm.project_id AND pm.user_id = user_id
    WHERE p.id = project_id;
END //
DELIMITER ;

-- =============================================
-- 6. 创建触发器
-- =============================================

-- 自动添加项目创建者为项目经理
DELIMITER //
CREATE TRIGGER `trg_add_project_owner_as_manager`
AFTER INSERT ON `projects`
FOR EACH ROW
BEGIN
    INSERT INTO `project_members` (`project_id`, `user_id`, `role`, `status`, `invited_by`)
    VALUES (NEW.id, NEW.owner_id, 'PROJECT_MANAGER', 'ACTIVE', NEW.owner_id);
END //
DELIMITER ;

-- 防止移除最后一个项目经理
DELIMITER //
CREATE TRIGGER `trg_prevent_last_manager_removal`
BEFORE UPDATE ON `project_members`
FOR EACH ROW
BEGIN
    DECLARE manager_count INT DEFAULT 0;
    
    -- 如果正在将项目经理改为其他角色或移除
    IF OLD.role = 'PROJECT_MANAGER' AND OLD.status = 'ACTIVE' AND 
       (NEW.role != 'PROJECT_MANAGER' OR NEW.status != 'ACTIVE') THEN
        
        -- 计算剩余的活跃项目经理数量
        SELECT COUNT(*) INTO manager_count
        FROM `project_members`
        WHERE `project_id` = NEW.project_id 
        AND `role` = 'PROJECT_MANAGER' 
        AND `status` = 'ACTIVE'
        AND `id` != NEW.id;
        
        -- 如果只剩一个项目经理，阻止操作
        IF manager_count = 0 THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Cannot remove the last project manager';
        END IF;
    END IF;
END //
DELIMITER ;

-- =============================================
-- 7. 恢复外键检查
-- =============================================
SET FOREIGN_KEY_CHECKS = 1;

-- =============================================
-- 8. 插入示例数据（可选）
-- =============================================

-- 插入示例项目
INSERT INTO `projects` (`name`, `description`, `status`, `priority`, `owner_id`, `progress`) VALUES
('周报系统开发', '开发公司内部周报管理系统', 'ACTIVE', 'HIGH', 1, 75),
('移动端应用', '开发移动端应用程序', 'PLANNING', 'MEDIUM', 1, 10);

-- 注意：项目创建者会通过触发器自动添加为项目经理