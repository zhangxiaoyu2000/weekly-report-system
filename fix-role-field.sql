-- 修复用户表role字段类型问题
-- 问题：数据库中role字段为ENUM类型，但应用代码期望VARCHAR类型

USE qr_auth_dev;

-- 1. 首先查看当前表结构
DESCRIBE users;

-- 2. 修改role字段类型为VARCHAR，以匹配Java @Enumerated(EnumType.STRING)
ALTER TABLE users MODIFY COLUMN role VARCHAR(50) NOT NULL;

-- 3. 确保所有用户的role值匹配Java枚举
UPDATE users SET role = 'ADMIN' WHERE role = 'admin' OR username = 'admin';
UPDATE users SET role = 'SUPER_ADMIN' WHERE role = 'super_admin' OR role = 'superadmin' OR username IN ('superadmin', 'zhangxiaoyu');
UPDATE users SET role = 'MANAGER' WHERE role = 'manager' OR role = 'supervisor' OR username = 'manager1';

-- 4. 确保所有用户都有正确的状态
UPDATE users SET status = 'ACTIVE' WHERE status IS NULL OR status = '';

-- 5. 验证修复结果
SELECT username, email, role, status, created_at FROM users ORDER BY role, username;

-- 6. 显示表结构确认修改
SHOW CREATE TABLE users;