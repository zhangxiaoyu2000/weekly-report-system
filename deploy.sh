#!/bin/bash

# 周报管理系统 - 测试服务器部署脚本
# 使用方法: ./deploy.sh

set -e  # 遇到错误立即退出

echo "🚀 开始部署周报管理系统到测试服务器..."

# 1. 停止现有服务
echo "📋 1. 停止现有服务..."
docker-compose down 2>/dev/null || true

# 2. 清理旧资源（可选，谨慎使用）
echo "📋 2. 清理Docker资源..."
# docker system prune -f  # 取消注释以清理所有未使用的资源
docker container prune -f

# 3. 构建并启动完整系统
echo "📋 3. 构建并启动完整系统..."
docker-compose -f docker-compose.prod.yml up -d --build

# 4. 等待服务启动
echo "📋 4. 等待服务启动中..."
sleep 30

# 5. 检查服务状态
echo "📋 5. 检查服务状态..."
docker-compose -f docker-compose.prod.yml ps

# 6. 检查服务健康状态
echo "📋 6. 健康检查..."

echo "⏳ 等待MySQL启动..."
timeout 60 bash -c 'until docker exec weekly-report-mysql mysqladmin ping -h localhost --silent; do sleep 2; done'
echo "✅ MySQL 已启动"

echo "⏳ 等待后端服务启动..."
timeout 120 bash -c 'until curl -f http://localhost:8081/api/health >/dev/null 2>&1; do sleep 5; done'
echo "✅ 后端服务已启动"

echo "⏳ 等待前端服务启动..."
timeout 60 bash -c 'until curl -f http://localhost:3001/health >/dev/null 2>&1; do sleep 5; done'
echo "✅ 前端服务已启动"

# 7. 测试登录功能
echo "📋 7. 测试登录功能..."
echo "测试admin用户登录..."
LOGIN_RESULT=$(curl -s -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"admin","password":"admin123"}' \
  | jq -r '.success' 2>/dev/null || echo "failed")

if [ "$LOGIN_RESULT" = "true" ]; then
    echo "✅ 登录测试成功！"
else
    echo "❌ 登录测试失败，检查日志..."
    docker-compose -f docker-compose.prod.yml logs backend | tail -20
fi

# 8. 显示访问信息
echo ""
echo "🎉 部署完成！"
echo ""
echo "📊 服务访问地址:"
echo "   前端: http://$(hostname -I | awk '{print $1}'):3001"
echo "   后端API: http://$(hostname -I | awk '{print $1}'):8081"
echo "   数据库: $(hostname -I | awk '{print $1}'):3308"
echo ""
echo "🔐 登录凭据:"
echo "   用户名: admin"
echo "   密码: admin123"
echo ""
echo "📝 查看日志: docker-compose -f docker-compose.prod.yml logs -f"
echo "🛑 停止服务: docker-compose -f docker-compose.prod.yml down"