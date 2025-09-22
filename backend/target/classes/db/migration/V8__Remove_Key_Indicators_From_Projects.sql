-- =============================================
-- 数据库迁移脚本 - 移除项目表中的关键性指标字段
-- Version: 8.0
-- Date: 2025-09-16
-- Description: 移除simple_projects表中的key_indicators字段，只保留expected_results
-- =============================================

-- 移除simple_projects表中的key_indicators字段
ALTER TABLE simple_projects 
DROP COLUMN key_indicators;

-- 移除simple_weekly_reports表中的key_indicators字段
ALTER TABLE simple_weekly_reports 
DROP COLUMN key_indicators;

-- 为expected_results字段添加注释，说明需要量化指标
ALTER TABLE simple_projects 
MODIFY COLUMN expected_results TEXT NOT NULL COMMENT '预期结果（需要以量化指标形式填写）';