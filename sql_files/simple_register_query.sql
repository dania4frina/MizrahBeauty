-- MizrahBeauty Simple Registration Query
-- For Register Page with Email, Password, and Confirm Password only

-- Use the dania database
USE dania;
GO

-- 1. Check if email already exists (for registration validation)
SELECT 'Check Email Exists Query:' AS QueryType;
SELECT 'SELECT COUNT(*) FROM users WHERE email = ?' AS Query;
GO

-- 2. Insert new user with email and password only
SELECT 'Insert New User Query:' AS QueryType;
SELECT 'INSERT INTO users (email, password) VALUES (?, ?)' AS Query;
GO

-- 3. Validate login (email and password)
SELECT 'Validate Login Query:' AS QueryType;
SELECT 'SELECT * FROM users WHERE email = ? AND password = ? AND is_active = 1' AS Query;
GO

-- Simple Stored Procedure for Registration
SELECT '=== Simple Registration Stored Procedure ===' AS Message;
GO

-- Create simple stored procedure for user registration
IF EXISTS (SELECT * FROM sys.objects WHERE type = 'P' AND name = 'sp_SimpleRegisterUser')
    DROP PROCEDURE sp_SimpleRegisterUser
GO

CREATE PROCEDURE sp_SimpleRegisterUser
    @Email VARCHAR(255),
    @Password VARCHAR(255)
AS
BEGIN
    SET NOCOUNT ON;
    
    -- Check if email already exists
    IF EXISTS (SELECT 1 FROM users WHERE email = @Email)
    BEGIN
        SELECT -1 AS UserId, 'Email already exists' AS Message;
        RETURN;
    END
    
    -- Insert new user with only email and password
    INSERT INTO users (email, password)
    VALUES (@Email, @Password);
    
    SELECT SCOPE_IDENTITY() AS UserId, 'Registration successful' AS Message;
END
GO

-- Simple Stored Procedure for Login
SELECT '=== Simple Login Stored Procedure ===' AS Message;
GO

-- Create simple stored procedure for user login
IF EXISTS (SELECT * FROM sys.objects WHERE type = 'P' AND name = 'sp_SimpleValidateUser')
    DROP PROCEDURE sp_SimpleValidateUser
GO

CREATE PROCEDURE sp_SimpleValidateUser
    @Email VARCHAR(255),
    @Password VARCHAR(255)
AS
BEGIN
    SET NOCOUNT ON;
    
    SELECT id, email, is_active
    FROM users 
    WHERE email = @Email 
    AND password = @Password 
    AND is_active = 1;
END
GO

-- Test the simple registration
SELECT '=== Testing Simple Registration ===' AS Message;
GO

-- Test 1: Register new user
SELECT 'Testing Simple Registration:' AS Test;
EXEC sp_SimpleRegisterUser 'simpleuser@test.com', 'password123';
GO

-- Test 2: Try to register same email again (should fail)
SELECT 'Testing Duplicate Email:' AS Test;
EXEC sp_SimpleRegisterUser 'simpleuser@test.com', 'password123';
GO

-- Test 3: Login with the new user
SELECT 'Testing Simple Login:' AS Test;
EXEC sp_SimpleValidateUser 'simpleuser@test.com', 'password123';
GO

-- Show all users
SELECT '=== Current Users in Database ===' AS Message;
SELECT id, email, created_at, is_active FROM users ORDER BY created_at DESC;
GO

-- Direct SQL queries for your Android app
SELECT '=== Direct SQL Queries for Android ===' AS Message;
GO

-- Query 1: Check if email exists
SELECT 'Email Check Query:' AS QueryType;
SELECT 'SELECT COUNT(*) FROM users WHERE email = ?' AS Query;
GO

-- Query 2: Register new user
SELECT 'Register Query:' AS QueryType;
SELECT 'INSERT INTO users (email, password) VALUES (?, ?)' AS Query;
GO

-- Query 3: Login validation
SELECT 'Login Query:' AS QueryType;
SELECT 'SELECT * FROM users WHERE email = ? AND password = ? AND is_active = 1' AS Query;
GO

PRINT '=== Simple Registration Queries Ready ===';
PRINT 'Perfect for your register page with email, password, and confirm password!';
PRINT 'Use these queries in your ConnectionClass.java file.'; 