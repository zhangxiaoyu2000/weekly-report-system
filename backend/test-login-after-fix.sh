#!/bin/bash

echo "=== 测试修复后的登录功能 ==="

# 等待服务启动
sleep 2

echo "1. 测试 admin1 登录 (密码: Admin123@):"
curl -s -X POST "http://localhost:8081/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail": "admin1", "password": "Admin123@"}' | head -3

echo -e "\n\n2. 测试 manager1 登录 (密码: Manager123@):"
curl -s -X POST "http://localhost:8081/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail": "manager1", "password": "Manager123@"}' | head -3

echo -e "\n\n3. 测试 admin 登录 (密码: admin123):"
curl -s -X POST "http://localhost:8081/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail": "admin", "password": "admin123"}' | head -3

echo -e "\n\n4. 测试错误密码:"
curl -s -X POST "http://localhost:8081/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail": "admin1", "password": "wrongpassword"}' | head -3

echo -e "\n\n=== 登录测试完成 ==="