#!/bin/bash

echo "🚀 启动后端服务（Docker版本）..."

# 停止可能运行的后端容器
docker stop weekly-report-backend-final 2>/dev/null || true
docker rm weekly-report-backend-final 2>/dev/null || true

# 使用Docker运行后端JAR
echo "📋 启动后端容器..."
docker run -d \
  --name weekly-report-backend-final \
  -p 8081:8081 \
  -v "/Volumes/project/my-project/backend/target/weekly-report-backend-0.0.1-SNAPSHOT.jar:/app.jar" \
  -e SPRING_PROFILES_ACTIVE=docker \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://23.95.193.155:3308/qr_auth_dev \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=rootpass123 \
  -e SERVER_PORT=8081 \
  openjdk:17-jdk-slim \
  java -jar /app.jar --server.port=8081

if [ $? -eq 0 ]; then
    echo "✅ 后端容器已启动"
    
    # 等待启动
    echo "⏳ 等待后端服务启动..."
    sleep 30
    
    # 测试健康检查
    echo "🧪 测试后端健康..."
    curl -s http://23.95.193.155:8081/api/health
    echo ""
    
    # 测试登录
    echo "🧪 测试登录..."
    curl -s -X POST http://23.95.193.155:8081/api/auth/login \
      -H "Content-Type: application/json" \
      -d '{"usernameOrEmail":"admin","password":"admin123"}'
    echo ""
    
else
    echo "❌ 后端容器启动失败"
fi