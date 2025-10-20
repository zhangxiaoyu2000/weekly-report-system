# API接口完整核对报告

## 生成信息
- **生成时间**: 2025-10-09
- **对比来源**: 实际Controller代码 vs API接口文档.md
- **核对模块**: 8个核心模块
- **总接口数**: 80个接口

---

## 1. 认证模块 (AuthController)

### 1.1 用户登录
- **接口**: `POST /auth/login`
- **权限**: 公开
- **请求体**: `LoginRequest`
  ```json
  {
    "usernameOrEmail": "用户名或邮箱",  // ✅ required
    "password": "密码"                  // ✅ required
  }
  ```
- **响应**: `AuthResponse`
  ```json
  {
    "userId": 1,
    "username": "user1",
    "email": "user@example.com",
    "role": "MANAGER",
    "accessToken": "eyJhbGc...",
    "refreshToken": "eyJhbGc...",
    "tokenType": "Bearer",
    "expiresIn": 3600
  }
  ```

### 1.2 用户注册
- **接口**: `POST /auth/register`
- **权限**: 公开（需检查注册是否启用）
- **请求体**: `RegisterRequest`
  ```json
  {
    "username": "新用户名",     // ✅ required, 3-50字符
    "email": "邮箱地址",        // ✅ required, 有效邮箱格式
    "password": "密码",         // ✅ required, 至少8位
    "role": "MANAGER"          // ❌ optional, 默认MANAGER
  }
  ```

### 1.3 刷新令牌
- **接口**: `POST /auth/refresh`
- **权限**: 需要有效的刷新令牌
- **请求体**: `RefreshTokenRequest`
  ```json
  {
    "refreshToken": "eyJhbGc..."  // ✅ required
  }
  ```

### 1.4 用户登出
- **接口**: `POST /auth/logout`
- **权限**: 需要认证
- **请求头**: `Authorization: Bearer <token>`

### 1.5 修改密码
- **接口**: `POST /auth/change-password`
- **权限**: 需要认证
- **请求体**: `ChangePasswordRequest`
  ```json
  {
    "currentPassword": "当前密码",    // ✅ required
    "newPassword": "新密码",          // ✅ required
    "confirmNewPassword": "确认新密码" // ✅ required, 必须与newPassword匹配
  }
  ```

### 1.6 检查用户名可用性
- **接口**: `GET /auth/check-username?username={username}`
- **权限**: 公开
- **响应**: `Boolean` (true=可用, false=已被使用)

### 1.7 检查邮箱可用性
- **接口**: `GET /auth/check-email?email={email}`
- **权限**: 公开
- **响应**: `Boolean` (true=可用, false=已注册)

---

## 2. 用户管理模块 (UserController)

### 2.1 创建用户（管理员）
- **接口**: `POST /users`
- **权限**: ADMIN, SUPER_ADMIN
- **请求体**: `RegisterRequest`

### 2.2 获取当前用户信息
- **接口**: `GET /users/profile`
- **权限**: MANAGER, ADMIN, SUPER_ADMIN
- **响应**: `User` 对象

### 2.3 更新当前用户信息
- **接口**: `PUT /users/profile`
- **权限**: MANAGER, ADMIN, SUPER_ADMIN
- **请求体**: `UpdateProfileRequest`
  ```json
  {
    "username": "新用户名",     // ❌ optional
    "email": "新邮箱",         // ❌ optional
    "realName": "真实姓名",    // ❌ optional
    "department": "部门"       // ❌ optional
  }
  ```
- **特殊说明**: 如果更改用户名，会返回新的JWT令牌

### 2.4 根据ID获取用户
- **接口**: `GET /users/{userId}`
- **权限**: MANAGER, ADMIN, SUPER_ADMIN
- **说明**: MANAGER只能查看自己

### 2.5 获取所有用户（分页）
- **接口**: `GET /users?page=0&size=10`
- **权限**: ADMIN, SUPER_ADMIN
- **查询参数**:
  - `page` (default=0)
  - `size` (default=10)
- **响应**: `Page<User>`
- **特殊说明**: SUPER_ADMIN不会看到自己

### 2.6 快速用户列表（性能优化）
- **接口**: `GET /users/fast?page=0&size=10`
- **权限**: ADMIN, SUPER_ADMIN

### 2.7 搜索用户
- **接口**: `GET /users/search?keyword={keyword}&page=0&size=10`
- **权限**: SUPER_ADMIN
- **说明**: 按用户名、邮箱、真实姓名搜索

### 2.8 按角色获取用户
- **接口**: `GET /users/role/{role}`
- **权限**: SUPER_ADMIN
- **路径参数**: `role` (MANAGER|ADMIN|SUPER_ADMIN)
- **响应**: `List<User>`

### 2.9 更新用户状态
- **接口**: `PUT /users/{userId}/status?status={status}`
- **权限**: ADMIN, SUPER_ADMIN
- **查询参数**: `status` (ACTIVE|INACTIVE|LOCKED|DELETED)

### 2.10 启用用户
- **接口**: `PUT /users/{userId}/enable`
- **权限**: ADMIN, SUPER_ADMIN

### 2.11 停用用户
- **接口**: `PUT /users/{userId}/disable`
- **权限**: ADMIN, SUPER_ADMIN

### 2.12 更新用户角色
- **接口**: `PUT /users/{userId}/role?role={role}`
- **权限**: ADMIN, SUPER_ADMIN
- **查询参数**: `role` (MANAGER|ADMIN|SUPER_ADMIN)

### 2.13 更新用户信息（管理员）
- **接口**: `PUT /users/{userId}`
- **权限**: SUPER_ADMIN
- **请求体**: `UpdateUserRequest`

### 2.14 删除用户（软删除）
- **接口**: `DELETE /users/{userId}`
- **权限**: SUPER_ADMIN

### 2.15 获取用户统计
- **接口**: `GET /users/statistics`
- **权限**: ADMIN, SUPER_ADMIN, HR_MANAGER
- **响应**: `UserStatistics`
  ```json
  {
    "totalUsers": 100,
    "activeUsers": 85,
    "inactiveUsers": 10,
    "lockedUsers": 3,
    "deletedUsers": 2,
    "managerCount": 70,
    "adminCount": 10,
    "superAdminCount": 5
  }
  ```

### 2.16 重置用户密码
- **接口**: `POST /users/{userId}/reset-password?newPassword={password}`
- **权限**: SUPER_ADMIN
- **说明**: 管理员重置用户密码

---

## 3. 项目管理模块 (ProjectController)

### 3.1 创建项目
- **接口**: `POST /projects`
- **权限**: MANAGER, ADMIN, SUPER_ADMIN
- **请求体**: `ProjectCreateRequest`
  ```json
  {
    "name": "项目名称",              // ✅ required, 2-200字符
    "description": "项目内容",       // ❌ optional, <5000字符
    "members": "项目成员",          // ❌ optional, <5000字符
    "expectedResults": "预期结果",   // ❌ optional, <5000字符
    "timeline": "时间线",           // ❌ optional, <5000字符
    "stopLoss": "止损点",           // ❌ optional, <5000字符
    "phases": []                    // ❌ optional, Array<ProjectPhaseCreateRequest>
  }
  ```

**⚠️ 文档错误字段**:
- ❌ `projectName` → 应为 `name`
- ❌ `projectDescription` → 应为 `description`
- ❌ `startDate`, `endDate`, `budget`, `objectives` → 不存在
- ❌ `expectedOutcomes` → 应为 `expectedResults`

### 3.2 获取所有项目（分页）
- **接口**: `GET /projects?page=0&size=10&sortBy=createdAt&sortDir=desc`
- **权限**: MANAGER, ADMIN, SUPER_ADMIN

### 3.3 项目过滤查询
- **接口**: `GET /projects/filter`
- **权限**: MANAGER, ADMIN, SUPER_ADMIN
- **查询参数**:
  - `name` (optional)
  - `status` (optional)
  - `priority` (optional)
  - `approvalStatus` (optional)
  - `createdBy` (optional)
  - `page`, `size`, `sortBy`, `sortDir`

### 3.4 获取待审核项目
- **接口**: `GET /projects/pending-review`
- **权限**: ADMIN, SUPER_ADMIN
- **说明**:
  - ADMIN看到待管理员审核的项目
  - SUPER_ADMIN看到待超级管理员审核的项目

### 3.5 获取已通过项目
- **接口**: `GET /projects/approved`
- **权限**: ADMIN, SUPER_ADMIN

### 3.6 获取已拒绝项目
- **接口**: `GET /projects/rejected`
- **权限**: ADMIN, SUPER_ADMIN

### 3.7 获取项目详情
- **接口**: `GET /projects/{id}`
- **权限**: MANAGER, ADMIN, SUPER_ADMIN
- **说明**: MANAGER只能查看自己创建的项目

### 3.8 更新项目
- **接口**: `PUT /projects/{id}`
- **权限**: MANAGER (仅创建者)
- **限制**: 只有草稿或已拒绝状态可更新

### 3.9 删除项目
- **接口**: `DELETE /projects/{id}`
- **权限**: MANAGER (仅创建者)
- **限制**: 只有草稿状态可删除

### 3.10 提交项目进行AI分析
- **接口**: `PUT /projects/{id}/submit`
- **权限**: MANAGER (仅创建者)
- **限制**: 只有草稿或已拒绝状态可提交

### 3.11 强制提交项目（跳过AI）
- **接口**: `POST /projects/{id}/force-submit`
- **权限**: ADMIN, SUPER_ADMIN

### 3.12 重新提交项目
- **接口**: `PUT /projects/{id}/resubmit`
- **权限**: MANAGER (仅创建者)
- **请求体**: `ProjectUpdateRequest`
- **限制**: 只有已拒绝状态可重新提交

### 3.13 管理员审核通过
- **接口**: `PUT /projects/{id}/approve`
- **别名**: `PUT /projects/{id}/admin-approve`
- **权限**: ADMIN

### 3.14 超级管理员最终审核
- **接口**: `PUT /projects/{id}/final-approve`
- **别名**: `PUT /projects/{id}/super-admin-approve`
- **权限**: SUPER_ADMIN

### 3.15 拒绝项目
- **接口**: `PUT /projects/{id}/reject`
- **权限**: ADMIN, SUPER_ADMIN
- **请求体**: String (拒绝原因)

### 3.16 获取我的项目
- **接口**: `GET /projects/my?approvalStatus={status}`
- **权限**: MANAGER
- **查询参数**: `approvalStatus` (optional)

### 3.17 获取待审核项目
- **接口**: `GET /projects/pending`
- **权限**: ADMIN, SUPER_ADMIN
- **说明**: 与 `/pending-review` 相同

### 3.18 获取已审核项目
- **接口**: `GET /projects/reviewed`
- **权限**: ADMIN, SUPER_ADMIN

### 3.19 获取项目阶段
- **接口**: `GET /projects/{projectId}/phases`
- **权限**: MANAGER, ADMIN, SUPER_ADMIN

### 3.20 创建项目阶段
- **接口**: `POST /projects/{projectId}/phases`
- **权限**: MANAGER, ADMIN, SUPER_ADMIN
- **请求体**: `ProjectPhaseCreateRequest`
- **说明**: MANAGER只能为自己的项目添加阶段

### 审批状态枚举
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

## 4. 任务管理模块 (TaskController)

### 4.1 获取所有任务（分页）
- **接口**: `GET /tasks?page=0&size=10&sortBy=createdAt&sortDir=desc`
- **权限**: MANAGER, ADMIN, SUPER_ADMIN
- **说明**: MANAGER只能看到自己创建的任务

### 4.2 创建任务
- **接口**: `POST /tasks`
- **权限**: MANAGER, ADMIN, SUPER_ADMIN
- **请求体**: `TaskCreateRequest`
  ```json
  {
    "taskName": "任务名称",           // ✅ required
    "personnelAssignment": "人员分配", // ❌ optional
    "timeline": "时间线",             // ❌ optional
    "expectedResults": "预期结果"     // ❌ optional
  }
  ```
- **说明**: `TaskType`字段已删除，不再使用

### 4.3 获取任务详情
- **接口**: `GET /tasks/{id}`
- **权限**: MANAGER, ADMIN, SUPER_ADMIN
- **说明**: MANAGER只能查看自己创建的任务

### 4.4 更新任务
- **接口**: `PUT /tasks/{id}`
- **权限**: MANAGER (仅创建者)

### 4.5 删除任务
- **接口**: `DELETE /tasks/{id}`
- **权限**: MANAGER (仅创建者)

### 4.6 获取我的任务列表
- **接口**: `GET /tasks/my`
- **权限**: MANAGER, ADMIN, SUPER_ADMIN

### 4.7 按类型获取任务
- **接口**: `GET /tasks/by-type/{taskType}`
- **权限**: MANAGER, ADMIN, SUPER_ADMIN
- **说明**: ⚠️ 由于TaskType已删除，此接口返回所有任务

### 4.8 获取任务统计
- **接口**: `GET /tasks/statistics`
- **权限**: MANAGER, ADMIN, SUPER_ADMIN
- **响应**:
  ```json
  {
    "totalTasks": 50,
    "completedTasks": 30,
    "pendingTasks": 20,
    "routineTasks": 0,       // ⚠️ TaskType已删除，无法统计
    "developmentTasks": 0,   // ⚠️ TaskType已删除，无法统计
    "completionRate": 60.0
  }
  ```

### 4.9 根据周报ID获取关联任务
- **接口**: `GET /tasks/by-report/{reportId}`
- **权限**: MANAGER, ADMIN, SUPER_ADMIN
- **说明**: 返回日常性任务(TaskReport)和发展性任务(DevTaskReport)

---

## 5. 周报管理模块 (WeeklyReportController)

### 5.1 创建周报
- **接口**: `POST /weekly-reports`
- **权限**: MANAGER, ADMIN, SUPER_ADMIN
- **请求体**: `WeeklyReportCreateRequest`
  ```json
  {
    "userId": 1,                      // ✅ required
    "title": "周报标题",               // ✅ required
    "reportWeek": "2025-10-07 至 2025-10-11",  // ✅ required
    "weekStart": "2025-10-07",        // ❌ optional
    "weekEnd": "2025-10-11",          // ❌ optional
    "content": {                      // ✅ required, 本周工作汇报
      "routine_tasks": [              // ❌ optional, 日常性任务
        {
          "task_id": "1",
          "actual_result": "实际结果",
          "analysisofResultDifferences": "差异分析"
        }
      ],
      "developmental_tasks": [        // ❌ optional, 发展性任务
        {
          "project_id": "1",
          "phase_id": "1",
          "actual_result": "实际结果",
          "analysisofResultDifferences": "差异分析"
        }
      ]
    },
    "nextWeekPlan": {                 // ❌ optional, 下周工作计划
      "routine_tasks": [
        {
          "task_id": "1"
        }
      ],
      "developmental_tasks": [
        {
          "project_id": "1",
          "phase_id": "1"
        }
      ]
    },
    "additionalNotes": "补充说明"      // ❌ optional
  }
  ```

### 5.2 更新周报
- **接口**: `PUT /weekly-reports/{id}`
- **权限**: MANAGER (仅创建者)
- **请求体**: `WeeklyReportUpdateRequest`
- **限制**: 只能更新AI分析中、AI拒绝或管理员拒绝状态的周报

### 5.3 提交周报
- **接口**: `PUT /weekly-reports/{id}/submit`
- **权限**: MANAGER (仅创建者)
- **说明**: 提交周报进入AI分析

### 5.4 强行提交周报
- **接口**: `PUT /weekly-reports/{id}/force-submit`
- **权限**: MANAGER (仅创建者)
- **说明**: AI拒绝后，强行提交到管理员审核

### 5.5 AI审批通过（系统内部）
- **接口**: `PUT /weekly-reports/{id}/ai-approve?aiAnalysisId={id}`
- **权限**: 系统内部调用

### 5.6 管理员审批通过
- **接口**: `PUT /weekly-reports/{id}/admin-approve`
- **权限**: ADMIN, SUPER_ADMIN

### 5.7 拒绝周报
- **接口**: `PUT /weekly-reports/{id}/reject`
- **权限**: ADMIN, SUPER_ADMIN
- **请求体**:
  ```json
  {
    "reason": "拒绝原因"
  }
  ```

### 5.8 获取周报详情
- **接口**: `GET /weekly-reports/{id}`
- **权限**: MANAGER (作者), ADMIN, SUPER_ADMIN
- **响应**: `WeeklyReportDetailResponse` (包含完整关联数据)

### 5.9 删除周报
- **接口**: `DELETE /weekly-reports/{id}`
- **权限**: MANAGER (仅创建者)
- **限制**: 只能删除AI分析中或被拒绝状态的周报

### 5.10 获取我的周报列表
- **接口**: `GET /weekly-reports/my?page=0&size=20`
- **权限**: MANAGER, ADMIN, SUPER_ADMIN
- **说明**: ⚠️ 严格返回当前登录用户的周报
- **响应**: `Page<WeeklyReportDetailResponse>`

### 5.11 获取所有周报（根据权限）
- **接口**: `GET /weekly-reports?status={status}&page=0&size=20`
- **权限**: 所有角色
- **说明**:
  - ADMIN/SUPER_ADMIN: 返回所有周报
  - MANAGER: 只返回自己的周报（建议使用 `/my` 接口）
- **查询参数**: `status` (optional) - 按审批状态过滤

### 5.12 获取待审批周报
- **接口**: `GET /weekly-reports/pending?status={status}&page=0&size=20`
- **权限**: ADMIN, SUPER_ADMIN
- **查询参数**: `status` (optional, default=ADMIN_REVIEWING)

### 5.13 测试更新（调试用）
- **接口**: `PUT /weekly-reports/{id}/test`
- **权限**: 需要认证
- **说明**: 用于调试JSON解析问题

### 周报审批状态枚举
```java
public enum ApprovalStatus {
    AI_ANALYZING,      // AI分析中
    AI_APPROVED,       // AI分析通过
    AI_REJECTED,       // AI分析拒绝
    ADMIN_REVIEWING,   // 管理员审核中
    ADMIN_APPROVED,    // 管理员审核通过
    ADMIN_REJECTED     // 管理员审核拒绝
}
```

---

## 6. 评论模块 (CommentController)

### 6.1 获取周报评论列表
- **接口**: `GET /weekly-reports/{weeklyReportId}/comments?page=0&size=10`
- **权限**: MANAGER, ADMIN, SUPER_ADMIN
- **查询参数**:
  - `page` (default=0)
  - `size` (default=10)
- **响应**: `CommentListResponse`

### 6.2 创建评论
- **接口**: `POST /weekly-reports/{weeklyReportId}/comments`
- **权限**: MANAGER, ADMIN, SUPER_ADMIN
- **请求体**: `CommentCreateRequest`
  ```json
  {
    "content": "评论内容",            // ✅ required
    "parentCommentId": 123           // ❌ optional, 父评论ID（回复）
  }
  ```
- **响应**: `CommentResponse`

---

## 7. 文件管理模块 (FileManagementController)

### 7.1 上传文件
- **接口**: `POST /file-management/upload`
- **权限**: MANAGER, ADMIN, SUPER_ADMIN
- **请求类型**: `multipart/form-data`
- **参数**:
  - `file` (required) - 文件
  - `weeklyReportId` (optional) - 周报ID，不绑定时不需要
  - `attachmentType` (optional, default=GENERAL) - 附件类型
  - `relatedTaskId` (optional) - 关联任务ID
  - `relatedProjectId` (optional) - 关联项目ID
  - `relatedPhaseId` (optional) - 关联阶段ID
  - `description` (optional) - 附件描述
  - `displayOrder` (optional) - 显示顺序
  - `isPublic` (optional, default=false) - 是否公开
- **响应**: `FileUploadResponse`
- **更新**: v2.1.0 - weeklyReportId改为可选

### 7.2 批量上传文件
- **接口**: `POST /file-management/upload/batch`
- **权限**: MANAGER, ADMIN, SUPER_ADMIN
- **请求类型**: `multipart/form-data`
- **参数**:
  - `files` (required) - 文件列表
  - 其他参数同单文件上传
- **响应**: `List<FileUploadResponse>`
- **更新**: v2.1.0 - weeklyReportId改为可选

### 7.3 下载文件
- **接口**: `GET /file-management/download/{fileId}`
- **权限**: MANAGER, ADMIN, SUPER_ADMIN
- **响应**: 文件流 (Resource)

### 7.4 获取文件预览URL
- **接口**: `GET /file-management/preview/{fileId}`
- **权限**: MANAGER, ADMIN, SUPER_ADMIN
- **响应**: String (预览URL)

### 7.5 删除文件（软删除）
- **接口**: `DELETE /file-management/{fileId}`
- **权限**: MANAGER, ADMIN, SUPER_ADMIN
- **说明**: 软删除，文件标记为已删除

### 7.6 获取周报附件
- **接口**: `GET /file-management/weekly-report/{weeklyReportId}/attachments?attachmentType={type}`
- **权限**: MANAGER, ADMIN, SUPER_ADMIN
- **查询参数**: `attachmentType` (optional) - 按类型过滤
- **响应**: `List<FileUploadResponse>`

### 7.7 更新附件信息
- **接口**: `PUT /file-management/attachment/{relationId}?description={desc}&displayOrder={order}`
- **权限**: MANAGER, ADMIN, SUPER_ADMIN
- **查询参数**:
  - `description` (optional)
  - `displayOrder` (optional)
- **状态**: ⚠️ TODO - 待实现

### 7.8 移除附件关联
- **接口**: `DELETE /file-management/weekly-report/{weeklyReportId}/attachment/{fileId}`
- **权限**: MANAGER, ADMIN, SUPER_ADMIN
- **说明**: 移除周报与文件的关联，但不删除文件本身
- **状态**: ⚠️ TODO - 待实现

### 附件类型枚举
```java
public enum AttachmentType {
    GENERAL,                      // 通用附件
    ROUTINE_TASK_RESULT,          // 日常任务结果
    DEVELOPMENT_TASK_RESULT,      // 发展任务结果
    ANALYSIS_DOCUMENT,            // 分析文档
    NEXT_WEEK_PLAN_REFERENCE,     // 下周计划参考
    ADDITIONAL_NOTE_ATTACHMENT,   // 补充说明附件
    AI_ANALYSIS_OUTPUT            // AI分析输出
}
```

---

## 8. 健康检查模块 (HealthController)

### 8.1 基础健康检查
- **接口**: `GET /health`
- **权限**: 公开
- **响应**:
  ```json
  {
    "status": "UP",
    "timestamp": "2025-10-09T10:00:00",
    "service": "weekly-report-backend",
    "version": "1.0.0"
  }
  ```

### 8.2 就绪检查
- **接口**: `GET /health/ready`
- **权限**: 公开
- **响应**:
  ```json
  {
    "status": "READY",
    "timestamp": "2025-10-09T10:00:00",
    "service": "weekly-report-backend",
    "version": "1.0.0",
    "ready": true
  }
  ```

### 8.3 存活检查
- **接口**: `GET /health/live`
- **权限**: 公开
- **响应**:
  ```json
  {
    "status": "LIVE",
    "timestamp": "2025-10-09T10:00:00",
    "service": "weekly-report-backend",
    "version": "1.0.0",
    "live": true
  }
  ```

### 8.4 认证健康检查
- **接口**: `GET /health/authenticated`
- **权限**: MANAGER, ADMIN, SUPER_ADMIN
- **响应**:
  ```json
  {
    "status": "AUTHENTICATED",
    "timestamp": "2025-10-09T10:00:00",
    "user": "username",
    "authorities": ["ROLE_MANAGER"]
  }
  ```

---

## 9. 需要修正的API文档问题总结

### 9.1 项目管理模块
**错误字段（需删除或修正）**:
- ❌ `projectName` → `name`
- ❌ `projectDescription` → `description`
- ❌ `startDate` - 不存在
- ❌ `endDate` - 不存在
- ❌ `budget` - 不存在
- ❌ `priority` - 创建时不需要
- ❌ `objectives` - 不存在
- ❌ `expectedOutcomes` → `expectedResults`

### 9.2 文件管理模块
**已修正（v2.1.0）**:
- ✅ `weeklyReportId` 参数改为可选
- ✅ 支持上传文件无需绑定周报
- ✅ 三角色权限验证通过

### 9.3 缺少文档的接口
**未在API文档中记录的接口**:
1. `GET /auth/check-username` - 检查用户名可用性
2. `GET /auth/check-email` - 检查邮箱可用性
3. `GET /users/fast` - 快速用户列表
4. `GET /users/search` - 搜索用户
5. `GET /users/role/{role}` - 按角色获取用户
6. `GET /users/statistics` - 用户统计
7. `POST /users/{userId}/reset-password` - 重置密码
8. `GET /tasks/by-type/{taskType}` - 按类型获取任务
9. `GET /tasks/statistics` - 任务统计
10. `GET /tasks/by-report/{reportId}` - 根据周报获取任务
11. `PUT /weekly-reports/{id}/test` - 测试更新
12. `GET /health/ready` - 就绪检查
13. `GET /health/live` - 存活检查
14. `GET /health/authenticated` - 认证健康检查

### 9.4 权限要求不准确
**需要明确或修正权限的接口**:
1. 用户管理接口 - 需要区分ADMIN和SUPER_ADMIN权限
2. 项目管理接口 - 需要明确MANAGER的权限限制
3. 周报管理接口 - 需要说明"仅创建者"限制

---

## 10. 接口统计

| 模块 | 接口数量 | 已实现 | TODO |
|------|---------|--------|------|
| 认证模块 | 7 | 7 | 0 |
| 用户管理 | 17 | 17 | 0 |
| 项目管理 | 20 | 20 | 0 |
| 任务管理 | 9 | 9 | 0 |
| 周报管理 | 13 | 13 | 0 |
| 评论模块 | 2 | 2 | 0 |
| 文件管理 | 8 | 6 | 2 |
| 健康检查 | 4 | 4 | 0 |
| **总计** | **80** | **78** | **2** |

**TODO接口**:
1. `PUT /file-management/attachment/{relationId}` - 更新附件信息
2. `DELETE /file-management/weekly-report/{weeklyReportId}/attachment/{fileId}` - 移除附件关联

---

## 11. 建议事项

### 11.1 立即修正
1. ✅ 修正项目管理模块的所有字段名称
2. ✅ 为所有接口补充权限说明
3. ✅ 补充缺失的13个接口文档

### 11.2 优化建议
1. 统一响应格式 - 所有接口都使用 `ApiResponse<T>` 包装
2. 统一错误处理 - 所有错误都返回标准格式
3. 补充示例 - 为复杂接口提供完整请求/响应示例
4. 版本标注 - 为每个接口标注引入版本

### 11.3 待实现功能
1. 文件管理模块的2个TODO接口
2. 任务类型统计功能（TaskType已删除的影响）

---

## 附录：角色权限矩阵

| 接口类型 | MANAGER | ADMIN | SUPER_ADMIN |
|---------|---------|-------|-------------|
| 认证相关 | ✅ | ✅ | ✅ |
| 个人信息 | ✅ | ✅ | ✅ |
| 用户管理 | ❌ | ✅ | ✅ |
| 创建项目 | ✅ | ✅ | ✅ |
| 查看所有项目 | ❌ | ✅ | ✅ |
| 项目审批 | ❌ | ✅ | ✅ |
| 任务管理 | ✅(自己) | ✅ | ✅ |
| 周报管理 | ✅(自己) | ✅ | ✅ |
| 周报审批 | ❌ | ✅ | ✅ |
| 评论管理 | ✅ | ✅ | ✅ |
| 文件管理 | ✅ | ✅ | ✅ |

---

**报告生成时间**: 2025-10-09
**下一步**: 根据此核对报告更新完整API接口文档
