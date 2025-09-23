-- 完整修复用户表结构和数据
-- 问题1: 缺少status字段
-- 问题2: role字段为ENUM类型，应为VARCHAR
-- 问题3: role枚举值不匹配Java定义

USE qr_auth_dev;

-- 查看当前表结构
SELECT 'Current table structure:' as info;
DESCRIBE users;

-- 查看当前用户数据
SELECT 'Current user data:' as info;
SELECT username, role FROM users;

-- 1. 添加status字段
ALTER TABLE users ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';

-- 2. 修改role字段类型为VARCHAR
ALTER TABLE users MODIFY COLUMN role VARCHAR(50) NOT NULL;

-- 3. 更新role值以匹配Java枚举
UPDATE users SET role = 'MANAGER' WHERE role = 'SUPERVISOR';
UPDATE users SET role = 'ADMIN' WHERE role = 'ADMIN';  
UPDATE users SET role = 'SUPER_ADMIN' WHERE role = 'SUPER_ADMIN';

-- 4. 确保所有用户状态为ACTIVE
UPDATE users SET status = 'ACTIVE';

-- 5. 验证修复结果
SELECT 'Fixed table structure:' as info;
DESCRIBE users;

SELECT 'Fixed user data:' as info;
SELECT username, email, role, status, created_at FROM users ORDER BY role, username;

-- 6. 显示admin用户具体信息
SELECT 'Admin user details:' as info;
SELECT id, username, email, role, status, password FROM users WHERE username = 'admin';