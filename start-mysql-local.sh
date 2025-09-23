#!/bin/bash

# 周报管理系统 - 本地MySQL数据库启动脚本

set -e  # 遇到错误时退出

echo "=== 周报管理系统 - 本地MySQL数据库启动 ==="
echo "开始时间: $(date)"
echo

# 检查Docker是否运行
if ! docker info > /dev/null 2>&1; then
    echo "❌ 错误: Docker未运行，请先启动Docker"
    exit 1
fi

# 检查必要文件是否存在
if [ ! -f "docker-compose-mysql-local.yml" ]; then
    echo "❌ 错误: docker-compose-mysql-local.yml 文件不存在"
    exit 1
fi

if [ ! -f "create-database-schema.sql" ]; then
    echo "❌ 错误: create-database-schema.sql 文件不存在"
    exit 1
fi

# 停止并清理现有容器（如果存在）
echo "🧹 清理现有容器..."
docker-compose -f docker-compose-mysql-local.yml down --volumes --remove-orphans 2>/dev/null || true

# 启动MySQL数据库
echo "🚀 启动MySQL数据库容器..."
docker-compose -f docker-compose-mysql-local.yml up -d

# 等待MySQL启动完成
echo "⏳ 等待MySQL数据库启动完成..."
timeout=120
counter=0
while [ $counter -lt $timeout ]; do
    if docker-compose -f docker-compose-mysql-local.yml exec -T mysql-local mysqladmin ping -h localhost -u root -prootpass123 --silent > /dev/null 2>&1; then
        echo "✅ MySQL数据库启动成功！"
        break
    fi
    
    counter=$((counter + 5))
    if [ $counter -ge $timeout ]; then
        echo "❌ 错误: MySQL启动超时"
        echo "📋 容器日志:"
        docker-compose -f docker-compose-mysql-local.yml logs mysql-local
        exit 1
    fi
    
    echo "   等待中... ($counter/$timeout 秒)"
    sleep 5
done

# 验证数据库和表结构
echo "🔍 验证数据库结构..."
table_count=$(docker-compose -f docker-compose-mysql-local.yml exec -T mysql-local mysql -u root -prootpass123 -e "USE weekly_report_system; SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='weekly_report_system';" 2>/dev/null | tail -n +2)

if [ "$table_count" -ge 8 ]; then
    echo "✅ 数据库结构创建成功 (共 $table_count 张表)"
    
    # 显示创建的表
    echo "📋 数据库表列表:"
    docker-compose -f docker-compose-mysql-local.yml exec -T mysql-local mysql -u root -prootpass123 -e "USE weekly_report_system; SHOW TABLES;" 2>/dev/null | tail -n +2
    
    # 显示默认用户
    echo "👥 默认用户账户:"
    docker-compose -f docker-compose-mysql-local.yml exec -T mysql-local mysql -u root -prootpass123 -e "USE weekly_report_system; SELECT username, email, role, status FROM users;" 2>/dev/null | tail -n +2
else
    echo "❌ 错误: 数据库结构创建失败 (只有 $table_count 张表)"
    echo "📋 容器日志:"
    docker-compose -f docker-compose-mysql-local.yml logs mysql-local
    exit 1
fi

echo
echo "🎉 MySQL数据库部署完成！"
echo "📊 连接信息:"
echo "   主机: localhost"
echo "   端口: 3306"
echo "   数据库: weekly_report_system"
echo "   Root用户: root / rootpass123"
echo "   应用用户: weekly_user / weekly123"
echo
echo "🛠️ 管理命令:"
echo "   停止数据库: docker-compose -f docker-compose-mysql-local.yml down"
echo "   查看日志: docker-compose -f docker-compose-mysql-local.yml logs mysql-local"
echo "   进入数据库: docker-compose -f docker-compose-mysql-local.yml exec mysql-local mysql -u root -p"
echo "   重新部署: ./start-mysql-local.sh"
echo
echo "✅ 部署完成时间: $(date)"