-- 测试迁移后的数据完整性
-- 验证weekly_reports表结构

SELECT 'Testing weekly_reports table structure' as test_name;

-- 检查weekly_reports表是否仍包含所需字段
DESCRIBE weekly_reports;

-- 验证是否成功删除了不需要的字段
SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
AND TABLE_NAME = 'weekly_reports' 
AND COLUMN_NAME IN ('work_summary', 'achievements', 'challenges', 'next_week_plan');

-- 验证是否添加了additional_notes字段
SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
AND TABLE_NAME = 'weekly_reports' 
AND COLUMN_NAME = 'additional_notes';

-- 测试tasks表结构
SELECT 'Testing tasks table structure' as test_name;

-- 验证tasks表是否包含新的字段
SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
AND TABLE_NAME = 'tasks' 
AND COLUMN_NAME IN ('actual_results', 'result_difference_analysis');

-- 检查索引是否创建成功
SELECT INDEX_NAME FROM INFORMATION_SCHEMA.STATISTICS 
WHERE TABLE_SCHEMA = DATABASE() 
AND TABLE_NAME = 'tasks' 
AND INDEX_NAME IN ('idx_tasks_actual_results', 'idx_tasks_result_difference');

-- 验证现有数据是否完整
SELECT 'Testing data integrity' as test_name;

-- 检查周报数据是否完整
SELECT COUNT(*) as total_weekly_reports FROM weekly_reports;

-- 检查任务数据是否完整
SELECT COUNT(*) as total_tasks FROM tasks;

-- 验证周报和任务的关联关系
SELECT 
    wr.id as report_id,
    wr.title,
    COUNT(t.id) as task_count
FROM weekly_reports wr
LEFT JOIN tasks t ON t.weekly_report_id = wr.id
GROUP BY wr.id, wr.title
ORDER BY wr.id;