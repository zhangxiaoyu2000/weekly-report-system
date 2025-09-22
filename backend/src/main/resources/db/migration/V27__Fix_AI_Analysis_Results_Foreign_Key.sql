-- 修复 ai_analysis_results 表的外键约束以支持项目分析
-- V27__Fix_AI_Analysis_Results_Foreign_Key.sql

-- 1. 首先删除现有的外键约束（如果存在）
-- 查看当前约束
SET @constraint_name = (SELECT CONSTRAINT_NAME FROM information_schema.KEY_COLUMN_USAGE 
                       WHERE TABLE_SCHEMA = DATABASE() 
                       AND TABLE_NAME = 'ai_analysis_results' 
                       AND COLUMN_NAME = 'report_id' 
                       AND REFERENCED_TABLE_NAME IS NOT NULL
                       LIMIT 1);

-- 删除外键约束（如果存在）
SET @sql = IF(@constraint_name IS NOT NULL, 
              CONCAT('ALTER TABLE ai_analysis_results DROP FOREIGN KEY ', @constraint_name), 
              'SELECT "No foreign key to drop" as message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. 修改 report_id 字段的注释，明确它可以存储项目ID或周报ID
ALTER TABLE ai_analysis_results MODIFY COLUMN report_id BIGINT NOT NULL 
    COMMENT '关联的实体ID (项目ID或周报ID，取决于analysis_type)';

-- 3. 添加 entity_type 字段来明确标识关联的实体类型
ALTER TABLE ai_analysis_results 
ADD COLUMN entity_type ENUM('PROJECT', 'WEEKLY_REPORT') NOT NULL DEFAULT 'WEEKLY_REPORT'
    COMMENT '关联的实体类型: PROJECT-项目, WEEKLY_REPORT-周报';

-- 4. 更新现有记录，将它们标记为周报类型（如果有的话）
UPDATE ai_analysis_results 
SET entity_type = 'WEEKLY_REPORT' 
WHERE entity_type = 'WEEKLY_REPORT';

-- 5. 为了保持数据完整性，添加检查约束（可选）
-- 注意：MySQL不支持复杂的CHECK约束，所以我们通过应用层来保证数据完整性

-- 6. 添加复合索引以提高查询性能
CREATE INDEX idx_ai_analysis_entity ON ai_analysis_results(entity_type, report_id);

-- 7. 添加注释说明新的表结构
ALTER TABLE ai_analysis_results 
COMMENT = 'AI分析结果表，支持项目和周报的AI分析结果存储';