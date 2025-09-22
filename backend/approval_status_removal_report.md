# Approval Status 字段简化报告

## 修改概述

根据用户要求，从 `approval_status` ENUM 字段中移除了 `DRAFT` 和 `SUBMITTED` 状态，简化了审批流程。

## 修改内容

### 1. 数据库迁移 (V30)

**迁移文件**: `V30__Remove_Draft_And_Submitted_From_Approval_Status.sql`

**操作内容**:
1. 将所有 `DRAFT` 状态记录更新为 `AI_ANALYZING`
2. 将所有 `SUBMITTED` 状态记录更新为 `AI_ANALYZING`
3. 修改 ENUM 定义，移除 `DRAFT` 和 `SUBMITTED`
4. 更新默认值为 `AI_ANALYZING`

**最终 ENUM 值**:
```sql
enum(
    'AI_ANALYZING',             -- AI分析中
    'AI_APPROVED',              -- AI分析通过
    'AI_REJECTED',              -- AI分析不通过
    'ADMIN_REVIEWING',          -- 管理员审核中
    'ADMIN_APPROVED',           -- 管理员审核通过
    'ADMIN_REJECTED',           -- 管理员审核不通过
    'SUPER_ADMIN_REVIEWING',    -- 超级管理员审核中
    'SUPER_ADMIN_APPROVED',     -- 超级管理员审核通过
    'SUPER_ADMIN_REJECTED',     -- 超级管理员审核不通过
    'REJECTED',                 -- 已拒绝
    'FINAL_APPROVED'            -- 最终通过
)
```

### 2. 实体类更新

**文件**: `WeeklyReport.java`

**修改内容**:
1. **枚举定义**: 移除 `DRAFT` 和 `SUBMITTED`，添加完整的审批状态
2. **默认状态**: `ApprovalStatus.AI_ANALYZING`
3. **构造函数**: 使用 `AI_ANALYZING` 作为初始状态
4. **业务方法更新**:
   - `submit()`: 直接设置为 `AI_ANALYZING`
   - `isDraft()`: 检查是否为 `AI_ANALYZING` 状态
   - `isSubmitted()`: 检查状态是否非空
   - `getStatus()`: 更新状态映射逻辑

### 3. 状态流程变更

**修改前**:
```
DRAFT → SUBMITTED → AI_ANALYZING → AI_APPROVED → ADMIN_APPROVED → SUPER_ADMIN_APPROVED
```

**修改后**:
```
AI_ANALYZING → AI_APPROVED → ADMIN_APPROVED → SUPER_ADMIN_APPROVED → FINAL_APPROVED
```

## 影响的表

1. **weekly_reports**
   - ✅ 字段更新完成
   - ✅ 数据迁移完成
   - ✅ 默认值更新为 `AI_ANALYZING`

2. **projects**
   - ✅ 字段更新完成 (保持一致性)
   - ✅ 数据迁移完成
   - ✅ 默认值更新为 `AI_ANALYZING`

## 验证结果

### 数据库验证
```bash
# 检查字段结构
SHOW COLUMNS FROM weekly_reports WHERE Field = 'approval_status';

# 检查现有数据
SELECT DISTINCT approval_status FROM weekly_reports;
```

**结果**: 
- ✅ ENUM 不再包含 `DRAFT` 和 `SUBMITTED`
- ✅ 现有数据已迁移到 `AI_ANALYZING`、`AI_APPROVED`、`ADMIN_APPROVED`
- ✅ 默认值正确设置为 `AI_ANALYZING`

### 代码验证
- ✅ 实体类枚举与数据库 ENUM 一致
- ✅ 业务逻辑方法已适配新的状态流程
- ✅ 默认状态和构造函数已更新

## 业务影响

### 简化的流程
1. **创建周报**: 直接进入 `AI_ANALYZING` 状态
2. **提交周报**: 保持 `AI_ANALYZING` 状态 (无状态变更)
3. **AI分析**: `AI_ANALYZING` → `AI_APPROVED`/`AI_REJECTED`
4. **管理员审核**: `AI_APPROVED` → `ADMIN_APPROVED`/`ADMIN_REJECTED`
5. **超级管理员审核**: `ADMIN_APPROVED` → `SUPER_ADMIN_APPROVED`/`SUPER_ADMIN_REJECTED`

### 兼容性
- ✅ 现有 API 端点继续工作
- ✅ 前端显示逻辑需要相应调整
- ✅ 业务逻辑简化，减少状态转换复杂性

## 注意事项

1. **前端适配**: 前端代码可能需要更新，不再依赖 `DRAFT` 和 `SUBMITTED` 状态
2. **API 文档**: 需要更新 API 文档反映新的状态值
3. **测试更新**: 相关测试用例需要更新状态值
4. **日志分析**: 现有日志中的 `DRAFT`/`SUBMITTED` 状态需要考虑历史兼容性

## 迁移完成状态

- ✅ 数据库迁移: V30 成功应用
- ✅ 实体类更新: WeeklyReport.java 已同步
- ✅ 数据完整性: 所有记录已正确迁移
- ✅ 默认值设置: AI_ANALYZING
- ✅ 业务逻辑: 方法已适配新流程

---

**完成时间**: 2025-09-21  
**版本**: V30 数据库迁移