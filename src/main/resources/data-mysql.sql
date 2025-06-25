-- MySQL Data Initialization Script
-- Run this script to set up initial data in your MySQL database
-- This is separate from the H2 data.sql to avoid compatibility issues

-- Note: Make sure your MySQL database 'techsupport' exists before running this script
-- CREATE DATABASE IF NOT EXISTS techsupport CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Insert initial data (adjust as needed for your production requirements)
-- The application will create the tables automatically with ddl-auto: update

-- Example: Insert default admin user or initial configuration data
-- INSERT INTO clients (name, email, phone, status, created_at, updated_at) 
-- VALUES ('System Admin', 'admin@techsupport.com', '+1-555-0100', 'ACTIVE', NOW(), NOW());

-- Add your production-specific initialization data here
-- This file will not be automatically loaded - you need to run it manually against your MySQL database 