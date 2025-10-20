# 周报管理系统 API 接口文档

**最后更新**: 2025-10-09 16:00
**版本**: v2.2.0
**基础URL**: `http://localhost:8081/api`
**认证方式**: JWT Bearer Token
**响应格式**: JSON

**v2.2.0 更新内容** (2025-10-09):
- ✅ 项目管理模块：修正所有字段名称（projectName→name, projectDescription→description等）
- ✅ 项目管理模块：删除不存在的字段（startDate, endDate, budget, objectives等）
- ✅ 项目管理模块：统一响应格式为标准ApiResponse结构
- ✅ 项目管理模块：明确所有接口的权限要求和业务限制
- ✅ 项目管理模块：补充完整的请求/响应示例
- ✅ 全局：统一所有响应格式包含success, message, data, timestamp字段

**v2.1.0 更新内容** (2025-10-09):
- ✅ 文件管理模块：weeklyReportId参数改为可选
- ✅ 文件管理模块：明确三角色权限（MANAGER、ADMIN、SUPER_ADMIN）
- ✅ 文件管理模块：新增文件去重、访问控制等特性说明
- ✅ 文件管理模块：新增MinIO存储配置和限制说明

---

## 📋 目录

1. [认证模块](#1-认证模块-auth)
2. [用户管理模块](#2-用户管理模块-users)
3. [项目管理模块](#3-项目管理模块-projects)
4. [任务管理模块](#4-任务管理模块-tasks)
5. [周报管理模块](#5-周报管理模块-weekly-reports)
6. [评论管理模块](#6-评论管理模块-comments)
7. [文件管理模块](#7-文件管理模块-file-management)
8. [健康检查](#8-健康检查-health)

---

## 通用响应格式

### 成功响应
```json
{
    "success": true,
    "message": "操作成功",
    "data": {},
    "timestamp": "2025-10-09T10:30:00"
}
```

### 错误响应
```json
{
    "success": false,
    "message": "错误描述",
    "data": null,
    "timestamp": "2025-10-09T10:30:00"
}
```

### 分页响应
```json
{
    "success": true,
    "data": {
        "content": [],
        "totalElements": 100,
        "totalPages": 10,
        "size": 10,
        "number": 0,
        "first": true,
        "last": false
    }
}
```

---

## 1. 认证模块 (Auth)

**基础路径**: `/auth`

### 1.1 用户登录
- **接口**: `POST /auth/login`
- **说明**: 用户登录，返回JWT令牌
- **认证**: 不需要
- **请求体**:
```json
{
    "usernameOrEmail": "admin",
    "password": "password123",
    "rememberMe": false
}
```
- **响应**:
```json
{
    "success": true,
    "data": {
        "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        "tokenType": "Bearer",
        "expiresIn": 86400,
        "user": {
            "id": 1,
            "username": "admin",
            "email": "admin@example.com",
            "role": "SUPER_ADMIN",
            "status": "ACTIVE",
            "fullName": "系统管理员"
        }
    }
}
```

### 1.2 用户注册
- **接口**: `POST /auth/register`
- **说明**: 注册新用户
- **认证**: 不需要
- **请求体**:
```json
{
    "username": "newuser",
    "email": "user@example.com",
    "password": "Password123!",
    "fullName": "张三",
    "role": "MANAGER"
}
```
- **响应**: 同登录响应

### 1.3 刷新令牌
- **接口**: `POST /auth/refresh`
- **说明**: 使用刷新令牌获取新的访问令牌
- **认证**: 不需要
- **请求体**:
```json
{
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```
- **响应**: 同登录响应

### 1.4 退出登录
- **接口**: `POST /auth/logout`
- **说明**: 用户登出
- **认证**: 需要
- **请求体**: 无
- **响应**:
```json
{
    "success": true,
    "message": "登出成功"
}
```

### 1.5 修改密码
- **接口**: `POST /auth/change-password`
- **说明**: 修改当前用户密码
- **认证**: 需要
- **请求体**:
```json
{
    "oldPassword": "OldPassword123!",
    "newPassword": "NewPassword123!"
}
```
- **响应**:
```json
{
    "success": true,
    "message": "密码修改成功"
}
```

### 1.6 检查用户名是否可用
- **接口**: `GET /auth/check-username?username=testuser`
- **说明**: 检查用户名是否已被使用
- **认证**: 不需要
- **参数**:
  - `username` (required): 要检查的用户名
- **响应**:
```json
{
    "success": true,
    "data": {
        "available": true,
        "message": "用户名可用"
    }
}
```

### 1.7 检查邮箱是否可用
- **接口**: `GET /auth/check-email?email=test@example.com`
- **说明**: 检查邮箱是否已被使用
- **认证**: 不需要
- **参数**:
  - `email` (required): 要检查的邮箱
- **响应**:
```json
{
    "success": true,
    "data": {
        "available": true,
        "message": "邮箱可用"
    }
}
```

---

## 2. 用户管理模块 (Users)

**基础路径**: `/users`
**权限要求**: 大部分接口需要 ADMIN 或 SUPER_ADMIN 角色

### 2.1 创建用户
- **接口**: `POST /users`
- **说明**: 创建新用户（管理员）
- **认证**: 需要 (ADMIN/SUPER_ADMIN)
- **请求体**:
```json
{
    "username": "newuser",
    "email": "user@example.com",
    "password": "Password123!",
    "fullName": "张三",
    "role": "MANAGER",
    "department": "技术部"
}
```
- **响应**:
```json
{
    "success": true,
    "data": {
        "id": 5,
        "username": "newuser",
        "email": "user@example.com",
        "fullName": "张三",
        "role": "MANAGER",
        "status": "ACTIVE",
        "department": "技术部",
        "createdAt": "2025-10-09T10:30:00"
    }
}
```

### 2.2 获取当前用户信息
- **接口**: `GET /users/profile`
- **说明**: 获取当前登录用户的详细信息
- **认证**: 需要
- **响应**:
```json
{
    "success": true,
    "data": {
        "id": 1,
        "username": "admin",
        "email": "admin@example.com",
        "fullName": "系统管理员",
        "role": "SUPER_ADMIN",
        "status": "ACTIVE",
        "department": "管理部",
        "createdAt": "2025-09-01T00:00:00",
        "updatedAt": "2025-10-09T10:30:00"
    }
}
```

### 2.3 更新当前用户信息
- **接口**: `PUT /users/profile`
- **说明**: 更新当前登录用户的信息
- **认证**: 需要
- **请求体**:
```json
{
    "fullName": "新名字",
    "email": "newemail@example.com",
    "department": "新部门"
}
```
- **响应**: 同获取用户信息

### 2.4 获取用户详情
- **接口**: `GET /users/{userId}`
- **说明**: 获取指定用户的详细信息
- **认证**: 需要 (ADMIN/SUPER_ADMIN)
- **参数**:
  - `userId` (path): 用户ID
- **响应**: 同获取当前用户信息

### 2.5 获取用户列表（分页）
- **接口**: `GET /users?page=0&size=10&sortBy=createdAt&sortDir=desc`
- **说明**: 分页获取用户列表
- **认证**: 需要 (ADMIN/SUPER_ADMIN)
- **参数**:
  - `page` (optional, default=0): 页码
  - `size` (optional, default=10): 每页大小
  - `sortBy` (optional, default=createdAt): 排序字段
  - `sortDir` (optional, default=desc): 排序方向 (asc/desc)
- **响应**:
```json
{
    "success": true,
    "data": {
        "content": [
            {
                "id": 1,
                "username": "admin",
                "email": "admin@example.com",
                "fullName": "系统管理员",
                "role": "SUPER_ADMIN",
                "status": "ACTIVE"
            }
        ],
        "totalElements": 50,
        "totalPages": 5,
        "size": 10,
        "number": 0
    }
}
```

### 2.6 获取用户列表（不分页）
- **接口**: `GET /users/fast`
- **说明**: 快速获取所有用户列表（不分页）
- **认证**: 需要 (ADMIN/SUPER_ADMIN)
- **响应**:
```json
{
    "success": true,
    "data": [
        {
            "id": 1,
            "username": "admin",
            "fullName": "系统管理员",
            "role": "SUPER_ADMIN"
        }
    ]
}
```

### 2.7 搜索用户
- **接口**: `GET /users/search?keyword=张三`
- **说明**: 根据关键字搜索用户（用户名、邮箱、姓名）
- **认证**: 需要 (ADMIN/SUPER_ADMIN)
- **参数**:
  - `keyword` (required): 搜索关键字
- **响应**: 同获取用户列表

### 2.8 按角色获取用户
- **接口**: `GET /users/role/{role}`
- **说明**: 获取指定角色的所有用户
- **认证**: 需要 (ADMIN/SUPER_ADMIN)
- **参数**:
  - `role` (path): 用户角色 (MANAGER/ADMIN/SUPER_ADMIN)
- **响应**: 同获取用户列表

### 2.9 更新用户状态
- **接口**: `PUT /users/{userId}/status`
- **说明**: 更新用户状态
- **认证**: 需要 (ADMIN/SUPER_ADMIN)
- **请求体**:
```json
{
    "status": "ACTIVE"
}
```
- **响应**: 同获取用户信息

### 2.10 启用用户
- **接口**: `PUT /users/{userId}/enable`
- **说明**: 启用用户账号
- **认证**: 需要 (ADMIN/SUPER_ADMIN)
- **响应**:
```json
{
    "success": true,
    "message": "用户已启用"
}
```

### 2.11 禁用用户
- **接口**: `PUT /users/{userId}/disable`
- **说明**: 禁用用户账号
- **认证**: 需要 (ADMIN/SUPER_ADMIN)
- **响应**:
```json
{
    "success": true,
    "message": "用户已禁用"
}
```

### 2.12 更新用户角色
- **接口**: `PUT /users/{userId}/role`
- **说明**: 更新用户角色
- **认证**: 需要 (SUPER_ADMIN)
- **请求体**:
```json
{
    "role": "ADMIN"
}
```
- **响应**: 同获取用户信息

### 2.13 更新用户信息
- **接口**: `PUT /users/{userId}`
- **说明**: 更新指定用户的信息
- **认证**: 需要 (ADMIN/SUPER_ADMIN)
- **请求体**:
```json
{
    "fullName": "新名字",
    "email": "newemail@example.com",
    "department": "新部门",
    "role": "MANAGER"
}
```
- **响应**: 同获取用户信息

### 2.14 删除用户
- **接口**: `DELETE /users/{userId}`
- **说明**: 删除用户（软删除）
- **认证**: 需要 (SUPER_ADMIN)
- **响应**:
```json
{
    "success": true,
    "message": "用户已删除"
}
```

### 2.15 获取用户统计信息
- **接口**: `GET /users/statistics`
- **说明**: 获取用户统计数据
- **认证**: 需要 (ADMIN/SUPER_ADMIN)
- **响应**:
```json
{
    "success": true,
    "data": {
        "totalUsers": 50,
        "activeUsers": 45,
        "inactiveUsers": 5,
        "managerCount": 30,
        "adminCount": 15,
        "superAdminCount": 5
    }
}
```

### 2.16 重置用户密码
- **接口**: `POST /users/{userId}/reset-password`
- **说明**: 管理员重置用户密码
- **认证**: 需要 (ADMIN/SUPER_ADMIN)
- **请求体**:
```json
{
    "newPassword": "NewPassword123!"
}
```
- **响应**:
```json
{
    "success": true,
    "message": "密码已重置"
}
```

---

## 3. 项目管理模块 (Projects)

**基础路径**: `/projects`
**权限要求**: 大部分接口需要 MANAGER 及以上角色

### 3.1 获取项目列表（分页）
- **接口**: `GET /projects?page=0&size=10&sortBy=createdAt&sortDir=desc`
- **说明**: 分页获取项目列表（根据用户角色过滤）
- **权限**: MANAGER, ADMIN, SUPER_ADMIN
- **参数**:
  - `page` (optional, default=0): 页码
  - `size` (optional, default=10): 每页大小
  - `sortBy` (optional, default=createdAt): 排序字段
  - `sortDir` (optional, default=desc): 排序方向
- **响应**:
```json
{
    "success": true,
    "message": "获取项目列表成功",
    "data": {
        "content": [
            {
                "id": 1,
                "name": "项目A",
                "description": "项目描述",
                "members": "张三、李四",
                "expectedResults": "预期成果",
                "timeline": "2025年10月-12月",
                "stopLoss": "预算超出20%",
                "status": "ACTIVE",
                "priority": "MEDIUM",
                "progress": 50,
                "approvalStatus": "ADMIN_APPROVED",
                "createdBy": 1,
                "createdByUsername": "zhangsan",
                "createdAt": "2025-10-01T10:00:00",
                "updatedAt": "2025-10-09T10:00:00"
            }
        ],
        "totalElements": 20,
        "totalPages": 2,
        "size": 10,
        "number": 0
    },
    "timestamp": "2025-10-09T10:30:00"
}
```

### 3.2 项目过滤查询
- **接口**: `GET /projects/filter?name=项目A&status=ACTIVE&priority=HIGH`
- **说明**: 根据多个条件筛选项目
- **权限**: MANAGER, ADMIN, SUPER_ADMIN
- **参数**:
  - `name` (optional): 项目名称（模糊搜索）
  - `status` (optional): 项目状态 (ACTIVE|COMPLETED|PAUSED|CANCELLED)
  - `priority` (optional): 优先级 (LOW|MEDIUM|HIGH|URGENT)
  - `approvalStatus` (optional): 审批状态
  - `createdBy` (optional): 创建者ID
  - `page`, `size`, `sortBy`, `sortDir`: 分页参数
- **响应**: 同获取项目列表

### 3.3 创建项目
- **接口**: `POST /projects`
- **说明**: 创建新项目
- **权限**: MANAGER, ADMIN, SUPER_ADMIN
- **请求体**:
```json
{
    "name": "新项目名称",
    "description": "项目内容描述",
    "members": "张三、李四、王五",
    "expectedResults": "预期达成的成果",
    "timeline": "2025年10月-12月，共3个月",
    "stopLoss": "预算超出20%或进度延误1个月",
    "phases": [
        {
            "name": "需求分析阶段",
            "description": "完成需求调研和分析",
            "expectedDuration": "2周"
        }
    ]
}
```
**字段说明**:
- `name` (required, 2-200字符): 项目名称
- `description` (optional, <5000字符): 项目内容描述
- `members` (optional, <5000字符): 项目成员
- `expectedResults` (optional, <5000字符): 预期结果
- `timeline` (optional, <5000字符): 时间线
- `stopLoss` (optional, <5000字符): 止损点
- `phases` (optional): 项目阶段列表

- **响应**:
```json
{
    "success": true,
    "message": "项目创建成功",
    "data": {
        "id": 5,
        "name": "新项目名称",
        "description": "项目内容描述",
        "approvalStatus": "AI_ANALYZING",
        "status": "ACTIVE",
        "priority": "MEDIUM",
        "progress": 0,
        "createdBy": 1,
        "createdByUsername": "zhangsan",
        "createdAt": "2025-10-09T10:30:00",
        "updatedAt": "2025-10-09T10:30:00"
    },
    "timestamp": "2025-10-09T10:30:00"
}
```

### 3.4 获取待审核项目
- **接口**: `GET /projects/pending-review?page=0&size=10`
- **说明**: 获取待当前用户审核的项目（ADMIN看到待管理员审核，SUPER_ADMIN看到待超级管理员审核）
- **权限**: ADMIN, SUPER_ADMIN
- **响应**: 同获取项目列表

### 3.5 获取已通过项目
- **接口**: `GET /projects/approved?page=0&size=10`
- **说明**: 获取已审批通过的项目
- **权限**: ADMIN, SUPER_ADMIN
- **响应**: 同获取项目列表

### 3.6 获取已拒绝项目
- **接口**: `GET /projects/rejected?page=0&size=10`
- **说明**: 获取已被拒绝的项目
- **权限**: ADMIN, SUPER_ADMIN
- **响应**: 同获取项目列表

### 3.7 获取项目详情
- **接口**: `GET /projects/{id}`
- **说明**: 获取项目详细信息
- **权限**: MANAGER (仅自己创建的), ADMIN, SUPER_ADMIN
- **响应**:
```json
{
    "success": true,
    "message": "获取项目详情成功",
    "data": {
        "id": 1,
        "name": "项目A",
        "description": "详细的项目内容描述",
        "members": "张三、李四、王五",
        "expectedResults": "完成系统开发并上线",
        "timeline": "2025年10月-12月",
        "stopLoss": "预算超出20%",
        "status": "ACTIVE",
        "priority": "HIGH",
        "progress": 60,
        "approvalStatus": "ADMIN_APPROVED",
        "aiAnalysisId": 123,
        "adminReviewerId": 2,
        "superAdminReviewerId": null,
        "rejectionReason": null,
        "createdBy": 1,
        "createdByUsername": "zhangsan",
        "createdAt": "2025-09-01T10:00:00",
        "updatedAt": "2025-10-09T10:00:00",
        "phases": [
            {
                "id": 1,
                "name": "需求分析",
                "description": "完成需求调研和分析",
                "expectedDuration": "2周"
            }
        ],
        "aiAnalysisResult": {
            "id": 123,
            "analysisType": "PROJECT",
            "status": "COMPLETED",
            "result": "项目方案可行"
        }
    },
    "timestamp": "2025-10-09T10:30:00"
}
```

### 3.8 更新项目
- **接口**: `PUT /projects/{id}`
- **说明**: 更新项目信息
- **权限**: MANAGER (仅创建者)
- **限制**: 只有草稿或已拒绝状态的项目可以更新
- **请求体**: 同创建项目
- **响应**:
```json
{
    "success": true,
    "message": "项目更新成功",
    "data": {
        "id": 1,
        "name": "更新后的项目名称",
        "description": "更新后的描述",
        "approvalStatus": "AI_ANALYZING",
        "updatedAt": "2025-10-09T10:30:00"
    },
    "timestamp": "2025-10-09T10:30:00"
}
```

### 3.9 删除项目
- **接口**: `DELETE /projects/{id}`
- **说明**: 删除项目
- **权限**: MANAGER (仅创建者)
- **限制**: 只有草稿状态的项目可以删除
- **响应**:
```json
{
    "success": true,
    "message": "项目删除成功",
    "data": null,
    "timestamp": "2025-10-09T10:30:00"
}
```

### 3.10 提交项目进行AI分析
- **接口**: `PUT /projects/{id}/submit`
- **说明**: 提交项目进入审批流程
- **权限**: MANAGER (仅创建者)
- **限制**: 只有草稿或已拒绝状态的项目可以提交
- **响应**:
```json
{
    "success": true,
    "message": "项目提交成功，等待AI分析",
    "data": null,
    "timestamp": "2025-10-09T10:30:00"
}
```

### 3.11 强制提交项目（跳过AI）
- **接口**: `POST /projects/{id}/force-submit`
- **说明**: 跳过AI分析，直接进入管理员审核
- **权限**: ADMIN, SUPER_ADMIN
- **响应**:
```json
{
    "success": true,
    "message": "项目已强制提交到管理员审核",
    "data": null,
    "timestamp": "2025-10-09T10:30:00"
}
```

### 3.12 重新提交项目
- **接口**: `PUT /projects/{id}/resubmit`
- **说明**: 修改后重新提交项目
- **权限**: MANAGER (仅创建者)
- **请求体**: `ProjectUpdateRequest`
- **限制**: 只有已拒绝状态的项目可以重新提交
- **响应**:
```json
{
    "success": true,
    "message": "项目重新提交成功",
    "data": null,
    "timestamp": "2025-10-09T10:30:00"
}
```

### 3.13 管理员审核通过
- **接口**: `PUT /projects/{id}/approve`
- **别名**: `PUT /projects/{id}/admin-approve`
- **说明**: 管理员审批通过项目
- **权限**: ADMIN
- **响应**:
```json
{
    "success": true,
    "message": "管理员审批通过",
    "data": null,
    "timestamp": "2025-10-09T10:30:00"
}
```

### 3.14 超级管理员最终审核
- **接口**: `PUT /projects/{id}/final-approve`
- **别名**: `PUT /projects/{id}/super-admin-approve`
- **说明**: 超级管理员最终审批通过
- **权限**: SUPER_ADMIN
- **响应**:
```json
{
    "success": true,
    "message": "超级管理员最终审批通过",
    "data": null,
    "timestamp": "2025-10-09T10:30:00"
}
```

### 3.15 拒绝项目
- **接口**: `PUT /projects/{id}/reject`
- **说明**: 拒绝项目
- **权限**: ADMIN, SUPER_ADMIN
- **请求体**: String (拒绝原因)
- **说明**:
  - ADMIN可以拒绝AI通过、管理员审核中的项目
  - SUPER_ADMIN可以拒绝管理员通过的项目
- **响应**:
```json
{
    "success": true,
    "message": "项目已拒绝",
    "data": null,
    "timestamp": "2025-10-09T10:30:00"
}
```

### 3.16 获取我的项目
- **接口**: `GET /projects/my?approvalStatus={status}&page=0&size=10`
- **说明**: 获取当前用户创建的项目
- **权限**: MANAGER
- **查询参数**:
  - `approvalStatus` (optional): 按审批状态过滤
  - `page`, `size`: 分页参数
- **响应**: 同获取项目列表

### 3.17 获取待审核项目
- **接口**: `GET /projects/pending?page=0&size=10`
- **说明**: 获取待审核项目列表
- **权限**: ADMIN, SUPER_ADMIN
- **响应**: 同获取项目列表（与 `/pending-review` 相同）

### 3.18 获取已审核项目
- **接口**: `GET /projects/reviewed?page=0&size=10`
- **说明**: 获取当前用户审核过的项目
- **权限**: ADMIN, SUPER_ADMIN
- **响应**: 同获取项目列表

### 3.19 获取项目阶段
- **接口**: `GET /projects/{projectId}/phases`
- **说明**: 获取项目的所有阶段
- **权限**: MANAGER, ADMIN, SUPER_ADMIN
- **响应**:
```json
{
    "success": true,
    "message": "获取项目阶段成功",
    "data": [
        {
            "id": 1,
            "projectId": 1,
            "name": "需求分析",
            "description": "完成需求调研和分析",
            "expectedDuration": "2周"
        }
    ],
    "timestamp": "2025-10-09T10:30:00"
}
```

### 3.20 创建项目阶段
- **接口**: `POST /projects/{projectId}/phases`
- **说明**: 为项目添加阶段
- **权限**: MANAGER, ADMIN, SUPER_ADMIN
- **说明**: MANAGER只能为自己创建的项目添加阶段
- **请求体**: `ProjectPhaseCreateRequest`
```json
{
    "name": "需求分析",
    "description": "完成需求调研和分析",
    "expectedDuration": "2周"
}
```
- **响应**:
```json
{
    "success": true,
    "message": "项目阶段创建成功",
    "data": {
        "id": 2,
        "projectId": 1,
        "name": "需求分析",
        "description": "完成需求调研和分析",
        "expectedDuration": "2周"
    },
    "timestamp": "2025-10-09T10:30:00"
}
```

### 项目审批状态枚举
```java
public enum ApprovalStatus {
    AI_ANALYZING,            // AI分析中
    AI_APPROVED,             // AI分析通过
    AI_REJECTED,             // AI分析拒绝
    ADMIN_REVIEWING,         // 管理员审核中
    ADMIN_APPROVED,          // 管理员审核通过
    ADMIN_REJECTED,          // 管理员审核拒绝
    SUPER_ADMIN_REVIEWING,   // 超级管理员审核中
    SUPER_ADMIN_APPROVED,    // 超级管理员审核通过
    SUPER_ADMIN_REJECTED,    // 超级管理员审核拒绝
    FINAL_APPROVED           // 最终批准
}
```

---

## 4. 任务管理模块 (Tasks)

**基础路径**: `/tasks`
**权限要求**: MANAGER 及以上角色

### 4.1 获取任务列表（分页）
- **接口**: `GET /tasks?page=0&size=10&sortBy=createdAt&sortDir=desc`
- **说明**: 分页获取任务列表（主管看自己创建的，管理员看所有）
- **认证**: 需要 (MANAGER及以上)
- **参数**:
  - `page`, `size`, `sortBy`, `sortDir`
- **响应**:
```json
{
    "success": true,
    "data": {
        "content": [
            {
                "id": 1,
                "taskName": "任务A",
                "personnelAssignment": "张三、李四",
                "timeline": "2025-10-01 至 2025-10-15",
                "expectedResults": "完成需求分析",
                "createdBy": 1,
                "createdAt": "2025-09-25T10:00:00"
            }
        ],
        "totalElements": 15,
        "totalPages": 2
    }
}
```

### 4.2 创建任务
- **接口**: `POST /tasks`
- **说明**: 创建新任务
- **认证**: 需要 (MANAGER及以上)
- **请求体**:
```json
{
    "taskName": "新任务",
    "personnelAssignment": "张三、李四",
    "timeline": "2025-10-01 至 2025-10-15",
    "expectedResults": "完成开发工作"
}
```
- **响应**:
```json
{
    "success": true,
    "data": {
        "id": 10,
        "taskName": "新任务",
        "createdAt": "2025-10-09T10:30:00"
    }
}
```

### 4.3 获取任务详情
- **接口**: `GET /tasks/{id}`
- **说明**: 获取任务详细信息
- **认证**: 需要 (MANAGER及以上)
- **响应**:
```json
{
    "success": true,
    "data": {
        "id": 1,
        "taskName": "任务A",
        "personnelAssignment": "张三、李四",
        "timeline": "2025-10-01 至 2025-10-15",
        "expectedResults": "完成需求分析",
        "createdBy": 1,
        "createdByName": "王主管",
        "createdAt": "2025-09-25T10:00:00"
    }
}
```

### 4.4 更新任务
- **接口**: `PUT /tasks/{id}`
- **说明**: 更新任务（仅创建者）
- **认证**: 需要 (MANAGER)
- **请求体**: 同创建任务
- **响应**: 同获取任务详情

### 4.5 删除任务
- **接口**: `DELETE /tasks/{id}`
- **说明**: 删除任务（仅创建者）
- **认证**: 需要 (MANAGER)
- **响应**:
```json
{
    "success": true,
    "message": "任务已删除"
}
```

### 4.6 获取我的任务
- **接口**: `GET /tasks/my`
- **说明**: 获取当前用户创建的所有任务
- **认证**: 需要 (MANAGER及以上)
- **响应**:
```json
{
    "success": true,
    "data": [
        {
            "id": 1,
            "taskName": "任务A",
            "createdAt": "2025-09-25T10:00:00"
        }
    ]
}
```

### 4.7 按类型获取任务
- **接口**: `GET /tasks/by-type/{taskType}`
- **说明**: 根据任务类型获取任务
- **认证**: 需要 (MANAGER及以上)
- **参数**:
  - `taskType` (path): 任务类型
- **响应**: 同获取我的任务

### 4.8 获取任务统计信息
- **接口**: `GET /tasks/statistics`
- **说明**: 获取任务统计数据
- **认证**: 需要 (MANAGER及以上)
- **响应**:
```json
{
    "success": true,
    "data": {
        "totalTasks": 50,
        "completedTasks": 30,
        "pendingTasks": 20,
        "routineTasks": 0,
        "developmentTasks": 0,
        "completionRate": 60.0
    }
}
```

### 4.9 获取周报关联任务
- **接口**: `GET /tasks/by-report/{reportId}`
- **说明**: 获取指定周报关联的所有任务
- **认证**: 需要 (MANAGER及以上)
- **参数**:
  - `reportId` (path): 周报ID
- **响应**:
```json
{
    "success": true,
    "data": [
        {
            "id": 1,
            "taskName": "任务A",
            "actualResults": "实际完成情况",
            "resultDifferenceAnalysis": "差异分析",
            "reportSection": "THIS_WEEK_REPORT",
            "taskTypeString": "ROUTINE"
        }
    ]
}
```

---

## 5. 周报管理模块 (Weekly Reports)

**基础路径**: `/weekly-reports`
**权限要求**: 所有认证用户

### 5.1 创建周报
- **接口**: `POST /weekly-reports`
- **说明**: 创建新周报
- **认证**: 需要
- **请求体**:
```json
{
    "userId": 1,
    "title": "2025年第40周周报",
    "reportWeek": "2025-10-01 至 2025-10-05",
    "weekStart": "2025-10-01",
    "weekEnd": "2025-10-05",
    "content": {
        "routineTasks": [
            {
                "task_id": "1",
                "actual_result": "完成需求分析文档",
                "analysisofResultDifferences": "按计划完成"
            }
        ],
        "developmentalTasks": [
            {
                "project_id": "1",
                "phase_id": "1",
                "actual_result": "完成系统设计",
                "analysisofResultDifferences": "超出预期"
            }
        ]
    },
    "nextWeekPlan": {
        "routineTasks": [
            {
                "task_id": "2"
            }
        ],
        "developmentalTasks": [
            {
                "project_id": "1",
                "phase_id": "2"
            }
        ]
    },
    "additionalNotes": "本周工作顺利"
}
```
- **响应**:
```json
{
    "success": true,
    "message": "周报创建成功",
    "data": {
        "id": 10,
        "title": "2025年第40周周报",
        "approvalStatus": "DRAFT",
        "createdAt": "2025-10-09T10:30:00"
    }
}
```

### 5.2 提交周报
- **接口**: `PUT /weekly-reports/{id}/submit`
- **说明**: 提交周报进入审批流程
- **认证**: 需要（仅创建者）
- **响应**:
```json
{
    "success": true,
    "message": "周报提交成功，等待AI分析"
}
```

### 5.3 强制提交周报
- **接口**: `PUT /weekly-reports/{id}/force-submit`
- **说明**: AI拒绝后强制提交到管理员审核
- **认证**: 需要（仅创建者）
- **响应**:
```json
{
    "success": true,
    "message": "周报强行提交成功，已转入管理员审核"
}
```

### 5.4 AI审批通过
- **接口**: `PUT /weekly-reports/{id}/ai-approve?aiAnalysisId=123`
- **说明**: AI分析通过（内部接口）
- **认证**: 需要
- **参数**:
  - `aiAnalysisId` (required): AI分析结果ID
- **响应**:
```json
{
    "success": true,
    "message": "AI分析通过"
}
```

### 5.5 管理员审批
- **接口**: `PUT /weekly-reports/{id}/admin-approve`
- **说明**: 管理员审批通过
- **认证**: 需要 (ADMIN/SUPER_ADMIN)
- **响应**:
```json
{
    "success": true,
    "message": "管理员审批通过"
}
```

### 5.6 拒绝周报
- **接口**: `PUT /weekly-reports/{id}/reject`
- **说明**: 拒绝周报
- **认证**: 需要 (ADMIN/SUPER_ADMIN)
- **请求体**:
```json
{
    "reason": "内容不完整，需要补充"
}
```
- **响应**:
```json
{
    "success": true,
    "message": "周报已拒绝"
}
```

### 5.7 获取周报详情
- **接口**: `GET /weekly-reports/{id}`
- **说明**: 获取周报详细信息
- **认证**: 需要（创建者或审批者）
- **响应**:
```json
{
    "success": true,
    "message": "获取周报详情成功",
    "data": {
        "id": 1,
        "userId": 1,
        "username": "zhangsan",
        "fullName": "张三",
        "title": "2025年第40周周报",
        "reportWeek": "2025-10-01 至 2025-10-05",
        "approvalStatus": "ADMIN_APPROVED",
        "content": {
            "routineTasks": [...],
            "developmentalTasks": [...]
        },
        "nextWeekPlan": {
            "routineTasks": [...],
            "developmentalTasks": [...]
        },
        "additionalNotes": "本周工作顺利",
        "createdAt": "2025-10-09T10:00:00",
        "updatedAt": "2025-10-09T15:00:00"
    }
}
```

### 5.8 更新周报
- **接口**: `PUT /weekly-reports/{id}`
- **说明**: 更新周报（仅草稿或被拒绝状态）
- **认证**: 需要（仅创建者）
- **请求体**: 同创建周报
- **响应**:
```json
{
    "success": true,
    "message": "周报更新成功",
    "data": {...}
}
```

### 5.9 获取我的周报列表
- **接口**: `GET /weekly-reports/my?page=0&size=20`
- **说明**: 获取当前用户的周报列表
- **认证**: 需要
- **参数**:
  - `page` (optional, default=0)
  - `size` (optional, default=20)
- **响应**:
```json
{
    "success": true,
    "message": "获取我的周报列表成功",
    "data": {
        "content": [
            {
                "id": 1,
                "title": "2025年第40周周报",
                "approvalStatus": "ADMIN_APPROVED",
                "createdAt": "2025-10-09T10:00:00"
            }
        ],
        "totalElements": 10,
        "totalPages": 1
    }
}
```

### 5.10 获取所有周报
- **接口**: `GET /weekly-reports?status=ADMIN_APPROVED&page=0&size=20`
- **说明**: 获取周报列表（管理员看所有，普通用户看自己的）
- **认证**: 需要
- **参数**:
  - `status` (optional): 按状态过滤
  - `page`, `size`
- **响应**: 同获取我的周报列表

### 5.11 获取待审批周报
- **接口**: `GET /weekly-reports/pending?status=ADMIN_REVIEWING&page=0&size=20`
- **说明**: 获取待审批的周报
- **认证**: 需要 (ADMIN/SUPER_ADMIN)
- **参数**:
  - `status` (optional, default=ADMIN_REVIEWING)
  - `page`, `size`
- **响应**: 同获取我的周报列表

---

## 6. 评论管理模块 (Comments)

**基础路径**: `/api/weekly-reports/{weeklyReportId}/comments`
**权限要求**: MANAGER 及以上角色

### 6.1 获取周报评论列表
- **接口**: `GET /api/weekly-reports/{weeklyReportId}/comments?page=0&size=10`
- **说明**: 获取指定周报的所有评论和回复
- **认证**: 需要 (MANAGER及以上)
- **参数**:
  - `weeklyReportId` (path): 周报ID
  - `page` (optional, default=0)
  - `size` (optional, default=10)
- **响应**:
```json
{
    "success": true,
    "data": {
        "comments": [
            {
                "id": 1,
                "content": "周报写得很详细",
                "commentType": "REVIEW",
                "status": "ACTIVE",
                "createdBy": {
                    "id": 2,
                    "username": "admin",
                    "fullName": "管理员"
                },
                "createdAt": "2025-10-09T10:30:00",
                "replies": [
                    {
                        "id": 2,
                        "content": "谢谢",
                        "createdBy": {
                            "id": 1,
                            "username": "user",
                            "fullName": "张三"
                        },
                        "createdAt": "2025-10-09T11:00:00"
                    }
                ]
            }
        ],
        "totalElements": 5,
        "totalPages": 1
    }
}
```

### 6.2 创建评论
- **接口**: `POST /api/weekly-reports/{weeklyReportId}/comments`
- **说明**: 为周报创建评论或回复
- **认证**: 需要 (MANAGER及以上)
- **请求体**:
```json
{
    "weeklyReportId": 1,
    "content": "评论内容",
    "commentType": "REVIEW",
    "parentCommentId": null
}
```
- **响应**:
```json
{
    "success": true,
    "data": {
        "id": 10,
        "content": "评论内容",
        "commentType": "REVIEW",
        "status": "ACTIVE",
        "createdAt": "2025-10-09T10:30:00"
    }
}
```

---

## 7. 文件管理模块 (File Management)

**基础路径**: `/file-management`
**权限要求**: MANAGER、ADMIN、SUPER_ADMIN（所有认证用户）
**存储方式**: MinIO对象存储
**文件大小限制**: 100MB
**支持的文件类型**: 图片(jpg/png/gif)、PDF、Office文档(doc/docx/xls/xlsx/ppt/pptx)、压缩包(zip/rar)、文本(txt)

**附件类型说明** (AttachmentType):
- `ROUTINE_TASK_RESULT`: 日常任务完成情况
- `ROUTINE_TASK_ANALYSIS`: 日常任务分析总结
- `DEV_TASK_RESULT`: 发展任务完成情况
- `DEV_TASK_ANALYSIS`: 发展任务分析总结
- `ADDITIONAL_NOTES`: 补充说明
- `DEVELOPMENT_OPPORTUNITIES`: 发展机会
- `GENERAL`: 通用附件

### 7.1 上传文件
- **接口**: `POST /file-management/upload`
- **说明**: 上传文件，可选择关联到周报的特定部分
- **权限**: MANAGER、ADMIN、SUPER_ADMIN
- **Content-Type**: `multipart/form-data`
- **请求参数**:
  - `file` (required): 文件
  - `weeklyReportId` (optional): 周报ID，不提供则为普通文件上传，不关联周报
  - `attachmentType` (optional, default=GENERAL): 附件类型，仅在提供weeklyReportId时有效
  - `relatedTaskId` (optional): 关联任务ID
  - `relatedProjectId` (optional): 关联项目ID
  - `relatedPhaseId` (optional): 关联阶段ID
  - `description` (optional): 附件描述
  - `displayOrder` (optional): 显示顺序
  - `isPublic` (optional, default=false): 是否公开
- **响应**:
```json
{
    "success": true,
    "data": {
        "fileId": 123,
        "originalFilename": "report.pdf",
        "storedFilename": "uuid-report.pdf",
        "fileSize": 1024000,
        "mimeType": "application/pdf",
        "uploadStatus": "COMPLETED",
        "attachmentType": "GENERAL",
        "fileUrl": "/file-management/download/123",
        "uploadedAt": "2025-10-09T10:30:00"
    }
}
```

### 7.2 批量上传文件
- **接口**: `POST /file-management/upload/batch`
- **说明**: 一次上传多个文件，可选择关联到同一个周报
- **权限**: MANAGER、ADMIN、SUPER_ADMIN
- **Content-Type**: `multipart/form-data`
- **请求参数**:
  - `files` (required): 文件列表
  - `weeklyReportId` (optional): 周报ID，不提供则为普通文件上传，不关联周报
  - 其他参数同单个上传
- **响应**:
```json
{
    "success": true,
    "data": [
        {
            "fileId": 123,
            "originalFilename": "file1.pdf",
            "uploadStatus": "COMPLETED"
        },
        {
            "fileId": 124,
            "originalFilename": "file2.pdf",
            "uploadStatus": "COMPLETED"
        }
    ]
}
```

### 7.3 下载文件
- **接口**: `GET /file-management/download/{fileId}`
- **说明**: 下载指定文件
- **权限**: MANAGER、ADMIN、SUPER_ADMIN
- **参数**:
  - `fileId` (path): 文件ID
- **响应**: 文件流（application/octet-stream）
- **Headers**:
  - `Content-Disposition`: attachment; filename="原始文件名"
  - `Content-Type`: 文件的MIME类型
  - `Content-Length`: 文件大小

### 7.4 获取文件预览URL
- **接口**: `GET /file-management/preview/{fileId}`
- **说明**: 获取文件的临时预览链接（MinIO预签名URL，7天有效期）
- **权限**: MANAGER、ADMIN、SUPER_ADMIN
- **参数**:
  - `fileId` (path): 文件ID
- **响应**:
```json
{
    "success": true,
    "data": "https://example.com/preview/temp-token-uuid"
}
```

### 7.5 删除文件
- **接口**: `DELETE /file-management/{fileId}`
- **说明**: 软删除文件（仅文件上传者可删除）
- **权限**: MANAGER、ADMIN、SUPER_ADMIN（需要是文件上传者）
- **参数**:
  - `fileId` (path): 文件ID
- **响应**:
```json
{
    "success": true,
    "data": "文件删除成功"
}
```

### 7.6 获取周报附件列表
- **接口**: `GET /file-management/weekly-report/{weeklyReportId}/attachments?attachmentType=GENERAL`
- **说明**: 获取指定周报的所有附件
- **权限**: MANAGER、ADMIN、SUPER_ADMIN
- **参数**:
  - `weeklyReportId` (path): 周报ID
  - `attachmentType` (optional): 附件类型过滤，可选值见附件类型说明
- **响应**:
```json
{
    "success": true,
    "data": [
        {
            "fileId": 123,
            "originalFilename": "report.pdf",
            "fileSize": 1024000,
            "attachmentType": "GENERAL",
            "description": "周报附件",
            "uploadedAt": "2025-10-09T10:30:00"
        }
    ]
}
```

### 7.7 更新附件信息
- **接口**: `PUT /file-management/attachment/{relationId}`
- **说明**: 更新附件的描述、显示顺序等信息
- **权限**: MANAGER、ADMIN、SUPER_ADMIN
- **参数**:
  - `relationId` (path): 周报附件关联ID
  - `description` (optional): 新描述
  - `displayOrder` (optional): 新显示顺序
- **响应**:
```json
{
    "success": true,
    "data": "附件信息更新成功"
}
```

### 7.8 移除附件关联
- **接口**: `DELETE /file-management/weekly-report/{weeklyReportId}/attachment/{fileId}`
- **说明**: 从周报中移除文件关联（不删除文件本身）
- **权限**: MANAGER、ADMIN、SUPER_ADMIN
- **参数**:
  - `weeklyReportId` (path): 周报ID
  - `fileId` (path): 文件ID
- **响应**:
```json
{
    "success": true,
    "data": "附件关联移除成功"
}
```

### 重要提示

**文件上传模式**:
1. **普通文件上传**（不关联周报）：
   - 不提供 `weeklyReportId` 参数
   - 适用场景：预先上传文件获取fileId，后续再关联到周报
   - 文件存储在MinIO中，返回fileId供后续使用

2. **关联周报上传**：
   - 提供 `weeklyReportId` 参数
   - 适用场景：直接上传文件并关联到指定周报
   - 自动创建周报附件关联关系

**文件去重机制**:
- 系统使用SHA-256哈希值检测重复文件
- 如果上传已存在的文件，将复用现有文件，不会重复存储
- 返回的fileId指向现有文件

**文件访问控制**:
- 公开文件（isPublic=true）：所有认证用户可访问
- 私有文件（isPublic=false）：仅文件上传者可访问
- 删除权限：仅文件上传者可删除自己上传的文件

**文件存储信息**:
- 存储位置：MinIO对象存储服务器
- 存储路径格式：`weekly-reports/{attachmentType}/{date}/{uuid}.{ext}`
- 预览URL有效期：7天（604800秒）

**测试状态**:
- ✅ 数据库表结构完整（3个表）
- ✅ MinIO配置正确（已验证）
- ✅ 三角色权限验证通过（MANAGER、ADMIN、SUPER_ADMIN）
- ✅ 文件上传功能测试通过（6/6测试100%通过）

---

## 8. 健康检查 (Health)

**基础路径**: `/health`
**权限要求**: 公开（部分接口需要认证）

### 8.1 服务健康检查
- **接口**: `GET /health`
- **说明**: 检查服务运行状态
- **认证**: 不需要
- **响应**:
```json
{
    "success": true,
    "message": "服务运行正常",
    "data": {
        "status": "UP",
        "timestamp": "2025-10-09T10:30:00",
        "service": "weekly-report-backend",
        "version": "1.0.0"
    }
}
```

### 8.2 服务就绪检查
- **接口**: `GET /health/ready`
- **说明**: 检查服务是否就绪
- **认证**: 不需要
- **响应**:
```json
{
    "success": true,
    "message": "服务就绪",
    "data": {
        "status": "READY",
        "ready": true,
        "timestamp": "2025-10-09T10:30:00"
    }
}
```

### 8.3 服务存活检查
- **接口**: `GET /health/live`
- **说明**: 检查服务是否存活
- **认证**: 不需要
- **响应**:
```json
{
    "success": true,
    "message": "服务正在运行",
    "data": {
        "status": "LIVE",
        "live": true,
        "timestamp": "2025-10-09T10:30:00"
    }
}
```

### 8.4 认证检查
- **接口**: `GET /health/authenticated`
- **说明**: 检查用户认证状态
- **认证**: 需要 (MANAGER及以上)
- **响应**:
```json
{
    "success": true,
    "message": "身份验证成功",
    "data": {
        "status": "AUTHENTICATED",
        "user": "admin",
        "authorities": ["ROLE_SUPER_ADMIN"],
        "timestamp": "2025-10-09T10:30:00"
    }
}
```

---

## 📝 附录

### A. 用户角色说明

| 角色 | 代码 | 权限说明 |
|------|------|---------|
| 主管 | MANAGER | 可以创建周报、任务、项目 |
| 管理员 | ADMIN | 可以审批项目和周报、管理用户 |
| 超级管理员 | SUPER_ADMIN | 系统最高权限 |

### B. 项目审批状态

| 状态 | 代码 | 说明 |
|------|------|------|
| 草稿 | DRAFT | 初始状态 |
| AI分析中 | AI_ANALYZING | 等待AI分析 |
| AI已通过 | AI_APPROVED | AI分析通过 |
| AI已拒绝 | AI_REJECTED | AI分析拒绝 |
| 管理员审核中 | ADMIN_REVIEWING | 等待管理员审核 |
| 管理员已通过 | ADMIN_APPROVED | 管理员审批通过 |
| 管理员已拒绝 | ADMIN_REJECTED | 管理员审批拒绝 |
| 超管审核中 | SUPER_ADMIN_REVIEWING | 等待超级管理员审核 |
| 超管已通过 | SUPER_ADMIN_APPROVED | 超级管理员审批通过 |
| 超管已拒绝 | SUPER_ADMIN_REJECTED | 超级管理员审批拒绝 |
| 最终通过 | FINAL_APPROVED | 最终审批通过 |

### C. 周报审批状态

| 状态 | 代码 | 说明 |
|------|------|------|
| 草稿 | DRAFT | 初始状态 |
| AI分析中 | AI_ANALYZING | 等待AI分析 |
| AI已通过 | AI_APPROVED | AI分析通过 |
| AI已拒绝 | AI_REJECTED | AI分析拒绝 |
| 管理员审核中 | ADMIN_REVIEWING | 等待管理员审核 |
| 管理员已通过 | ADMIN_APPROVED | 管理员审批通过 |
| 管理员已拒绝 | ADMIN_REJECTED | 管理员审批拒绝 |

### D. 附件类型

| 类型 | 代码 | 说明 |
|------|------|------|
| 通用附件 | GENERAL | 一般附件 |
| 日常任务结果 | ROUTINE_TASK_RESULT | 日常任务完成结果 |
| 发展任务结果 | DEVELOPMENT_TASK_RESULT | 发展任务完成结果 |
| 下周计划附件 | NEXT_WEEK_PLAN | 下周计划相关 |
| 其他 | OTHER | 其他类型 |

### E. 错误代码

| HTTP状态码 | 说明 |
|-----------|------|
| 200 | 请求成功 |
| 201 | 创建成功 |
| 400 | 请求参数错误 |
| 401 | 未认证 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

**文档版本**: v2.0.0
**生成日期**: 2025-10-09
**维护者**: 后端开发团队
