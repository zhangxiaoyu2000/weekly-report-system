# 周报管理系统 - 测试服务器部署指南

## 📋 部署概述

本指南将帮助你将完整的周报管理系统（前端 + 后端 + 数据库）部署到测试服务器 `23.95.193.155`。

### 🎯 系统架构
- **前端**: Vue.js + Nginx (端口 3001)
- **后端**: Spring Boot (端口 8081) 
- **数据库**: MySQL 8.0 (端口 3308)
- **通信**: nginx反向代理 → 后端API

## 🚀 快速部署

### 1. 上传代码到服务器
```bash
# 将项目目录上传到服务器
scp -r my-project user@23.95.193.155:/home/user/weekly-report
```

### 2. 登录服务器并部署
```bash
ssh user@23.95.193.155
cd /home/user/weekly-report

# 执行自动部署脚本
./deploy.sh
```

### 3. 验证部署
```bash
# 运行测试脚本
./test-deployment.sh
```

## 📁 部署文件说明

### 核心文件
- `docker-compose.prod.yml` - 生产环境Docker编排配置
- `Dockerfile.frontend` - 前端镜像构建文件（已修复nginx代理）
- `Dockerfile.backend` - 后端镜像构建文件
- `deploy.sh` - 自动部署脚本
- `test-deployment.sh` - 部署验证脚本

### 配置要点
- **nginx代理**: 已修复指向正确的后端地址 `http://23.95.193.155:8081/api/`
- **数据库连接**: 后端正确连接到MySQL容器
- **JWT密钥**: 使用生产级安全密钥
- **健康检查**: 所有服务都配置了健康检查

## 🔧 手动部署步骤

如果自动脚本失败，可以手动执行：

### 1. 停止现有服务
```bash
docker-compose down
```

### 2. 构建并启动
```bash
docker-compose -f docker-compose.prod.yml up -d --build
```

### 3. 检查状态
```bash
# 查看服务状态
docker-compose -f docker-compose.prod.yml ps

# 查看日志
docker-compose -f docker-compose.prod.yml logs -f
```

## 🧪 测试登录功能

### 方式1: 命令行测试
```bash
curl -X POST http://23.95.193.155:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"admin","password":"admin123"}'
```

### 方式2: 浏览器测试
1. 访问: http://23.95.193.155:3001
2. 使用凭据登录:
   - 用户名: `admin`
   - 密码: `admin123`

### 期望结果
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "refreshToken": "...",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "userId": 1,
    "username": "admin",
    "role": "ADMIN"
  }
}
```

## 🛠 故障排除

### 问题1: 登录返回500错误
**原因**: 后端无法连接数据库  
**解决**: 
```bash
# 检查MySQL容器状态
docker exec weekly-report-mysql mysqladmin ping -h localhost

# 重启后端服务
docker-compose -f docker-compose.prod.yml restart backend
```

### 问题2: 前端无法访问
**原因**: nginx配置或端口问题  
**解决**:
```bash
# 检查nginx配置
docker exec weekly-report-frontend cat /etc/nginx/conf.d/default.conf

# 检查端口占用
netstat -tulpn | grep :3001
```

### 问题3: 构建失败
**原因**: 依赖下载或网络问题  
**解决**:
```bash
# 清理并重建
docker system prune -f
docker-compose -f docker-compose.prod.yml build --no-cache
```

## 📊 服务访问信息

部署成功后的访问地址：

| 服务 | 地址 | 说明 |
|------|------|------|
| 前端 | http://23.95.193.155:3001 | 用户界面 |
| 后端API | http://23.95.193.155:8081 | REST API |
| 数据库 | 23.95.193.155:3308 | MySQL连接 |

## 🔐 默认账户

| 用户名 | 密码 | 角色 | 说明 |
|--------|------|------|------|
| admin | admin123 | ADMIN | 系统管理员 |
| superadmin | admin123 | SUPER_ADMIN | 超级管理员（如果存在） |

## 📝 维护命令

```bash
# 查看所有容器状态
docker-compose -f docker-compose.prod.yml ps

# 查看特定服务日志
docker-compose -f docker-compose.prod.yml logs backend
docker-compose -f docker-compose.prod.yml logs frontend
docker-compose -f docker-compose.prod.yml logs mysql

# 重启特定服务
docker-compose -f docker-compose.prod.yml restart backend

# 停止所有服务
docker-compose -f docker-compose.prod.yml down

# 完全清理（谨慎使用）
docker-compose -f docker-compose.prod.yml down -v
docker system prune -f
```

## ✅ 部署验证清单

- [ ] MySQL容器启动并健康
- [ ] 后端容器启动并通过健康检查
- [ ] 前端容器启动并可访问
- [ ] admin用户可以成功登录
- [ ] nginx代理正确转发API请求
- [ ] JWT token正确生成

## 🎉 部署完成

如果所有测试都通过，恭喜！你的周报管理系统已成功部署到测试服务器。

**访问地址**: http://23.95.193.155:3001  
**登录凭据**: admin / admin123