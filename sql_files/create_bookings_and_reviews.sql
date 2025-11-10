-- Create required tables for booking and reviews in MizrahBeauty DB

-- Use the MizrahBeauty database (matches ConnectionClass DB_URL)
IF DB_ID('MizrahBeauty') IS NULL
BEGIN
    CREATE DATABASE MizrahBeauty;
END
GO

USE MizrahBeauty;
GO

-- Ensure services table exists (minimal shape used by the app)
IF OBJECT_ID('dbo.services', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.services (
        id INT IDENTITY(1,1) PRIMARY KEY,
        category VARCHAR(100) NULL,
        service_name VARCHAR(255) NOT NULL,
        price DECIMAL(10,2) NOT NULL,
        duration_minutes INT NULL,
        details VARCHAR(1000) NULL
    );
END
GO

-- Create bookings table
IF OBJECT_ID('dbo.bookings', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.bookings (
        id INT IDENTITY(1,1) PRIMARY KEY,
        user_email VARCHAR(255) NOT NULL,
        service_id INT NOT NULL,
        appointment_time DATETIME NOT NULL,
        notes VARCHAR(1000) NULL,
        status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE'
    );
    CREATE INDEX IX_bookings_user_email ON dbo.bookings(user_email);
    CREATE INDEX IX_bookings_service_id ON dbo.bookings(service_id);
    CREATE INDEX IX_bookings_time ON dbo.bookings(appointment_time);
END
GO

-- Create reviews table
IF OBJECT_ID('dbo.reviews', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.reviews (
        id INT IDENTITY(1,1) PRIMARY KEY,
        user_email VARCHAR(255) NOT NULL,
        service_id INT NOT NULL,
        rating INT NOT NULL,
        comment VARCHAR(2000) NULL,
        created_at DATETIME NOT NULL DEFAULT GETDATE()
    );
    CREATE INDEX IX_reviews_user_email ON dbo.reviews(user_email);
    CREATE INDEX IX_reviews_service_id ON dbo.reviews(service_id);
END
GO

PRINT 'Tables ensured: services, bookings, reviews in MizrahBeauty database.';

