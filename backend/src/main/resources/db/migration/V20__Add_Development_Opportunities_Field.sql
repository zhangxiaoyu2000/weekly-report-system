-- V17: 为 weekly_reports 表添加可发展性清单字段
-- 用于存储主管填写的可发展性机会和建议

ALTER TABLE `weekly_reports` 
ADD COLUMN `development_opportunities` TEXT COMMENT '可发展性清单 - 主管填写的可发展性机会和建议';