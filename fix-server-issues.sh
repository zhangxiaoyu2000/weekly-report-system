#!/bin/bash

# 测试服务器问题修复脚本
# 修复：1. 后端数据库连接 2. 前端nginx代理配置

set -e

echo "🔧 开始修复测试服务器问题..."

# ================================================
# 问题1: 修复后端数据库连接
# ================================================

echo "📋 1. 检查当前容器状态..."
docker ps -a

echo "📋 2. 检查MySQL容器是否存在..."
if docker ps -a | grep -q mysql; then
    echo "✅ 发现MySQL容器"
    MYSQL_CONTAINER=$(docker ps -a | grep mysql | awk '{print $1}' | head -1)
    echo "MySQL容器ID: $MYSQL_CONTAINER"
    
    # 检查MySQL是否运行
    if docker ps | grep -q mysql; then
        echo "✅ MySQL容器正在运行"
    else
        echo "⚠️ MySQL容器已停止，正在启动..."
        docker start $MYSQL_CONTAINER || docker run -d --name weekly-report-mysql \
            -e MYSQL_ROOT_PASSWORD=rootpass123 \
            -e MYSQL_DATABASE=qr_auth_dev \
            -e MYSQL_USER=qrauth \
            -e MYSQL_PASSWORD=qrauth123 \
            -p 3308:3306 \
            mysql:8.0
    fi
else
    echo "⚠️ 未发现MySQL容器，创建新容器..."
    docker run -d --name weekly-report-mysql \
        -e MYSQL_ROOT_PASSWORD=rootpass123 \
        -e MYSQL_DATABASE=qr_auth_dev \
        -e MYSQL_USER=qrauth \
        -e MYSQL_PASSWORD=qrauth123 \
        -p 3308:3306 \
        mysql:8.0
fi

echo "📋 3. 等待MySQL启动..."
sleep 15

# 检查MySQL连接
echo "📋 4. 检查MySQL连接..."
timeout 30 bash -c 'until docker exec $(docker ps | grep mysql | awk "{print \$1}" | head -1) mysqladmin ping -h localhost --silent; do sleep 2; done'
echo "✅ MySQL连接正常"

# ================================================
# 问题2: 修复前端nginx代理配置
# ================================================

echo "📋 5. 修复前端nginx代理配置..."

# 创建正确的nginx配置文件
cat > /tmp/nginx-fixed.conf << 'EOF'
server {
    listen 80;
    server_name localhost;
    
    # 启用gzip压缩
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml application/xml+rss text/javascript;
    
    # 静态文件配置
    location / {
        root /usr/share/nginx/html;
        index index.html index.htm;
        try_files $uri $uri/ /index.html;
        
        # 静态资源缓存
        location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
            expires 1y;
            add_header Cache-Control "public, immutable";
        }
    }
    
    # API代理 - 修复指向正确的后端地址
    location /api/ {
        proxy_pass http://23.95.193.155:8081/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header X-Forwarded-Host $host;
        proxy_set_header X-Forwarded-Server $host;
        proxy_redirect off;
    }
    
    # 健康检查
    location /health {
        access_log off;
        return 200 "healthy\n";
        add_header Content-Type text/plain;
    }
}
EOF

# 查找前端容器
FRONTEND_CONTAINER=$(docker ps | grep -E "(frontend|nginx)" | grep ":3002->" | awk '{print $1}' | head -1)
if [ -n "$FRONTEND_CONTAINER" ]; then
    echo "✅ 发现前端容器: $FRONTEND_CONTAINER"
    
    # 备份原始配置
    echo "📋 6. 备份原始nginx配置..."
    docker exec $FRONTEND_CONTAINER cp /etc/nginx/conf.d/default.conf /etc/nginx/conf.d/default.conf.backup 2>/dev/null || true
    
    # 更新nginx配置
    echo "📋 7. 更新nginx配置..."
    docker cp /tmp/nginx-fixed.conf $FRONTEND_CONTAINER:/etc/nginx/conf.d/default.conf
    
    # 重新加载nginx配置
    echo "📋 8. 重新加载nginx配置..."
    docker exec $FRONTEND_CONTAINER nginx -s reload
    
    echo "✅ 前端nginx配置已更新"
else
    echo "❌ 未找到前端容器"
fi

# ================================================
# 修复后端数据库连接配置
# ================================================

echo "📋 9. 检查后端容器..."
BACKEND_CONTAINER=$(docker ps | grep -E "(backend|spring)" | awk '{print $1}' | head -1)
if [ -n "$BACKEND_CONTAINER" ]; then
    echo "✅ 发现后端容器: $BACKEND_CONTAINER"
    
    # 检查后端环境变量
    echo "📋 10. 检查后端数据库配置..."
    docker exec $BACKEND_CONTAINER env | grep -E "DB_|MYSQL_" || echo "未找到数据库配置"
    
    # 更新后端环境变量（如果需要）
    echo "📋 11. 重启后端服务以连接数据库..."
    docker restart $BACKEND_CONTAINER
    
    # 等待后端启动
    echo "⏳ 等待后端服务重启..."
    sleep 30
    
    # 检查后端健康状态
    timeout 60 bash -c 'until curl -f http://localhost:8081/api/health >/dev/null 2>&1; do sleep 5; done'
    echo "✅ 后端服务已重启"
else
    echo "❌ 未找到后端容器"
fi

# ================================================
# 测试修复结果
# ================================================

echo "📋 12. 测试修复结果..."

# 测试后端登录
echo "🧪 测试后端登录API..."
BACKEND_RESULT=$(curl -s -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"admin","password":"admin123"}' \
  | jq -r '.success' 2>/dev/null || echo "failed")

if [ "$BACKEND_RESULT" = "true" ]; then
    echo "✅ 后端登录测试成功！"
else
    echo "⚠️ 后端登录仍有问题，响应："
    curl -s -X POST http://localhost:8081/api/auth/login \
      -H "Content-Type: application/json" \
      -d '{"usernameOrEmail":"admin","password":"admin123"}'
fi

# 测试前端代理
echo "🧪 测试前端nginx代理..."
PROXY_RESULT=$(curl -s -X POST http://localhost:3002/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"admin","password":"admin123"}' \
  | jq -r '.message' 2>/dev/null || echo "failed")

if [ "$PROXY_RESULT" != "null" ] && [ "$PROXY_RESULT" != "" ] && [ "$PROXY_RESULT" != "failed" ]; then
    echo "✅ 前端nginx代理工作正常"
    echo "   代理响应: $PROXY_RESULT"
else
    echo "⚠️ 前端nginx代理可能仍有问题"
fi

echo ""
echo "🎉 修复完成！"
echo ""
echo "📊 服务状态："
echo "   前端: http://$(hostname -I | awk '{print $1}'):3002"
echo "   后端: http://$(hostname -I | awk '{print $1}'):8081"
echo "   登录: admin / admin123"
echo ""
echo "🔍 如果仍有问题，请检查："
echo "   - docker ps  # 查看容器状态"
echo "   - docker logs \$BACKEND_CONTAINER  # 查看后端日志"
echo "   - docker logs \$FRONTEND_CONTAINER  # 查看前端日志"

# 清理临时文件
rm -f /tmp/nginx-fixed.conf