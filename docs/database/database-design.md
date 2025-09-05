# 周报系统数据库设计文档

## 文档信息
- **版本**: 1.0
- **创建日期**: 2025-09-05
- **最后更新**: 2025-09-05
- **负责人**: Database Schema Design Team
- **状态**: 完成

## 目录
1. [概述](#概述)
2. [设计原则](#设计原则)
3. [数据库架构](#数据库架构)
4. [表结构详述](#表结构详述)
5. [索引策略](#索引策略)
6. [数据约束](#数据约束)
7. [存储过程和触发器](#存储过程和触发器)
8. [性能优化](#性能优化)
9. [安全考虑](#安全考虑)
10. [部署指南](#部署指南)

## 概述

周报系统数据库采用MySQL 8.0设计，遵循第三范式，包含5个核心实体表：
- **users**: 用户信息管理
- **departments**: 部门组织架构
- **weekly_reports**: 周报内容存储
- **templates**: 周报模板配置
- **comments**: 评论和反馈数据

### 系统特性
- 支持多层级部门结构
- 灵活的周报模板系统
- 完整的评论和审批流程
- 软删除机制保证数据完整性
- 高性能索引设计
- 数据迁移和版本控制

## 设计原则

### 1. 数据完整性
- 遵循第三范式，消除数据冗余
- 通过外键约束保证引用完整性
- 使用检查约束验证数据有效性
- 软删除机制避免数据丢失

### 2. 扩展性设计
- 预留扩展字段支持未来需求
- 使用JSON字段存储动态配置
- 模块化表结构便于系统扩展
- 版本控制支持数据迁移

### 3. 性能优化
- 合理的索引设计覆盖常用查询
- 复合索引优化多条件查询
- 分页查询和数据统计优化
- 连接池配置优化并发性能

### 4. 安全性
- 密码哈希存储
- 敏感数据加密处理
- 操作日志记录
- 权限分离和访问控制

## 数据库架构

### 核心实体关系图
```
┌─────────────┐    ┌──────────────┐    ┌───────────────┐
│ Departments │◄──┤    Users     │───►│ WeeklyReports │
│             │    │              │    │               │
│ - id        │    │ - id         │    │ - id          │
│ - name      │    │ - username   │    │ - title       │
│ - parent_id │    │ - email      │    │ - content     │
│ - manager_id│    │ - role       │    │ - status      │
└─────────────┘    │ - dept_id    │    │ - week_start  │
       ▲           └──────────────┘    └───────────────┘
       │                                       ▲
       │           ┌──────────────┐            │
       └───────────┤  Templates   │────────────┘
                   │              │
                   │ - id         │
                   │ - name       │
                   │ - content    │
                   │ - fields     │
                   └──────────────┘
                          ▲
                          │
                   ┌──────────────┐
                   │   Comments   │
                   │              │
                   │ - id         │
                   │ - report_id  │
                   │ - user_id    │
                   │ - content    │
                   └──────────────┘
```

### 数据流向
1. **用户认证**: users → role validation → department assignment
2. **周报创建**: templates → weekly_reports → content validation
3. **审批流程**: weekly_reports → status update → comments
4. **数据统计**: aggregated queries → reporting views

## 表结构详述

### 1. users (用户表)
**用途**: 存储系统用户的基本信息和权限
**记录数预估**: 1000-5000

| 字段名 | 数据类型 | 约束 | 说明 | 索引 |
|--------|----------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 用户唯一标识 | PRIMARY |
| username | VARCHAR(50) | NOT NULL, UNIQUE | 用户名 | UNIQUE |
| email | VARCHAR(100) | NOT NULL, UNIQUE | 邮箱地址 | UNIQUE |
| password | VARCHAR(255) | NOT NULL | 密码哈希值 | - |
| first_name | VARCHAR(50) | NOT NULL | 姓 | - |
| last_name | VARCHAR(50) | NOT NULL | 名 | - |
| role | ENUM | NOT NULL, DEFAULT 'EMPLOYEE' | 用户角色 | INDEX |
| department_id | BIGINT | FK | 所属部门ID | INDEX |
| status | ENUM | NOT NULL, DEFAULT 'ACTIVE' | 用户状态 | INDEX |
| last_login | TIMESTAMP | NULL | 最后登录时间 | - |
| created_at | TIMESTAMP | NOT NULL | 创建时间 | INDEX |
| updated_at | TIMESTAMP | NOT NULL | 更新时间 | - |
| deleted_at | TIMESTAMP | NULL | 软删除时间 | INDEX |

### 2. departments (部门表)
**用途**: 存储组织架构信息，支持多层级结构
**记录数预估**: 50-200

| 字段名 | 数据类型 | 约束 | 说明 | 索引 |
|--------|----------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 部门唯一标识 | PRIMARY |
| name | VARCHAR(100) | NOT NULL | 部门名称 | - |
| description | TEXT | NULL | 部门描述 | - |
| parent_id | BIGINT | FK, NULL | 父部门ID | INDEX |
| manager_id | BIGINT | FK, NULL | 部门经理ID | INDEX |
| level | INT | NOT NULL, DEFAULT 1 | 部门层级 | INDEX |
| sort_order | INT | NOT NULL, DEFAULT 0 | 排序字段 | - |
| status | ENUM | NOT NULL, DEFAULT 'ACTIVE' | 部门状态 | INDEX |
| created_at | TIMESTAMP | NOT NULL | 创建时间 | - |
| updated_at | TIMESTAMP | NOT NULL | 更新时间 | - |
| deleted_at | TIMESTAMP | NULL | 软删除时间 | INDEX |

### 3. templates (模板表)
**用途**: 存储周报模板配置信息
**记录数预估**: 20-100

| 字段名 | 数据类型 | 约束 | 说明 | 索引 |
|--------|----------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 模板唯一标识 | PRIMARY |
| name | VARCHAR(100) | NOT NULL | 模板名称 | - |
| description | TEXT | NULL | 模板描述 | - |
| content | LONGTEXT | NOT NULL | 模板内容 | - |
| fields | JSON | NULL | 字段配置 | - |
| is_default | BOOLEAN | NOT NULL, DEFAULT FALSE | 是否默认 | INDEX |
| is_public | BOOLEAN | NOT NULL, DEFAULT TRUE | 是否公开 | INDEX |
| created_by | BIGINT | FK, NOT NULL | 创建者ID | INDEX |
| department_id | BIGINT | FK, NULL | 适用部门ID | INDEX |
| status | ENUM | NOT NULL, DEFAULT 'ACTIVE' | 模板状态 | INDEX |
| sort_order | INT | NOT NULL, DEFAULT 0 | 排序字段 | - |
| usage_count | INT | NOT NULL, DEFAULT 0 | 使用次数 | - |
| created_at | TIMESTAMP | NOT NULL | 创建时间 | - |
| updated_at | TIMESTAMP | NOT NULL | 更新时间 | - |
| deleted_at | TIMESTAMP | NULL | 软删除时间 | INDEX |

### 4. weekly_reports (周报表)
**用途**: 存储周报内容和状态信息
**记录数预估**: 10000-100000

| 字段名 | 数据类型 | 约束 | 说明 | 索引 |
|--------|----------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 周报唯一标识 | PRIMARY |
| user_id | BIGINT | FK, NOT NULL | 创建用户ID | INDEX |
| template_id | BIGINT | FK, NULL | 使用模板ID | INDEX |
| title | VARCHAR(200) | NOT NULL | 周报标题 | - |
| content | LONGTEXT | NOT NULL | 周报内容 | - |
| summary | TEXT | NULL | 周报摘要 | - |
| status | ENUM | NOT NULL, DEFAULT 'DRAFT' | 周报状态 | INDEX |
| week_start | DATE | NOT NULL | 周开始日期 | INDEX |
| week_end | DATE | NOT NULL | 周结束日期 | INDEX |
| submitted_at | TIMESTAMP | NULL | 提交时间 | - |
| reviewed_at | TIMESTAMP | NULL | 审核时间 | - |
| reviewed_by | BIGINT | FK, NULL | 审核人ID | INDEX |
| review_comments | TEXT | NULL | 审核意见 | - |
| priority | ENUM | NOT NULL, DEFAULT 'NORMAL' | 优先级 | INDEX |
| tags | JSON | NULL | 标签列表 | - |
| attachments | JSON | NULL | 附件列表 | - |
| view_count | INT | NOT NULL, DEFAULT 0 | 查看次数 | - |
| created_at | TIMESTAMP | NOT NULL | 创建时间 | INDEX |
| updated_at | TIMESTAMP | NOT NULL | 更新时间 | - |
| deleted_at | TIMESTAMP | NULL | 软删除时间 | INDEX |

**复合索引**:
- `idx_reports_user_week` (user_id, week_start)
- `idx_reports_status_date` (status, created_at)

### 5. comments (评论表)
**用途**: 存储周报评论和反馈信息
**记录数预估**: 50000-500000

| 字段名 | 数据类型 | 约束 | 说明 | 索引 |
|--------|----------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 评论唯一标识 | PRIMARY |
| report_id | BIGINT | FK, NOT NULL | 周报ID | INDEX |
| user_id | BIGINT | FK, NOT NULL | 评论用户ID | INDEX |
| parent_id | BIGINT | FK, NULL | 父评论ID | INDEX |
| content | TEXT | NOT NULL | 评论内容 | - |
| type | ENUM | NOT NULL, DEFAULT 'COMMENT' | 评论类型 | INDEX |
| is_private | BOOLEAN | NOT NULL, DEFAULT FALSE | 是否私密 | - |
| status | ENUM | NOT NULL, DEFAULT 'ACTIVE' | 评论状态 | INDEX |
| created_at | TIMESTAMP | NOT NULL | 创建时间 | INDEX |
| updated_at | TIMESTAMP | NOT NULL | 更新时间 | - |
| deleted_at | TIMESTAMP | NULL | 软删除时间 | INDEX |

**复合索引**:
- `idx_comments_report_status` (report_id, status)

## 索引策略

### 1. 主键索引
- 所有表都使用BIGINT AUTO_INCREMENT主键
- 提供唯一标识和最快的行定位
- 支持高并发插入操作

### 2. 唯一索引
- `users.username` 和 `users.email` 保证用户唯一性
- 防止重复数据插入
- 提高登录查询性能

### 3. 外键索引
- 所有外键字段都建立索引
- 提高关联查询性能
- 支持快速数据完整性检查

### 4. 业务索引
- `status` 字段：支持按状态过滤查询
- `created_at` 字段：支持时间范围查询
- `deleted_at` 字段：支持软删除过滤

### 5. 复合索引
- `(user_id, week_start)`: 查询用户特定时间周报
- `(status, created_at)`: 按状态和时间排序查询
- `(report_id, status)`: 查询周报的有效评论

### 索引维护策略
- 定期分析索引使用情况
- 删除未使用的索引
- 优化慢查询相关索引
- 监控索引碎片化情况

## 数据约束

### 1. 主键约束
- 所有表都有自增主键
- 保证数据唯一性

### 2. 外键约束
```sql
-- 用户部门关联
CONSTRAINT fk_users_department_id 
FOREIGN KEY (department_id) REFERENCES departments (id) ON DELETE SET NULL

-- 部门层级关系
CONSTRAINT fk_departments_parent_id 
FOREIGN KEY (parent_id) REFERENCES departments (id) ON DELETE SET NULL

-- 周报用户关联
CONSTRAINT fk_reports_user_id 
FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
```

### 3. 检查约束
```sql
-- 部门层级验证
CONSTRAINT chk_departments_level CHECK (level > 0)

-- 周报日期验证
CONSTRAINT chk_reports_week_dates CHECK (week_end >= week_start)

-- 模板使用次数验证
CONSTRAINT chk_templates_usage_count CHECK (usage_count >= 0)
```

### 4. 枚举约束
- 用户角色: ADMIN, MANAGER, EMPLOYEE
- 用户状态: ACTIVE, INACTIVE, SUSPENDED
- 周报状态: DRAFT, SUBMITTED, REVIEWED, PUBLISHED
- 评论类型: COMMENT, SUGGESTION, APPROVAL, REJECTION

## 存储过程和触发器

### 1. 存储过程

#### GetDepartmentUsers
**功能**: 获取部门及其子部门的所有用户
**参数**: dept_id (部门ID)
**返回**: 用户列表
```sql
CALL GetDepartmentUsers(10);
```

#### GetUserReportStats  
**功能**: 获取用户周报统计信息
**参数**: user_id, start_date, end_date
**返回**: 统计数据
```sql
CALL GetUserReportStats(100, '2025-01-01', '2025-12-31');
```

### 2. 触发器

#### trg_update_template_usage
**触发时机**: 插入周报时
**功能**: 自动更新模板使用次数

#### trg_set_week_end_date
**触发时机**: 插入周报前
**功能**: 自动设置周结束日期

### 3. 视图

#### v_active_users
**功能**: 查询活跃用户及部门信息
```sql
SELECT * FROM v_active_users WHERE department_name = '技术部';
```

#### v_department_hierarchy
**功能**: 显示部门层级结构
```sql
SELECT * FROM v_department_hierarchy ORDER BY level, sort_order;
```

#### v_latest_reports
**功能**: 最新周报列表
```sql
SELECT * FROM v_latest_reports WHERE status = 'PUBLISHED' LIMIT 10;
```

## 性能优化

### 1. 查询优化
- 使用合适的索引覆盖查询条件
- 避免SELECT * 查询
- 使用LIMIT进行分页查询
- 预编译语句减少解析开销

### 2. 连接池配置
```yaml
spring:
  datasource:
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
      idle-timeout: 300000
      max-lifetime: 1800000
      connection-timeout: 20000
```

### 3. 缓存策略
- 部门层级数据缓存
- 模板信息缓存
- 用户会话信息缓存
- 统计数据定时更新缓存

### 4. 分区策略
- 考虑按时间分区weekly_reports表
- 历史数据归档处理
- 定期清理软删除数据

## 安全考虑

### 1. 数据加密
- 用户密码使用BCrypt哈希
- 敏感字段考虑列级加密
- 传输过程使用TLS加密

### 2. 访问控制
- 数据库用户权限最小化
- 应用层权限控制
- SQL注入防护

### 3. 审计日志
- 重要操作记录日志
- 数据变更追踪
- 异常访问监控

## 部署指南

### 1. 环境要求
- MySQL 8.0+
- Java 17+
- Maven 3.6+

### 2. 数据库初始化
```bash
# 创建数据库
CREATE DATABASE weekly_report CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 创建用户
CREATE USER 'weekly_report'@'%' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON weekly_report.* TO 'weekly_report'@'%';
FLUSH PRIVILEGES;
```

### 3. Flyway迁移
```bash
# Maven方式
mvn flyway:migrate

# 或者应用启动时自动迁移
spring.flyway.enabled=true
```

### 4. 监控和维护
- 定期备份数据库
- 监控慢查询日志
- 定期优化表和索引
- 监控存储空间使用

## 版本历史

| 版本 | 日期 | 变更内容 |
|------|------|----------|
| 1.0 | 2025-09-05 | 初始版本，包含核心表结构设计 |

## 相关文档
- [ER图设计文档](er-diagram.md)
- [API接口文档](../api/api-overview.md)
- [部署指南](../deployment/deployment-guide.md)