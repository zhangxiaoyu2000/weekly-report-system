## 部署流程
你先通过ssh了解当前测试服务器上面的接口情况
然后使用空余端口部署本项目

本地查看deploy.md -》 按照文档中过往经验修改 -》 git push到 gitea -》 通过Jenkinsfile  部署到 测试服务器 -》所有服务健康    |
             ｜
             ｜                                                      ｜
             ｜                                                       ｜
             -----------------------------------  使用printwrigt mcp去查看Jenkins控制台日志,错误原因，解决 方法写到deploy.md文档中

## 需要的资料

测试服务器：
    23.95.193.155
    root  To1YHvWPvyX157jf38
jenkins:
    http://23.95.193.155:12088/
    用户名：zhangxiaoyu  密码:2049251148
gitea:  
    http://23.95.193.155:12300/zhangxiaoyu/WeeklyReport.git
    用户名：zhangxiaoyu  密码:2049251148abcZY

mysql数据库脚本：
    create-database-schema.sql    

## 重点
不要回退版本，就用这个版本，不要想去拉取gitea中之前的版本

## 需要部署的服务有
mysql+

## 部署回滚测试
部署过程中使用playwright mcp去查看Jenkins控制台，来查看构建部署情况，若是失败就查看构建日志，然后回滚修改提交
若是构建成功但是镜像容器启动失败可以使用ssh登录到服务器，使用docker logs命令去查看日志，并反馈回滚
并且在回滚纠错和修复的过程补充已知问题和修复

## 已知问题和修复

### 问题1: Gitea仓库推送失败
**现象**: `git push gitea main` 返回 HTTP 400 错误
**原因**: Gitea仓库可能不存在或权限配置问题
**解决方案**: 
1. 需要先在Gitea Web界面手动创建WeeklyReport仓库
2. 或使用以下命令直接在服务器上创建本地Git仓库：
```bash
# 在服务器上创建项目目录和Git仓库
ssh root@23.95.193.155 "mkdir -p /opt/WeeklyReport && cd /opt/WeeklyReport && git init"
```

### 问题2: Jenkins构建失败 - 找不到main分支
**现象**: `fatal: couldn't find remote ref refs/heads/main`
**原因**: Gitea仓库中没有main分支的代码
**解决方案**:
1. 方案A: 修复Gitea仓库推送问题后重新推送代码
2. 方案B: 修改Jenkins配置使用Pipeline script直接输入Jenkinsfile内容
3. 方案C: 在服务器上手动创建项目目录并部署

### 问题3: 端口冲突
**现象**: 默认端口8080和3307已被占用
**解决方案**: 
- 修改docker-compose.yml端口映射为8081:8080和3308:3306
- 已完成修改

### 问题4: Jenkins容器缺少docker-compose
**现象**: Jenkins构建失败，错误信息："docker-compose: not found"
**原因**: Jenkins容器中没有安装docker-compose工具
**解决方案**:
1. 在Jenkins容器中安装docker-compose:
```bash
# 下载docker-compose二进制文件
docker exec -u root jenkins-container curl -L "https://github.com/docker/compose/releases/download/v2.29.1/docker-compose-linux-x86_64" -o /usr/local/bin/docker-compose
# 设置执行权限
docker exec -u root jenkins-container chmod +x /usr/local/bin/docker-compose
```
2. 验证安装: `docker exec jenkins-container docker-compose --version`

### 问题5: 数据库迁移脚本未执行
**现象**: Spring Boot应用启动失败，错误信息："Schema-validation: missing table [ai_analysis_results]"
**原因**: MySQL init脚本只在首次创建数据库时执行，但数据库已存在且为空
**解决方案**:
1. 手动执行数据库迁移脚本:
```bash
# 手动执行V1初始化脚本
docker exec weekly-report-mysql mysql -u root -prootpass123 qr_auth_dev < /docker-entrypoint-initdb.d/V1__Initial_Schema.sql
# 手动执行V3 AI分析表脚本  
docker exec weekly-report-mysql mysql -u root -prootpass123 qr_auth_dev < /docker-entrypoint-initdb.d/V3__Add_AI_Analysis_Tables.sql
```
2. 创建必要的表，特别是ai_analysis_results表
3. 重启backend容器使应用重新连接数据库

### 问题6: 用户表role字段类型不匹配
**现象**: Spring Boot应用启动失败，错误信息："Data truncated for column 'role' at row 1"
**原因**: 数据库中role字段为ENUM类型，但应用代码期望VARCHAR类型
**解决方案**:
```bash
# 修改role字段类型为VARCHAR
docker exec weekly-report-mysql mysql -u root -prootpass123 qr_auth_dev -e 'ALTER TABLE users MODIFY COLUMN role VARCHAR(50) NOT NULL;'
```

### 问题7: 应用启动后网络端口异常
**现象**: Spring Boot应用日志显示启动成功，但无法通过HTTP访问API端点
**症状**: 
- 应用日志显示: "Tomcat started on port 8081 (http) with context path '/api'"
- 应用日志显示: "Started WeeklyReportApplication in XX seconds"
- 但容器内部8080端口未监听，Java进程无法找到
- 外部访问API返回连接错误
**当前状态**: 需要进一步调查网络绑定或应用配置问题

### 问题8: 后端数据库连接配置问题 (当前问题)
**现象**: 
- 后端健康检查正常: `GET /api/health` 返回200 OK
- 登录API失败: `POST /api/auth/login` 返回500内部服务器错误
- 错误信息: "Login failed due to server error"
**原因**: 后端可能连接到错误的MySQL实例，或者目标数据库中缺少必要的表结构和用户数据
**解决方案**: 
1. 确认后端连接的是3308端口的weekly-report-mysql-new容器
2. 如果连接错误实例，需要修改docker-compose环境变量指向正确的MySQL
3. 如果连接正确但数据缺失，需要：
   - 在目标数据库中创建用户表和数据
   - 执行数据库迁移脚本
   - 修复用户表role字段类型

## 部署最佳实践和经验总结

### 当前部署状态
- ✅ Jenkins项目已创建
- ✅ 端口配置已优化(8081:8080, 3308:3306)
- ✅ Jenkinsfile已准备就绪
- ✅ Gitea仓库推送成功 (commit: 73b8084f8f87ad04efd27ef79aceaef4fef4585a)
- ✅ Jenkins构建成功 (Build #3)
- ✅ Jenkins容器安装docker-compose v2.29.1
- ✅ MySQL容器运行正常 (端口3308)
- ✅ 数据库表结构已创建
- ✅ 用户表role字段类型问题已修复
- ✅ Spring Boot应用启动成功 (日志显示正常)
- ⚠️ 网络端口绑定异常 - 应用运行但HTTP端点无法访问

### 推荐的部署流程修正
1. **优先级1**: 修复Gitea仓库问题
   - 在Gitea Web界面创建WeeklyReport仓库
   - 重新配置Git认证
   - 推送代码到仓库

2. **优先级2**: 验证Jenkins构建
   - 确认仓库可访问后重新构建
   - 监控构建过程并解决依赖问题

3. **优先级3**: 验证服务部署
   - 检查Docker容器启动状态
   - 验证服务健康检查
   - 测试API接口可用性