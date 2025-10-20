-- V38__Add_Status_And_Rejected_By_Fields.sql
-- 添加5状态系统需要的字段，从三层状态迁移到单一status字段

-- ===================================================================
-- 第一步：添加新字段
-- ===================================================================

-- 1. 添加单一status字段（5状态系统）
ALTER TABLE weekly_reports
ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'DRAFT'
COMMENT '周报状态（5状态系统）: DRAFT-草稿, AI_PROCESSING-AI分析中, PENDING_REVIEW-待审核, APPROVED-已通过, REJECTED-已拒绝';

-- 2. 添加拒绝者字段
ALTER TABLE weekly_reports
ADD COLUMN rejected_by VARCHAR(20) NULL
COMMENT '拒绝者: AI-AI系统, ADMIN-管理员, SUPER_ADMIN-超级管理员';

-- 3. 添加拒绝时间字段
ALTER TABLE weekly_reports
ADD COLUMN rejected_at DATETIME NULL
COMMENT '拒绝时间';

-- 4. 添加提交时间字段（检查是否已存在）
-- 如果字段已存在，这条语句会失败，可以忽略
ALTER TABLE weekly_reports
ADD COLUMN submitted_at DATETIME NULL
COMMENT '提交时间';

-- 5. 添加通过时间字段（检查是否已存在）
-- 如果字段已存在，这条语句会失败，可以忽略
ALTER TABLE weekly_reports
ADD COLUMN approved_at DATETIME NULL
COMMENT '通过时间';

-- ===================================================================
-- 第二步：数据迁移（将三层状态转换为单一status）
-- ===================================================================

-- 数据迁移逻辑：
-- edit_status + processing_status + approval_status → status + rejected_by

UPDATE weekly_reports
SET
    status = CASE
        -- 1. 草稿状态：edit_status = 'DRAFT'
        WHEN edit_status = 'DRAFT' AND approval_status = 'NOT_STARTED' THEN 'DRAFT'

        -- 2. AI处理中：processing_status = 'AI_PROCESSING'
        WHEN processing_status = 'AI_PROCESSING' THEN 'AI_PROCESSING'

        -- 3. AI完成且待审核：processing_status = 'AI_COMPLETED' AND approval_status = 'PENDING_REVIEW'
        WHEN processing_status = 'AI_COMPLETED' AND approval_status = 'PENDING_REVIEW' THEN 'PENDING_REVIEW'

        -- 4. 已通过：approval_status = 'APPROVED'
        WHEN approval_status = 'APPROVED' THEN 'APPROVED'

        -- 5. 已拒绝：approval_status = 'REJECTED'
        -- 注意：旧系统无法区分AI拒绝和管理员拒绝，默认设置为管理员拒绝
        WHEN approval_status = 'REJECTED' THEN 'REJECTED'

        -- 6. AI失败：processing_status = 'AI_FAILED'
        WHEN processing_status = 'AI_FAILED' THEN 'REJECTED'

        -- 默认：草稿
        ELSE 'DRAFT'
    END,

    -- 设置拒绝者（旧系统无法区分，默认设置为ADMIN）
    rejected_by = CASE
        WHEN approval_status = 'REJECTED' THEN 'ADMIN'
        WHEN processing_status = 'AI_FAILED' THEN 'AI'
        ELSE NULL
    END,

    -- 设置拒绝时间（使用updated_at作为近似值）
    rejected_at = CASE
        WHEN approval_status = 'REJECTED' OR processing_status = 'AI_FAILED' THEN updated_at
        ELSE NULL
    END;

-- ===================================================================
-- 第三步：创建索引
-- ===================================================================

-- 创建status字段索引
CREATE INDEX idx_weekly_reports_status ON weekly_reports(status);

-- 创建rejected_by字段索引
CREATE INDEX idx_weekly_reports_rejected_by ON weekly_reports(rejected_by);

-- 创建组合索引：status + rejected_by（用于区分AI拒绝）
CREATE INDEX idx_weekly_reports_status_rejected_by ON weekly_reports(status, rejected_by);

-- ===================================================================
-- 第四步：数据验证
-- ===================================================================

-- 验证1：检查状态分布
SELECT
    status,
    rejected_by,
    COUNT(*) as count
FROM weekly_reports
GROUP BY status, rejected_by
ORDER BY status, rejected_by;

-- 验证2：检查是否有NULL状态（不应该有）
SELECT COUNT(*) as null_status_count
FROM weekly_reports
WHERE status IS NULL;

-- 验证3：对比新旧状态映射
SELECT
    edit_status,
    processing_status,
    approval_status,
    status,
    rejected_by,
    COUNT(*) as count
FROM weekly_reports
GROUP BY edit_status, processing_status, approval_status, status, rejected_by
ORDER BY count DESC;

-- ===================================================================
-- 第五步：添加注释说明
-- ===================================================================

-- 说明：保留旧的三层状态字段（edit_status, processing_status, approval_status）
-- 用于过渡期验证和回滚，待新系统稳定运行一段时间后再删除

-- 未来清理计划（在V39或更晚执行）：
-- ALTER TABLE weekly_reports
-- DROP COLUMN edit_status,
-- DROP COLUMN processing_status,
-- DROP COLUMN approval_status;

-- ===================================================================
-- Migration完成
-- ===================================================================
