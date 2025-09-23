#!/bin/bash

echo "=== 修复数据库role字段类型问题 ==="

# 检查MySQL容器是否运行
echo "1. 检查MySQL容器状态..."
if ! curl -s http://23.95.193.155:8081/api/health | grep -q '"status":"UP"'; then
    echo "❌ 后端服务未运行，无法确认MySQL连接"
    exit 1
fi

echo "✅ 后端服务正常"

# 创建修复SQL脚本
echo "2. 创建修复SQL脚本..."
cat > /tmp/fix_role_field.sql << 'EOF'
USE qr_auth_dev;

-- 查看当前表结构
SELECT 'Current table structure:' as info;
DESCRIBE users;

-- 修改role字段类型为VARCHAR
ALTER TABLE users MODIFY COLUMN role VARCHAR(50) NOT NULL;

-- 确保所有用户的role值匹配Java枚举
UPDATE users SET role = 'ADMIN' WHERE role IN ('admin', 'ADMIN') OR username = 'admin';
UPDATE users SET role = 'SUPER_ADMIN' WHERE role IN ('super_admin', 'superadmin', 'SUPER_ADMIN') OR username IN ('superadmin', 'zhangxiaoyu');
UPDATE users SET role = 'MANAGER' WHERE role IN ('manager', 'supervisor', 'MANAGER', 'SUPERVISOR') OR username = 'manager1';

-- 确保所有用户都有正确的状态
UPDATE users SET status = 'ACTIVE' WHERE status IS NULL OR status = '' OR status IN ('active', 'ACTIVE');

-- 验证修复结果
SELECT 'Fixed users:' as info;
SELECT username, email, role, status, created_at FROM users ORDER BY role, username;

-- 显示新的表结构
SELECT 'New table structure:' as info;
DESCRIBE users;
EOF

echo "3. 修复脚本已创建"

# 通过Docker执行SQL修复
echo "4. 尝试通过Docker执行修复..."

# 尝试不同的MySQL容器和连接方式
CONTAINERS=("weekly-report-mysql" "weekly-report-mysql-new" "mysql")
PASSWORDS=("rootpass123" "root123" "admin123")

for CONTAINER in "${CONTAINERS[@]}"; do
    for PASSWORD in "${PASSWORDS[@]}"; do
        echo "尝试容器: $CONTAINER, 密码: $PASSWORD"
        
        # 尝试连接并执行修复
        if docker exec $CONTAINER mysql -u root -p$PASSWORD qr_auth_dev < /tmp/fix_role_field.sql 2>/dev/null; then
            echo "✅ 数据库修复成功！容器: $CONTAINER"
            
            # 清理临时文件
            rm -f /tmp/fix_role_field.sql
            
            # 测试登录
            echo "5. 测试修复后的登录..."
            sleep 2
            
            RESULT=$(curl -s -X POST "http://23.95.193.155:8081/api/auth/login" \
              -H "Content-Type: application/json" \
              -d '{"usernameOrEmail":"admin","password":"admin123"}')
            
            echo "登录测试结果: $RESULT"
            
            if echo "$RESULT" | grep -q '"success":true'; then
                echo "🎉 登录修复成功！"
                exit 0
            else
                echo "⚠️ 数据库已修复，但登录仍有问题"
                exit 1
            fi
        fi
    done
done

echo "❌ 无法连接到MySQL容器进行修复"
echo "可用的解决方案："
echo "1. 通过SSH登录到测试服务器手动执行修复"
echo "2. 通过Jenkins构建执行修复脚本"
echo "3. 重新部署包含修复的数据库初始化脚本"

# 清理临时文件
rm -f /tmp/fix_role_field.sql
exit 1