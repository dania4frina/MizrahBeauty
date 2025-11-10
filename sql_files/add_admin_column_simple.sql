-- Add Admin Column to Simple Users Table
-- For users table with only: id, name, email, password

-- Use the dania database
USE dania;
GO

-- Add is_admin column to existing users table
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'users' AND COLUMN_NAME = 'is_admin')
BEGIN
    ALTER TABLE users ADD is_admin BIT DEFAULT 0;
    PRINT 'is_admin column added successfully!';
END
ELSE
BEGIN
    PRINT 'is_admin column already exists!';
END
GO

-- Update existing admin user to have admin privileges (if exists)
IF EXISTS (SELECT * FROM users WHERE email = 'admin@mizrahbeauty.com')
BEGIN
    UPDATE users SET is_admin = 1 WHERE email = 'admin@mizrahbeauty.com';
    PRINT 'Existing admin user updated with admin privileges';
END
GO

-- Create a new admin user if none exists
IF NOT EXISTS (SELECT * FROM users WHERE is_admin = 1)
BEGIN
    INSERT INTO users (name, email, password, is_admin) 
    VALUES ('Admin User', 'admin@mizrahbeauty.com', 'admin123', 1);
    PRINT 'New admin user created: admin@mizrahbeauty.com / admin123';
END
GO

-- Show the updated table structure
SELECT 'Updated Users Table Structure:' AS Message;
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'users'
ORDER BY ORDINAL_POSITION;
GO

-- Show current users with admin status
SELECT 'Current Users with Admin Status:' AS Message;
SELECT id, name, email, is_admin 
FROM users 
ORDER BY is_admin DESC, id DESC;
GO

-- Show final admin users
SELECT 'Final Admin Users:' AS Message;
SELECT id, name, email 
FROM users 
WHERE is_admin = 1 
ORDER BY id DESC;
GO

PRINT '=== Admin Column Setup Complete ===';
PRINT 'You can now use admin functionality in your Android app!';
PRINT 'Admin user: admin@mizrahbeauty.com (password: admin123)'; 