#!/bin/bash

echo "🔧 修复nginx代理配置..."

FRONTEND_CONTAINER=$(docker ps | grep frontend | awk '{print $1}')

cat > /tmp/nginx-final.conf << 'EOF'
server {
    listen 80;
    server_name localhost;
    
    # 静态文件
    location / {
        root /usr/share/nginx/html;
        index index.html index.htm;
        try_files $uri $uri/ /index.html;
    }
    
    # API代理 - 使用localhost而不是IP
    location /api/ {
        proxy_pass http://host.docker.internal:8081/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_redirect off;
    }
    
    # 健康检查
    location /health {
        return 200 "healthy\n";
        add_header Content-Type text/plain;
    }
}
EOF

# 更新配置
docker cp /tmp/nginx-final.conf $FRONTEND_CONTAINER:/etc/nginx/conf.d/default.conf
docker exec $FRONTEND_CONTAINER nginx -s reload

echo "✅ nginx配置已更新"

# 测试
echo "🧪 测试代理..."
curl -X POST http://23.95.193.155:3002/api/health -H "Content-Type: application/json"

# 清理
rm -f /tmp/nginx-final.conf