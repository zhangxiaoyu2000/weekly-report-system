-- Migration script to add AI Analysis tables
-- Version: V3
-- Description: Add AI Analysis tables for weekly report analysis functionality

-- Create ai_analysis_results table
CREATE TABLE IF NOT EXISTS ai_analysis_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_id BIGINT NOT NULL,
    analysis_type VARCHAR(50) NOT NULL,
    result TEXT NOT NULL,
    confidence DECIMAL(3,2),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    processing_time_ms BIGINT,
    model_version VARCHAR(100),
    parameters TEXT,
    error_message TEXT,
    metadata JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    
    -- Foreign key constraints
    CONSTRAINT fk_analysis_report FOREIGN KEY (report_id) REFERENCES weekly_reports(id) ON DELETE CASCADE,
    
    -- Check constraints
    CONSTRAINT chk_confidence_range CHECK (confidence >= 0.0 AND confidence <= 1.0),
    CONSTRAINT chk_analysis_status CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'CANCELLED')),
    CONSTRAINT chk_analysis_type CHECK (analysis_type IN (
        'SUMMARY', 'KEYWORDS', 'SENTIMENT', 'RISK_ASSESSMENT', 'SUGGESTIONS', 
        'PROGRESS_ANALYSIS', 'WORKLOAD_ANALYSIS', 'COLLABORATION_ANALYSIS', 
        'TREND_PREDICTION', 'COMPLETENESS_CHECK'
    ))
);

-- Create indexes for ai_analysis_results (with IF NOT EXISTS)
CREATE INDEX IF NOT EXISTS idx_analysis_report ON ai_analysis_results(report_id);
CREATE INDEX IF NOT EXISTS idx_analysis_type ON ai_analysis_results(analysis_type);
CREATE INDEX IF NOT EXISTS idx_analysis_status ON ai_analysis_results(status);
CREATE INDEX IF NOT EXISTS idx_analysis_created ON ai_analysis_results(created_at);
CREATE INDEX IF NOT EXISTS idx_analysis_completed ON ai_analysis_results(completed_at);

-- Composite indexes for common query patterns
CREATE INDEX IF NOT EXISTS idx_analysis_report_type ON ai_analysis_results(report_id, analysis_type);
CREATE INDEX IF NOT EXISTS idx_analysis_type_status ON ai_analysis_results(analysis_type, status);
CREATE INDEX IF NOT EXISTS idx_analysis_status_created ON ai_analysis_results(status, created_at);

-- Index for confidence-based queries (MySQL doesn't support partial indexes)
CREATE INDEX IF NOT EXISTS idx_analysis_confidence ON ai_analysis_results(confidence);

-- Index for performance monitoring (MySQL doesn't support partial indexes)
CREATE INDEX IF NOT EXISTS idx_analysis_processing_time ON ai_analysis_results(processing_time_ms);

-- Comments for documentation
-- AI-generated analysis results for weekly reports
-- Column comments removed (MySQL doesn't support COMMENT ON COLUMN syntax)