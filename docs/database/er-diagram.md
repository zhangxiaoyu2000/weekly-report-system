# 周报系统数据库 ER 图设计

## 概述
周报系统的数据库设计遵循第三范式，包含5个核心实体：用户(Users)、部门(Departments)、周报(WeeklyReports)、模板(Templates)、评论(Comments)。

## 核心实体设计

### 1. Users (用户表)
```
Users
├── id (BIGINT, PK, AUTO_INCREMENT) - 用户唯一标识
├── username (VARCHAR(50), UNIQUE, NOT NULL) - 用户名
├── email (VARCHAR(100), UNIQUE, NOT NULL) - 邮箱地址
├── password (VARCHAR(255), NOT NULL) - 密码哈希值
├── first_name (VARCHAR(50), NOT NULL) - 姓
├── last_name (VARCHAR(50), NOT NULL) - 名
├── role (ENUM('ADMIN','MANAGER','EMPLOYEE'), NOT NULL, DEFAULT 'EMPLOYEE') - 用户角色
├── department_id (BIGINT, FK -> Departments.id) - 所属部门
├── status (ENUM('ACTIVE','INACTIVE','SUSPENDED'), NOT NULL, DEFAULT 'ACTIVE') - 用户状态
├── last_login (TIMESTAMP) - 最后登录时间
├── created_at (TIMESTAMP, NOT NULL, DEFAULT CURRENT_TIMESTAMP) - 创建时间
├── updated_at (TIMESTAMP, NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP) - 更新时间
└── deleted_at (TIMESTAMP) - 软删除时间
```

### 2. Departments (部门表)
```
Departments
├── id (BIGINT, PK, AUTO_INCREMENT) - 部门唯一标识
├── name (VARCHAR(100), NOT NULL) - 部门名称
├── description (TEXT) - 部门描述
├── parent_id (BIGINT, FK -> Departments.id) - 父部门ID (自关联)
├── manager_id (BIGINT, FK -> Users.id) - 部门经理ID
├── level (INT, NOT NULL, DEFAULT 1) - 部门层级
├── sort_order (INT, NOT NULL, DEFAULT 0) - 排序字段
├── status (ENUM('ACTIVE','INACTIVE'), NOT NULL, DEFAULT 'ACTIVE') - 部门状态
├── created_at (TIMESTAMP, NOT NULL, DEFAULT CURRENT_TIMESTAMP) - 创建时间
├── updated_at (TIMESTAMP, NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP) - 更新时间
└── deleted_at (TIMESTAMP) - 软删除时间
```

### 3. WeeklyReports (周报表)
```
WeeklyReports
├── id (BIGINT, PK, AUTO_INCREMENT) - 周报唯一标识
├── user_id (BIGINT, FK -> Users.id, NOT NULL) - 创建用户ID
├── template_id (BIGINT, FK -> Templates.id) - 使用的模板ID
├── title (VARCHAR(200), NOT NULL) - 周报标题
├── content (LONGTEXT, NOT NULL) - 周报内容
├── summary (TEXT) - 周报摘要
├── status (ENUM('DRAFT','SUBMITTED','REVIEWED','PUBLISHED'), NOT NULL, DEFAULT 'DRAFT') - 周报状态
├── week_start (DATE, NOT NULL) - 周开始日期
├── week_end (DATE, NOT NULL) - 周结束日期
├── submitted_at (TIMESTAMP) - 提交时间
├── reviewed_at (TIMESTAMP) - 审核时间
├── reviewed_by (BIGINT, FK -> Users.id) - 审核人ID
├── review_comments (TEXT) - 审核意见
├── priority (ENUM('LOW','NORMAL','HIGH','URGENT'), NOT NULL, DEFAULT 'NORMAL') - 优先级
├── tags (JSON) - 标签列表
├── attachments (JSON) - 附件列表
├── view_count (INT, NOT NULL, DEFAULT 0) - 查看次数
├── created_at (TIMESTAMP, NOT NULL, DEFAULT CURRENT_TIMESTAMP) - 创建时间
├── updated_at (TIMESTAMP, NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP) - 更新时间
└── deleted_at (TIMESTAMP) - 软删除时间
```

### 4. Templates (模板表)
```
Templates
├── id (BIGINT, PK, AUTO_INCREMENT) - 模板唯一标识
├── name (VARCHAR(100), NOT NULL) - 模板名称
├── description (TEXT) - 模板描述
├── content (LONGTEXT, NOT NULL) - 模板内容
├── fields (JSON) - 模板字段配置
├── is_default (BOOLEAN, NOT NULL, DEFAULT FALSE) - 是否默认模板
├── is_public (BOOLEAN, NOT NULL, DEFAULT TRUE) - 是否公开模板
├── created_by (BIGINT, FK -> Users.id, NOT NULL) - 创建者ID
├── department_id (BIGINT, FK -> Departments.id) - 适用部门ID
├── status (ENUM('ACTIVE','INACTIVE'), NOT NULL, DEFAULT 'ACTIVE') - 模板状态
├── sort_order (INT, NOT NULL, DEFAULT 0) - 排序字段
├── usage_count (INT, NOT NULL, DEFAULT 0) - 使用次数
├── created_at (TIMESTAMP, NOT NULL, DEFAULT CURRENT_TIMESTAMP) - 创建时间
├── updated_at (TIMESTAMP, NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP) - 更新时间
└── deleted_at (TIMESTAMP) - 软删除时间
```

### 5. Comments (评论表)
```
Comments
├── id (BIGINT, PK, AUTO_INCREMENT) - 评论唯一标识
├── report_id (BIGINT, FK -> WeeklyReports.id, NOT NULL) - 周报ID
├── user_id (BIGINT, FK -> Users.id, NOT NULL) - 评论者ID
├── parent_id (BIGINT, FK -> Comments.id) - 父评论ID (用于回复)
├── content (TEXT, NOT NULL) - 评论内容
├── type (ENUM('COMMENT','SUGGESTION','APPROVAL','REJECTION'), NOT NULL, DEFAULT 'COMMENT') - 评论类型
├── is_private (BOOLEAN, NOT NULL, DEFAULT FALSE) - 是否私密评论
├── status (ENUM('ACTIVE','HIDDEN','DELETED'), NOT NULL, DEFAULT 'ACTIVE') - 评论状态
├── created_at (TIMESTAMP, NOT NULL, DEFAULT CURRENT_TIMESTAMP) - 创建时间
├── updated_at (TIMESTAMP, NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP) - 更新时间
└── deleted_at (TIMESTAMP) - 软删除时间
```

## 实体关系

### 1. 用户与部门关系
- **Users** -> **Departments**: 多对一关系
  - 一个用户属于一个部门
  - 一个部门可以有多个用户

### 2. 部门层级关系
- **Departments** -> **Departments**: 自关联一对多关系
  - 一个部门可以有一个父部门
  - 一个部门可以有多个子部门

### 3. 用户与周报关系
- **Users** -> **WeeklyReports**: 一对多关系
  - 一个用户可以创建多个周报
  - 一个周报只能属于一个用户

### 4. 模板与周报关系
- **Templates** -> **WeeklyReports**: 一对多关系
  - 一个模板可以被多个周报使用
  - 一个周报使用一个模板

### 5. 周报与评论关系
- **WeeklyReports** -> **Comments**: 一对多关系
  - 一个周报可以有多个评论
  - 一个评论属于一个周报

### 6. 用户与评论关系
- **Users** -> **Comments**: 一对多关系
  - 一个用户可以写多个评论
  - 一个评论属于一个用户

### 7. 评论回复关系
- **Comments** -> **Comments**: 自关联一对多关系
  - 一个评论可以有多个回复
  - 一个回复属于一个父评论

## 索引策略

### 主要索引
1. **Users表索引**
   - PRIMARY KEY (id)
   - UNIQUE KEY uk_users_username (username)
   - UNIQUE KEY uk_users_email (email)
   - KEY idx_users_department_id (department_id)
   - KEY idx_users_status (status)
   - KEY idx_users_created_at (created_at)

2. **Departments表索引**
   - PRIMARY KEY (id)
   - KEY idx_departments_parent_id (parent_id)
   - KEY idx_departments_manager_id (manager_id)
   - KEY idx_departments_level (level)
   - KEY idx_departments_status (status)

3. **WeeklyReports表索引**
   - PRIMARY KEY (id)
   - KEY idx_reports_user_id (user_id)
   - KEY idx_reports_template_id (template_id)
   - KEY idx_reports_status (status)
   - KEY idx_reports_week_start (week_start)
   - KEY idx_reports_created_at (created_at)
   - KEY idx_reports_user_week (user_id, week_start)

4. **Templates表索引**
   - PRIMARY KEY (id)
   - KEY idx_templates_created_by (created_by)
   - KEY idx_templates_department_id (department_id)
   - KEY idx_templates_status (status)
   - KEY idx_templates_is_default (is_default)

5. **Comments表索引**
   - PRIMARY KEY (id)
   - KEY idx_comments_report_id (report_id)
   - KEY idx_comments_user_id (user_id)
   - KEY idx_comments_parent_id (parent_id)
   - KEY idx_comments_created_at (created_at)

## 约束条件

### 外键约束
1. fk_users_department_id: Users.department_id -> Departments.id
2. fk_departments_parent_id: Departments.parent_id -> Departments.id
3. fk_departments_manager_id: Departments.manager_id -> Users.id
4. fk_reports_user_id: WeeklyReports.user_id -> Users.id
5. fk_reports_template_id: WeeklyReports.template_id -> Templates.id
6. fk_reports_reviewed_by: WeeklyReports.reviewed_by -> Users.id
7. fk_templates_created_by: Templates.created_by -> Users.id
8. fk_templates_department_id: Templates.department_id -> Departments.id
9. fk_comments_report_id: Comments.report_id -> WeeklyReports.id
10. fk_comments_user_id: Comments.user_id -> Users.id
11. fk_comments_parent_id: Comments.parent_id -> Comments.id

### 检查约束
1. week_end >= week_start (WeeklyReports表)
2. level > 0 (Departments表)
3. usage_count >= 0 (Templates表)
4. view_count >= 0 (WeeklyReports表)

## 数据完整性设计

### 1. 软删除
- 所有核心表都包含 `deleted_at` 字段
- 删除操作设置时间戳而非物理删除
- 查询时过滤 `deleted_at IS NULL` 的记录

### 2. 时间戳
- `created_at`: 记录创建时间，不可更新
- `updated_at`: 记录最后更新时间，自动更新
- `deleted_at`: 软删除时间戳

### 3. 状态管理
- Users: ACTIVE, INACTIVE, SUSPENDED
- Departments: ACTIVE, INACTIVE  
- WeeklyReports: DRAFT, SUBMITTED, REVIEWED, PUBLISHED
- Templates: ACTIVE, INACTIVE
- Comments: ACTIVE, HIDDEN, DELETED

### 4. 级联操作
- 删除部门时，检查是否有用户关联
- 删除用户时，周报保留但标记创建者
- 删除周报时，相关评论级联软删除