#!/bin/bash

# 部署后测试脚本

echo "🧪 测试部署结果..."

# 测试服务器IP（根据实际情况修改）
SERVER_IP="23.95.193.155"

echo "📋 1. 测试后端健康状态..."
HEALTH_STATUS=$(curl -s -w "%{http_code}" -o /dev/null http://$SERVER_IP:8081/api/health)
if [ "$HEALTH_STATUS" = "200" ]; then
    echo "✅ 后端健康检查通过"
else
    echo "❌ 后端健康检查失败 (HTTP $HEALTH_STATUS)"
fi

echo "📋 2. 测试前端访问..."
FRONTEND_STATUS=$(curl -s -w "%{http_code}" -o /dev/null http://$SERVER_IP:3001)
if [ "$FRONTEND_STATUS" = "200" ]; then
    echo "✅ 前端访问正常"
else
    echo "❌ 前端访问失败 (HTTP $FRONTEND_STATUS)"
fi

echo "📋 3. 测试登录功能..."
LOGIN_RESPONSE=$(curl -s -X POST http://$SERVER_IP:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"admin","password":"admin123"}')

LOGIN_SUCCESS=$(echo "$LOGIN_RESPONSE" | jq -r '.success' 2>/dev/null)
LOGIN_MESSAGE=$(echo "$LOGIN_RESPONSE" | jq -r '.message' 2>/dev/null)

if [ "$LOGIN_SUCCESS" = "true" ]; then
    echo "✅ admin登录成功！"
    TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.data.accessToken' 2>/dev/null)
    echo "   获得JWT Token: ${TOKEN:0:50}..."
elif [ "$LOGIN_MESSAGE" = "Login failed due to server error" ]; then
    echo "⚠️  登录出现服务器错误（数据库连接问题）"
    echo "   响应: $LOGIN_MESSAGE"
else
    echo "❌ 登录失败"
    echo "   响应: $LOGIN_MESSAGE"
fi

echo "📋 4. 测试nginx代理..."
PROXY_RESPONSE=$(curl -s -X POST http://$SERVER_IP:3001/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"admin","password":"admin123"}')

PROXY_MESSAGE=$(echo "$PROXY_RESPONSE" | jq -r '.message' 2>/dev/null)
if [ "$PROXY_MESSAGE" != "null" ] && [ "$PROXY_MESSAGE" != "" ]; then
    echo "✅ nginx代理工作正常"
    echo "   代理响应: $PROXY_MESSAGE"
else
    echo "❌ nginx代理可能有问题"
fi

echo ""
echo "🎯 部署测试总结:"
echo "   - 后端健康: $([ "$HEALTH_STATUS" = "200" ] && echo "✅" || echo "❌")"
echo "   - 前端访问: $([ "$FRONTEND_STATUS" = "200" ] && echo "✅" || echo "❌")"  
echo "   - 登录功能: $([ "$LOGIN_SUCCESS" = "true" ] && echo "✅" || echo "❌")"
echo "   - nginx代理: $([ "$PROXY_MESSAGE" != "null" ] && [ "$PROXY_MESSAGE" != "" ] && echo "✅" || echo "❌")"

if [ "$LOGIN_SUCCESS" = "true" ]; then
    echo ""
    echo "🎉 部署成功！系统已可正常使用"
    echo "🌐 访问地址: http://$SERVER_IP:3001"
    echo "🔑 登录: admin / admin123"
else
    echo ""
    echo "⚠️  部署部分成功，但登录功能需要进一步调试"
fi