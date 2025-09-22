-- V7__Add_Enhanced_AI_Analysis_Fields.sql
-- Add enhanced AI analysis fields to simple_projects and weekly_reports tables

-- Add enhanced AI analysis fields to simple_projects table
ALTER TABLE simple_projects 
ADD COLUMN ai_confidence DECIMAL(3,2),
ADD COLUMN ai_feasibility_score DECIMAL(3,2),
ADD COLUMN ai_risk_level VARCHAR(20) CHECK (ai_risk_level IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
ADD COLUMN ai_provider_used VARCHAR(50),
ADD COLUMN ai_processing_time_ms BIGINT,
ADD COLUMN ai_analyzed_at TIMESTAMP,
ADD COLUMN ai_key_issues JSON,
ADD COLUMN ai_recommendations JSON;

-- Add enhanced AI analysis fields to weekly_reports table
ALTER TABLE weekly_reports 
ADD COLUMN ai_confidence DECIMAL(3,2),
ADD COLUMN ai_quality_score DECIMAL(3,2),
ADD COLUMN ai_risk_level VARCHAR(20) CHECK (ai_risk_level IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
ADD COLUMN ai_provider_used VARCHAR(50),
ADD COLUMN ai_processing_time_ms BIGINT,
ADD COLUMN ai_analyzed_at TIMESTAMP,
ADD COLUMN ai_key_issues JSON,
ADD COLUMN ai_recommendations JSON;

-- Create indexes for better performance
CREATE INDEX idx_simple_projects_ai_risk ON simple_projects(ai_risk_level);
CREATE INDEX idx_simple_projects_ai_analyzed ON simple_projects(ai_analyzed_at);
CREATE INDEX idx_simple_projects_ai_provider ON simple_projects(ai_provider_used);

CREATE INDEX idx_weekly_reports_ai_risk ON weekly_reports(ai_risk_level);
CREATE INDEX idx_weekly_reports_ai_analyzed ON weekly_reports(ai_analyzed_at);
CREATE INDEX idx_weekly_reports_ai_provider ON weekly_reports(ai_provider_used);

-- Update existing records to have default values
UPDATE simple_projects 
SET ai_confidence = 0.95, 
    ai_feasibility_score = 0.80,
    ai_risk_level = 'LOW',
    ai_provider_used = 'mock',
    ai_analyzed_at = updated_at
WHERE ai_analysis_result IS NOT NULL 
  AND ai_confidence IS NULL;

UPDATE weekly_reports 
SET ai_confidence = 0.90,
    ai_quality_score = 0.85,
    ai_risk_level = 'LOW',
    ai_provider_used = 'mock',
    ai_analyzed_at = updated_at
WHERE status IN ('PENDING_ADMIN_REVIEW', 'ADMIN_REJECTED', 'PENDING_SA_REVIEW', 'SUPER_ADMIN_REJECTED', 'APPROVED')
  AND ai_confidence IS NULL;