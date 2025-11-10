-- MizrahBeauty Users Table Setup
-- SQL Server Database Script

-- Create the database if it doesn't exist
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'dania')
BEGIN
    CREATE DATABASE dania;
END
GO

-- Use the dania database
USE dania;
GO

-- Create users table
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[users]') AND type in (N'U'))
BEGIN
    CREATE TABLE users (
        id INT IDENTITY(1,1) PRIMARY KEY,
        email VARCHAR(255) NOT NULL UNIQUE,
        password VARCHAR(255) NOT NULL,
        first_name VARCHAR(100),
        last_name VARCHAR(100),
        phone VARCHAR(20),
        created_at DATETIME DEFAULT GETDATE(),
        updated_at DATETIME DEFAULT GETDATE(),
        is_active BIT DEFAULT 1
    );
    
    PRINT 'Users table created successfully!';
END
ELSE
BEGIN
    PRINT 'Users table already exists!';
END
GO

-- Insert sample users for testing
IF NOT EXISTS (SELECT * FROM users WHERE email = 'admin@mizrahbeauty.com')
BEGIN
    INSERT INTO users (email, password, first_name, last_name, phone) 
    VALUES ('admin@mizrahbeauty.com', 'admin123', 'Admin', 'User', '+1234567890');
    PRINT 'Admin user created';
END

IF NOT EXISTS (SELECT * FROM users WHERE email = 'user@example.com')
BEGIN
    INSERT INTO users (email, password, first_name, last_name, phone) 
    VALUES ('user@example.com', 'password123', 'John', 'Doe', '+1234567891');
    PRINT 'Sample user created';
END

IF NOT EXISTS (SELECT * FROM users WHERE email = 'customer@test.com')
BEGIN
    INSERT INTO users (email, password, first_name, last_name, phone) 
    VALUES ('customer@test.com', 'test123', 'Jane', 'Smith', '+1234567892');
    PRINT 'Customer user created';
END
GO

-- Create index for better performance
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_users_email')
BEGIN
    CREATE INDEX IX_users_email ON users(email);
    PRINT 'Email index created';
END
GO

-- Display all users
SELECT 'All Users in Database:' AS Message;
SELECT id, email, first_name, last_name, phone, created_at, is_active FROM users;
GO

PRINT '=== Database Setup Complete ===';
PRINT 'Test login credentials:';
PRINT 'Email: admin@mizrahbeauty.com, Password: admin123';
PRINT 'Email: user@example.com, Password: password123';
PRINT 'Email: customer@test.com, Password: test123'; 