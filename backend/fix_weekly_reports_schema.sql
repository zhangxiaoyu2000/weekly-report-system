-- 修复weekly_reports表缺失的AI相关字段和时间戳字段
-- 对应WeeklyReport实体的字段映射

USE qr_auth_dev;

-- 检查并添加AI分析相关字段
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = 'qr_auth_dev' 
       AND TABLE_NAME = 'weekly_reports' 
       AND COLUMN_NAME = 'ai_analyzed_at') = 0,
    'ALTER TABLE weekly_reports ADD COLUMN ai_analyzed_at DATETIME NULL COMMENT \'AI分析时间\'',
    'SELECT \'ai_analyzed_at already exists\' as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = 'qr_auth_dev' 
       AND TABLE_NAME = 'weekly_reports' 
       AND COLUMN_NAME = 'ai_quality_score') = 0,
    'ALTER TABLE weekly_reports ADD COLUMN ai_quality_score DOUBLE NULL COMMENT \'AI质量评分\'',
    'SELECT \'ai_quality_score already exists\' as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = 'qr_auth_dev' 
       AND TABLE_NAME = 'weekly_reports' 
       AND COLUMN_NAME = 'ai_risk_level') = 0,
    'ALTER TABLE weekly_reports ADD COLUMN ai_risk_level VARCHAR(50) NULL COMMENT \'AI风险等级\'',
    'SELECT \'ai_risk_level already exists\' as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加时间戳字段
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = 'qr_auth_dev' 
       AND TABLE_NAME = 'weekly_reports' 
       AND COLUMN_NAME = 'created_at') = 0,
    'ALTER TABLE weekly_reports ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT \'创建时间\'',
    'SELECT \'created_at already exists\' as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = 'qr_auth_dev' 
       AND TABLE_NAME = 'weekly_reports' 
       AND COLUMN_NAME = 'updated_at') = 0,
    'ALTER TABLE weekly_reports ADD COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT \'更新时间\'',
    'SELECT \'updated_at already exists\' as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 验证表结构
DESCRIBE weekly_reports;

-- 显示成功信息
SELECT 'Weekly reports schema updated successfully!' AS status;