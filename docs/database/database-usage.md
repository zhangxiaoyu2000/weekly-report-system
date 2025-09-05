# 数据库使用指南

## 快速开始

### 1. 环境准备
确保已安装并运行以下服务：
- MySQL 8.0+
- Java 17+
- Maven 3.6+

### 2. 数据库初始化
```bash
# 启动MySQL服务
sudo systemctl start mysql

# 连接MySQL并创建数据库
mysql -u root -p
```

```sql
CREATE DATABASE weekly_report CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'weekly_report'@'localhost' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON weekly_report.* TO 'weekly_report'@'localhost';
FLUSH PRIVILEGES;
```

### 3. 运行数据迁移
```bash
cd backend
mvn flyway:migrate
```

### 4. 启动应用
```bash
mvn spring-boot:run
```

## 常用查询示例

### 1. 用户管理
```sql
-- 查询所有活跃用户
SELECT u.username, u.email, d.name as department 
FROM users u 
LEFT JOIN departments d ON u.department_id = d.id 
WHERE u.deleted_at IS NULL AND u.status = 'ACTIVE';

-- 查询部门下的所有用户
CALL GetDepartmentUsers(10);
```

### 2. 周报查询
```sql
-- 查询用户的最新周报
SELECT title, status, week_start, created_at 
FROM weekly_reports 
WHERE user_id = 100 AND deleted_at IS NULL 
ORDER BY created_at DESC LIMIT 5;

-- 查询某周的所有已发布周报
SELECT wr.title, u.username, d.name as department
FROM weekly_reports wr
JOIN users u ON wr.user_id = u.id
LEFT JOIN departments d ON u.department_id = d.id
WHERE wr.week_start = '2025-09-02' 
AND wr.status = 'PUBLISHED'
AND wr.deleted_at IS NULL;
```

### 3. 统计查询
```sql
-- 用户周报统计
CALL GetUserReportStats(100, '2025-01-01', '2025-12-31');

-- 部门周报统计
SELECT d.name, COUNT(wr.id) as report_count
FROM departments d
JOIN users u ON d.id = u.department_id
JOIN weekly_reports wr ON u.id = wr.user_id
WHERE wr.created_at >= '2025-09-01'
AND wr.deleted_at IS NULL
GROUP BY d.id, d.name;
```

## 开发测试

### 1. H2内存数据库（开发环境）
```yaml
spring:
  profiles:
    active: dev
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  h2:
    console:
      enabled: true
      path: /h2-console
```

访问 H2 控制台：http://localhost:8080/api/h2-console

### 2. 重置数据库
```bash
# 清理并重建
mvn flyway:clean
mvn flyway:migrate
```

### 3. 添加测试数据
测试数据已包含在 `V2__Seed_Data.sql` 中，包括：
- 系统管理员账号：admin/admin123
- 测试部门和用户
- 示例周报和评论

## 数据库维护

### 1. 备份
```bash
# 备份整个数据库
mysqldump -u weekly_report -p weekly_report > backup_$(date +%Y%m%d).sql

# 只备份结构
mysqldump -u weekly_report -p --no-data weekly_report > schema_backup.sql
```

### 2. 恢复
```bash
mysql -u weekly_report -p weekly_report < backup_20250905.sql
```

### 3. 性能监控
```sql
-- 查看慢查询
SELECT * FROM mysql.slow_log ORDER BY start_time DESC LIMIT 10;

-- 查看索引使用情况
SELECT * FROM INFORMATION_SCHEMA.STATISTICS 
WHERE table_schema = 'weekly_report';
```

## 常见问题

### 1. 连接问题
- 检查MySQL服务是否启动
- 确认用户名密码正确
- 检查防火墙设置

### 2. 迁移失败
- 检查Flyway配置
- 确认数据库连接正常
- 查看迁移日志

### 3. 性能问题
- 检查索引使用
- 分析慢查询日志
- 考虑查询优化

## 扩展功能

### 1. 读写分离
```yaml
spring:
  datasource:
    master:
      url: jdbc:mysql://master:3306/weekly_report
    slave:
      url: jdbc:mysql://slave:3306/weekly_report
```

### 2. 连接池优化
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 50
      minimum-idle: 10
      idle-timeout: 300000
```

### 3. 缓存配置
```yaml
spring:
  cache:
    type: redis
  redis:
    host: localhost
    port: 6379
```