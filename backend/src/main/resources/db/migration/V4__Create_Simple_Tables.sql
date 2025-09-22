-- 创建简化的项目表
CREATE TABLE IF NOT EXISTS simple_projects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_name VARCHAR(255) NOT NULL COMMENT '项目名称',
    project_content TEXT NOT NULL COMMENT '项目内容',
    project_members TEXT NOT NULL COMMENT '项目成员',
    key_indicators TEXT NOT NULL COMMENT '关键性指标',
    expected_results TEXT NOT NULL COMMENT '预期结果',
    actual_results TEXT COMMENT '实际结果',
    timeline TEXT NOT NULL COMMENT '时间线',
    stop_loss TEXT NOT NULL COMMENT '止损点',
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING_AI_ANALYSIS' COMMENT '状态',
    ai_analysis_result TEXT COMMENT 'AI分析结果',
    created_by BIGINT NOT NULL COMMENT '创建者ID',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_simple_project_created_by (created_by),
    INDEX idx_simple_project_status (status),
    INDEX idx_simple_project_created_at (created_at),
    
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='简化项目表';

-- 创建简化的周报表
CREATE TABLE IF NOT EXISTS simple_weekly_reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL COMMENT '关联项目ID',
    key_indicators TEXT NOT NULL COMMENT '关键性指标',
    actual_results TEXT NOT NULL COMMENT '实际结果',
    created_by BIGINT NOT NULL COMMENT '创建者ID',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX idx_simple_report_project_id (project_id),
    INDEX idx_simple_report_created_by (created_by),
    INDEX idx_simple_report_created_at (created_at),
    
    FOREIGN KEY (project_id) REFERENCES simple_projects(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='简化周报表';