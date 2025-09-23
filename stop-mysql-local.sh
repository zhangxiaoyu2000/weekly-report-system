#!/bin/bash

# 周报管理系统 - 本地MySQL数据库停止脚本

set -e

echo "=== 周报管理系统 - 停止本地MySQL数据库 ==="
echo "时间: $(date)"
echo

# 检查Docker是否运行
if ! docker info > /dev/null 2>&1; then
    echo "❌ 错误: Docker未运行"
    exit 1
fi

# 停止MySQL容器
echo "🛑 停止MySQL数据库容器..."
if docker-compose -f docker-compose-mysql-local.yml down; then
    echo "✅ MySQL数据库已停止"
else
    echo "❌ 停止MySQL时出现错误"
    exit 1
fi

echo "📋 数据保留: MySQL数据卷已保留，下次启动时数据仍然存在"
echo "🗑️  如需完全清理（包括数据），请使用: docker-compose -f docker-compose-mysql-local.yml down --volumes"
echo "✅ 停止完成时间: $(date)"