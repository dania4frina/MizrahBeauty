-- =============================================================================
-- MizrahBeauty consolidated database setup
-- Target platform : Microsoft SQL Server (tested with SQL Server 2019+)
-- Purpose         : Create all tables actually used by the Android application
--                   and seed essential reference data.
-- Idempotency     : Safe to run multiple times; guarded by IF NOT EXISTS checks.
-- =============================================================================

/* ---------------------------------------------------------------------------
   Database
--------------------------------------------------------------------------- */
IF NOT EXISTS (SELECT 1 FROM sys.databases WHERE name = N'dania')
BEGIN
    PRINT 'Creating database dania';
    EXEC ('CREATE DATABASE dania');
END
GO

USE dania;
GO

/* ---------------------------------------------------------------------------
   users
   - Core identity table for customers, staff, and admins
--------------------------------------------------------------------------- */
IF OBJECT_ID(N'dbo.users', N'U') IS NULL
BEGIN
    PRINT 'Creating table dbo.users';
    CREATE TABLE dbo.users (
        id            INT             IDENTITY(1,1) PRIMARY KEY,
        email         VARCHAR(255)    NOT NULL UNIQUE,
        password      VARCHAR(255)    NOT NULL,
        name          VARCHAR(255)    NULL,
        first_name    VARCHAR(100)    NULL,
        last_name     VARCHAR(100)    NULL,
        phone         VARCHAR(50)     NULL,
        role          VARCHAR(50)     NOT NULL CONSTRAINT DF_users_role DEFAULT 'user',
        is_admin      BIT             NOT NULL CONSTRAINT DF_users_is_admin DEFAULT 0,
        is_active     BIT             NOT NULL CONSTRAINT DF_users_is_active DEFAULT 1,
        created_at    DATETIME        NOT NULL CONSTRAINT DF_users_created_at DEFAULT GETDATE(),
        updated_at    DATETIME        NOT NULL CONSTRAINT DF_users_updated_at DEFAULT GETDATE()
    );
END
GO

IF COL_LENGTH('dbo.users', 'name') IS NULL
BEGIN
    ALTER TABLE dbo.users ADD name VARCHAR(255) NULL;
END;
IF COL_LENGTH('dbo.users', 'first_name') IS NULL
BEGIN
    ALTER TABLE dbo.users ADD first_name VARCHAR(100) NULL;
END;
IF COL_LENGTH('dbo.users', 'last_name') IS NULL
BEGIN
    ALTER TABLE dbo.users ADD last_name VARCHAR(100) NULL;
END;
IF COL_LENGTH('dbo.users', 'phone') IS NULL
BEGIN
    ALTER TABLE dbo.users ADD phone VARCHAR(50) NULL;
END;
IF COL_LENGTH('dbo.users', 'role') IS NULL
BEGIN
    ALTER TABLE dbo.users ADD role VARCHAR(50) NOT NULL CONSTRAINT DF_users_role DEFAULT 'user';
END;
IF COL_LENGTH('dbo.users', 'is_admin') IS NULL
BEGIN
    ALTER TABLE dbo.users ADD is_admin BIT NOT NULL CONSTRAINT DF_users_is_admin DEFAULT 0;
END;
IF COL_LENGTH('dbo.users', 'is_active') IS NULL
BEGIN
    ALTER TABLE dbo.users ADD is_active BIT NOT NULL CONSTRAINT DF_users_is_active DEFAULT 1;
END;
IF COL_LENGTH('dbo.users', 'created_at') IS NULL
BEGIN
    ALTER TABLE dbo.users ADD created_at DATETIME NOT NULL CONSTRAINT DF_users_created_at DEFAULT GETDATE();
END;
IF COL_LENGTH('dbo.users', 'updated_at') IS NULL
BEGIN
    ALTER TABLE dbo.users ADD updated_at DATETIME NOT NULL CONSTRAINT DF_users_updated_at DEFAULT GETDATE();
END;
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'UX_users_email' AND object_id = OBJECT_ID(N'dbo.users'))
BEGIN
    CREATE UNIQUE INDEX UX_users_email ON dbo.users(email);
END
GO

/* ---------------------------------------------------------------------------
   staff
   - Stores staff-specific metadata
--------------------------------------------------------------------------- */
IF OBJECT_ID(N'dbo.staff', N'U') IS NULL
BEGIN
    PRINT 'Creating table dbo.staff';
    CREATE TABLE dbo.staff (
        id              INT           IDENTITY(1,1) PRIMARY KEY,
        user_email      VARCHAR(255)  NOT NULL UNIQUE,
        name            VARCHAR(255)  NULL,
        phone           VARCHAR(50)   NULL,
        position        VARCHAR(100)  NULL,
        service_details VARCHAR(1000) NULL,
        is_available    BIT           NOT NULL CONSTRAINT DF_staff_is_available DEFAULT 1,
        is_active       BIT           NOT NULL CONSTRAINT DF_staff_is_active DEFAULT 1,
        created_at      DATETIME      NOT NULL CONSTRAINT DF_staff_created_at DEFAULT GETDATE(),
        updated_at      DATETIME      NOT NULL CONSTRAINT DF_staff_updated_at DEFAULT GETDATE(),
        CONSTRAINT FK_staff_users FOREIGN KEY (user_email) REFERENCES dbo.users(email)
    );
END
GO

IF COL_LENGTH('dbo.staff', 'service_details') IS NULL
BEGIN
    ALTER TABLE dbo.staff ADD service_details VARCHAR(1000) NULL;
END;
IF COL_LENGTH('dbo.staff', 'is_available') IS NULL
BEGIN
    ALTER TABLE dbo.staff ADD is_available BIT NOT NULL CONSTRAINT DF_staff_is_available DEFAULT 1;
END;
IF COL_LENGTH('dbo.staff', 'is_active') IS NULL
BEGIN
    ALTER TABLE dbo.staff ADD is_active BIT NOT NULL CONSTRAINT DF_staff_is_active DEFAULT 1;
END;
IF COL_LENGTH('dbo.staff', 'updated_at') IS NULL
BEGIN
    ALTER TABLE dbo.staff ADD updated_at DATETIME NOT NULL CONSTRAINT DF_staff_updated_at DEFAULT GETDATE();
END;
GO

/* ---------------------------------------------------------------------------
   services
   - Service catalogue displayed across user/staff/admin flows
--------------------------------------------------------------------------- */
IF OBJECT_ID(N'dbo.services', N'U') IS NULL
BEGIN
    PRINT 'Creating table dbo.services';
    CREATE TABLE dbo.services (
        id               INT           IDENTITY(1,1) PRIMARY KEY,
        category         VARCHAR(100)  NULL,
        service_name     VARCHAR(255)  NOT NULL,
        price            DECIMAL(10,2) NOT NULL,
        duration_minutes INT           NULL,
        details          VARCHAR(1000) NULL,
        is_active        BIT           NOT NULL CONSTRAINT DF_services_is_active DEFAULT 1,
        created_by       VARCHAR(255)  NULL,
        created_by_email VARCHAR(255)  NULL,
        created_at       DATETIME      NOT NULL CONSTRAINT DF_services_created_at DEFAULT GETDATE(),
        updated_at       DATETIME      NOT NULL CONSTRAINT DF_services_updated_at DEFAULT GETDATE()
    );
END
GO

IF COL_LENGTH('dbo.services', 'is_active') IS NULL
BEGIN
    ALTER TABLE dbo.services ADD is_active BIT NOT NULL CONSTRAINT DF_services_is_active DEFAULT 1;
END;
IF COL_LENGTH('dbo.services', 'created_by') IS NULL
BEGIN
    ALTER TABLE dbo.services ADD created_by VARCHAR(255) NULL;
END;
IF COL_LENGTH('dbo.services', 'created_by_email') IS NULL
BEGIN
    ALTER TABLE dbo.services ADD created_by_email VARCHAR(255) NULL;
END;
IF COL_LENGTH('dbo.services', 'created_at') IS NULL
BEGIN
    ALTER TABLE dbo.services ADD created_at DATETIME NOT NULL CONSTRAINT DF_services_created_at DEFAULT GETDATE();
END;
IF COL_LENGTH('dbo.services', 'updated_at') IS NULL
BEGIN
    ALTER TABLE dbo.services ADD updated_at DATETIME NOT NULL CONSTRAINT DF_services_updated_at DEFAULT GETDATE();
END;
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_services_category' AND object_id = OBJECT_ID(N'dbo.services'))
BEGIN
    CREATE INDEX IX_services_category ON dbo.services(category);
END
GO

/* ---------------------------------------------------------------------------
   bookings
   - Stores customer appointments and optional staff assignment
--------------------------------------------------------------------------- */
IF OBJECT_ID(N'dbo.bookings', N'U') IS NULL
BEGIN
    PRINT 'Creating table dbo.bookings';
    CREATE TABLE dbo.bookings (
        id               INT           IDENTITY(1,1) PRIMARY KEY,
        user_email       VARCHAR(255)  NOT NULL,
        service_id       INT           NOT NULL,
        appointment_time DATETIME      NOT NULL,
        notes            VARCHAR(1000) NULL,
        status           VARCHAR(50)   NOT NULL CONSTRAINT DF_bookings_status DEFAULT 'ACTIVE',
        staff_email      VARCHAR(255)  NULL,
        staff_name       VARCHAR(255)  NULL,
        created_at       DATETIME      NOT NULL CONSTRAINT DF_bookings_created_at DEFAULT GETDATE(),
        updated_at       DATETIME      NOT NULL CONSTRAINT DF_bookings_updated_at DEFAULT GETDATE(),
        CONSTRAINT FK_bookings_users    FOREIGN KEY (user_email)  REFERENCES dbo.users(email),
        CONSTRAINT FK_bookings_services FOREIGN KEY (service_id)  REFERENCES dbo.services(id)
    );
END
GO

IF COL_LENGTH('dbo.bookings', 'staff_email') IS NULL
BEGIN
    ALTER TABLE dbo.bookings ADD staff_email VARCHAR(255) NULL;
END;
IF COL_LENGTH('dbo.bookings', 'staff_name') IS NULL
BEGIN
    ALTER TABLE dbo.bookings ADD staff_name VARCHAR(255) NULL;
END;
IF COL_LENGTH('dbo.bookings', 'created_at') IS NULL
BEGIN
    ALTER TABLE dbo.bookings ADD created_at DATETIME NOT NULL CONSTRAINT DF_bookings_created_at DEFAULT GETDATE();
END;
IF COL_LENGTH('dbo.bookings', 'updated_at') IS NULL
BEGIN
    ALTER TABLE dbo.bookings ADD updated_at DATETIME NOT NULL CONSTRAINT DF_bookings_updated_at DEFAULT GETDATE();
END;
GO

IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_bookings_staff')
    AND COL_LENGTH('dbo.bookings', 'staff_email') IS NOT NULL
BEGIN
    ALTER TABLE dbo.bookings
    ADD CONSTRAINT FK_bookings_staff FOREIGN KEY (staff_email) REFERENCES dbo.staff(user_email);
END;
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_bookings_user_email' AND object_id = OBJECT_ID(N'dbo.bookings'))
BEGIN
    CREATE INDEX IX_bookings_user_email ON dbo.bookings(user_email);
END;
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_bookings_service_id' AND object_id = OBJECT_ID(N'dbo.bookings'))
BEGIN
    CREATE INDEX IX_bookings_service_id ON dbo.bookings(service_id);
END;
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_bookings_appointment_time' AND object_id = OBJECT_ID(N'dbo.bookings'))
BEGIN
    CREATE INDEX IX_bookings_appointment_time ON dbo.bookings(appointment_time);
END;
GO

/* ---------------------------------------------------------------------------
   reviews
   - User reviews tied to services
--------------------------------------------------------------------------- */
IF OBJECT_ID(N'dbo.reviews', N'U') IS NULL
BEGIN
    PRINT 'Creating table dbo.reviews';
    CREATE TABLE dbo.reviews (
        id         INT           IDENTITY(1,1) PRIMARY KEY,
        user_email VARCHAR(255)  NOT NULL,
        service_id INT           NOT NULL,
        rating     INT           NOT NULL CHECK (rating BETWEEN 1 AND 5),
        comment    VARCHAR(2000) NULL,
        created_at DATETIME      NOT NULL CONSTRAINT DF_reviews_created_at DEFAULT GETDATE(),
        CONSTRAINT FK_reviews_users    FOREIGN KEY (user_email) REFERENCES dbo.users(email),
        CONSTRAINT FK_reviews_services FOREIGN KEY (service_id) REFERENCES dbo.services(id)
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_reviews_user_email' AND object_id = OBJECT_ID(N'dbo.reviews'))
BEGIN
    CREATE INDEX IX_reviews_user_email ON dbo.reviews(user_email);
END;
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_reviews_service_id' AND object_id = OBJECT_ID(N'dbo.reviews'))
BEGIN
    CREATE INDEX IX_reviews_service_id ON dbo.reviews(service_id);
END;
GO

/* ---------------------------------------------------------------------------
   feedback
   - Stores in-app feedback with workflow state and optional responses
--------------------------------------------------------------------------- */
IF OBJECT_ID(N'dbo.feedback', N'U') IS NULL
BEGIN
    PRINT 'Creating table dbo.feedback';
    CREATE TABLE dbo.feedback (
        id            INT            IDENTITY(1,1) PRIMARY KEY,
        user_email    VARCHAR(255)   NOT NULL,
        user_name     VARCHAR(255)   NOT NULL,
        feedback_text NVARCHAR(MAX)  NOT NULL,
        rating        INT            NOT NULL CHECK (rating BETWEEN 1 AND 5),
        feedback_type VARCHAR(50)    NOT NULL,
        status        VARCHAR(20)    NOT NULL CONSTRAINT DF_feedback_status DEFAULT 'PENDING',
        created_at    DATETIME2      NOT NULL CONSTRAINT DF_feedback_created_at DEFAULT GETDATE(),
        response      NVARCHAR(MAX)  NULL,
        responded_at  DATETIME2      NULL,
        CONSTRAINT FK_feedback_users FOREIGN KEY (user_email) REFERENCES dbo.users(email)
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_feedback_user_email' AND object_id = OBJECT_ID(N'dbo.feedback'))
BEGIN
    CREATE INDEX IX_feedback_user_email ON dbo.feedback(user_email);
END;
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_feedback_status' AND object_id = OBJECT_ID(N'dbo.feedback'))
BEGIN
    CREATE INDEX IX_feedback_status ON dbo.feedback(status);
END;
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_feedback_created_at' AND object_id = OBJECT_ID(N'dbo.feedback'))
BEGIN
    CREATE INDEX IX_feedback_created_at ON dbo.feedback(created_at);
END;
GO

/* ---------------------------------------------------------------------------
   password_reset_codes
   - Temporary store for password recovery codes
--------------------------------------------------------------------------- */
IF OBJECT_ID(N'dbo.password_reset_codes', N'U') IS NULL
BEGIN
    PRINT 'Creating table dbo.password_reset_codes';
    CREATE TABLE dbo.password_reset_codes (
        id         INT           IDENTITY(1,1) PRIMARY KEY,
        email      VARCHAR(255)  NOT NULL,
        code       VARCHAR(10)   NOT NULL,
        created_at DATETIME      NOT NULL CONSTRAINT DF_password_reset_created_at DEFAULT GETDATE(),
        expires_at DATETIME      NOT NULL,
        CONSTRAINT FK_password_reset_users FOREIGN KEY (email) REFERENCES dbo.users(email)
    );
END
GO

/* ---------------------------------------------------------------------------
   Seed data (safe re-run using IF NOT EXISTS)
--------------------------------------------------------------------------- */
-- Admin account
IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE email = 'admin@mizrahbeauty.com')
BEGIN
    INSERT INTO dbo.users (email, password, name, first_name, last_name, role, is_admin)
    VALUES ('admin@mizrahbeauty.com', 'admin123', 'Admin User', 'Admin', 'User', 'admin', 1);
END
ELSE
BEGIN
    UPDATE dbo.users
    SET role = 'admin',
        is_admin = 1,
        name = COALESCE(NULLIF(name,''), 'Admin User'),
        updated_at = GETDATE()
    WHERE email = 'admin@mizrahbeauty.com';
END;

-- Default customer
IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE email = 'user@example.com')
BEGIN
    INSERT INTO dbo.users (email, password, name, first_name, last_name, role)
    VALUES ('user@example.com', 'password123', 'John Doe', 'John', 'Doe', 'user');
END;

-- Default staff member (also ensures staff table has at least one entry)
IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE email = 'staff@mizrahbeauty.com')
BEGIN
    INSERT INTO dbo.users (email, password, name, first_name, last_name, role)
    VALUES ('staff@mizrahbeauty.com', 'staff123', 'Sara Lee', 'Sara', 'Lee', 'staff');
END;

IF NOT EXISTS (SELECT 1 FROM dbo.staff WHERE user_email = 'staff@mizrahbeauty.com')
BEGIN
    INSERT INTO dbo.staff (user_email, name, phone, position, is_available, is_active)
    VALUES ('staff@mizrahbeauty.com', 'Sara Lee', '+60123456789', 'Senior Stylist', 1, 1);
END;

/* ---------------------------------------------------------------------------
   Seed core service catalogue (idempotent inserts per service)
--------------------------------------------------------------------------- */
DECLARE @services TABLE (
    category VARCHAR(100),
    service_name VARCHAR(255),
    price DECIMAL(10,2),
    duration_minutes INT,
    details VARCHAR(1000)
);

INSERT INTO @services (category, service_name, price, duration_minutes, details)
VALUES
('Basic Spa',    'Full Body Massage', 105.00,  60, 'Relaxing full body massage with essential oils'),
('Basic Spa',    'Foot Massage',      105.00,  45, 'Therapeutic foot massage with reflexology'),
('Basic Spa',    'Body Scrub + Massage', 135.00, 90, 'Exfoliating body scrub followed by relaxing massage'),
('Basic Spa',    'Herbal Sauna',       45.00,  30, 'Detoxifying herbal sauna session'),
('Face Relaxing','Normal Facial',      69.00,  60, 'Basic facial treatment for healthy skin maintenance'),
('Face Relaxing','Sulam Bedak BB Glow',179.00, 90, 'BB Glow treatment for natural-looking foundation'),
('Face Relaxing','Deep Treatment Facial',109.00, 90, 'Deep cleansing and treatment facial'),
('Face Relaxing','Collagen Face Up',  109.00,  75, 'Collagen treatment for skin firming'),
('Hair Salon',   'Basic Hair Cut',     20.00,  30, 'Classic haircut service'),
('Hair Salon',   'Hair Wash',          42.50,  30, 'Professional wash and conditioning'),
('Hair Salon',   'Rebonding',         250.00, 180, 'Straightening treatment'),
('Hair Salon',   'Kids Hair Cut',      15.00,  20, 'Haircut service for children'),
('Bridal Package','Complete Bridal Package', 300.00, 300, 'Comprehensive bridal beauty experience');

INSERT INTO dbo.services (category, service_name, price, duration_minutes, details, is_active, created_by_email, created_by)
SELECT s.category, s.service_name, s.price, s.duration_minutes, s.details, 1, 'admin@mizrahbeauty.com', 'admin@mizrahbeauty.com'
FROM @services s
WHERE NOT EXISTS (
    SELECT 1 FROM dbo.services
    WHERE service_name = s.service_name
      AND category     = s.category
);
GO

PRINT 'dania schema ensured successfully.';
GO

