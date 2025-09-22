-- =============================================
-- Fix week_end field constraint to allow proper trigger handling
-- Version: 1.0
-- Date: 2025-09-18
-- Description: Remove NOT NULL constraint from week_end to allow trigger to work properly
-- =============================================

-- Modify week_end column to allow NULL temporarily, then trigger will set proper value
ALTER TABLE `weekly_reports` 
MODIFY COLUMN `week_end` DATE NULL COMMENT '周结束日期';

-- Update the trigger to handle both NULL and empty values more robustly
DROP TRIGGER IF EXISTS `trg_set_week_end_date`;

DELIMITER //
CREATE TRIGGER `trg_set_week_end_date`
BEFORE INSERT ON `weekly_reports`
FOR EACH ROW
BEGIN
    -- Always calculate week_end based on week_start to ensure consistency
    IF NEW.week_start IS NOT NULL THEN
        SET NEW.week_end = DATE_ADD(NEW.week_start, INTERVAL 6 DAY);
    END IF;
END //
DELIMITER ;