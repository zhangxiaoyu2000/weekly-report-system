# 部署命令范围说明 - 快速参考

> 📅 更新时间: 2025-10-20
> 📋 详细分析: 参见 `deploy_test命令部署范围分析_20251020.md`

---

## ⚡ 核心要点

### `/deploy test` 实际部署范围

```
✅ 部署: weekly-report-backend (Spring Boot容器)
🟢 检查: weekly-report-backend-mysql (不重启，仅健康检查)
🚫 不涉及: Redis、Nginx、其他任何服务
```

---

## 🎯 命令参数解析

### `/deploy` 命令设计

```bash
/deploy [backend|frontend|all] [--force]
```

| 参数 | 实际行为 |
|------|---------|
| `backend` | 部署后端 ✅ |
| `frontend` | 部署前端 ✅ |
| `all` | 部署后端+前端 ✅ |
| `test` | **默认行为** → 部署后端 ⚠️ |
| (其他任意值) | **默认行为** → 部署后端 ⚠️ |
| (未指定) | 部署后端 ✅ |

**⚠️ 重要**: `test`不是环境参数，而是部署目标参数，会被当作无效值处理，触发默认部署后端行为。

---

## 🔍 MySQL容器处理机制

### Docker Compose幂等性

**Jenkins执行的命令**:
```bash
docker-compose up -d mysql     # 检查并确保MySQL运行
docker-compose up --build -d backend  # 仅重建后端
```

**测试服务器场景**:
```
当前状态: weekly-report-backend-mysql 容器已运行
Jenkins执行: docker-compose up -d mysql
Docker检测: 容器已运行且配置未变
执行结果: ✅ 什么都不做，直接跳过
输出日志: weekly-report-backend-mysql is up-to-date
```

### 关键原理

1. **`docker-compose up -d mysql` 是幂等操作**
   - 容器运行中 → 不做任何操作
   - 容器已停止 → 启动容器
   - 容器不存在 → 创建并启动

2. **不会触发重启的原因**
   - ✅ 无 `--force-recreate` 强制重建标志
   - ✅ docker-compose.yml配置未变更
   - ✅ Docker Compose优先保持现有状态

3. **测试服务器数据完全安全**
   - ✅ MySQL容器不会重启
   - ✅ 数据库连接不会中断
   - ✅ 现有数据不受影响

---

## 📦 实际部署流程

### Jenkins Pipeline阶段

```
1️⃣ Environment Setup
   └─ docker-compose down (停止所有容器)

2️⃣ Database Preparation
   ├─ docker-compose up -d mysql (幂等检查)
   └─ 等待MySQL健康检查通过

3️⃣ Backend Build & Deploy
   ├─ docker-compose up --build -d backend (仅重建后端)
   └─ 等待后端服务健康检查通过

4️⃣ Health Check
   ├─ 测试 http://23.95.193.155:8082/api/health
   └─ 测试登录API

5️⃣ Integration Test
   └─ 验证后端API和数据库连接
```

### 涉及的服务和影响

| 服务 | 操作 | 影响 |
|------|------|------|
| **weekly-report-backend** | 🔴 重新构建+重启 | 服务中断30-60秒 |
| **weekly-report-backend-mysql** | 🟢 健康检查 | **无影响** |
| **Redis** | 🟢 不涉及 | **无影响** |
| **Nginx** | 🟢 不涉及 | **无影响** |
| **其他服务** | 🟢 不涉及 | **无影响** |

---

## 🌐 环境配置说明

### 部署目标 vs 运行环境

**部署目标**（通过`/deploy`参数控制）:
- 决定**哪个应用**被部署
- 可选值: `backend` | `frontend` | `all`

**运行环境**（通过环境变量控制）:
- 决定应用以**什么配置**运行
- 后端: `SPRING_PROFILES_ACTIVE=dev|test|docker|prod`
- 前端: `VITE_ENV=development|production`

### 部署到测试环境的正确方式

❌ **错误理解**:
```bash
/deploy test  # 这不会部署到测试环境！
```

✅ **正确方式**:
```bash
# 步骤1: 修改 docker-compose.yml 或测试服务器环境变量
environment:
  SPRING_PROFILES_ACTIVE: test  # 修改为test环境

# 步骤2: 执行部署
/deploy backend  # 或 /deploy（效果相同）
```

---

## 📋 常见问题

### Q1: `/deploy test` 会重启MySQL吗？

**答**: 不会。
- Jenkins执行`docker-compose up -d mysql`仅做健康检查
- 测试服务器MySQL已运行，Docker Compose检测到后直接跳过
- **数据库连接不会中断，数据完全安全**

### Q2: `/deploy test` 会部署Redis吗？

**答**: 不会。
- `/deploy test`等价于`/deploy backend`
- 仅部署`weekly-report-backend`容器
- Redis完全不涉及，使用测试服务器现有实例

### Q3: 如何指定部署到test环境？

**答**: 修改环境变量，而非命令参数
```bash
# 方式1: 修改docker-compose.yml
environment:
  SPRING_PROFILES_ACTIVE: test

# 方式2: 启动容器时指定
docker run -e SPRING_PROFILES_ACTIVE=test weekly-report-backend
```

### Q4: `/deploy`命令参数如何验证？

**答**: 当前缺少参数验证
- 任意无效参数（如`test`、`prod`、`typo`）都会触发默认行为
- 建议改进: 添加参数验证和错误提示（见详细分析文档）

---

## ⚠️ 注意事项

### 安全操作

✅ **安全的操作**:
- `/deploy backend` - 仅部署后端
- `/deploy` - 仅部署后端（默认）
- `docker-compose up -d mysql` - 幂等检查，不重启

❌ **危险的操作**（避免使用）:
- `docker-compose down` - 停止所有容器（包括MySQL）
- `docker-compose up -d --force-recreate` - 强制重建所有容器
- `docker-compose restart mysql` - 强制重启MySQL

### 数据安全保障

**Jenkins当前配置已确保**:
- ✅ 不使用 `--force-recreate` 标志
- ✅ MySQL配置未变更
- ✅ 数据持久化到volume: `mysql_data`
- ✅ 即使容器重建，数据也会保留

---

## 📚 相关文档

- **详细分析**: `deploy_test命令部署范围分析_20251020.md`
- **部署助手**: `.claude/deploy-helper.md`
- **已知问题**: `.claude/DEPLOY.md`
- **部署工作流**: `.claude/workflow/deployment-workflow.md`
- **数据库一致性分析**: `数据库一致性与环境配置分析报告_20251020.md`

---

**最后更新**: 2025-10-20 11:00:00
**维护者**: Claude Code (SuperClaude Framework)
