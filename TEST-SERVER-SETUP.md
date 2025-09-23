# 测试服务器用户账户设置指南

## 🎯 目标
在测试服务器 23.95.193.155 的MySQL数据库中创建账户.md中的所有用户账户。

## 📋 执行步骤

### 1. 登录到测试服务器
```bash
ssh user@23.95.193.155
```

### 2. 查找MySQL容器
```bash
docker ps | grep mysql
```

### 3. 创建用户账户
选择以下方法之一：

#### 方法A: 自动脚本（推荐）
```bash
# 下载并执行设置脚本
wget -O setup-users.sh [脚本URL] 
chmod +x setup-users.sh
./setup-users.sh
```

#### 方法B: 手动执行
```bash
# 1. 找到MySQL容器ID
MYSQL_CONTAINER=$(docker ps | grep mysql | awk '{print $1}' | head -1)

# 2. 创建SQL文件
cat > create_users.sql << 'EOF'
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

DELETE FROM users;

INSERT INTO users (username, email, password, full_name, role) VALUES 
('superadmin', 'superadmin@weeklyreport.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM7lbEYxXH6l3KSoztxKK', '超级管理员', 'SUPER_ADMIN'),
('zhangxiaoyu', 'zhangxiaoyu@weeklyreport.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM7lbEYxXH6l3KSoztxKK', '张小宇', 'SUPER_ADMIN'),
('admin', 'admin@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM7lbEYxXH6l3KSoztxKK', 'Administrator', 'ADMIN'),
('admin1', 'admin1@weeklyreport.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM7lbEYxXH6l3KSoztxKK', '管理员一', 'ADMIN'),
('admin2', 'admin2@weeklyreport.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM7lbEYxXH6l3KSoztxKK', '管理员二', 'ADMIN'),
('manager1', 'manager1@weeklyreport.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM7lbEYxXH6l3KSoztxKK', '主管一', 'SUPERVISOR');
EOF

# 3. 执行SQL（尝试不同数据库名称）
docker exec $MYSQL_CONTAINER mysql -u root -prootpass123 qr_auth_dev < create_users.sql
# 或者尝试其他可能的数据库名称
# docker exec $MYSQL_CONTAINER mysql -u root weekly_report < create_users.sql

# 4. 验证用户创建
docker exec $MYSQL_CONTAINER mysql -u root -prootpass123 qr_auth_dev -e "SELECT username, email, full_name, role FROM users ORDER BY role, username;"
```

## 🔑 创建的用户账户

| 用户名 | 密码 | 角色 | 姓名 | 邮箱 |
|--------|------|------|------|------|
| superadmin | admin123 | SUPER_ADMIN | 超级管理员 | superadmin@weeklyreport.com |
| zhangxiaoyu | admin123 | SUPER_ADMIN | 张小宇 | zhangxiaoyu@weeklyreport.com |
| admin | admin123 | ADMIN | Administrator | admin@example.com |
| admin1 | admin123 | ADMIN | 管理员一 | admin1@weeklyreport.com |
| admin2 | admin123 | ADMIN | 管理员二 | admin2@weeklyreport.com |
| manager1 | admin123 | SUPERVISOR | 主管一 | manager1@weeklyreport.com |

## 🧪 测试登录

执行完成后，测试登录功能：

```bash
# 测试后端登录
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"admin","password":"admin123"}'

# 测试前端代理登录  
curl -X POST http://localhost:3002/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"admin","password":"admin123"}'
```

预期结果：应该返回包含 `"success":true` 和 JWT token 的JSON响应。

## 🔧 故障排除

如果遇到问题：

1. 检查MySQL容器状态：`docker ps | grep mysql`
2. 查看容器日志：`docker logs [MYSQL_CONTAINER_ID]`
3. 检查数据库连接：`docker exec [MYSQL_CONTAINER_ID] mysql -u root -p`
4. 重启后端服务：`docker restart [BACKEND_CONTAINER_ID]`