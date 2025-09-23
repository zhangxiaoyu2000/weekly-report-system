# 测试服务器部署指南

## 当前问题分析
- nginx代理已修复 ✅
- 正确登录凭据：admin/admin123 ✅  
- 问题：远程后端无法连接数据库，导致500错误

## 部署方案

### 方案1：完整重新部署（推荐）
在测试服务器(23.95.193.155)上重新部署完整系统：

1. **上传代码到服务器**
```bash
# 将整个项目目录上传到服务器
scp -r /path/to/my-project user@23.95.193.155:/home/user/weekly-report
```

2. **在服务器上执行部署**
```bash
ssh user@23.95.193.155
cd /home/user/weekly-report

# 停止现有服务
docker-compose down

# 清理旧容器和数据（可选）
docker system prune -f
docker volume prune -f

# 构建并启动完整系统
docker-compose up -d --build

# 检查服务状态
docker-compose ps
docker-compose logs -f
```

### 方案2：修复现有后端数据库连接
如果要保留现有后端服务，需要：

1. **检查数据库配置**
```bash
# 在服务器上检查后端配置
docker exec weekly-report-backend env | grep -E "DB_|MYSQL_"
```

2. **启动数据库服务**
```bash
# 确保MySQL数据库运行
docker-compose up -d mysql
```

3. **重启后端服务**
```bash
# 重启后端以连接数据库
docker-compose restart backend
```

## 测试部署结果

部署完成后，测试登录功能：
```bash
# 测试API健康状态
curl http://23.95.193.155:8081/api/health

# 测试登录（使用正确凭据）
curl -X POST http://23.95.193.155:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"admin","password":"admin123"}'

# 测试前端访问
curl -I http://23.95.193.155:3001
```

期望结果：
- 健康检查返回200 OK
- 登录返回成功响应和JWT token（不是500错误）
- 前端可正常访问

## 登录凭据
- 用户名: admin
- 密码: admin123
- 预期行为: 登录成功，获得JWT token

## 端口配置
- 前端: 3001
- 后端: 8081  
- 数据库: 3308