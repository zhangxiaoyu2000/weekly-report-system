-- Fix Flyway V3 migration failure
-- This script removes the failed V3 migration record and allows it to be retried

USE weekly_report_system;

-- Check current flyway_schema_history status
SELECT * FROM flyway_schema_history WHERE version = '3';

-- Delete the failed V3 migration record
DELETE FROM flyway_schema_history WHERE version = '3' AND success = 0;

-- Verify deletion
SELECT * FROM flyway_schema_history WHERE version = '3';

-- Show all migration history for reference
SELECT * FROM flyway_schema_history ORDER BY installed_rank;