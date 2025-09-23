-- V34__Fix_User_Role_And_Status_Fields.sql
-- 修复用户表role和status字段类型，使其与Java实体定义匹配
-- 问题：数据库role字段为ENUM('SUPERVISOR','ADMIN','SUPER_ADMIN')，但Java期望('MANAGER','ADMIN','SUPER_ADMIN','EMPLOYEE')
-- 问题：数据库缺少status字段，Java需要status字段

USE qr_auth_dev;

-- 查看当前表结构
SELECT 'Current users table structure:' as info;
DESCRIBE users;

-- 查看当前用户数据
SELECT 'Current user data:' as info;
SELECT username, role FROM users;

-- 1. 添加status字段（如果不存在）
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'users' AND COLUMN_NAME = 'status' AND TABLE_SCHEMA = DATABASE()) = 0, 
    'ALTER TABLE users ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT ''ACTIVE'' COMMENT ''用户状态''', 
    'SELECT "status column already exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 2. 修改role字段类型为VARCHAR以匹配@Enumerated(EnumType.STRING)
ALTER TABLE users MODIFY COLUMN role VARCHAR(50) NOT NULL COMMENT '用户角色';

-- 3. 更新role值以匹配Java枚举定义
-- SUPERVISOR -> MANAGER (主管)
-- ADMIN -> ADMIN (管理员) 
-- SUPER_ADMIN -> SUPER_ADMIN (超级管理员)
UPDATE users SET role = 'MANAGER' WHERE role = 'SUPERVISOR';
UPDATE users SET role = 'ADMIN' WHERE role = 'ADMIN';
UPDATE users SET role = 'SUPER_ADMIN' WHERE role = 'SUPER_ADMIN';

-- 4. 确保所有用户状态为ACTIVE
UPDATE users SET status = 'ACTIVE' WHERE status IS NULL OR status = '';

-- 5. 添加时间戳字段（如果不存在）
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'users' AND COLUMN_NAME = 'created_at' AND TABLE_SCHEMA = DATABASE()) = 0, 
    'ALTER TABLE users ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT ''创建时间''', 
    'SELECT "created_at column already exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'users' AND COLUMN_NAME = 'updated_at' AND TABLE_SCHEMA = DATABASE()) = 0, 
    'ALTER TABLE users ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''更新时间''', 
    'SELECT "updated_at column already exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 6. 创建用户状态和角色索引（如果不存在）
SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_NAME = 'users' AND INDEX_NAME = 'idx_user_status' AND TABLE_SCHEMA = DATABASE()) = 0, 
    'CREATE INDEX idx_user_status ON users(status)', 
    'SELECT "idx_user_status index already exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_NAME = 'users' AND INDEX_NAME = 'idx_user_role' AND TABLE_SCHEMA = DATABASE()) = 0, 
    'CREATE INDEX idx_user_role ON users(role)', 
    'SELECT "idx_user_role index already exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 7. 验证修复结果
SELECT 'Fixed users table structure:' as info;
DESCRIBE users;

SELECT 'Fixed user data:' as info;
SELECT username, email, role, status, created_at, updated_at FROM users ORDER BY role, username;

-- 8. 显示admin用户具体信息用于登录测试
SELECT 'Admin user for login test:' as info;
SELECT id, username, email, role, status, password FROM users WHERE username = 'admin';