#!/bin/bash

echo "=== 触发登录请求并监控日志 ==="

# 1. 先获取当前时间作为基准
TIMESTAMP=$(date '+%Y-%m-%d %H:%M')
echo "开始时间: $TIMESTAMP"

# 2. 触发登录请求
echo "发送登录请求..."
curl -X POST "http://23.95.193.155:8081/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"admin","password":"admin123"}' \
  -w "\nHTTP状态码: %{http_code}\n" 

echo ""

# 3. 等待1秒让日志生成
sleep 1

# 4. 获取最新的Jenkins构建日志中的错误信息
echo "=== 获取最新日志 ==="
curl -s "http://zhangxiaoyu:2049251148@23.95.193.155:12088/job/WeeklyReport/9/consoleText" | \
  grep -A 5 -B 5 -E "(Login|login|ERROR|Exception|Failed|failed)" | \
  tail -30

echo ""
echo "=== 完成 ==="