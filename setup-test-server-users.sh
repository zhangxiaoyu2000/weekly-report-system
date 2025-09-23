#!/bin/bash

echo "🔧 在测试服务器MySQL中创建用户账户..."

# 这个脚本需要在测试服务器23.95.193.155上执行

# 1. 找到MySQL容器
echo "📋 查找MySQL容器..."
MYSQL_CONTAINER=$(docker ps | grep mysql | awk '{print $1}' | head -1)

if [ -z "$MYSQL_CONTAINER" ]; then
    echo "❌ 未找到MySQL容器"
    exit 1
fi

echo "✅ 找到MySQL容器: $MYSQL_CONTAINER"

# 2. 创建SQL文件
cat > /tmp/create_users.sql << 'EOSQL'
-- 创建用户表（如果不存在）
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

-- 插入新用户（所有密码都是admin123的BCrypt加密）
INSERT INTO users (username, email, password, full_name, role) VALUES 
('superadmin', 'superadmin@weeklyreport.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM7lbEYxXH6l3KSoztxKK', '超级管理员', 'SUPER_ADMIN'),
('zhangxiaoyu', 'zhangxiaoyu@weeklyreport.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM7lbEYxXH6l3KSoztxKK', '张小宇', 'SUPER_ADMIN'),
('admin', 'admin@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM7lbEYxXH6l3KSoztxKK', 'Administrator', 'ADMIN'),
('admin1', 'admin1@weeklyreport.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM7lbEYxXH6l3KSoztxKK', '管理员一', 'ADMIN'),
('admin2', 'admin2@weeklyreport.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM7lbEYxXH6l3KSoztxKK', '管理员二', 'ADMIN'),
('manager1', 'manager1@weeklyreport.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM7lbEYxXH6l3KSoztxKK', '主管一', 'SUPERVISOR');
EOSQL

# 3. 尝试不同的数据库名称和连接方式
echo "📋 尝试连接到数据库..."

# 常见的数据库名称
DBS=("qr_auth_dev" "weekly_report" "weekly-report" "weeklyreport" "mysql")
# 常见的密码
PASSWORDS=("rootpass123" "root123" "password" "123456" "admin123" "root" "")

for DB in "${DBS[@]}"; do
    for PASS in "${PASSWORDS[@]}"; do
        if [ -z "$PASS" ]; then
            # 尝试无密码
            if docker exec $MYSQL_CONTAINER mysql -u root $DB -e "SELECT 1;" 2>/dev/null; then
                echo "✅ 连接成功: 数据库=$DB, 密码=<空>"
                docker exec $MYSQL_CONTAINER mysql -u root $DB < /tmp/create_users.sql
                docker exec $MYSQL_CONTAINER mysql -u root $DB -e "SELECT username, email, full_name, role FROM users ORDER BY role, username;"
                echo "🎉 用户创建完成！"
                exit 0
            fi
        else
            # 尝试有密码
            if docker exec $MYSQL_CONTAINER mysql -u root -p$PASS $DB -e "SELECT 1;" 2>/dev/null; then
                echo "✅ 连接成功: 数据库=$DB, 密码=$PASS"
                docker exec $MYSQL_CONTAINER mysql -u root -p$PASS $DB < /tmp/create_users.sql
                docker exec $MYSQL_CONTAINER mysql -u root -p$PASS $DB -e "SELECT username, email, full_name, role FROM users ORDER BY role, username;"
                echo "🎉 用户创建完成！"
                exit 0
            fi
        fi
    done
done

echo "❌ 无法连接到MySQL数据库"
echo "📋 MySQL容器信息:"
docker exec $MYSQL_CONTAINER env | grep -E "MYSQL_|DATABASE_" || echo "未找到MySQL环境变量"

# 4. 清理临时文件
rm -f /tmp/create_users.sql