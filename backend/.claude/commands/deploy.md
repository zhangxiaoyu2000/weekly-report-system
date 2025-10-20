---
description: 智能化部署命令，基于deploy-helper.md进行部署流程管理
argument-hint: [backend|frontend|all] [--force]
---

# 智能部署命令

## 执行流程

### 第1步：确定部署范围
- 如果 $1 为 "backend" 或未指定：部署后端
- 如果 $1 为 "frontend"：部署前端
- 如果 $1 为 "all"：部署后端和前端

### 第2步：读取部署助手文档
必须先读取相应的 deploy-helper.md 文件以了解部署规范：

**后端部署**：
1. 读取 `.claude/deploy-helper.md`
2. 读取 `.claude/DEPLOY.md` 了解已知问题
3. 读取 `.claude/workflow/deployment-workflow.md` 了解完整流程

**前端部署**：
1. 读取 `../frontend/.claude/deploy-helper.md`
2. 读取 `../frontend/.claude/DEPLOY.md` 了解已知问题
3. 读取 `../frontend/.claude/workflow/deployment-workflow.md` 了解完整流程

### 第3步：执行部署前检查
根据 deploy-helper.md 中的强制检查清单：

**后端检查**：
- [ ] Maven构建状态检查
- [ ] application.yml配置验证
- [ ] 数据库连接测试
- [ ] JWT配置验证

**前端检查**：
- [ ] npm依赖安装状态
- [ ] Vite构建配置检查
- [ ] nginx配置验证
- [ ] API代理配置检查

### 第4步：执行部署操作
按照 deploy-helper.md 中的标准命令模板执行：

**后端部署命令**：
```bash
# 构建验证
mvn clean package -DskipTests=false

# Git推送
git add .
git commit -m "后端部署: $ARGUMENTS"
git push origin main
```

**前端部署命令**：
```bash
# 构建验证
npm run build

# Git推送
git add .
git commit -m "前端部署: $ARGUMENTS"
git push origin main
```

### 第5步：监控部署状态
1. 使用 Playwright MCP 监控 Jenkins 控制台
2. 查看构建和Docker阶段的日志输出
3. 检测是否有错误发生

### 第6步：错误处理
如果部署失败：
1. 🏷️ 进行错误分类标签识别
2. 🔍 在 DEPLOY.md 中搜索相同错误
3. ✅ 应用已知解决方案
4. 📝 如果是新错误，记录到 DEPLOY.md

### 第7步：健康检查
**后端健康检查**：
```bash
curl -I http://23.95.193.155:8082/api/health
curl -X POST http://23.95.193.155:8082/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail": "admin1", "password": "Admin123@"}'
```

**前端健康检查**：
```bash
curl -I http://23.95.193.155:3003
```

### 第8步：部署成功确认
根据 deploy-helper.md 中的成功标准进行验证：
- [ ] 容器运行正常
- [ ] 健康检查通过
- [ ] API接口响应正常
- [ ] 前端页面可访问

## 强制遵守规则

⚠️ **部署过程中必须严格遵循以下规则**：

1. **不跳过检查**：必须执行 deploy-helper.md 中的所有检查项
2. **错误必搜索**：遇到错误必须先在 DEPLOY.md 中搜索
3. **优先已知方案**：优先使用已知解决方案
4. **新错误必记录**：新问题必须记录到 DEPLOY.md
5. **超时限制**：遵守30分钟超时和10次重试限制
6. **工具规范**：使用 Playwright MCP 和 sshpass SSH

## 使用示例

```bash
# 部署后端
/deploy backend

# 部署前端
/deploy frontend

# 部署所有
/deploy all

# 强制部署后端（跳过确认）
/deploy backend --force
```

## 注意事项

- 本命令会自动读取并遵循 `.claude/deploy-helper.md` 中的所有规范
- 部署过程中会实时监控 Jenkins 并处理错误
- 所有新发现的问题都会被记录到 DEPLOY.md 知识库中
- 如果超过重试限制，会自动升级到手动处理
