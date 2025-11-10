-- MizrahBeauty Registration Queries
-- SQL Server Queries for User Registration

-- Use the dania database
USE dania;
GO

-- 1. Check if email already exists (for registration validation)
-- This query checks if an email is already registered
SELECT 'Check Email Exists Query:' AS QueryType;
SELECT 'SELECT COUNT(*) FROM users WHERE email = ?' AS Query;
GO

-- 2. Insert new user registration
-- This query inserts a new user into the database
SELECT 'Insert New User Query:' AS QueryType;
SELECT 'INSERT INTO users (email, password, first_name, last_name, phone) VALUES (?, ?, ?, ?, ?)' AS Query;
GO

-- 3. Get user by email (for login validation)
-- This query validates user login credentials
SELECT 'Validate User Login Query:' AS QueryType;
SELECT 'SELECT * FROM users WHERE email = ? AND password = ? AND is_active = 1' AS Query;
GO

-- 4. Update user information
-- This query updates user profile information
SELECT 'Update User Query:' AS QueryType;
SELECT 'UPDATE users SET first_name = ?, last_name = ?, phone = ?, updated_at = GETDATE() WHERE email = ?' AS Query;
GO

-- 5. Deactivate user account
-- This query deactivates a user account
SELECT 'Deactivate User Query:' AS QueryType;
SELECT 'UPDATE users SET is_active = 0, updated_at = GETDATE() WHERE email = ?' AS Query;
GO

-- 6. Get all active users
-- This query gets all active users
SELECT 'Get All Active Users Query:' AS QueryType;
SELECT 'SELECT id, email, first_name, last_name, phone, created_at FROM users WHERE is_active = 1' AS Query;
GO

-- 7. Get user by ID
-- This query gets user information by ID
SELECT 'Get User By ID Query:' AS QueryType;
SELECT 'SELECT id, email, first_name, last_name, phone, created_at FROM users WHERE id = ? AND is_active = 1' AS Query;
GO

-- 8. Change password
-- This query allows users to change their password
SELECT 'Change Password Query:' AS QueryType;
SELECT 'UPDATE users SET password = ?, updated_at = GETDATE() WHERE email = ? AND password = ?' AS Query;
GO

-- 9. Search users by name
-- This query searches users by first or last name
SELECT 'Search Users Query:' AS QueryType;
SELECT 'SELECT id, email, first_name, last_name, phone FROM users WHERE (first_name LIKE ? OR last_name LIKE ?) AND is_active = 1' AS Query;
GO

-- 10. Get user count
-- This query gets total number of registered users
SELECT 'Get User Count Query:' AS QueryType;
SELECT 'SELECT COUNT(*) as total_users FROM users WHERE is_active = 1' AS Query;
GO

-- Sample registration test queries
SELECT '=== Sample Registration Test Queries ===' AS Message;
GO

-- Test 1: Check if email exists
SELECT 'Test 1 - Check if email exists:' AS Test;
SELECT 'SELECT COUNT(*) FROM users WHERE email = ''test@example.com''' AS Query;
GO

-- Test 2: Insert new user
SELECT 'Test 2 - Insert new user:' AS Test;
SELECT 'INSERT INTO users (email, password, first_name, last_name, phone) VALUES (''newuser@test.com'', ''password123'', ''New'', ''User'', ''+1234567899'')' AS Query;
GO

-- Test 3: Validate login
SELECT 'Test 3 - Validate login:' AS Test;
SELECT 'SELECT * FROM users WHERE email = ''newuser@test.com'' AND password = ''password123'' AND is_active = 1' AS Query;
GO

-- Stored Procedure for Registration
SELECT '=== Stored Procedure for Registration ===' AS Message;
GO

-- Create stored procedure for user registration
IF EXISTS (SELECT * FROM sys.objects WHERE type = 'P' AND name = 'sp_RegisterUser')
    DROP PROCEDURE sp_RegisterUser
GO

CREATE PROCEDURE sp_RegisterUser
    @Email VARCHAR(255),
    @Password VARCHAR(255),
    @FirstName VARCHAR(100) = NULL,
    @LastName VARCHAR(100) = NULL,
    @Phone VARCHAR(20) = NULL
AS
BEGIN
    SET NOCOUNT ON;
    
    -- Check if email already exists
    IF EXISTS (SELECT 1 FROM users WHERE email = @Email)
    BEGIN
        SELECT -1 AS UserId, 'Email already exists' AS Message;
        RETURN;
    END
    
    -- Insert new user
    INSERT INTO users (email, password, first_name, last_name, phone)
    VALUES (@Email, @Password, @FirstName, @LastName, @Phone);
    
    SELECT SCOPE_IDENTITY() AS UserId, 'Registration successful' AS Message;
END
GO

-- Stored Procedure for Login Validation
SELECT '=== Stored Procedure for Login ===' AS Message;
GO

-- Create stored procedure for user login
IF EXISTS (SELECT * FROM sys.objects WHERE type = 'P' AND name = 'sp_ValidateUser')
    DROP PROCEDURE sp_ValidateUser
GO

CREATE PROCEDURE sp_ValidateUser
    @Email VARCHAR(255),
    @Password VARCHAR(255)
AS
BEGIN
    SET NOCOUNT ON;
    
    SELECT id, email, first_name, last_name, is_active
    FROM users 
    WHERE email = @Email 
    AND password = @Password 
    AND is_active = 1;
END
GO

-- Test the stored procedures
SELECT '=== Testing Stored Procedures ===' AS Message;
GO

-- Test registration procedure
SELECT 'Testing Registration Procedure:' AS Test;
EXEC sp_RegisterUser 'testuser@example.com', 'testpass123', 'Test', 'User', '+1234567898';
GO

-- Test login procedure
SELECT 'Testing Login Procedure:' AS Test;
EXEC sp_ValidateUser 'testuser@example.com', 'testpass123';
GO

-- Show all users
SELECT '=== Current Users in Database ===' AS Message;
SELECT id, email, first_name, last_name, phone, created_at, is_active FROM users ORDER BY created_at DESC;
GO

PRINT '=== Registration Queries Ready ===';
PRINT 'You can now use these queries in your Android app!';
PRINT 'The stored procedures provide better security and performance.'; 