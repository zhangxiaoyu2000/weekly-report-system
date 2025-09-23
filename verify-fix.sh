#!/bin/bash

# 修复结果验证脚本

echo "🧪 验证修复结果..."

SERVER_IP="23.95.193.155"
FRONTEND_PORT="3002"
BACKEND_PORT="8081"

# 验证函数
test_endpoint() {
    local url=$1
    local description=$2
    local expected_pattern=$3
    
    echo "🔍 测试: $description"
    response=$(curl -s -w "%{http_code}" -o /tmp/response.txt "$url")
    http_code=${response: -3}
    content=$(cat /tmp/response.txt)
    
    if [ "$http_code" = "200" ]; then
        if [ -n "$expected_pattern" ]; then
            if echo "$content" | grep -q "$expected_pattern"; then
                echo "✅ $description - 成功"
                return 0
            else
                echo "⚠️ $description - HTTP 200但内容不符合预期"
                echo "   内容: ${content:0:100}..."
                return 1
            fi
        else
            echo "✅ $description - 成功"
            return 0
        fi
    else
        echo "❌ $description - 失败 (HTTP $http_code)"
        echo "   响应: ${content:0:100}..."
        return 1
    fi
}

# 测试登录功能
test_login() {
    local url=$1
    local description=$2
    
    echo "🔍 测试: $description"
    response=$(curl -s -X POST "$url" \
        -H "Content-Type: application/json" \
        -d '{"usernameOrEmail":"admin","password":"admin123"}')
    
    success=$(echo "$response" | jq -r '.success' 2>/dev/null || echo "parse_error")
    message=$(echo "$response" | jq -r '.message' 2>/dev/null || echo "parse_error")
    
    if [ "$success" = "true" ]; then
        echo "✅ $description - 登录成功！"
        token=$(echo "$response" | jq -r '.data.accessToken' 2>/dev/null)
        echo "   获得Token: ${token:0:50}..."
        return 0
    else
        echo "❌ $description - 登录失败"
        echo "   消息: $message"
        echo "   完整响应: ${response:0:200}..."
        return 1
    fi
}

echo "=================================================="
echo "🎯 开始验证修复结果"
echo "=================================================="

# 1. 测试基础连通性
echo ""
echo "📋 1. 基础连通性测试"
test_endpoint "http://$SERVER_IP:$BACKEND_PORT/api/health" "后端健康检查"
test_endpoint "http://$SERVER_IP:$FRONTEND_PORT" "前端页面访问" "WeeklyReport"
test_endpoint "http://$SERVER_IP:$FRONTEND_PORT/health" "前端健康检查"

# 2. 测试登录功能
echo ""
echo "📋 2. 登录功能测试"
test_login "http://$SERVER_IP:$BACKEND_PORT/api/auth/login" "后端直接登录"
test_login "http://$SERVER_IP:$FRONTEND_PORT/api/auth/login" "前端代理登录"

# 3. 测试nginx代理配置
echo ""
echo "📋 3. nginx代理配置测试"
echo "🔍 检查代理路径..."

# 测试代理是否正确转发
DIRECT_HEALTH=$(curl -s http://$SERVER_IP:$BACKEND_PORT/api/health | jq -r '.data.service' 2>/dev/null || echo "failed")
PROXY_HEALTH=$(curl -s http://$SERVER_IP:$FRONTEND_PORT/api/health | jq -r '.data.service' 2>/dev/null || echo "failed")

if [ "$DIRECT_HEALTH" = "$PROXY_HEALTH" ] && [ "$DIRECT_HEALTH" != "failed" ]; then
    echo "✅ nginx代理配置正确"
    echo "   服务名: $DIRECT_HEALTH"
else
    echo "❌ nginx代理配置可能有问题"
    echo "   直接访问: $DIRECT_HEALTH"
    echo "   代理访问: $PROXY_HEALTH"
fi

# 4. 容器状态检查
echo ""
echo "📋 4. 容器状态检查"
echo "🔍 检查Docker容器..."

if command -v docker >/dev/null 2>&1; then
    MYSQL_STATUS=$(docker ps --format "table {{.Names}}\t{{.Status}}" | grep mysql | awk '{print $2}' || echo "未找到")
    BACKEND_STATUS=$(docker ps --format "table {{.Names}}\t{{.Status}}" | grep backend | awk '{print $2}' || echo "未找到")
    FRONTEND_STATUS=$(docker ps --format "table {{.Names}}\t{{.Status}}" | grep -E "(frontend|nginx)" | head -1 | awk '{print $2}' || echo "未找到")
    
    echo "   MySQL容器: $MYSQL_STATUS"
    echo "   后端容器: $BACKEND_STATUS"  
    echo "   前端容器: $FRONTEND_STATUS"
    
    if echo "$MYSQL_STATUS$BACKEND_STATUS$FRONTEND_STATUS" | grep -q "Up"; then
        echo "✅ 关键容器正在运行"
    else
        echo "⚠️ 某些容器可能未运行"
    fi
else
    echo "⚠️ 无法检查Docker状态（权限或Docker未安装）"
fi

# 5. 生成总结报告
echo ""
echo "=================================================="
echo "📊 修复验证总结"
echo "=================================================="

# 计算成功率
TOTAL_TESTS=5
PASSED_TESTS=0

# 重新进行关键测试并计数
curl -s http://$SERVER_IP:$BACKEND_PORT/api/health >/dev/null && ((PASSED_TESTS++))
curl -s http://$SERVER_IP:$FRONTEND_PORT >/dev/null && ((PASSED_TESTS++))

# 登录测试
BACKEND_LOGIN=$(curl -s -X POST http://$SERVER_IP:$BACKEND_PORT/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"usernameOrEmail":"admin","password":"admin123"}' \
    | jq -r '.success' 2>/dev/null)
[ "$BACKEND_LOGIN" = "true" ] && ((PASSED_TESTS++))

FRONTEND_LOGIN=$(curl -s -X POST http://$SERVER_IP:$FRONTEND_PORT/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"usernameOrEmail":"admin","password":"admin123"}' \
    | jq -r '.success' 2>/dev/null)
[ "$FRONTEND_LOGIN" = "true" ] && ((PASSED_TESTS++))

# nginx代理测试
PROXY_WORKS=$(curl -s http://$SERVER_IP:$FRONTEND_PORT/api/health | jq -r '.data.service' 2>/dev/null)
[ "$PROXY_WORKS" != "null" ] && [ "$PROXY_WORKS" != "" ] && ((PASSED_TESTS++))

SUCCESS_RATE=$((PASSED_TESTS * 100 / TOTAL_TESTS))

echo "测试结果: $PASSED_TESTS/$TOTAL_TESTS 通过 ($SUCCESS_RATE%)"
echo ""

if [ $SUCCESS_RATE -ge 80 ]; then
    echo "🎉 修复成功！系统基本正常"
    echo ""
    echo "✅ 可以使用的功能:"
    [ "$BACKEND_LOGIN" = "true" ] && echo "   - 后端API登录: admin/admin123"
    [ "$FRONTEND_LOGIN" = "true" ] && echo "   - 前端页面登录: admin/admin123"
    echo ""
    echo "🌐 访问地址:"
    echo "   前端: http://$SERVER_IP:$FRONTEND_PORT"
    echo "   后端: http://$SERVER_IP:$BACKEND_PORT"
elif [ $SUCCESS_RATE -ge 50 ]; then
    echo "⚠️ 部分修复成功，仍有问题需要解决"
    echo ""
    echo "🔧 建议检查:"
    [ "$BACKEND_LOGIN" != "true" ] && echo "   - 后端数据库连接问题"
    [ "$FRONTEND_LOGIN" != "true" ] && echo "   - 前端nginx代理配置"
    echo "   - 查看容器日志: docker logs [container_name]"
else
    echo "❌ 修复失败，需要进一步调试"
    echo ""
    echo "🆘 建议操作:"
    echo "   1. 检查所有容器是否运行: docker ps"
    echo "   2. 查看容器日志: docker logs [container_name]"
    echo "   3. 重新运行修复脚本: ./fix-server-issues.sh"
fi

echo ""
echo "📝 详细故障排除请参考: troubleshoot-guide.md"

# 清理临时文件
rm -f /tmp/response.txt