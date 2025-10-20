-- V35__Separate_Draft_From_Approval_Status.sql
-- 分离草稿状态与审核流程状态

-- 1. 添加新的状态字段
ALTER TABLE weekly_reports
ADD COLUMN edit_status VARCHAR(20) DEFAULT 'DRAFT' COMMENT '编辑状态: DRAFT-草稿, SUBMITTED-已提交',
ADD COLUMN processing_status VARCHAR(30) DEFAULT 'PENDING' COMMENT '处理状态: PENDING-待处理, AI_PROCESSING-AI处理中, AI_COMPLETED-AI完成, AI_FAILED-AI失败';

-- 2. 数据迁移：根据旧的 approval_status 推断新状态
UPDATE weekly_reports
SET
    edit_status = CASE
        WHEN approval_status IN ('AI_ANALYZING', 'AI_REJECTED') THEN 'DRAFT'
        ELSE 'SUBMITTED'
    END,
    processing_status = CASE
        WHEN approval_status = 'AI_ANALYZING' THEN 'AI_PROCESSING'
        WHEN approval_status IN ('AI_REJECTED', 'ADMIN_REVIEWING', 'ADMIN_APPROVED', 'ADMIN_REJECTED') THEN 'AI_COMPLETED'
        ELSE 'PENDING'
    END;

-- 3. 修改 approval_status 的枚举值
-- 注意：MySQL 8.0 不支持直接修改 ENUM，需要先转为 VARCHAR，再转回 ENUM
ALTER TABLE weekly_reports
MODIFY COLUMN approval_status VARCHAR(20) NOT NULL;

UPDATE weekly_reports
SET approval_status = CASE
    WHEN approval_status = 'AI_ANALYZING' THEN 'NOT_STARTED'
    WHEN approval_status = 'AI_REJECTED' THEN 'NOT_STARTED'
    WHEN approval_status = 'ADMIN_REVIEWING' THEN 'PENDING_REVIEW'
    WHEN approval_status = 'ADMIN_APPROVED' THEN 'APPROVED'
    WHEN approval_status = 'ADMIN_REJECTED' THEN 'REJECTED'
    ELSE 'NOT_STARTED'
END;

ALTER TABLE weekly_reports
MODIFY COLUMN approval_status ENUM('NOT_STARTED', 'PENDING_REVIEW', 'APPROVED', 'REJECTED')
NOT NULL DEFAULT 'NOT_STARTED'
COMMENT '审核状态: NOT_STARTED-未开始, PENDING_REVIEW-待审核, APPROVED-已通过, REJECTED-已拒绝';

-- 4. 设置字段为非空（已有默认值）
ALTER TABLE weekly_reports
MODIFY COLUMN edit_status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
MODIFY COLUMN processing_status VARCHAR(30) NOT NULL DEFAULT 'PENDING';

-- 5. 创建索引加速查询
CREATE INDEX idx_edit_status ON weekly_reports(edit_status);
CREATE INDEX idx_processing_status ON weekly_reports(processing_status);

-- 6. 更新已有的 approval_status 索引（如果存在则先删除再创建）
DROP INDEX IF EXISTS idx_approval_status ON weekly_reports;
CREATE INDEX idx_approval_status ON weekly_reports(approval_status);

-- 7. 创建组合索引支持常见查询场景
CREATE INDEX idx_user_edit_status ON weekly_reports(user_id, edit_status);
CREATE INDEX idx_approval_processing ON weekly_reports(approval_status, processing_status);
