-- =============================================
-- 数据库迁移脚本 - 优化用户查询性能
-- Version: 9.0
-- Date: 2025-09-16
-- Description: 添加索引和优化用户查询性能
-- =============================================

-- 添加复合索引以优化用户列表查询
CREATE INDEX IF NOT EXISTS idx_user_status_role ON users(status, role);

-- 添加复合索引以优化用户搜索
CREATE INDEX IF NOT EXISTS idx_user_search_fields ON users(full_name, username, email);

-- 添加索引以优化部门关联查询
CREATE INDEX IF NOT EXISTS idx_user_department_status ON users(department_id, status);

-- 添加索引以优化时间相关查询
CREATE INDEX IF NOT EXISTS idx_user_created_at ON users(created_at);
CREATE INDEX IF NOT EXISTS idx_user_last_login ON users(last_login_time);

-- 添加索引以优化角色过滤
CREATE INDEX IF NOT EXISTS idx_user_role_status ON users(role, status);

-- 分析表统计信息以帮助查询优化器
ANALYZE TABLE users;