-- =============================================
-- CREATE FEEDBACK TABLE - MIZRAH BEAUTY
-- =============================================

-- Drop table if exists (for testing)
IF EXISTS (SELECT * FROM sysobjects WHERE name='feedback' AND xtype='U')
    DROP TABLE feedback;

-- Create feedback table
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
    
    -- Foreign key constraint
    FOREIGN KEY (user_email) REFERENCES users(user_email) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX IX_feedback_user_email ON feedback(user_email);
CREATE INDEX IX_feedback_status ON feedback(status);
CREATE INDEX IX_feedback_created_at ON feedback(created_at);
CREATE INDEX IX_feedback_feedback_type ON feedback(feedback_type);

-- =============================================
-- INSERT SAMPLE DATA
-- =============================================

-- Insert sample feedback data
INSERT INTO feedback (user_email, user_name, feedback_text, rating, feedback_type, status) VALUES
('user1@example.com', 'Ahmad Ali', 'Service yang sangat memuaskan! Staff sangat professional dan mesra. Tempat juga sangat bersih dan selesa.', 5, 'Service Quality', 'PENDING'),
('user2@example.com', 'Siti Sarah', 'Tempat yang sangat bersih dan selesa. Akan datang lagi!', 4, 'Facility & Environment', 'PENDING'),
('user3@example.com', 'Muhammad Hassan', 'Harga agak mahal untuk service yang diberikan. Tapi kualiti service memang bagus.', 3, 'Pricing', 'PENDING'),
('user1@example.com', 'Ahmad Ali', 'Booking system sangat mudah digunakan. Terima kasih!', 5, 'Booking Experience', 'PENDING'),
('user2@example.com', 'Siti Sarah', 'Staff sangat pandai dan memberikan nasihat yang berguna. Sangat puas hati!', 5, 'Staff Performance', 'PENDING'),
('user4@example.com', 'Fatimah Zahra', 'Saya cadangkan untuk tambah lebih banyak jenis treatment untuk lelaki.', 4, 'Suggestions', 'PENDING'),
('user5@example.com', 'Ali Rahman', 'Waktu tunggu agak lama. Mungkin boleh improve sistem appointment.', 3, 'Complaints', 'PENDING'),
('user3@example.com', 'Muhammad Hassan', 'Overall experience sangat bagus. Akan recommend kepada kawan-kawan.', 4, 'General Feedback', 'PENDING');

-- Update some feedback with responses (simulate admin responses)
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

-- =============================================
-- USEFUL QUERIES FOR FEEDBACK MANAGEMENT
-- =============================================

-- 1. Get all feedback with user details
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
    u.phone_number
FROM feedback f
LEFT JOIN users u ON f.user_email = u.user_email
ORDER BY f.created_at DESC;

-- 2. Get feedback by status
SELECT * FROM feedback WHERE status = 'PENDING' ORDER BY created_at DESC;
SELECT * FROM feedback WHERE status = 'REVIEWED' ORDER BY created_at DESC;
SELECT * FROM feedback WHERE status = 'RESPONDED' ORDER BY created_at DESC;

-- 3. Get feedback by type
SELECT * FROM feedback WHERE feedback_type = 'Service Quality' ORDER BY created_at DESC;
SELECT * FROM feedback WHERE feedback_type = 'Staff Performance' ORDER BY created_at DESC;
SELECT * FROM feedback WHERE feedback_type = 'Pricing' ORDER BY created_at DESC;

-- 4. Get feedback by rating
SELECT * FROM feedback WHERE rating = 5 ORDER BY created_at DESC; -- Excellent
SELECT * FROM feedback WHERE rating = 4 ORDER BY created_at DESC; -- Good
SELECT * FROM feedback WHERE rating = 3 ORDER BY created_at DESC; -- Average
SELECT * FROM feedback WHERE rating <= 2 ORDER BY created_at DESC; -- Poor

-- 5. Get user's feedback history
SELECT * FROM feedback WHERE user_email = 'user1@example.com' ORDER BY created_at DESC;

-- 6. Get feedback statistics
SELECT 
    COUNT(*) as total_feedback,
    AVG(CAST(rating AS FLOAT)) as average_rating,
    COUNT(CASE WHEN status = 'PENDING' THEN 1 END) as pending_count,
    COUNT(CASE WHEN status = 'REVIEWED' THEN 1 END) as reviewed_count,
    COUNT(CASE WHEN status = 'RESPONDED' THEN 1 END) as responded_count
FROM feedback;

-- 7. Get feedback by date range
SELECT * FROM feedback 
WHERE created_at >= DATEADD(day, -7, GETDATE()) 
ORDER BY created_at DESC;

-- 8. Get feedback by month
SELECT 
    YEAR(created_at) as year,
    MONTH(created_at) as month,
    COUNT(*) as feedback_count,
    AVG(CAST(rating AS FLOAT)) as average_rating
FROM feedback 
GROUP BY YEAR(created_at), MONTH(created_at)
ORDER BY year DESC, month DESC;

-- 9. Get top feedback types
SELECT 
    feedback_type,
    COUNT(*) as count,
    AVG(CAST(rating AS FLOAT)) as average_rating
FROM feedback 
GROUP BY feedback_type
ORDER BY count DESC;

-- 10. Get feedback without responses
SELECT * FROM feedback 
WHERE response IS NULL OR response = '' 
ORDER BY created_at DESC;

-- =============================================
-- UPDATE QUERIES
-- =============================================

-- Update feedback status
UPDATE feedback SET status = 'REVIEWED' WHERE id = 1;

-- Update feedback with response
UPDATE feedback SET 
    status = 'RESPONDED',
    response = 'Terima kasih atas feedback anda. Kami akan improve service kami.',
    responded_at = GETDATE()
WHERE id = 1;

-- =============================================
-- DELETE QUERIES (BE CAREFUL!)
-- =============================================

-- Delete specific feedback (use with caution!)
-- DELETE FROM feedback WHERE id = 1;

-- Delete old feedback (older than 1 year)
-- DELETE FROM feedback WHERE created_at < DATEADD(year, -1, GETDATE());

-- =============================================
-- VIEWS FOR EASY ACCESS
-- =============================================

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
    u.phone_number,
    u.full_name
FROM feedback f
LEFT JOIN users u ON f.user_email = u.user_email;

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

-- =============================================
-- STORED PROCEDURES
-- =============================================

-- Procedure to submit feedback
CREATE PROCEDURE sp_submit_feedback
    @user_email NVARCHAR(255),
    @user_name NVARCHAR(255),
    @feedback_text NVARCHAR(MAX),
    @rating INT,
    @feedback_type NVARCHAR(50)
AS
BEGIN
    INSERT INTO feedback (user_email, user_name, feedback_text, rating, feedback_type)
    VALUES (@user_email, @user_name, @feedback_text, @rating, @feedback_type);
    
    SELECT @@IDENTITY as new_feedback_id;
END;

-- Procedure to update feedback status
CREATE PROCEDURE sp_update_feedback_status
    @feedback_id INT,
    @status NVARCHAR(20),
    @response NVARCHAR(MAX) = NULL
AS
BEGIN
    UPDATE feedback 
    SET status = @status,
        response = @response,
        responded_at = CASE WHEN @response IS NOT NULL THEN GETDATE() ELSE responded_at END
    WHERE id = @feedback_id;
END;

-- =============================================
-- TRIGGERS
-- =============================================

-- Trigger to update responded_at when response is added
CREATE TRIGGER tr_feedback_response_update
ON feedback
AFTER UPDATE
AS
BEGIN
    IF UPDATE(response) AND EXISTS(SELECT 1 FROM inserted WHERE response IS NOT NULL AND response != '')
    BEGIN
        UPDATE f
        SET responded_at = GETDATE()
        FROM feedback f
        INNER JOIN inserted i ON f.id = i.id
        WHERE f.response IS NOT NULL AND f.response != '';
    END
END;

-- =============================================
-- SAMPLE QUERIES FOR TESTING
-- =============================================

-- Test insert
INSERT INTO feedback (user_email, user_name, feedback_text, rating, feedback_type) 
VALUES ('test@example.com', 'Test User', 'This is a test feedback', 5, 'General Feedback');

-- Test select
SELECT * FROM feedback WHERE user_email = 'test@example.com';

-- Test update
UPDATE feedback SET status = 'RESPONDED', response = 'Thank you for your feedback!' 
WHERE user_email = 'test@example.com';

-- Test delete
DELETE FROM feedback WHERE user_email = 'test@example.com';

PRINT 'Feedback table setup completed successfully!';
