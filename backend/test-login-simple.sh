#!/bin/bash

echo "=== 直接测试登录功能 ==="

# 测试服务是否可达
echo "1. 测试服务状态:"
timeout 5s netcat -z localhost 8081 && echo "✅ 服务端口8081可达" || echo "❌ 服务端口8081不可达"

echo -e "\n2. 测试基本接口:"
curl -w "\nHTTP Status: %{http_code}\n" -s -X GET "http://localhost:8081/api/simple/hello" 2>/dev/null || echo "接口访问失败"

echo -e "\n3. 直接查看应用日志中的登录信息:"
tail -10 backend-password-fix.log | grep -i "login\|password\|auth" || echo "没有相关日志"

echo -e "\n=== 测试完成 ==="