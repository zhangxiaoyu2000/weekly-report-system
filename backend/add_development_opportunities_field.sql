-- Add development_opportunities field to weekly_reports table
ALTER TABLE weekly_reports 
ADD COLUMN development_opportunities TEXT COMMENT '可发展性清单 - 主管填写的可发展性机会和建议';