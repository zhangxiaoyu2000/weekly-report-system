# 后端部署经验知识库

> 📚 **文档关系**:
> - 本文档: 已解决问题和经验
> - 智能部署工作流: `.claude/workflow/deployment-workflow.md`
> - 部署助手指导: `.claude/deploy-helper.md`

## 后端部署核心信息

### 部署配置
- **端口**: 8082:8080 (内部Spring Boot运行在8080端口，外部映射到8082)
- **技术栈**: Spring Boot + MySQL + Flyway + JWT
- **容器**: weekly-report-backend

### 测试服务器信息
- **服务器**: 23.95.193.155
- **SSH用户**: root / To1YHvWPvyX157jf38
- **Jenkins**: http://23.95.193.155:12088/
- **后端访问**: http://23.95.193.155:8082
- **MySQL端口**: 3309:3306

## 已知问题和解决方案

### 问题1: JWT密钥长度不足导致认证失败 (已解决 ✅)

**现象**:
- 后端健康检查正常: `GET /api/health` 返回200 OK
- 登录API失败: `POST /api/auth/login` 返回500内部服务器错误
- 错误信息: "Login failed due to server error"
- 日志显示: "The specified key byte array is 192 bits which is not secure enough for any JWT HMAC-SHA algorithm"

**根本原因**:
Docker compose配置中的JWT_SECRET密钥"mySecretKeyForProduction"只有192位，不满足HMAC-SHA算法要求的最少256位

**解决方案** (2025-10-14):
1. **修复docker-compose.yml中的JWT密钥**:
   ```yaml
   # 错误配置 (192位，不安全)
   JWT_SECRET: mySecretKeyForProduction

   # 正确配置 (256+位，安全)
   JWT_SECRET: MyVerySecureWeeklyReportJwtSigningKeyForHS512AlgorithmMustBe512BitsOrGreater2024!@#$%^&*()_+=
   ```

2. **验证修复**:
   ```bash
   # 测试认证API
   curl -X POST http://23.95.193.155:8082/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"usernameOrEmail": "admin1", "password": "Admin123@"}'

   # 成功响应示例
   {"success":true,"message":"Login successful","data":{"accessToken":"eyJ...","user":{"username":"admin1","role":"ADMIN"}}}
   ```

**部署状态**: ✅ 已成功修复并验证
- ✅ JWT令牌正确生成
- ✅ 用户认证完全正常
- ✅ 管理员账户admin1登录成功
- ✅ 角色权限系统工作正常

**分类**: 🔐 安全问题

---

### 问题2: 用户表role字段类型不匹配 (已解决 ✅)

**现象**:
Spring Boot应用启动失败，错误信息："Data truncated for column 'role' at row 1"

**根本原因**:
数据库中role字段为ENUM类型，但应用代码期望VARCHAR类型

**解决方案** (2025-10-12):
```bash
# 修改role字段类型为VARCHAR
docker exec weekly-report-mysql mysql -u root -prootpass123 qr_auth_dev \
  -e 'ALTER TABLE users MODIFY COLUMN role VARCHAR(50) NOT NULL;'
```

**分类**: 🌐 环境问题

---

### 问题3: Jenkins容器缺少docker-compose (已解决 ✅)

**现象**:
Jenkins构建失败，错误信息："docker-compose: not found"

**根本原因**:
Jenkins容器中没有安装docker-compose工具

**解决方案** (2025-10-12):
```bash
# 在Jenkins容器中安装docker-compose
docker exec -u root jenkins-container curl -L \
  "https://github.com/docker/compose/releases/download/v2.29.1/docker-compose-linux-x86_64" \
  -o /usr/local/bin/docker-compose

docker exec -u root jenkins-container chmod +x /usr/local/bin/docker-compose
```

**分类**: 🐳 Docker问题

---

### 问题4: 数据库迁移脚本未执行 (已解决 ✅)

**现象**:
Spring Boot应用启动失败，错误信息："Schema-validation: missing table [ai_analysis_results]"

**根本原因**:
MySQL init脚本只在首次创建数据库时执行，但数据库已存在且为空

**解决方案** (2025-10-11):
```bash
# 手动执行数据库迁移脚本
docker exec weekly-report-mysql mysql -u root -prootpass123 qr_auth_dev \
  < /docker-entrypoint-initdb.d/V1__Initial_Schema.sql

docker exec weekly-report-mysql mysql -u root -prootpass123 qr_auth_dev \
  < /docker-entrypoint-initdb.d/V3__Add_AI_Analysis_Tables.sql
```

**分类**: 🔗 依赖问题

---

## 后端配置要点

### 数据库配置
- **数据库**: weekly_report_system
- **MySQL端口**: 3309:3306
- **容器**: weekly-report-mysql
- **用户**: root / rootpass123

### Spring Boot配置
- **Profile**: Docker (spring.profiles.active=docker)
- **端口**: 8080 (容器内) → 8082 (外部)
- **JWT密钥**: 512位安全密钥
- **Flyway**: 在Docker profile中禁用

### CORS配置
```yaml
cors:
  allowed-origins: http://localhost:3000,http://localhost:3002,http://localhost:3005,http://localhost:3006,http://localhost:3007,http://localhost:3008,http://localhost:3009,http://23.95.193.155:3003
  allowed-methods: GET,POST,PUT,DELETE,OPTIONS
  allowed-headers: Content-Type,Authorization
  allow-credentials: true
```

## 后端部署最佳实践

### 健康检查
```bash
# 后端服务健康检查
curl -I http://23.95.193.155:8082/api/health
# 期望: HTTP 200 OK

# 登录API测试
curl -X POST http://23.95.193.155:8082/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail": "admin1", "password": "Admin123@"}'
# 期望: {"success":true,"message":"Login successful",...}

# 数据库连接测试
curl -X GET http://23.95.193.155:8082/api/auth/verify \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
# 期望: HTTP 200 OK
```

### 数据库管理
```bash
# 连接到MySQL容器
docker exec -it weekly-report-mysql mysql -u root -prootpass123 weekly_report_system

# 检查数据库表
SHOW TABLES;

# 检查用户数据
SELECT username, role, status FROM users;

# 检查Flyway迁移状态
SELECT * FROM flyway_schema_history ORDER BY installed_on DESC LIMIT 5;
```

### SSH调试命令
```bash
# SSH登录到服务器
sshpass -p 'To1YHvWPvyX157jf38' ssh -o StrictHostKeyChecking=no root@23.95.193.155

# 查看后端容器状态
docker ps | grep backend

# 查看后端容器日志
docker logs weekly-report-backend

# 重启后端服务
docker restart weekly-report-backend

# 查看MySQL容器状态
docker ps | grep mysql
docker logs weekly-report-mysql
```

## 故障排查

### 常见问题
1. **容器启动失败**: 检查application.yml配置，特别是数据库连接和JWT密钥
2. **数据库连接失败**: 确认MySQL容器运行正常，检查连接字符串和端口
3. **认证失败**: 验证JWT密钥长度和算法配置
4. **API调用失败**: 检查CORS配置，确保包含前端域名

### 日志分析
```bash
# 查看详细的Spring Boot启动日志
docker logs weekly-report-backend | grep -i error

# 查看JWT相关日志
docker logs weekly-report-backend | grep -i jwt

# 查看数据库连接日志
docker logs weekly-report-backend | grep -i connection
```

### 应急恢复
```bash
# 重新部署数据库
docker-compose down mysql
docker-compose up -d mysql

# 重建后端服务
docker-compose build --no-cache backend
docker-compose up -d backend

# 完整重启
docker-compose down
docker-compose up -d
```

## 📝 最佳实践

### 部署流程
1. **测试环境验证**: 先在测试环境部署验证
2. **数据备份**: 生产部署前备份数据
3. **分步部署**: 数据库 → 后端 → 验证
4. **健康检查**: 每个步骤后检查服务状态
5. **回滚准备**: 保持上一版本可快速回滚

### 安全配置
1. **数据库密码**: 使用强密码
2. **JWT密钥**: 定期轮换密钥
3. **网络隔离**: 使用Docker网络隔离
4. **访问控制**: 限制数据库访问权限
5. **日志审计**: 记录关键操作日志

### 性能优化
1. **数据库连接池**: 合理配置连接池大小
2. **JVM参数**: 根据服务器资源调整
3. **缓存策略**: 使用Redis缓存热点数据
4. **日志级别**: 生产环境使用WARN级别
5. **资源监控**: 监控CPU、内存、磁盘使用

## 部署成功状态 ✅

- ✅ 后端服务正常运行在端口8082
- ✅ 后端API完全可访问: http://23.95.193.155:8082
- ✅ MySQL数据库连接正常，运行在端口3309
- ✅ Flyway迁移问题完全解决，数据库结构正确
- ✅ JWT认证系统工作正常，密钥配置安全
- ✅ CORS配置正确，支持生产环境前端域名
- ✅ 用户认证和权限系统完全可用
- ✅ 所有核心API端点正常工作
