-- =============================================
-- SIMPLE FEEDBACK TABLE CREATION
-- =============================================

-- Drop table if exists
IF EXISTS (SELECT * FROM sysobjects WHERE name='feedback' AND xtype='U')
    DROP TABLE feedback;

-- Create feedback table with correct foreign key
CREATE TABLE feedback (
    id INT IDENTITY(1,1) PRIMARY KEY,
    user_email NVARCHAR(255) NOT NULL,
    user_name NVARCHAR(255) NOT NULL,
    feedback_text NVARCHAR(MAX) NOT NULL,
    rating INT NOT NULL,
    feedback_type NVARCHAR(50) NOT NULL,
    status NVARCHAR(20) DEFAULT 'PENDING',
    created_at DATETIME2 DEFAULT GETDATE(),
    response NVARCHAR(MAX),
    responded_at DATETIME2,
    FOREIGN KEY (user_email) REFERENCES users(email)
);

-- Insert sample data
INSERT INTO feedback (user_email, user_name, feedback_text, rating, feedback_type) VALUES
('admin@mizrahbeauty.com', 'Admin User', 'Service yang sangat memuaskan!', 5, 'Service Quality'),
('user@example.com', 'John Doe', 'Tempat yang sangat bersih dan selesa.', 4, 'Facility & Environment'),
('customer@test.com', 'Jane Smith', 'Harga agak mahal tapi kualiti bagus.', 3, 'Pricing');

-- Test query
SELECT * FROM feedback;

PRINT 'Feedback table created successfully!';
