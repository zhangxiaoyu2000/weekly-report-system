#!/bin/bash

# 部署到测试服务器脚本
# 服务器: 23.95.193.155

set -e  # 遇到错误立即退出

SERVER_IP="23.95.193.155"
SERVER_USER="root"
SERVER_PASSWORD="To1YHvWPvyX157jf38"

echo "🚀 开始部署到测试服务器 $SERVER_IP"

# 1. 检查本地镜像是否存在
echo "📦 检查本地镜像..."
if ! docker images | grep -q "my-project-frontend"; then
    echo "❌ 前端镜像不存在"
    exit 1
fi

if ! docker images | grep -q "my-project-backend"; then
    echo "❌ 后端镜像不存在，请先构建完成"
    exit 1
fi

echo "✅ 本地镜像检查通过"

# 2. 保存镜像为tar文件
echo "💾 保存镜像为tar文件..."
docker save my-project-frontend:latest | gzip > weekly-report-frontend.tar.gz
docker save my-project-backend:latest | gzip > weekly-report-backend.tar.gz

echo "✅ 镜像保存完成"

# 3. 复制文件到测试服务器
echo "📤 上传文件到测试服务器..."

# 上传镜像文件
sshpass -p "$SERVER_PASSWORD" scp weekly-report-frontend.tar.gz $SERVER_USER@$SERVER_IP:/tmp/
sshpass -p "$SERVER_PASSWORD" scp weekly-report-backend.tar.gz $SERVER_USER@$SERVER_IP:/tmp/

# 上传配置文件
sshpass -p "$SERVER_PASSWORD" scp docker-compose.prod.yml $SERVER_USER@$SERVER_IP:/tmp/
sshpass -p "$SERVER_PASSWORD" scp create-database-schema.sql $SERVER_USER@$SERVER_IP:/tmp/

echo "✅ 文件上传完成"

# 4. 在服务器上加载镜像并部署
echo "🔧 在服务器上部署..."

sshpass -p "$SERVER_PASSWORD" ssh $SERVER_USER@$SERVER_IP << 'EOF'
set -e

echo "📋 在测试服务器上执行部署..."

# 停止现有服务（如果存在）
if docker ps | grep -q "weekly-report"; then
    echo "🛑 停止现有服务..."
    docker stop $(docker ps | grep "weekly-report" | awk '{print $1}') || true
    docker rm $(docker ps -a | grep "weekly-report" | awk '{print $1}') || true
fi

# 加载镜像
echo "📦 加载Docker镜像..."
cd /tmp
gunzip -c weekly-report-frontend.tar.gz | docker load
gunzip -c weekly-report-backend.tar.gz | docker load

# 创建工作目录
mkdir -p /opt/weekly-report
cp docker-compose.prod.yml /opt/weekly-report/docker-compose.yml
cp create-database-schema.sql /opt/weekly-report/

cd /opt/weekly-report

# 启动服务
echo "🚀 启动服务..."
docker-compose up -d

# 等待服务启动
echo "⏳ 等待服务启动..."
sleep 30

# 检查服务状态
echo "🏥 检查服务状态..."
docker ps | grep weekly-report

# 清理临时文件
rm -f /tmp/weekly-report-*.tar.gz /tmp/docker-compose.prod.yml /tmp/create-database-schema.sql

echo "✅ 测试服务器部署完成"
EOF

# 5. 清理本地临时文件
echo "🧹 清理本地临时文件..."
rm -f weekly-report-frontend.tar.gz weekly-report-backend.tar.gz

# 6. 测试部署结果
echo "🔍 测试部署结果..."
sleep 10

# 测试前端
if curl -s --connect-timeout 10 http://$SERVER_IP:3003 > /dev/null; then
    echo "✅ 前端服务正常 - http://$SERVER_IP:3003"
else
    echo "❌ 前端服务异常"
fi

# 测试后端
if curl -s --connect-timeout 10 http://$SERVER_IP:8082/api/health > /dev/null; then
    echo "✅ 后端服务正常 - http://$SERVER_IP:8082/api"
else
    echo "❌ 后端服务异常"
fi

echo ""
echo "🎉 部署完成！"
echo "🌐 访问地址:"
echo "  前端: http://$SERVER_IP:3003"
echo "  后端: http://$SERVER_IP:8082/api"
echo "  数据库: $SERVER_IP:3309"
echo ""
echo "📋 服务状态检查:"
sshpass -p "$SERVER_PASSWORD" ssh $SERVER_USER@$SERVER_IP "docker ps | grep weekly-report"