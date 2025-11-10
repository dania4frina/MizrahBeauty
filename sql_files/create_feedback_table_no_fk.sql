-- =============================================
-- CREATE FEEDBACK TABLE - WITHOUT FOREIGN KEY
-- =============================================

-- Drop table if exists
IF EXISTS (SELECT * FROM sysobjects WHERE name='feedback' AND xtype='U')
    DROP TABLE feedback;

-- Create feedback table without foreign key constraint
CREATE TABLE feedback (
    id INT IDENTITY(1,1) PRIMARY KEY,
    user_email VARCHAR(255) NOT NULL,
    user_name VARCHAR(255) NOT NULL,
    feedback_text NVARCHAR(MAX) NOT NULL,
    rating INT NOT NULL,
    feedback_type VARCHAR(50) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at DATETIME2 DEFAULT GETDATE(),
    response NVARCHAR(MAX),
    responded_at DATETIME2
    -- No foreign key constraint to avoid data type issues
);

-- Create indexes for better performance
CREATE INDEX IX_feedback_user_email ON feedback(user_email);
CREATE INDEX IX_feedback_status ON feedback(status);
CREATE INDEX IX_feedback_created_at ON feedback(created_at);

-- Insert sample data
INSERT INTO feedback (user_email, user_name, feedback_text, rating, feedback_type) VALUES
('admin@mizrahbeauty.com', 'Admin User', 'Service yang sangat memuaskan! Staff sangat professional dan mesra.', 5, 'Service Quality'),
('user@example.com', 'John Doe', 'Tempat yang sangat bersih dan selesa. Akan datang lagi!', 4, 'Facility & Environment'),
('customer@test.com', 'Jane Smith', 'Harga agak mahal untuk service yang diberikan. Tapi kualiti service memang bagus.', 3, 'Pricing'),
('admin@mizrahbeauty.com', 'Admin User', 'Booking system sangat mudah digunakan. Terima kasih!', 5, 'Booking Experience'),
('user@example.com', 'John Doe', 'Staff sangat pandai dan memberikan nasihat yang berguna. Sangat puas hati!', 5, 'Staff Performance'),
('customer@test.com', 'Jane Smith', 'Saya cadangkan untuk tambah lebih banyak jenis treatment untuk lelaki.', 4, 'Suggestions'),
('admin@mizrahbeauty.com', 'Admin User', 'Waktu tunggu agak lama. Mungkin boleh improve sistem appointment.', 3, 'Complaints'),
('user@example.com', 'John Doe', 'Overall experience sangat bagus. Akan recommend kepada kawan-kawan.', 4, 'General Feedback');

-- Update some feedback with responses
UPDATE feedback SET 
    status = 'RESPONDED', 
    response = 'Terima kasih atas feedback positif anda! Kami akan terus berusaha memberikan service terbaik.',
    responded_at = GETDATE()
WHERE id = 1;

UPDATE feedback SET 
    status = 'RESPONDED', 
    response = 'Terima kasih! Kami sentiasa menjaga kebersihan dan keselesaan untuk pelanggan.',
    responded_at = GETDATE()
WHERE id = 2;

UPDATE feedback SET 
    status = 'REVIEWED', 
    response = 'Kami akan menimbangkan semula struktur harga untuk memberikan nilai terbaik kepada pelanggan.',
    responded_at = GETDATE()
WHERE id = 3;

-- Test queries
SELECT 'Feedback table created successfully!' AS Message;
SELECT COUNT(*) as total_feedback FROM feedback;
SELECT * FROM feedback ORDER BY created_at DESC;

PRINT '=== FEEDBACK TABLE CREATED WITHOUT FOREIGN KEY ===';
PRINT 'Data types match users table (VARCHAR)';
PRINT 'No foreign key constraint to avoid data type issues';
PRINT 'Total feedback records: ' + CAST((SELECT COUNT(*) FROM feedback) AS VARCHAR(10));
