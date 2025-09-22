-- Create project_phases table
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

-- Create task_templates table
CREATE TABLE task_templates (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    template_name VARCHAR(200) NOT NULL,
    template_description TEXT,
    assigned_members TEXT,
    timeline TEXT,
    estimated_results TEXT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_task_templates_active (is_active),
    INDEX idx_task_templates_created_by (created_by),
    INDEX idx_task_templates_name (template_name),
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

-- Add new columns to tasks table for relationships
ALTER TABLE tasks 
ADD COLUMN project_phase_id BIGINT,
ADD COLUMN task_template_id BIGINT,
ADD INDEX idx_tasks_project_phase_id (project_phase_id),
ADD INDEX idx_tasks_task_template_id (task_template_id),
ADD FOREIGN KEY (project_phase_id) REFERENCES project_phases(id) ON DELETE SET NULL,
ADD FOREIGN KEY (task_template_id) REFERENCES task_templates(id) ON DELETE SET NULL;