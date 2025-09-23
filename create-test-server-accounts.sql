-- 在测试服务器MySQL中创建账户.md中的所有用户
-- 密码统一使用BCrypt加密的admin123 (临时密码)

-- 首先确保有users表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    role ENUM('SUPERVISOR', 'ADMIN', 'SUPER_ADMIN') NOT NULL DEFAULT 'SUPERVISOR',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 清空现有用户
DELETE FROM users;

-- 超级管理员 (SUPER_ADMIN)
INSERT INTO users (username, email, password, full_name, role) VALUES 
('superadmin', 'superadmin@weeklyreport.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM7lbEYxXH6l3KSoztxKK', '超级管理员', 'SUPER_ADMIN'),
('zhangxiaoyu', 'zhangxiaoyu@weeklyreport.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM7lbEYxXH6l3KSoztxKK', '张小宇', 'SUPER_ADMIN');

-- 管理员 (ADMIN)  
INSERT INTO users (username, email, password, full_name, role) VALUES 
('admin', 'admin@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM7lbEYxXH6l3KSoztxKK', 'Administrator', 'ADMIN'),
('admin1', 'admin1@weeklyreport.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM7lbEYxXH6l3KSoztxKK', '管理员一', 'ADMIN'),
('admin2', 'admin2@weeklyreport.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM7lbEYxXH6l3KSoztxKK', '管理员二', 'ADMIN');

-- 主管 (SUPERVISOR)
INSERT INTO users (username, email, password, full_name, role) VALUES 
('manager1', 'manager1@weeklyreport.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM7lbEYxXH6l3KSoztxKK', '主管一', 'SUPERVISOR');

-- 显示创建的用户
SELECT username, email, full_name, role FROM users ORDER BY role, username;