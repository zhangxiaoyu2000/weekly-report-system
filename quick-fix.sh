#!/bin/bash

echo "🚀 执行快速修复方案..."

# 1. 检查当前状态
echo "📋 检查当前容器状态..."
docker ps

# 2. 修复nginx代理配置
echo "📋 修复前端nginx代理配置..."
FRONTEND_CONTAINER=$(docker ps | grep frontend | awk '{print $1}')

if [ -n "$FRONTEND_CONTAINER" ]; then
    # 创建修复的nginx配置
    cat > /tmp/fixed-nginx.conf << 'EOF'
server {
    listen 80;
    server_name localhost;
    
    # 静态文件
    location / {
        root /usr/share/nginx/html;
        index index.html index.htm;
        try_files $uri $uri/ /index.html;
    }
    
    # API代理 - 修复为正确的后端地址
    location /api/ {
        proxy_pass http://23.95.193.155:8081/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_redirect off;
        
        # 添加CORS头
        add_header Access-Control-Allow-Origin *;
        add_header Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS";
        add_header Access-Control-Allow-Headers "Content-Type, Authorization";
    }
    
    # 健康检查
    location /health {
        return 200 "healthy\n";
        add_header Content-Type text/plain;
    }
}
EOF
    
    # 更新nginx配置
    docker cp /tmp/fixed-nginx.conf $FRONTEND_CONTAINER:/etc/nginx/conf.d/default.conf
    docker exec $FRONTEND_CONTAINER nginx -s reload
    echo "✅ nginx配置已更新"
else
    echo "❌ 未找到前端容器"
fi

# 3. 检查并修复数据库连接
echo "📋 检查MySQL容器..."
MYSQL_CONTAINER=$(docker ps | grep mysql | grep 3308 | awk '{print $1}')

if [ -n "$MYSQL_CONTAINER" ]; then
    echo "✅ MySQL容器运行中: $MYSQL_CONTAINER"
    
    # 创建基本的用户表和数据
    echo "📋 初始化数据库schema..."
    docker exec $MYSQL_CONTAINER mysql -u root -prootpass123 qr_auth_dev << 'EOSQL'
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

# 插入admin用户 (密码: admin123)
INSERT IGNORE INTO users (username, email, password, full_name, role) VALUES 
('admin', 'admin@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM7lbEYxXH6l3KSoztxKK', 'Administrator', 'ADMIN');
EOSQL
    echo "✅ 数据库初始化完成"
else
    echo "❌ MySQL容器未运行"
fi

# 4. 测试修复结果
echo "📋 测试修复结果..."

# 测试后端健康
echo "🧪 测试后端健康..."
HEALTH_RESPONSE=$(curl -s http://23.95.193.155:8081/api/health)
echo "后端健康状态: $HEALTH_RESPONSE"

# 测试登录
echo "🧪 测试登录功能..."
LOGIN_RESPONSE=$(curl -s -X POST http://23.95.193.155:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"admin","password":"admin123"}')
echo "登录测试结果: $LOGIN_RESPONSE"

# 测试前端代理
echo "🧪 测试前端代理..."
PROXY_RESPONSE=$(curl -s -X POST http://23.95.193.155:3002/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"admin","password":"admin123"}')
echo "前端代理测试: $PROXY_RESPONSE"

echo ""
echo "🎉 快速修复完成!"
echo "📱 访问地址: http://23.95.193.155:3002"
echo "🔑 登录凭据: admin / admin123"

# 清理临时文件
rm -f /tmp/fixed-nginx.conf