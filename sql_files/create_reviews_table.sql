-- Create reviews table if it doesn't exist
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'reviews')
BEGIN
    CREATE TABLE reviews (
        id INT IDENTITY(1,1) PRIMARY KEY,
        user_email VARCHAR(255) NOT NULL,
        service_id INT NOT NULL,
        rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
        comment VARCHAR(1000),
        created_at DATETIME DEFAULT GETDATE(),
        FOREIGN KEY (service_id) REFERENCES services(id)
    );
END

-- Create services table if it doesn't exist
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'services')
BEGIN
    CREATE TABLE services (
        id INT IDENTITY(1,1) PRIMARY KEY,
        service_name VARCHAR(255) NOT NULL,
        description VARCHAR(1000),
        price DECIMAL(10,2) NOT NULL,
        duration INT NOT NULL,
        category VARCHAR(100),
        is_active BIT DEFAULT 1,
        created_at DATETIME DEFAULT GETDATE()
    );
END

-- Insert sample services if table is empty
IF NOT EXISTS (SELECT 1 FROM services)
BEGIN
    INSERT INTO services (service_name, description, price, duration, category) VALUES
    ('Facial Treatment', 'Deep cleansing facial treatment', 150.00, 60, 'Facial'),
    ('Hair Styling', 'Professional hair styling and cutting', 80.00, 45, 'Hair'),
    ('Manicure', 'Nail care and polish application', 50.00, 30, 'Nails'),
    ('Pedicure', 'Foot care and nail polish', 60.00, 45, 'Nails'),
    ('Massage', 'Relaxing full body massage', 120.00, 90, 'Wellness');
END

-- Insert sample reviews if table is empty
IF NOT EXISTS (SELECT 1 FROM reviews)
BEGIN
    INSERT INTO reviews (user_email, service_id, rating, comment) VALUES
    ('customer1@example.com', 1, 5, 'Excellent service, very satisfied!'),
    ('customer2@example.com', 2, 4, 'Good haircut, friendly staff'),
    ('customer3@example.com', 3, 5, 'Perfect manicure, will come again'),
    ('customer4@example.com', 1, 4, 'Great facial treatment'),
    ('customer5@example.com', 4, 5, 'Amazing pedicure, very relaxing');
END
