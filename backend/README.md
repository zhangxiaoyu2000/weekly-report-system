# 周报管理系统 - 后端服务

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1+-6DB33F?style=flat&logo=spring-boot)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17+-ED8B00?style=flat&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0+-4479A1?style=flat&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Docker](https://img.shields.io/badge/Docker-20.10+-2496ED?style=flat&logo=docker&logoColor=white)](https://www.docker.com/)

企业周报管理系统的后端服务，提供RESTful API支持项目管理、周报提交、AI分析和审批流程。

## ✨ 核心功能

### 🔐 认证授权
- JWT令牌认证
- 角色权限控制（主管/管理员/超级管理员）
- 安全的密码加密存储

### 📊 项目管理
- 项目生命周期管理
- 项目成员管理
- 阶段性任务跟踪
- AI智能项目分析

### 📝 周报系统
- 结构化周报提交
- 本周汇报 + 下周规划
- 日常任务与发展性任务分类
- 实际结果与差异分析

### 🤖 AI分析
- 项目可行性AI分析
- 周报内容智能分析
- 风险识别和建议生成
- 多AI服务提供商支持

### ✅ 审批流程
- 多级审批工作流
- AI分析 → 管理员审核 → 超级管理员终审
- 审批历史记录
- 状态跟踪和通知

## 🚀 快速开始

### 环境要求
- Java 17+
- Docker & Docker Compose
- MySQL 8.0+

### 快速部署
```bash
# 克隆仓库
git clone <repository-url>
cd weekly-report-backend

# 启动服务
docker-compose up -d

# 验证部署
curl http://localhost:8080/api/health
```

### 本地开发
```bash
# 使用本地开发配置
docker-compose -f docker-compose.local.yml up -d

# 或者直接运行Spring Boot
./mvnw spring-boot:run --spring.profiles.active=local
```

## 📁 项目结构

```
backend/
├── src/main/java/com/weeklyreport/
│   ├── controller/          # REST控制器
│   ├── service/            # 业务逻辑层
│   ├── repository/         # 数据访问层
│   ├── entity/             # JPA实体类
│   ├── dto/               # 数据传输对象
│   ├── security/          # 安全配置
│   ├── config/            # 配置类
│   └── util/              # 工具类
├── src/main/resources/
│   ├── application.yml    # 应用配置
│   └── db/               # 数据库脚本
├── Dockerfile            # Docker构建文件
├── docker-compose.yml    # 生产环境配置
└── Jenkinsfile          # CI/CD流水线
```

## 🔌 API文档

### 认证端点
```http
POST /api/auth/login         # 用户登录
POST /api/auth/refresh       # 刷新令牌
POST /api/auth/logout        # 用户登出
```

### 项目管理
```http
GET    /api/projects         # 获取项目列表
POST   /api/projects         # 创建新项目
GET    /api/projects/{id}    # 获取项目详情
PUT    /api/projects/{id}    # 更新项目
DELETE /api/projects/{id}    # 删除项目
```

### 周报管理
```http
GET    /api/weekly-reports   # 获取周报列表
POST   /api/weekly-reports   # 提交周报
GET    /api/weekly-reports/{id} # 获取周报详情
PUT    /api/weekly-reports/{id} # 更新周报
```

### AI分析
```http
POST   /api/ai/analyze       # 触发AI分析
GET    /api/ai/results/{id}  # 获取分析结果
```

### 用户管理
```http
GET    /api/users            # 获取用户列表
POST   /api/users            # 创建用户
PUT    /api/users/{id}       # 更新用户信息
```

## ⚙️ 配置说明

### 环境变量
| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| `DB_HOST` | 数据库主机 | `mysql` |
| `DB_PORT` | 数据库端口 | `3306` |
| `DB_NAME` | 数据库名称 | `weekly_report_system` |
| `DB_USERNAME` | 数据库用户名 | `root` |
| `DB_PASSWORD` | 数据库密码 | `rootpass123` |
| `JWT_SECRET` | JWT签名密钥 | 必须设置 |
| `CORS_ALLOWED_ORIGINS` | CORS允许源 | `http://localhost:3000` |

### 应用配置文件
```yaml
# application.yml
spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:default}
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:weekly_report_system}
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:rootpass123}
```

## 🧪 测试

### 单元测试
```bash
./mvnw test
```

### 集成测试
```bash
./mvnw integration-test
```

### API测试
```bash
# 使用curl测试
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```

## 📦 部署

### Docker部署
```bash
# 构建镜像
docker build -t weekly-report-backend .

# 运行容器
docker run -d -p 8080:8080 \
  -e DB_HOST=mysql \
  -e JWT_SECRET=your-secret-key \
  weekly-report-backend
```

### Jenkins CI/CD
1. 配置Jenkins项目
2. 设置Git仓库
3. 配置环境变量
4. 运行构建流水线

详细部署指南请参考 [DEPLOY.md](./DEPLOY.md)

## 🔧 开发指南

### 添加新API端点
1. 在相应的Controller中添加方法
2. 实现Service层业务逻辑
3. 创建或更新DTO类
4. 添加单元测试
5. 更新API文档

### 数据库迁移
```sql
-- 在src/main/resources/db/migration/目录下创建新的迁移文件
-- 文件名格式: V{version}__description.sql
-- 例如: V2__Add_User_Profile_Table.sql
```

### AI服务集成
1. 实现`AIServiceProvider`接口
2. 在`AIServiceFactory`中注册新服务
3. 配置服务参数
4. 添加服务测试

## 📊 监控

### 健康检查
- 端点: `GET /api/health`
- 返回: 应用和数据库状态

### 性能指标
- JVM内存使用
- 数据库连接池状态
- API响应时间
- 错误率统计

### 日志配置
```yaml
# 日志配置
logging:
  level:
    com.weeklyreport: INFO
    org.springframework.security: DEBUG
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

