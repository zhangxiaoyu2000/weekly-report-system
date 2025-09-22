-- =============================================
-- Drop problematic week_end trigger that conflicts with service layer
-- Version: 1.0
-- Date: 2025-09-18
-- Description: Remove database trigger to allow service layer to handle weekEnd calculation
-- =============================================

-- Drop the existing trigger that's causing conflicts
DROP TRIGGER IF EXISTS `trg_set_week_end_date`;

-- Note: weekEnd will now be calculated entirely in the service layer
-- This prevents conflicts between database triggers and application logic