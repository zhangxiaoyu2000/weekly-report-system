-- 初始化用户账户
-- 根据账户.md文件创建系统用户

-- 清空现有用户（如果有）
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

-- 主管 (SUPERVISOR) - 使用SUPERVISOR而不是MANAGER因为这是数据库中定义的枚举值
INSERT INTO users (username, email, password, full_name, role) VALUES 
('manager1', 'manager1@weeklyreport.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM7lbEYxXH6l3KSoztxKK', '主管一', 'SUPERVISOR');