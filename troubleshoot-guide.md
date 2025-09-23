# 测试服务器故障排除指南

## 🎯 当前问题分析

通过Playwright测试确认的问题：

### 问题1: 前端nginx代理配置错误
- **现象**: 前端(3002)发送API请求返回404
- **原因**: nginx配置未正确代理到后端(8081)
- **修复**: 更新nginx配置文件

### 问题2: 后端数据库连接失败  
- **现象**: admin/admin123返回500服务器错误
- **原因**: 后端无法连接到MySQL数据库
- **修复**: 启动MySQL并重新配置连接

## 🚀 快速修复（在测试服务器执行）

### 方法1: 执行自动修复脚本
```bash
# 上传并执行修复脚本
./fix-server-issues.sh
```

### 方法2: 手动步骤修复

#### 步骤1: 检查并修复数据库连接
```bash
# 1. 检查MySQL容器
docker ps -a | grep mysql

# 2. 启动MySQL（如果已停止）
docker start $(docker ps -a | grep mysql | awk '{print $1}')

# 或创建新MySQL容器
docker run -d --name weekly-report-mysql \
  -e MYSQL_ROOT_PASSWORD=rootpass123 \
  -e MYSQL_DATABASE=qr_auth_dev \
  -e MYSQL_USER=qrauth \
  -e MYSQL_PASSWORD=qrauth123 \
  -p 3308:3306 \
  mysql:8.0

# 3. 重启后端容器
BACKEND_CONTAINER=$(docker ps | grep backend | awk '{print $1}')
docker restart $BACKEND_CONTAINER
```

#### 步骤2: 修复前端nginx代理
```bash
# 1. 找到前端容器
FRONTEND_CONTAINER=$(docker ps | grep nginx | grep ":3002->" | awk '{print $1}')

# 2. 创建正确的nginx配置
cat > /tmp/nginx-fixed.conf << 'EOF'
server {
    listen 80;
    server_name localhost;
    
    # 静态文件
    location / {
        root /usr/share/nginx/html;
        index index.html index.htm;
        try_files $uri $uri/ /index.html;
    }
    
    # API代理到正确的后端地址
    location /api/ {
        proxy_pass http://23.95.193.155:8081/api/;
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

# 3. 更新nginx配置
docker cp /tmp/nginx-fixed.conf $FRONTEND_CONTAINER:/etc/nginx/conf.d/default.conf

# 4. 重新加载nginx
docker exec $FRONTEND_CONTAINER nginx -s reload
```

## 🧪 验证修复结果

### 测试1: 后端API直接测试
```bash
curl -X POST http://23.95.193.155:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"admin","password":"admin123"}'
```

**期望结果**: 
```json
{"success":true,"message":"Login successful","data":{"accessToken":"...", ...}}
```

### 测试2: 前端代理测试
```bash
curl -X POST http://23.95.193.155:3002/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"admin","password":"admin123"}'
```

**期望结果**: 同样的成功登录响应

### 测试3: 浏览器端到端测试
1. 访问: http://23.95.193.155:3002/login
2. 输入: admin / admin123
3. 点击登录
4. **期望**: 成功登录并跳转到主页面

## 🔍 故障诊断命令

### 检查容器状态
```bash
# 查看所有容器
docker ps -a

# 查看特定日志
docker logs $(docker ps | grep backend | awk '{print $1}')
docker logs $(docker ps | grep frontend | awk '{print $1}')
docker logs $(docker ps | grep mysql | awk '{print $1}')
```

### 检查网络连接
```bash
# 测试端口开放
netstat -tulpn | grep -E ":3002|:8081|:3308"

# 测试内部连接
docker exec $(docker ps | grep backend | awk '{print $1}') curl -I http://localhost:8080/api/health
```

### 检查配置文件
```bash
# 查看nginx配置
docker exec $(docker ps | grep frontend | awk '{print $1}') cat /etc/nginx/conf.d/default.conf

# 查看后端环境变量
docker exec $(docker ps | grep backend | awk '{print $1}') env | grep -E "DB_|MYSQL_"
```

## 🎯 常见问题解决

### 问题: MySQL连接被拒绝
**解决**: 
```bash
# 重启MySQL容器
docker restart $(docker ps -a | grep mysql | awk '{print $1}')

# 检查MySQL日志
docker logs $(docker ps -a | grep mysql | awk '{print $1}')
```

### 问题: nginx配置更新不生效
**解决**:
```bash
# 强制重新加载nginx
docker exec $(docker ps | grep frontend | awk '{print $1}') nginx -s reload

# 或重启整个前端容器
docker restart $(docker ps | grep frontend | awk '{print $1}')
```

### 问题: 后端仍返回500错误
**解决**:
```bash
# 检查后端应用日志
docker exec $(docker ps | grep backend | awk '{print $1}') tail -f /app/logs/application.log

# 重启后端容器
docker restart $(docker ps | grep backend | awk '{print $1}')
```

## ✅ 修复完成检查清单

- [ ] MySQL容器运行并可连接
- [ ] 后端容器运行并通过健康检查  
- [ ] 前端容器运行并可访问
- [ ] nginx代理配置正确指向后端
- [ ] admin/admin123可以成功登录
- [ ] 登录后获得有效JWT token

## 🎉 预期最终状态

修复完成后：
- **前端访问**: http://23.95.193.155:3002 ✅
- **后端API**: http://23.95.193.155:8081 ✅  
- **登录功能**: admin/admin123 成功登录 ✅
- **API代理**: 前端→后端代理正常工作 ✅