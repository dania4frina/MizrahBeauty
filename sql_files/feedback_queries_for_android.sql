-- =============================================
-- FEEDBACK QUERIES FOR ANDROID APP
-- =============================================

-- 1. CREATE TABLE (untuk ConnectionClass.createFeedbackTable())
CREATE TABLE IF NOT EXISTS feedback (
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

-- 2. INSERT FEEDBACK (untuk ConnectionClass.submitFeedback())
INSERT INTO feedback (user_email, user_name, feedback_text, rating, feedback_type) 
VALUES (?, ?, ?, ?, ?);

-- 3. GET USER FEEDBACK (untuk ConnectionClass.getUserFeedback())
SELECT * FROM feedback 
WHERE user_email = ? 
ORDER BY created_at DESC;

-- 4. GET ALL FEEDBACK (untuk ConnectionClass.getAllFeedback())
SELECT * FROM feedback 
ORDER BY created_at DESC;

-- 5. UPDATE FEEDBACK STATUS (untuk ConnectionClass.updateFeedbackStatus())
UPDATE feedback 
SET status = ?, 
    response = ?, 
    responded_at = GETDATE() 
WHERE id = ?;

-- 6. GET FEEDBACK BY ID
SELECT * FROM feedback WHERE id = ?;

-- 7. DELETE FEEDBACK (optional)
DELETE FROM feedback WHERE id = ?;

-- 8. GET FEEDBACK STATISTICS
SELECT 
    COUNT(*) as total_feedback,
    AVG(CAST(rating AS FLOAT)) as average_rating,
    COUNT(CASE WHEN status = 'PENDING' THEN 1 END) as pending_count,
    COUNT(CASE WHEN status = 'REVIEWED' THEN 1 END) as reviewed_count,
    COUNT(CASE WHEN status = 'RESPONDED' THEN 1 END) as responded_count
FROM feedback;

-- 9. GET FEEDBACK BY STATUS
SELECT * FROM feedback WHERE status = ? ORDER BY created_at DESC;

-- 10. GET FEEDBACK BY RATING
SELECT * FROM feedback WHERE rating = ? ORDER BY created_at DESC;

-- 11. GET FEEDBACK BY TYPE
SELECT * FROM feedback WHERE feedback_type = ? ORDER BY created_at DESC;

-- 12. SEARCH FEEDBACK
SELECT * FROM feedback 
WHERE feedback_text LIKE ? 
ORDER BY created_at DESC;

-- 13. GET RECENT FEEDBACK (last 7 days)
SELECT * FROM feedback 
WHERE created_at >= DATEADD(day, -7, GETDATE()) 
ORDER BY created_at DESC;

-- 14. GET FEEDBACK WITH USER DETAILS
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
LEFT JOIN users u ON f.user_email = u.email
ORDER BY f.created_at DESC;

-- 15. GET FEEDBACK COUNT BY USER
SELECT 
    user_email,
    COUNT(*) as feedback_count,
    AVG(CAST(rating AS FLOAT)) as average_rating
FROM feedback 
GROUP BY user_email
ORDER BY feedback_count DESC;

-- 16. GET FEEDBACK BY DATE RANGE
SELECT * FROM feedback 
WHERE created_at BETWEEN ? AND ? 
ORDER BY created_at DESC;

-- 17. GET TOP FEEDBACK TYPES
SELECT 
    feedback_type,
    COUNT(*) as count,
    AVG(CAST(rating AS FLOAT)) as average_rating
FROM feedback 
GROUP BY feedback_type
ORDER BY count DESC;

-- 18. GET FEEDBACK WITHOUT RESPONSES
SELECT * FROM feedback 
WHERE response IS NULL OR response = '' 
ORDER BY created_at DESC;

-- 19. GET FEEDBACK BY MONTH
SELECT 
    YEAR(created_at) as year,
    MONTH(created_at) as month,
    COUNT(*) as feedback_count,
    AVG(CAST(rating AS FLOAT)) as average_rating
FROM feedback 
GROUP BY YEAR(created_at), MONTH(created_at)
ORDER BY year DESC, month DESC;

-- 20. GET FEEDBACK RATING DISTRIBUTION
SELECT 
    rating,
    COUNT(*) as count,
    (COUNT(*) * 100.0 / (SELECT COUNT(*) FROM feedback)) as percentage
FROM feedback 
GROUP BY rating
ORDER BY rating DESC;
