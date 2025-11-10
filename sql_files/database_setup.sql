-- MizrahBeauty Database Setup Script
-- SQL Server Database Creation and Setup

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
END
GO

-- Create products table for beauty products
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[products]') AND type in (N'U'))
BEGIN
    CREATE TABLE products (
        id INT IDENTITY(1,1) PRIMARY KEY,
        name VARCHAR(255) NOT NULL,
        description TEXT,
        price DECIMAL(10,2) NOT NULL,
        category VARCHAR(100),
        brand VARCHAR(100),
        stock_quantity INT DEFAULT 0,
        image_url VARCHAR(500),
        created_at DATETIME DEFAULT GETDATE(),
        updated_at DATETIME DEFAULT GETDATE(),
        is_active BIT DEFAULT 1
    );
END
GO

-- Create orders table
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[orders]') AND type in (N'U'))
BEGIN
    CREATE TABLE orders (
        id INT IDENTITY(1,1) PRIMARY KEY,
        user_id INT NOT NULL,
        order_date DATETIME DEFAULT GETDATE(),
        total_amount DECIMAL(10,2) NOT NULL,
        status VARCHAR(50) DEFAULT 'pending',
        shipping_address TEXT,
        payment_method VARCHAR(100),
        FOREIGN KEY (user_id) REFERENCES users(id)
    );
END
GO

-- Create order_items table
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[order_items]') AND type in (N'U'))
BEGIN
    CREATE TABLE order_items (
        id INT IDENTITY(1,1) PRIMARY KEY,
        order_id INT NOT NULL,
        product_id INT NOT NULL,
        quantity INT NOT NULL,
        unit_price DECIMAL(10,2) NOT NULL,
        FOREIGN KEY (order_id) REFERENCES orders(id),
        FOREIGN KEY (product_id) REFERENCES products(id)
    );
END
GO

-- Insert sample users
IF NOT EXISTS (SELECT * FROM users WHERE email = 'admin@mizrahbeauty.com')
BEGIN
    INSERT INTO users (email, password, first_name, last_name, phone) 
    VALUES ('admin@mizrahbeauty.com', 'admin123', 'Admin', 'User', '+1234567890');
END

IF NOT EXISTS (SELECT * FROM users WHERE email = 'user@example.com')
BEGIN
    INSERT INTO users (email, password, first_name, last_name, phone) 
    VALUES ('user@example.com', 'password123', 'John', 'Doe', '+1234567891');
END

IF NOT EXISTS (SELECT * FROM users WHERE email = 'customer@test.com')
BEGIN
    INSERT INTO users (email, password, first_name, last_name, phone) 
    VALUES ('customer@test.com', 'test123', 'Jane', 'Smith', '+1234567892');
END
GO

-- Insert sample beauty products
IF NOT EXISTS (SELECT * FROM products WHERE name = 'Natural Face Cream')
BEGIN
    INSERT INTO products (name, description, price, category, brand, stock_quantity) 
    VALUES ('Natural Face Cream', 'Hydrating face cream with natural ingredients', 29.99, 'Skincare', 'Mizrah Natural', 50);
END

IF NOT EXISTS (SELECT * FROM products WHERE name = 'Organic Shampoo')
BEGIN
    INSERT INTO products (name, description, price, category, brand, stock_quantity) 
    VALUES ('Organic Shampoo', 'Sulfate-free organic shampoo for all hair types', 19.99, 'Hair Care', 'Mizrah Organic', 75);
END

IF NOT EXISTS (SELECT * FROM products WHERE name = 'Lavender Body Lotion')
BEGIN
    INSERT INTO products (name, description, price, category, brand, stock_quantity) 
    VALUES ('Lavender Body Lotion', 'Soothing lavender body lotion with shea butter', 24.99, 'Body Care', 'Mizrah Natural', 60);
END

IF NOT EXISTS (SELECT * FROM products WHERE name = 'Vitamin C Serum')
BEGIN
    INSERT INTO products (name, description, price, category, brand, stock_quantity) 
    VALUES ('Vitamin C Serum', 'Brightening vitamin C serum for radiant skin', 34.99, 'Skincare', 'Mizrah Beauty', 40);
END

IF NOT EXISTS (SELECT * FROM products WHERE name = 'Argan Oil Hair Mask')
BEGIN
    INSERT INTO products (name, description, price, category, brand, stock_quantity) 
    VALUES ('Argan Oil Hair Mask', 'Deep conditioning hair mask with argan oil', 27.99, 'Hair Care', 'Mizrah Organic', 35);
END
GO

-- Create indexes for better performance
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_users_email')
BEGIN
    CREATE INDEX IX_users_email ON users(email);
END

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_products_category')
BEGIN
    CREATE INDEX IX_products_category ON products(category);
END

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_orders_user_id')
BEGIN
    CREATE INDEX IX_orders_user_id ON orders(user_id);
END
GO

-- Create stored procedure for user authentication
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
        RAISERROR ('Email already exists', 16, 1);
        RETURN;
    END
    
    -- Insert new user
    INSERT INTO users (email, password, first_name, last_name, phone)
    VALUES (@Email, @Password, @FirstName, @LastName, @Phone);
    
    SELECT SCOPE_IDENTITY() AS UserId;
END
GO

-- Display table information
SELECT 'Users Table' AS TableName, COUNT(*) AS RecordCount FROM users
UNION ALL
SELECT 'Products Table', COUNT(*) FROM products
UNION ALL
SELECT 'Orders Table', COUNT(*) FROM orders
UNION ALL
SELECT 'Order Items Table', COUNT(*) FROM order_items;
GO

PRINT 'Database setup completed successfully!';
PRINT 'Sample data has been inserted.';
PRINT 'You can now test the login with:';
PRINT 'Email: admin@mizrahbeauty.com, Password: admin123';
PRINT 'Email: user@example.com, Password: password123';
PRINT 'Email: customer@test.com, Password: test123'; 