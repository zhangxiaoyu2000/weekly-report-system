#!/bin/bash

# Schema fix script for weekly-report backend
# This script connects to MySQL and applies schema fixes

echo "🔧 Starting database schema fix..."

# Database connection parameters
DB_HOST="localhost"
DB_PORT="3307"
DB_NAME="qr_auth_dev"
DB_USER="root"
DB_PASS="rootpass123"

# Check if mysql client is available
if command -v mysql &> /dev/null; then
    echo "✅ Using mysql client"
    MYSQL_CMD="mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASS $DB_NAME"
elif command -v docker &> /dev/null && docker ps | grep -q mysql; then
    echo "✅ Using docker mysql"
    MYSQL_CMD="docker exec -i $(docker ps --format 'table {{.Names}}' | grep mysql | head -1) mysql -u $DB_USER -p$DB_PASS $DB_NAME"
else
    echo "❌ No mysql client found. Trying alternative approach..."
    echo "Please ensure MySQL is available or use Spring Boot to execute the SQL"
    exit 1
fi

# Execute the schema fix
echo "🔄 Executing schema fix SQL..."
$MYSQL_CMD < schema-fix.sql

if [ $? -eq 0 ]; then
    echo "✅ Schema fix completed successfully!"
    echo "🔄 Now you can restart the Spring Boot application"
else
    echo "❌ Schema fix failed. Please check the error above."
    exit 1
fi