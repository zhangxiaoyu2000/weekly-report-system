-- V16: 将SimpleWeeklyReport数据迁移到WeeklyReport统一架构
-- 这是双重架构统一的核心迁移脚本

-- 第一步：迁移simple_weekly_reports数据到weekly_reports
INSERT INTO weekly_reports (
    title,
    content,
    summary,
    week_start,
    week_end,
    user_id,
    project_id,
    status,
    priority,
    created_at,
    updated_at,
    submitted_at
)
SELECT 
    CONCAT('项目周报 - ', COALESCE(sp.project_name, '未知项目')) as title,
    swr.actual_results as content,
    LEFT(swr.actual_results, 500) as summary, -- 提取前500字符作为摘要
    DATE(swr.created_at) as week_start,
    DATE_ADD(DATE(swr.created_at), INTERVAL 6 DAY) as week_end,
    swr.created_by as user_id,
    swr.project_id,
    'PUBLISHED' as status, -- 简化周报默认设为已发布状态
    'NORMAL' as priority,
    swr.created_at,
    swr.created_at as updated_at,
    swr.created_at as submitted_at -- 创建时间作为提交时间
FROM simple_weekly_reports swr
LEFT JOIN simple_projects sp ON swr.project_id = sp.id
WHERE NOT EXISTS (
    -- 避免重复迁移
    SELECT 1 FROM weekly_reports wr 
    WHERE wr.project_id = swr.project_id 
    AND wr.user_id = swr.created_by 
    AND DATE(wr.created_at) = DATE(swr.created_at)
);

-- 第二步：为迁移的数据添加标识标签，便于后续识别
UPDATE weekly_reports 
SET tags = JSON_OBJECT('migrated_from', 'simple_weekly_report', 'migration_date', NOW())
WHERE project_id IS NOT NULL 
AND tags IS NULL 
AND title LIKE '项目周报 - %';

-- 第三步：备份simple_weekly_reports表的数据到备份表
CREATE TABLE IF NOT EXISTS simple_weekly_reports_backup AS 
SELECT *, NOW() as backup_date FROM simple_weekly_reports;

-- 记录迁移信息
INSERT INTO weekly_reports (
    title,
    content,
    week_start,
    week_end,
    user_id,
    status,
    priority,
    created_at,
    updated_at,
    tags
) VALUES (
    '双重架构统一迁移记录',
    CONCAT('系统完成了SimpleWeeklyReport到WeeklyReport的数据迁移。迁移时间：', NOW(), 
           '。迁移记录数：', (SELECT COUNT(*) FROM simple_weekly_reports_backup)),
    CURDATE(),
    DATE_ADD(CURDATE(), INTERVAL 6 DAY),
    1, -- 假设存在ID为1的系统用户，实际应该使用管理员用户ID
    'PUBLISHED',
    'HIGH',
    NOW(),
    NOW(),
    JSON_OBJECT('type', 'migration_log', 'source', 'simple_weekly_reports', 'target', 'weekly_reports')
);