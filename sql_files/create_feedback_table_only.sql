-- =============================================
-- CREATE FEEDBACK TABLE ONLY
-- =============================================

-- Drop table if exists
IF EXISTS (SELECT * FROM sysobjects WHERE name='feedback' AND xtype='U')
    DROP TABLE feedback;

-- Create feedback table
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
    responded_at DATETIME2,
    FOREIGN KEY (user_email) REFERENCES users(email)
);

-- Create indexes
CREATE INDEX IX_feedback_user_email ON feedback(user_email);
CREATE INDEX IX_feedback_status ON feedback(status);
CREATE INDEX IX_feedback_created_at ON feedback(created_at);

PRINT 'Feedback table created successfully!';
