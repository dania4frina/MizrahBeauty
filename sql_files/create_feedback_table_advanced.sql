-- =============================================
-- CREATE FEEDBACK TABLE - ADVANCED VERSION
-- =============================================

-- Drop table if exists
IF EXISTS (SELECT * FROM sysobjects WHERE name='feedback' AND xtype='U')
    DROP TABLE feedback;

-- Create feedback table with constraints
CREATE TABLE feedback (
    id INT IDENTITY(1,1) PRIMARY KEY,
    user_email NVARCHAR(255) NOT NULL,
    user_name NVARCHAR(255) NOT NULL,
    feedback_text NVARCHAR(MAX) NOT NULL,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    feedback_type NVARCHAR(50) NOT NULL,
    status NVARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'REVIEWED', 'RESPONDED')),
    created_at DATETIME2 DEFAULT GETDATE(),
    response NVARCHAR(MAX),
    responded_at DATETIME2,
    FOREIGN KEY (user_email) REFERENCES users(email) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX IX_feedback_user_email ON feedback(user_email);
CREATE INDEX IX_feedback_status ON feedback(status);
CREATE INDEX IX_feedback_created_at ON feedback(created_at);
CREATE INDEX IX_feedback_feedback_type ON feedback(feedback_type);
CREATE INDEX IX_feedback_rating ON feedback(rating);

-- Insert sample data
INSERT INTO feedback (user_email, user_name, feedback_text, rating, feedback_type, status) VALUES
('admin@mizrahbeauty.com', 'Admin User', 'Service yang sangat memuaskan! Staff sangat professional dan mesra. Tempat juga sangat bersih dan selesa.', 5, 'Service Quality', 'PENDING'),
('user@example.com', 'John Doe', 'Tempat yang sangat bersih dan selesa. Akan datang lagi!', 4, 'Facility & Environment', 'PENDING'),
('customer@test.com', 'Jane Smith', 'Harga agak mahal untuk service yang diberikan. Tapi kualiti service memang bagus.', 3, 'Pricing', 'PENDING'),
('admin@mizrahbeauty.com', 'Admin User', 'Booking system sangat mudah digunakan. Terima kasih!', 5, 'Booking Experience', 'PENDING'),
('user@example.com', 'John Doe', 'Staff sangat pandai dan memberikan nasihat yang berguna. Sangat puas hati!', 5, 'Staff Performance', 'PENDING'),
('customer@test.com', 'Jane Smith', 'Saya cadangkan untuk tambah lebih banyak jenis treatment untuk lelaki.', 4, 'Suggestions', 'PENDING'),
('admin@mizrahbeauty.com', 'Admin User', 'Waktu tunggu agak lama. Mungkin boleh improve sistem appointment.', 3, 'Complaints', 'PENDING'),
('user@example.com', 'John Doe', 'Overall experience sangat bagus. Akan recommend kepada kawan-kawan.', 4, 'General Feedback', 'PENDING');

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

UPDATE feedback SET 
    status = 'RESPONDED', 
    response = 'Alhamdulillah! Kami gembira sistem booking kami memudahkan anda.',
    responded_at = GETDATE()
WHERE id = 4;

-- Create view for feedback with user details
CREATE VIEW v_feedback_with_users AS
SELECT 
    f.id,
    f.user_email,
    f.user_name,
    f.feedback_text,
    f.rating,
    f.feedback_type,
    f.status,
    f.created_at,
    f.response,
    f.responded_at,
    u.phone,
    u.first_name,
    u.last_name
FROM feedback f
LEFT JOIN users u ON f.user_email = u.email;

-- Create view for feedback statistics
CREATE VIEW v_feedback_stats AS
SELECT 
    COUNT(*) as total_feedback,
    AVG(CAST(rating AS FLOAT)) as average_rating,
    COUNT(CASE WHEN status = 'PENDING' THEN 1 END) as pending_count,
    COUNT(CASE WHEN status = 'REVIEWED' THEN 1 END) as reviewed_count,
    COUNT(CASE WHEN status = 'RESPONDED' THEN 1 END) as responded_count,
    COUNT(CASE WHEN rating = 5 THEN 1 END) as excellent_count,
    COUNT(CASE WHEN rating = 4 THEN 1 END) as good_count,
    COUNT(CASE WHEN rating = 3 THEN 1 END) as average_count,
    COUNT(CASE WHEN rating <= 2 THEN 1 END) as poor_count
FROM feedback;

-- Test queries
SELECT 'Feedback table created successfully!' AS Message;
SELECT COUNT(*) as total_feedback FROM feedback;
SELECT * FROM v_feedback_stats;

PRINT '=== FEEDBACK TABLE ADVANCED VERSION CREATED ===';
PRINT 'Includes constraints, indexes, views, and sample data';
