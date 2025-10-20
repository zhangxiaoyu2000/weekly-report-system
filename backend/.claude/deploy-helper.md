# 后端 Claude Code 部署助手

> 📚 **参考文档**:
> - 智能部署工作流: `.claude/workflow/deployment-workflow.md`
> - 已知问题和经验: `DEPLOY.md`

## 🚨 **后端部署前强制检查清单**

### ✅ **第一步：文档确认**
在执行任何后端部署操作前，必须确认：
- [ ] 已阅读 `.claude/DEPLOY.md` 了解已知问题和解决方案
- [ ] 已阅读 `.claude/workflow/deployment-workflow.md` 了解完整部署流程
- [ ] 确认30分钟超时和10次重试限制

### ✅ **第二步：后端错误处理标准流程**
```
1. 🏷️ 后端错误分类标签识别
   - 🏗️ 构建问题 (Maven build failed, Java compilation error)
   - ⚙️ 配置问题 (application.yml error, JWT configuration)
   - 🌐 环境问题 (database connection refused, port 8082 in use)
   - 🔗 依赖问题 (MySQL connection, database schema)
   - 🐳 Docker问题 (Spring Boot container, backend build failed)
   - 🔐 安全问题 (JWT token error, Spring Security config)

2. 🔍 在DEPLOY.md中搜索匹配错误
   - 后端特定关键词匹配
   - Spring Boot/MySQL错误码匹配

3. ✅ 应用已知后端解决方案
   - 优先使用已有方案
   - 验证解决效果

4. 📝 方案失效时的处理
   - 分析新的根本原因
   - 追加解决方案到DEPLOY.md
   - 格式: **解决方案2** (日期): **追加原因**: 具体说明
```

### ✅ **第三步：工具使用规范**
- **Playwright MCP**: 监控Jenkins控制台(关注后端Maven构建和Docker阶段)
- **sshpass SSH**: 查看后端容器日志(weekly-report-backend)和MySQL连接状态
- **超时控制**: 30分钟自动停止
- **重试限制**: 最大10次重试，超过自动升级

## 📋 **后端标准部署命令模板**

### 🔄 **Git推送部署**
```bash
# 1. 检查DEPLOY.md已知问题
echo "检查backend/DEPLOY.md已知问题..."
grep -i "关键词" .claude/DEPLOY.md

# 2. 后端构建验证
mvn clean package -DskipTests=false

# 3. 推送代码
git add .
git commit -m "后端: 描述性提交信息"
git push origin main

# 4. 使用Playwright MCP监控Jenkins后端构建
# Claude Code应该自动使用Playwright MCP查看Jenkins控制台
```

### 🐳 **Docker部署操作**
```bash
# 1. 预检查
docker ps | grep weekly-report-backend
docker ps | grep mysql

# 2. 构建和部署
docker-compose down backend mysql
docker-compose up -d --build backend mysql

# 3. 健康检查
curl -I http://23.95.193.155:8082/api/health
curl -X POST http://23.95.193.155:8082/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail": "admin1", "password": "Admin123@"}'
```

### 🔧 **SSH调试模板**
```bash
# 后端容器状态
sshpass -p 'To1YHvWPvyX157jf38' ssh -o StrictHostKeyChecking=no root@23.95.193.155 \
  "docker ps | grep weekly-report-backend"

# 后端容器日志
sshpass -p 'To1YHvWPvyX157jf38' ssh -o StrictHostKeyChecking=no root@23.95.193.155 \
  "docker logs weekly-report-backend"

# MySQL数据库检查
sshpass -p 'To1YHvWPvyX157jf38' ssh -o StrictHostKeyChecking=no root@23.95.193.155 \
  "docker exec weekly-report-mysql mysql -u root -prootpass123 -e 'SHOW DATABASES;'"

# Spring Boot配置检查
sshpass -p 'To1YHvWPvyX157jf38' ssh -o StrictHostKeyChecking=no root@23.95.193.155 \
  "docker exec weekly-report-backend cat /app/application.yml"
```

## 🚫 **严格禁止的后端操作**

### ❌ **不允许的行为**
- 跳过DEPLOY.md已知问题检查
- 遇到后端错误时不进行分类标签识别
- 不优先使用后端已知解决方案
- 不记录新发现的后端问题和解决方案
- 超过重试限制后继续尝试
- 不使用Playwright MCP监控后端Jenkins构建
- 不使用sshpass查看后端容器日志

### ⚠️ **Claude Code检查点**
在执行以下后端操作前，Claude Code必须：
1. 阅读并引用DEPLOY.md相关内容
2. 进行后端错误搜索和匹配
3. 应用后端智能错误处理机制
4. 记录任何新发现的后端问题

**触发检查的关键词**：
- backend, 后端, spring, maven, mysql, database
- jwt, security, auth
- git push (后端代码), docker (后端容器)
- error, 错误, 失败, failed

## 📝 **后端问题记录模板**

### 🆕 **新问题记录格式**
```markdown
### 问题X: [后端问题标题] (🏷️[分类标签]) (已解决 ✅)
**现象**:
- 后端具体错误信息
- 出现的环境和步骤

**根本原因**:
后端相关的根本原因分析

**解决方案** (YYYY-MM-DD):
- 后端具体解决步骤
- 验证命令
- 验证结果: ✅/❌

**分类**: 🏗️构建/⚙️配置/🌐环境/🔗依赖/🐳Docker/🔐安全
```

### ➕ **追加解决方案格式**
```markdown
**解决方案2** (YYYY-MM-DD):
**追加原因**: 使用解决方案1后仍然失败，发现新的根本原因
- 新的解决步骤
- 验证结果: ✅
```

## 🎯 **后端成功标准**

### ✅ **部署完成确认**
- [ ] 后端容器运行正常 (weekly-report-backend)
- [ ] MySQL数据库容器运行正常
- [ ] 后端健康检查通过 (http://23.95.193.155:8082/api/health)
- [ ] 后端API接口响应正常
- [ ] 数据库连接正常
- [ ] JWT认证功能正常
- [ ] 新问题已记录到DEPLOY.md

### 📊 **质量指标**
- 后端部署成功率 > 85%
- 后端平均解决时间 < 20分钟
- 后端重复问题发生率 < 10%
- 后端知识库覆盖率持续提升

### 🌐 **后端特定验证**
```bash
# API健康检查
curl -I http://23.95.193.155:8082/api/health
# 期望: HTTP 200 OK

# 认证API测试
curl -X POST http://23.95.193.155:8082/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"admin1","password":"Admin123@"}'
# 期望: HTTP 200 OK 并返回JWT token

# JWT端点测试
curl -I http://23.95.193.155:8082/api/auth/test
# 期望: HTTP 401 (未授权,说明安全配置正常)
```

---

**记住：这个助手文件确保Claude Code严格遵循后端部署流程，提高部署成功率和知识积累效率！**
