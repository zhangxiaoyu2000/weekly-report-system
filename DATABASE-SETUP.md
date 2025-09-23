# 周报管理系统 - 本地MySQL数据库部署指南

## 概述

本文档说明如何使用Docker部署本地MySQL数据库，包含完整的周报管理系统数据库结构。

## 前置要求

- Docker 和 Docker Compose 已安装并运行
- 端口 3306 未被占用

## 快速启动

### 1. 启动数据库
```bash
./start-mysql-local.sh
```

这个脚本将会：
- 清理现有容器
- 启动MySQL 8.0容器
- 自动执行数据库初始化脚本
- 创建所有必要的表和索引
- 插入默认用户数据

### 2. 测试连接
```bash
./test-mysql-connection.sh
```

### 3. 停止数据库
```bash
./stop-mysql-local.sh
```

## 数据库连接信息

| 项目 | 值 |
|------|-----|
| 主机 | localhost |
| 端口 | 3306 |
| 数据库名 | weekly_report_system |
| Root用户 | root / rootpass123 |
| 应用用户 | weekly_user / weekly123 |

## 默认用户账户

系统会自动创建以下测试用户：

| 用户名 | 邮箱 | 角色 | 密码 |
|--------|------|------|------|
| admin | admin@company.com | ADMIN | password |
| super_admin | super@company.com | SUPER_ADMIN | password |
| manager1 | manager1@company.com | MANAGER | password |
| manager2 | manager2@company.com | MANAGER | password |

> 注意：密码已经过BCrypt加密，明文密码为 "password"

## 数据库结构

系统包含以下核心表：

1. **users** - 用户表（主管、管理员、超级管理员）
2. **projects** - 项目表
3. **project_phases** - 项目阶段表
4. **tasks** - 任务表
5. **weekly_reports** - 周报表
6. **task_reports** - 日常任务与周报关联表
7. **dev_task_reports** - 发展任务与周报关联表
8. **ai_analysis_results** - AI分析结果表

## 手动操作

### 进入数据库容器
```bash
docker-compose -f docker-compose-mysql-local.yml exec mysql-local mysql -u root -p
```

### 查看日志
```bash
docker-compose -f docker-compose-mysql-local.yml logs mysql-local
```

### 备份数据库
```bash
docker-compose -f docker-compose-mysql-local.yml exec mysql-local mysqldump -u root -prootpass123 weekly_report_system > backup.sql
```

### 恢复数据库
```bash
docker-compose -f docker-compose-mysql-local.yml exec -T mysql-local mysql -u root -prootpass123 weekly_report_system < backup.sql
```

## 故障排除

### 端口冲突
如果端口3306被占用，可以修改 `docker-compose-mysql-local.yml` 中的端口映射：
```yaml
ports:
  - "3307:3306"  # 使用3307端口
```

### 重新初始化
如果需要完全重新初始化数据库：
```bash
docker-compose -f docker-compose-mysql-local.yml down --volumes
./start-mysql-local.sh
```

### 查看容器状态
```bash
docker-compose -f docker-compose-mysql-local.yml ps
```

## 配置说明

数据库配置使用以下设置：
- 字符集：utf8mb4
- 排序规则：utf8mb4_unicode_ci
- 认证插件：mysql_native_password
- 自动重启：unless-stopped

## Spring Boot配置

在Spring Boot应用中使用以下配置连接数据库：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/weekly_report_system?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: weekly_user
    password: weekly123
    driver-class-name: com.mysql.cj.jdbc.Driver
```