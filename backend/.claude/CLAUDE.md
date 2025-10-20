# 后端 CLAUDE.md

> Think carefully and implement the most concise solution that changes as little code as possible.

## 后端项目信息
这是周报管理系统的后端部分
技术栈: 
    框架：Spring Boot 3.x
    数据库：MySQL 8.0
    ORM：JPA + Hibernate
    安全：Spring Security + JWT
    构建：Maven
    迁移：Flyway (已禁用，使用SQL脚本)

## ⚠️ **CRITICAL: 后端部署流程严格遵循要求**

**任何涉及后端部署、CI/CD、服务器操作的任务必须严格按照以下流程执行:**

### 🔄 **强制后端部署流程 (三层文档架构)**
1. **部署前检查**: 必须先阅读以下文件了解完整后端部署流程:
   - 📋 `.claude/DEPLOY.md` - 后端已知问题和经验知识库
   - 🔄 `.claude/workflow/deployment-workflow.md` - 智能部署工作流程
   - 🔧 `.claude/deploy-helper.md` - 核心部署指导和命令模板
2. **智能错误处理**: 遇到任何后端部署错误时，必须：
   - 🏷️ 先对错误进行分类标签识别（构建/配置/环境/依赖/Docker/安全问题）
   - 🔍 在backend/DEPLOY.md中搜索相同或相似错误
   - ✅ 优先应用已知解决方案
   - 📝 如果已知方案失效，分析新原因并追加解决方案到.claude/DEPLOY.md
3. **后端专用工具使用要求**:
   - 📊 使用Playwright MCP监控Jenkins控制台日志（关注后端构建和Docker阶段）
   - 🔧 使用sshpass SSH协议查看后端容器日志（Spring Boot应用、MySQL连接状态）
   - 🚨 遵守30分钟超时和10次最大重试限制
4. **后端知识记录**: 任何新发现的后端问题和解决方案必须按格式记录到.claude/DEPLOY.md

### 🚫 **禁止的后端部署行为**
- ❌ 绕过.claude/DEPLOY.md中的已知问题检查
- ❌ 不记录新发现的后端问题和解决方案
- ❌ 超过重试限制后继续尝试
- ❌ 不使用指定的监控工具
- ❌ 忽略后端特有的错误分类（Maven构建、数据库连接、JWT配置等）

### 📋 **后端部署相关命令前置检查**
执行以下任何后端操作前，必须先检查.claude/DEPLOY.md:
- `mvn compile`、`mvn package`、`mvn clean install`
- `git push` (后端代码变更)
- `docker-compose` (后端/数据库容器操作)
- application.yml配置修改、数据库连接配置
- 后端API健康检查、数据库连接测试

### 🏷️ **后端错误分类要求**
必须按后端特定标签分类错误：
- 🏗️ **构建问题**: Maven/Gradle构建、Java编译失败
- ⚙️ **配置问题**: application.yml、环境变量错误
- 🌐 **环境问题**: 数据库连接、端口冲突问题
- 🔗 **依赖问题**: 数据库、外部服务连接问题
- 🐳 **Docker问题**: 后端镜像构建、容器运行问题
- 🔐 **安全问题**: JWT、认证、授权配置问题

## 后端项目模块
- **认证模块**: JWT令牌生成、验证、用户认证
- **项目管理模块**: 项目CRUD、审核流程、AI分析集成
- **任务管理模块**: 任务创建、分配、状态跟踪
- **周报管理模块**: 周报CRUD、审核工作流、数据统计
- **用户管理模块**: 用户CRUD、角色权限管理
- **AI分析模块**: DeepSeek集成、智能分析服务

## 后端开发约定
- **包结构**: com.weeklyreport.[controller|service|repository|entity|dto]
- **命名规范**: 
  - Controller: xxxController
  - Service: xxxService
  - Repository: xxxRepository  
  - Entity: 实体名称
  - DTO: xxxRequest/xxxResponse
- **异常处理**: 统一使用GlobalExceptionHandler
- **API文档**: 使用OpenAPI 3.0注解
- **安全配置**: 基于角色的访问控制(RBAC)

## 数据库管理
- **迁移方式**: 使用create-database-schema.sql手动迁移
- **Flyway状态**: 在Docker profile中已禁用
- **连接配置**: 
  - 开发环境: localhost:3307
  - 生产环境: 容器内mysql:3306，外部23.95.193.155:3309

## Testing

后端测试要求:
- 单元测试: Service层业务逻辑
- 集成测试: Repository层数据访问
- API测试: Controller层接口
- 构建验证: `mvn clean package`

## Code Style

遵循现有的后端代码模式:
- Spring Boot最佳实践
- RESTful API设计
- 分层架构(Controller-Service-Repository)
- Java编码规范