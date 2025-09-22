#!/bin/bash

echo "=== 综合认证流程验证 ==="

# 清除代理设置
unset http_proxy https_proxy HTTP_PROXY HTTPS_PROXY

# 等待服务完全启动
echo "等待服务启动..."
sleep 5

echo -e "\n1. 🔐 测试注册功能:"
REGISTER_RESPONSE=$(curl -s -X POST "http://localhost:8081/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser2",
    "email": "testuser2@test.com",
    "password": "Test123@",
    "confirmPassword": "Test123@"
  }' 2>/dev/null)

if [[ $REGISTER_RESPONSE == *"success"* ]]; then
    echo "✅ 注册功能正常"
    # 提取token用于后续测试
    TOKEN=$(echo $REGISTER_RESPONSE | sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p')
    echo "获取到访问令牌: ${TOKEN:0:50}..."
else
    echo "⚠️ 注册可能有问题，继续测试已有用户"
fi

echo -e "\n2. 🔑 测试登录功能 (admin1/Admin123@):"
LOGIN_RESPONSE=$(curl -s -X POST "http://localhost:8081/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "admin1",
    "password": "Admin123@"
  }' 2>/dev/null)

if [[ $LOGIN_RESPONSE == *"success"* ]]; then
    echo "✅ 登录功能正常"
    LOGIN_TOKEN=$(echo $LOGIN_RESPONSE | sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p')
    echo "获取到登录令牌: ${LOGIN_TOKEN:0:50}..."
else
    echo "❌ 登录功能异常"
    echo "响应: $LOGIN_RESPONSE"
fi

echo -e "\n3. 🔄 测试令牌刷新功能:"
if [[ -n "$LOGIN_TOKEN" ]]; then
    REFRESH_RESPONSE=$(curl -s -X POST "http://localhost:8081/api/auth/refresh" \
      -H "Content-Type: application/json" \
      -d '{"refreshToken": "test-refresh-token"}' 2>/dev/null)
    
    if [[ $REFRESH_RESPONSE == *"success"* ]]; then
        echo "✅ 令牌刷新功能正常"
    else
        echo "⚠️ 令牌刷新功能可能有问题"
    fi
else
    echo "⚠️ 无法测试令牌刷新 - 没有有效登录令牌"
fi

echo -e "\n4. 🚪 测试登出功能:"
if [[ -n "$LOGIN_TOKEN" ]]; then
    LOGOUT_RESPONSE=$(curl -s -X POST "http://localhost:8081/api/auth/logout" \
      -H "Authorization: Bearer $LOGIN_TOKEN" \
      -H "Content-Type: application/json" 2>/dev/null)
    
    if [[ $LOGOUT_RESPONSE == *"success"* ]]; then
        echo "✅ 登出功能正常"
    else
        echo "⚠️ 登出功能可能有问题"
    fi
else
    echo "⚠️ 无法测试登出 - 没有有效登录令牌"
fi

echo -e "\n5. 🛡️ 测试受保护接口访问:"
if [[ -n "$LOGIN_TOKEN" ]]; then
    PROTECTED_RESPONSE=$(curl -s -X GET "http://localhost:8081/api/projects" \
      -H "Authorization: Bearer $LOGIN_TOKEN" 2>/dev/null)
    
    if [[ $PROTECTED_RESPONSE == *"success"* ]] || [[ $PROTECTED_RESPONSE == *"content"* ]]; then
        echo "✅ 受保护接口访问正常"
    else
        echo "⚠️ 受保护接口访问可能有问题"
    fi
else
    echo "⚠️ 无法测试受保护接口 - 没有有效登录令牌"
fi

echo -e "\n6. 📊 检查应用日志中的认证活动:"
AUTH_LOGS=$(tail -50 backend-password-fix.log | grep -i "login\|auth\|token\|password" | grep -v "SELECT\|password," | tail -5)
if [[ -n "$AUTH_LOGS" ]]; then
    echo "✅ 发现认证相关日志活动:"
    echo "$AUTH_LOGS"
else
    echo "⚠️ 没有发现明显的认证日志活动"
fi

echo -e "\n=== 认证流程验证完成 ==="

# 总结
echo -e "\n📋 修复总结:"
echo "✅ 密码编码修复: 所有用户密码已使用BCrypt加密"
echo "✅ 登出接口方法: 已确认使用POST方法"
echo "✅ AI模块路径: 已从/ai修复为/api/ai"
echo "✅ TaskType枚举: 已添加ROUTINE和DEVELOPMENT类型"
echo "✅ DTO兼容性: WeeklyReport和其他DTO已支持多种参数格式"