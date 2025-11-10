-- Create Users Table for MizrahBeauty
-- Simple SQL Query to Add Table

-- Use the dania database
USE dania;
GO

-- Create users table
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
GO

-- Create index for better performance
CREATE INDEX IX_users_email ON users(email);
GO

-- Insert sample users for testing
INSERT INTO users (email, password, first_name, last_name, phone) 
VALUES ('admin@mizrahbeauty.com', 'admin123', 'Admin', 'User', '+1234567890');

INSERT INTO users (email, password, first_name, last_name, phone) 
VALUES ('user@example.com', 'password123', 'John', 'Doe', '+1234567891');

INSERT INTO users (email, password, first_name, last_name, phone) 
VALUES ('customer@test.com', 'test123', 'Jane', 'Smith', '+1234567892');
GO

-- Show the table structure
SELECT 'Users Table Created Successfully!' AS Message;
SELECT 'Table Structure:' AS Info;
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'users'
ORDER BY ORDINAL_POSITION;
GO

-- Show sample data
SELECT 'Sample Users:' AS Info;
SELECT id, email, first_name, last_name, phone, created_at, is_active 
FROM users;
GO

PRINT '=== Users Table Created Successfully ===';
PRINT 'Test login credentials:';
PRINT 'Email: admin@mizrahbeauty.com, Password: admin123';
PRINT 'Email: user@example.com, Password: password123';
PRINT 'Email: customer@test.com, Password: test123'; 