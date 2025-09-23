#!/bin/bash

echo "🎯 执行最终系统测试..."

# 测试当前状态
echo "📋 1. 检查当前服务状态..."
docker ps

# 测试前端访问
echo ""
echo "📋 2. 测试前端访问..."
FRONTEND_STATUS=$(curl -s -w "%{http_code}" -o /dev/null http://23.95.193.155:3002)
echo "前端状态码: $FRONTEND_STATUS"

# 测试后端健康
echo ""
echo "📋 3. 测试后端健康..."
BACKEND_HEALTH=$(curl -s http://23.95.193.155:8081/api/health)
echo "后端健康: $BACKEND_HEALTH"

# 测试数据库连接
echo ""
echo "📋 4. 测试数据库连接..."
docker exec weekly-report-mysql-new mysql -u root -prootpass123 qr_auth_dev -e "SELECT COUNT(*) as user_count FROM users;" 2>/dev/null | grep -v "Warning"

# 测试直接后端登录
echo ""
echo "📋 5. 测试后端登录..."
BACKEND_LOGIN=$(curl -s -X POST http://23.95.193.155:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"admin","password":"admin123"}')
echo "后端登录结果: $BACKEND_LOGIN"

# 测试前端代理登录
echo ""
echo "📋 6. 测试前端代理登录..."
PROXY_LOGIN=$(curl -s -X POST http://23.95.193.155:3002/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"admin","password":"admin123"}')
echo "前端代理结果: $PROXY_LOGIN"

# 分析结果
echo ""
echo "🎯 系统状态分析:"
if echo "$BACKEND_LOGIN" | grep -q '"success":true'; then
    echo "✅ 后端登录成功"
    BACKEND_SUCCESS=true
else
    echo "❌ 后端登录失败"
    BACKEND_SUCCESS=false
fi

if echo "$PROXY_LOGIN" | grep -q '"success":true'; then
    echo "✅ 前端代理登录成功"
    PROXY_SUCCESS=true
elif echo "$PROXY_LOGIN" | grep -q "404"; then
    echo "❌ 前端代理返回404错误"
    PROXY_SUCCESS=false
else
    echo "❌ 前端代理登录失败"
    PROXY_SUCCESS=false
fi

echo ""
if [ "$BACKEND_SUCCESS" = true ] && [ "$PROXY_SUCCESS" = true ]; then
    echo "🎉 系统完全正常! 可以正常使用"
    echo "🌐 访问地址: http://23.95.193.155:3002"
    echo "🔑 登录凭据: admin / admin123"
elif [ "$BACKEND_SUCCESS" = true ]; then
    echo "⚠️  后端正常但前端代理有问题"
    echo "🔧 需要修复nginx代理配置"
else
    echo "❌ 后端登录失败，需要检查数据库连接"
fi