-- V14__Add_Missing_User_Fields.sql
-- Add missing fields to users table to match Java entity

-- Add missing fields to users table
ALTER TABLE users 
ADD COLUMN employee_id VARCHAR(50),
ADD COLUMN position VARCHAR(100),
ADD COLUMN phone VARCHAR(20),
ADD COLUMN avatar_url VARCHAR(500),
ADD COLUMN full_name VARCHAR(101),
ADD COLUMN last_login_time TIMESTAMP;

-- Add index for employee_id for better query performance
CREATE INDEX idx_users_employee_id ON users(employee_id);

-- Add index for full_name for better search performance  
CREATE INDEX idx_users_full_name ON users(full_name);

-- Add index for phone for contact searches
CREATE INDEX idx_users_phone ON users(phone);

-- Update existing users to populate full_name from first_name and last_name
UPDATE users 
SET full_name = CONCAT(first_name, ' ', last_name)
WHERE full_name IS NULL;

-- Copy last_login to last_login_time for consistency
UPDATE users 
SET last_login_time = last_login
WHERE last_login_time IS NULL AND last_login IS NOT NULL;