-- Create project_phases table without key_indicators
CREATE TABLE project_phases (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    project_id BIGINT NOT NULL,
    phase_name VARCHAR(200) NOT NULL,
    phase_description TEXT,
    timeline TEXT,
    assigned_members TEXT,
    estimated_results TEXT,
    actual_results TEXT,
    status ENUM('PENDING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    start_date DATETIME,
    end_date DATETIME,
    completion_date DATETIME,
    phase_order INT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_project_phases_project_id (project_id),
    INDEX idx_project_phases_status (status),
    INDEX idx_project_phases_order (phase_order),
    FOREIGN KEY (project_id) REFERENCES simple_projects(id) ON DELETE CASCADE
);