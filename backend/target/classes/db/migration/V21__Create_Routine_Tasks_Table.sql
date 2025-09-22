-- =============================================
-- 创建日常性任务表
-- Version: V21
-- Description: 为重构周报系统创建日常性任务表
-- =============================================

-- 创建日常性任务表
CREATE TABLE IF NOT EXISTS routine_tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '任务ID',
    task_name VARCHAR(255) NOT NULL COMMENT '任务名称',
    task_description TEXT COMMENT '任务描述',
    task_type ENUM('DAILY', 'WEEKLY', 'MONTHLY') NOT NULL DEFAULT 'DAILY' COMMENT '任务类型',
    status ENUM('ACTIVE', 'INACTIVE', 'COMPLETED') NOT NULL DEFAULT 'ACTIVE' COMMENT '任务状态',
    created_by BIGINT NOT NULL COMMENT '创建者ID',
    assigned_to BIGINT COMMENT '分配给用户ID',
    priority ENUM('LOW', 'NORMAL', 'HIGH', 'URGENT') NOT NULL DEFAULT 'NORMAL' COMMENT '优先级',
    expected_duration INT COMMENT '预期完成时间(分钟)',
    tags JSON COMMENT '标签',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    completed_at TIMESTAMP NULL COMMENT '完成时间',
    
    -- 索引
    INDEX idx_routine_tasks_created_by (created_by),
    INDEX idx_routine_tasks_assigned_to (assigned_to),
    INDEX idx_routine_tasks_status (status),
    INDEX idx_routine_tasks_task_type (task_type),
    INDEX idx_routine_tasks_priority (priority),
    INDEX idx_routine_tasks_created_at (created_at),
    
    -- 外键约束
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_to) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='日常性任务表';

-- 添加一些示例日常任务
INSERT INTO routine_tasks (task_name, task_description, task_type, created_by, assigned_to, priority) VALUES
('每日代码评审', '审查团队成员提交的代码，确保代码质量', 'DAILY', 1, 1, 'HIGH'),
('每周团队会议', '参加团队周例会，汇报工作进展', 'WEEKLY', 1, 1, 'NORMAL'),
('月度项目报告', '撰写月度项目进展报告', 'MONTHLY', 1, 1, 'HIGH'),
('系统监控检查', '检查系统运行状态和性能指标', 'DAILY', 1, 1, 'NORMAL'),
('客户支持处理', '处理客户反馈和技术支持请求', 'DAILY', 1, 1, 'NORMAL');