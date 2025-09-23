#!/bin/bash

# 本地部署测试脚本

echo "🚀 开始本地部署测试..."

# 1. 检查必要的镜像
echo "📦 检查Docker镜像..."
if docker images | grep -q "my-project-frontend"; then
    echo "✅ 前端镜像已存在"
else
    echo "❌ 前端镜像不存在"
    exit 1
fi

if docker images | grep -q "my-project-backend"; then
    echo "✅ 后端镜像已存在"
else
    echo "❌ 后端镜像不存在，请等待构建完成"
    exit 1
fi

# 2. 检查MySQL容器
echo "🗄️ 检查MySQL容器..."
if docker ps | grep -q "weekly-report-mysql-local"; then
    echo "✅ MySQL容器正在运行"
else
    echo "❌ MySQL容器未运行"
    exit 1
fi

# 3. 检查数据库连接和数据
echo "🔍 测试数据库连接..."
USER_COUNT=$(docker exec weekly-report-mysql-local mysql -u root -prootpass123 weekly_report_system -se "SELECT COUNT(*) FROM users;" 2>/dev/null)
if [ "$USER_COUNT" -eq 4 ]; then
    echo "✅ 数据库连接正常，用户数据正确: $USER_COUNT"
else
    echo "❌ 数据库连接异常或数据不正确"
    exit 1
fi

# 4. 启动完整的本地环境
echo "🚀 启动完整的本地环境..."
docker-compose -f docker-compose.local.yml up -d

# 5. 等待服务启动
echo "⏳ 等待服务启动..."
sleep 30

# 6. 测试服务健康状态
echo "🏥 检查服务健康状态..."

# 测试前端
if curl -s http://localhost:3003 > /dev/null; then
    echo "✅ 前端服务 (端口3003) 响应正常"
else
    echo "❌ 前端服务无响应"
fi

# 测试后端健康检查
if curl -s http://localhost:8082/api/health > /dev/null; then
    echo "✅ 后端服务 (端口8082) 响应正常"
else
    echo "❌ 后端服务无响应"
fi

# 7. 测试登录API
echo "🔐 测试登录API..."
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8082/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}')

if echo "$LOGIN_RESPONSE" | grep -q "token"; then
    echo "✅ 登录API测试成功"
else
    echo "❌ 登录API测试失败"
    echo "响应: $LOGIN_RESPONSE"
fi

echo "🎉 本地部署测试完成！"

# 显示服务状态
echo ""
echo "📊 服务状态:"
docker ps | grep "weekly-report"

echo ""
echo "🌐 访问地址:"
echo "- 前端: http://localhost:3003"
echo "- 后端API: http://localhost:8082/api"
echo "- 数据库: localhost:3309"