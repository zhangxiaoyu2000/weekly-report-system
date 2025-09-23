#!/bin/bash

# 周报管理系统 - MySQL连接测试脚本

set -e

echo "=== MySQL连接测试 ==="
echo "时间: $(date)"
echo

# 检查Docker是否运行
if ! docker info > /dev/null 2>&1; then
    echo "❌ 错误: Docker未运行"
    exit 1
fi

# 检查MySQL容器是否运行
if ! docker-compose -f docker-compose-mysql-local.yml ps mysql-local | grep -q "Up"; then
    echo "❌ MySQL容器未运行，请先运行: ./start-mysql-local.sh"
    exit 1
fi

echo "🔍 测试数据库连接..."

# 测试root用户连接
echo "📋 测试Root用户连接:"
if docker-compose -f docker-compose-mysql-local.yml exec -T mysql-local mysql -u root -prootpass123 -e "SELECT 'Root连接成功' as status;" 2>/dev/null; then
    echo "✅ Root用户连接成功"
else
    echo "❌ Root用户连接失败"
    exit 1
fi

# 测试应用用户连接
echo "📋 测试应用用户连接:"
if docker-compose -f docker-compose-mysql-local.yml exec -T mysql-local mysql -u weekly_user -pweekly123 -e "SELECT 'App用户连接成功' as status;" 2>/dev/null; then
    echo "✅ 应用用户连接成功"
else
    echo "❌ 应用用户连接失败"
fi

# 测试数据库访问
echo "📋 测试数据库访问:"
if docker-compose -f docker-compose-mysql-local.yml exec -T mysql-local mysql -u root -prootpass123 -e "USE weekly_report_system; SELECT COUNT(*) as table_count FROM information_schema.tables WHERE table_schema='weekly_report_system';" 2>/dev/null; then
    echo "✅ 数据库访问成功"
else
    echo "❌ 数据库访问失败"
    exit 1
fi

# 显示数据库状态
echo "📊 数据库状态信息:"
echo "   容器状态:"
docker-compose -f docker-compose-mysql-local.yml ps mysql-local

echo "   数据库表:"
docker-compose -f docker-compose-mysql-local.yml exec -T mysql-local mysql -u root -prootpass123 -e "USE weekly_report_system; SHOW TABLES;" 2>/dev/null | tail -n +2

echo "   用户数量:"
docker-compose -f docker-compose-mysql-local.yml exec -T mysql-local mysql -u root -prootpass123 -e "USE weekly_report_system; SELECT COUNT(*) as user_count FROM users;" 2>/dev/null | tail -n +2

echo "✅ 连接测试完成 - 时间: $(date)"