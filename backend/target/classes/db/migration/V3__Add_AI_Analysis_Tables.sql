-- Migration script to add AI Analysis tables
-- Version: V3
-- Description: Add AI Analysis tables for weekly report analysis functionality

-- Create ai_analysis_results table
CREATE TABLE ai_analysis_results (
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

-- Create indexes for ai_analysis_results
CREATE INDEX idx_analysis_report ON ai_analysis_results(report_id);
CREATE INDEX idx_analysis_type ON ai_analysis_results(analysis_type);
CREATE INDEX idx_analysis_status ON ai_analysis_results(status);
CREATE INDEX idx_analysis_created ON ai_analysis_results(created_at);
CREATE INDEX idx_analysis_completed ON ai_analysis_results(completed_at);

-- Composite indexes for common query patterns
CREATE INDEX idx_analysis_report_type ON ai_analysis_results(report_id, analysis_type);
CREATE INDEX idx_analysis_type_status ON ai_analysis_results(analysis_type, status);
CREATE INDEX idx_analysis_status_created ON ai_analysis_results(status, created_at);

-- Index for confidence-based queries
CREATE INDEX idx_analysis_confidence ON ai_analysis_results(confidence) WHERE status = 'COMPLETED';

-- Index for performance monitoring
CREATE INDEX idx_analysis_processing_time ON ai_analysis_results(processing_time_ms) WHERE status = 'COMPLETED';

-- Comments for documentation
COMMENT ON TABLE ai_analysis_results IS 'AI-generated analysis results for weekly reports';
COMMENT ON COLUMN ai_analysis_results.report_id IS 'Foreign key to weekly_reports table';
COMMENT ON COLUMN ai_analysis_results.analysis_type IS 'Type of analysis performed (SUMMARY, KEYWORDS, etc.)';
COMMENT ON COLUMN ai_analysis_results.result IS 'JSON or text result of the analysis';
COMMENT ON COLUMN ai_analysis_results.confidence IS 'Confidence score between 0.0 and 1.0';
COMMENT ON COLUMN ai_analysis_results.status IS 'Current status of the analysis task';
COMMENT ON COLUMN ai_analysis_results.processing_time_ms IS 'Time taken to process the analysis in milliseconds';
COMMENT ON COLUMN ai_analysis_results.model_version IS 'Version of the AI model used for analysis';
COMMENT ON COLUMN ai_analysis_results.parameters IS 'JSON parameters used for the analysis';
COMMENT ON COLUMN ai_analysis_results.error_message IS 'Error message if analysis failed';
COMMENT ON COLUMN ai_analysis_results.metadata IS 'Additional metadata in JSON format';
COMMENT ON COLUMN ai_analysis_results.completed_at IS 'Timestamp when analysis was completed';