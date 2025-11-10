package com.example.mizrahbeauty;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import com.example.mizrahbeauty.models.Service;
import com.example.mizrahbeauty.models.Feedback;
import com.example.mizrahbeauty.models.Staff;
import com.example.mizrahbeauty.models.Booking;

public class ConnectionClass {
    // --- Student file connection (original emulator setup) ---
    // private static final String DB_URL = "jdbc:jtds:sqlserver://10.0.2.2:1433/dania";
    // private static final String DB_USER = "sa";
    // private static final String DB_PASSWORD = "12345";

    // --- Docker DB connection (local SQL Server container) ---
    // new fix as date on 09 nov 2025 -7.15PM
    private static final String DB_URL = "jdbc:jtds:sqlserver://10.0.2.2:1433/dania";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "Fbi22031978&";
    
    public static Connection getConnection() throws SQLException, ClassNotFoundException {
        Class.forName("net.sourceforge.jtds.jdbc.Driver");
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
    
    // Updated to return user role information
    public static ResultSet validateUser(String email, String password) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = getConnection();
            ensureNameColumn(conn);
            // Include role field in the query
            String query = "SELECT id, email, role FROM users WHERE email = ? AND password = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, email);
            stmt.setString(2, password);
            return stmt.executeQuery();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    // Method to get user role by email
    public static String getUserRole(String email) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            String query = "SELECT role FROM users WHERE email = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, email);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("role");
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    public static boolean registerUser(String fullName, String email, String password) {
        Connection conn = null;
        PreparedStatement checkStmt = null;
        PreparedStatement insertStmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            
            // Check if email already exists
            String checkQuery = "SELECT COUNT(*) FROM users WHERE email = ?";
            checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setString(1, email);
            rs = checkStmt.executeQuery();
            
            if (rs.next() && rs.getInt(1) > 0) {
                return false; // Email already exists
            }
            
            String cleanName = fullName != null ? fullName.trim() : "";
            ensureNameColumn(conn);
            
            // Insert new user with email, password, and full name information
            ensurePhoneColumn(conn);

            String insertQuery = "INSERT INTO users (email, password, name, phone) VALUES (?, ?, ?, ?)";
            insertStmt = conn.prepareStatement(insertQuery);
            insertStmt.setString(1, email);
            insertStmt.setString(2, password);
            insertStmt.setString(3, cleanName);
            insertStmt.setString(4, "");
            
            int rows = insertStmt.executeUpdate();
            if (rows > 0) {
                // Ensure role column exists; if not, create it with a default
                ensureRoleColumn(conn);
                // Try set role='user' if role column exists
                try (PreparedStatement roleStmt = conn.prepareStatement("UPDATE users SET role = 'user' WHERE email = ?")) {
                    roleStmt.setString(1, email);
                    roleStmt.executeUpdate();
                } catch (Exception ignored) {
                    // If role column doesn't exist, ignore silently
                }
                return true;
            }
            return false;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            closeResources(rs, checkStmt, conn);
            try { if (insertStmt != null) insertStmt.close(); } catch (Exception ignored) {}
        }
    }

    private static void ensureRoleColumn(Connection conn) {
        PreparedStatement check = null;
        ResultSet rs = null;
        try {
            String checkSql = "SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'users' AND COLUMN_NAME = 'role'";
            check = conn.prepareStatement(checkSql);
            rs = check.executeQuery();
            boolean exists = rs.next();
            if (!exists) {
                try (PreparedStatement alter = conn.prepareStatement("ALTER TABLE users ADD role VARCHAR(50) CONSTRAINT DF_users_role DEFAULT 'user'")) {
                    alter.executeUpdate();
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignored) {}
            try { if (check != null) check.close(); } catch (Exception ignored) {}
        }
    }
    
    private static void ensureNameColumn(Connection conn) {
        PreparedStatement check = null;
        ResultSet rs = null;
        try {
            String checkSql = "SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'users' AND COLUMN_NAME = 'name'";
            check = conn.prepareStatement(checkSql);
            rs = check.executeQuery();
            boolean exists = rs.next();
            if (!exists) {
                try (PreparedStatement alter = conn.prepareStatement("ALTER TABLE users ADD name VARCHAR(255) NULL")) {
                    alter.executeUpdate();
                } catch (Exception ignored) {
                }
            }
            // Populate name column from legacy columns if needed
            try (PreparedStatement populateFromLegacy = conn.prepareStatement(
                    "UPDATE users SET name = LTRIM(RTRIM(ISNULL(first_name,''))) + " +
                    "CASE WHEN ISNULL(first_name,'') <> '' AND ISNULL(last_name,'') <> '' THEN ' ' ELSE '' END + " +
                    "LTRIM(RTRIM(ISNULL(last_name,''))) " +
                    "WHERE (name IS NULL OR name = '') AND (ISNULL(first_name,'') <> '' OR ISNULL(last_name,'') <> '')")) {
                populateFromLegacy.executeUpdate();
            } catch (Exception ignored) {
            }

            // Populate name column from staff table if still empty
            try (PreparedStatement populateFromStaff = conn.prepareStatement(
                    "UPDATE u SET u.name = s.name FROM users u " +
                    "INNER JOIN staff s ON s.user_email = u.email " +
                    "WHERE (u.name IS NULL OR u.name = '') AND (s.name IS NOT NULL AND s.name <> '')")) {
                populateFromStaff.executeUpdate();
            } catch (Exception ignored) {
            }
        } catch (Exception ignored) {
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignored) {}
            try { if (check != null) check.close(); } catch (Exception ignored) {}
        }
    }

    private static void ensurePhoneColumn(Connection conn) {
        PreparedStatement check = null;
        ResultSet rs = null;
        try {
            String checkSql = "SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'users' AND COLUMN_NAME = 'phone'";
            check = conn.prepareStatement(checkSql);
            rs = check.executeQuery();
            boolean exists = rs.next();
            if (!exists) {
                try (PreparedStatement alter = conn.prepareStatement("ALTER TABLE users ADD phone VARCHAR(20) NULL")) {
                    alter.executeUpdate();
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignored) {}
            try { if (check != null) check.close(); } catch (Exception ignored) {}
        }
    }

    private static String deriveNameFromEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return "";
        }

        String[] parts = email.split("@", 2);
        if (parts.length == 0 || parts[0].trim().isEmpty()) {
            return "";
        }

        String localPart = parts[0]
                .replace('.', ' ')
                .replace('_', ' ')
                .replace('-', ' ')
                .trim();

        if (localPart.isEmpty()) {
            return "";
        }

        String[] tokens = localPart.split("\\s+");
        StringBuilder builder = new StringBuilder();
        for (String token : tokens) {
            if (token.isEmpty()) {
                continue;
            }
            String lower = token.toLowerCase();
            String formatted = Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(formatted);
        }

        return builder.toString();
    }
    
    // New method to create admin user
    public static boolean createAdminUser(String email, String password, String name) {
        Connection conn = null;
        PreparedStatement checkStmt = null;
        PreparedStatement insertStmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            
            // Check if email already exists
            String checkQuery = "SELECT COUNT(*) FROM users WHERE email = ?";
            checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setString(1, email);
            rs = checkStmt.executeQuery();
            
            if (rs.next() && rs.getInt(1) > 0) {
                return false; // Email already exists
            }
            
            // Insert new admin user with name, email, password, and is_admin flag
            String insertQuery = "INSERT INTO users (name, email, password, is_admin) VALUES (?, ?, ?, 1)";
            insertStmt = conn.prepareStatement(insertQuery);
            insertStmt.setString(1, name);
            insertStmt.setString(2, email);
            insertStmt.setString(3, password);
            
            int rows = insertStmt.executeUpdate();
            return rows > 0;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            closeResources(rs, checkStmt, conn);
            try { if (insertStmt != null) insertStmt.close(); } catch (Exception ignored) {}
        }
    }
    
    // Method to check if a user is admin
    public static boolean isAdminUser(String email) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            String query = "SELECT is_admin FROM users WHERE email = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, email);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getBoolean("is_admin");
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    // Method to promote existing user to admin
    public static boolean promoteToAdmin(String email) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = getConnection();
            String query = "UPDATE users SET is_admin = 1 WHERE email = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, email);
            
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try { if (stmt != null) stmt.close(); } catch (Exception ignored) {}
            try { if (conn != null) conn.close(); } catch (Exception ignored) {}
        }
    }
    
    // Method to demote admin user to regular user
    public static boolean demoteFromAdmin(String email) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = getConnection();
            String query = "UPDATE users SET is_admin = 0 WHERE email = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, email);
            
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try { if (stmt != null) stmt.close(); } catch (Exception ignored) {}
            try { if (conn != null) conn.close(); } catch (Exception ignored) {}
        }
    }
    
    // Method to get all admin users
    public static ResultSet getAllAdminUsers() {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = getConnection();
            String query = "SELECT id, name, email FROM users WHERE is_admin = 1 ORDER BY id DESC";
            stmt = conn.prepareStatement(query);
            return stmt.executeQuery();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    // Method to get user details by email
    public static ResultSet getUserDetails(String email) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = getConnection();
            String query = "SELECT id, name, email FROM users WHERE email = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, email);
            return stmt.executeQuery();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    // Method to update user profile
    public static boolean updateUserProfile(String email, String name) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = getConnection();
            String query = "UPDATE users SET name = ? WHERE email = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, name);
            stmt.setString(2, email);
            
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try { if (stmt != null) stmt.close(); } catch (Exception ignored) {}
            try { if (conn != null) conn.close(); } catch (Exception ignored) {}
        }
    }
    
    // Method to change password
    public static boolean changePassword(String email, String oldPassword, String newPassword) {
        Connection conn = null;
        PreparedStatement checkStmt = null;
        PreparedStatement updateStmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            // If oldPassword is empty, allow direct update (admin flow)
            if (oldPassword == null || oldPassword.isEmpty()) {
                String updateQuery = "UPDATE users SET password = ? WHERE email = ?";
                updateStmt = conn.prepareStatement(updateQuery);
                updateStmt.setString(1, newPassword);
                updateStmt.setString(2, email);
                int rows = updateStmt.executeUpdate();
                return rows > 0;
            }
            
            // Otherwise verify old password before updating
            String checkQuery = "SELECT COUNT(*) FROM users WHERE email = ? AND password = ?";
            checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setString(1, email);
            checkStmt.setString(2, oldPassword);
            rs = checkStmt.executeQuery();
            
            if (!rs.next() || rs.getInt(1) == 0) {
                return false; // Old password incorrect
            }
            
            String updateQuery = "UPDATE users SET password = ? WHERE email = ?";
            updateStmt = conn.prepareStatement(updateQuery);
            updateStmt.setString(1, newPassword);
            updateStmt.setString(2, email);
            int rows = updateStmt.executeUpdate();
            return rows > 0;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            closeResources(rs, checkStmt, conn);
            try { if (updateStmt != null) updateStmt.close(); } catch (Exception ignored) {}
        }
    }
    
    // Method to send password reset verification code
    public static String sendPasswordResetCode(String email) {
        Connection conn = null;
        PreparedStatement checkStmt = null;
        PreparedStatement insertStmt = null;
        PreparedStatement deleteStmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            
            // Check if email exists
            String checkQuery = "SELECT COUNT(*) as count FROM users WHERE email = ?";
            checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setString(1, email);
            rs = checkStmt.executeQuery();
            
            if (rs.next() && rs.getInt("count") == 0) {
                // Email not found
                return null;
            }
            
            // Generate 6-digit verification code
            int code = (int)(Math.random() * 900000) + 100000; // 100000 to 999999
            String verificationCode = String.valueOf(code);
            
            // Create password_reset_codes table if not exists
            try {
                // Check if table exists
                String checkTableQuery = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'password_reset_codes'";
                PreparedStatement checkTableStmt = conn.prepareStatement(checkTableQuery);
                ResultSet tableRs = checkTableStmt.executeQuery();
                boolean tableExists = tableRs.next() && tableRs.getInt(1) > 0;
                tableRs.close();
                checkTableStmt.close();
                
                if (!tableExists) {
                    String createTableQuery = "CREATE TABLE password_reset_codes (" +
                                             "id INT IDENTITY(1,1) PRIMARY KEY, " +
                                             "email VARCHAR(255) NOT NULL, " +
                                             "code VARCHAR(10) NOT NULL, " +
                                             "created_at DATETIME DEFAULT GETDATE(), " +
                                             "expires_at DATETIME NOT NULL)";
                    PreparedStatement createStmt = conn.prepareStatement(createTableQuery);
                    createStmt.executeUpdate();
                    createStmt.close();
                }
            } catch (Exception e) {
                // Table might already exist or error creating
                e.printStackTrace();
            }
            
            // Delete old codes for this email
            String deleteQuery = "DELETE FROM password_reset_codes WHERE email = ?";
            deleteStmt = conn.prepareStatement(deleteQuery);
            deleteStmt.setString(1, email);
            deleteStmt.executeUpdate();
            
            // Insert new code (expires in 15 minutes)
            String insertQuery = "INSERT INTO password_reset_codes (email, code, expires_at) VALUES (?, ?, DATEADD(MINUTE, 15, GETDATE()))";
            insertStmt = conn.prepareStatement(insertQuery);
            insertStmt.setString(1, email);
            insertStmt.setString(2, verificationCode);
            insertStmt.executeUpdate();
            
            // In a real app, you would send email here
            // For now, we'll just return the code (for testing purposes)
            // In production, remove this and implement actual email sending
            System.out.println("Password reset code for " + email + ": " + verificationCode);
            
            return verificationCode;
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignored) {}
            try { if (checkStmt != null) checkStmt.close(); } catch (Exception ignored) {}
            try { if (insertStmt != null) insertStmt.close(); } catch (Exception ignored) {}
            try { if (deleteStmt != null) deleteStmt.close(); } catch (Exception ignored) {}
            try { if (conn != null) conn.close(); } catch (Exception ignored) {}
        }
    }
    
    // Method to verify password reset code
    public static boolean verifyPasswordResetCode(String email, String code) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            
            // Check if code exists and is valid
            String query = "SELECT COUNT(*) as count FROM password_reset_codes " +
                          "WHERE email = ? AND code = ? AND expires_at > GETDATE()";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, email);
            stmt.setString(2, code);
            rs = stmt.executeQuery();
            
            if (rs.next() && rs.getInt("count") > 0) {
                // Code is valid, delete it to prevent reuse
                PreparedStatement deleteStmt = conn.prepareStatement(
                    "DELETE FROM password_reset_codes WHERE email = ? AND code = ?");
                deleteStmt.setString(1, email);
                deleteStmt.setString(2, code);
                deleteStmt.executeUpdate();
                deleteStmt.close();
                
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignored) {}
            try { if (stmt != null) stmt.close(); } catch (Exception ignored) {}
            try { if (conn != null) conn.close(); } catch (Exception ignored) {}
        }
    }
    
    // Method to get all users (for admin management)
    public static ResultSet getAllUsers() {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = getConnection();
            String query = "SELECT id, name, email FROM users ORDER BY id DESC";
            stmt = conn.prepareStatement(query);
            return stmt.executeQuery();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private static void closeResources(ResultSet rs, PreparedStatement stmt, Connection conn) {
        try { if (rs != null) rs.close(); } catch (Exception ignored) {}
        try { if (stmt != null) stmt.close(); } catch (Exception ignored) {}
        try { if (conn != null) conn.close(); } catch (Exception ignored) {}
    }

    // ===== Staff Management =====
    public static boolean registerStaff(String email, String password, String name, String phone) {
        return registerStaff(email, password, name, phone, "Staff");
    }
    
    public static boolean registerStaff(String email, String password, String name, String phone, String position) {
        Connection conn = null;
        PreparedStatement userCheckStmt = null;
        PreparedStatement userInsertStmt = null;
        PreparedStatement userUpdateRoleStmt = null;
        PreparedStatement staffCheckStmt = null;
        PreparedStatement staffInsertStmt = null;
        PreparedStatement staffUpdateStmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            
            // 1) Ensure user exists (create if missing)
            String userCheckQuery = "SELECT COUNT(*) FROM users WHERE email = ?";
            userCheckStmt = conn.prepareStatement(userCheckQuery);
            userCheckStmt.setString(1, email);
            rs = userCheckStmt.executeQuery();
            boolean userExists = false;
            if (rs.next()) {
                userExists = rs.getInt(1) > 0;
            }
            try { if (rs != null) rs.close(); } catch (Exception ignored) {}
            
            if (!userExists) {
                String insertUser = "INSERT INTO users (email, password, name) VALUES (?, ?, ?)";
                userInsertStmt = conn.prepareStatement(insertUser);
                userInsertStmt.setString(1, email);
                userInsertStmt.setString(2, password);
                userInsertStmt.setString(3, name != null ? name.trim() : "");
                userInsertStmt.executeUpdate();
            } else {
                // Optional: update password for existing user
                try (PreparedStatement updPwd = conn.prepareStatement("UPDATE users SET password = ? WHERE email = ?")) {
                    updPwd.setString(1, password);
                    updPwd.setString(2, email);
                    updPwd.executeUpdate();
                }
                try (PreparedStatement updName = conn.prepareStatement("UPDATE users SET name = ? WHERE email = ?")) {
                    updName.setString(1, name != null ? name.trim() : "");
                    updName.setString(2, email);
                    updName.executeUpdate();
                }
            }
            
            // Optional: set role to 'staff' if column exists
            try {
                userUpdateRoleStmt = conn.prepareStatement("UPDATE users SET role = 'staff' WHERE email = ?");
                userUpdateRoleStmt.setString(1, email);
                userUpdateRoleStmt.executeUpdate();
            } catch (Exception ignored) {}
            
            // 2) Upsert into staff table with provided details
            String staffCheckQuery = "SELECT COUNT(*) FROM staff WHERE user_email = ?";
            staffCheckStmt = conn.prepareStatement(staffCheckQuery);
            staffCheckStmt.setString(1, email);
            rs = staffCheckStmt.executeQuery();
            boolean staffExists = false;
            if (rs.next()) {
                staffExists = rs.getInt(1) > 0;
            }
            try { if (rs != null) rs.close(); } catch (Exception ignored) {}
            
            if (!staffExists) {
                String insertStaff = "INSERT INTO staff (user_email, name, phone, position) VALUES (?, ?, ?, ?)";
                staffInsertStmt = conn.prepareStatement(insertStaff);
                staffInsertStmt.setString(1, email);
                staffInsertStmt.setString(2, name);
                staffInsertStmt.setString(3, phone);
                staffInsertStmt.setString(4, position);
                staffInsertStmt.executeUpdate();
            } else {
                String updateStaff = "UPDATE staff SET name = ?, phone = ?, position = ?, is_active = 1 WHERE user_email = ?";
                staffUpdateStmt = conn.prepareStatement(updateStaff);
                staffUpdateStmt.setString(1, name);
                staffUpdateStmt.setString(2, phone);
                staffUpdateStmt.setString(3, position);
                staffUpdateStmt.setString(4, email);
                staffUpdateStmt.executeUpdate();
            }
            
            return true;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignored) {}
            try { if (userCheckStmt != null) userCheckStmt.close(); } catch (Exception ignored) {}
            try { if (userInsertStmt != null) userInsertStmt.close(); } catch (Exception ignored) {}
            try { if (userUpdateRoleStmt != null) userUpdateRoleStmt.close(); } catch (Exception ignored) {}
            try { if (staffCheckStmt != null) staffCheckStmt.close(); } catch (Exception ignored) {}
            try { if (staffInsertStmt != null) staffInsertStmt.close(); } catch (Exception ignored) {}
            try { if (staffUpdateStmt != null) staffUpdateStmt.close(); } catch (Exception ignored) {}
            try { if (conn != null) conn.close(); } catch (Exception ignored) {}
        }
    }

    public static ResultSet getAllStaff() {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = getConnection();
            // Read from staff table to include staff details
            String query = "SELECT id, user_email, name, phone, is_available, is_active FROM staff ORDER BY id ASC";
            stmt = conn.prepareStatement(query);
            return stmt.executeQuery();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    // Get all available staff as List for dropdown
    public static List<Staff> getAvailableStaffList() {
        List<Staff> staffList = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            String query = "SELECT id, user_email, name, phone, position, service_details, is_available, is_active " +
                          "FROM staff WHERE is_active = 1 AND is_available = 1 ORDER BY name ASC";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Staff staff = new Staff();
                staff.setId(rs.getInt("id"));
                staff.setUserEmail(rs.getString("user_email"));
                staff.setName(rs.getString("name"));
                staff.setPhone(rs.getString("phone"));
                
                // Handle nullable columns
                try {
                    staff.setPosition(rs.getString("position"));
                } catch (Exception e) {
                    staff.setPosition("");
                }
                
                try {
                    staff.setServiceDetails(rs.getString("service_details"));
                } catch (Exception e) {
                    staff.setServiceDetails("");
                }
                
                try {
                    staff.setAvailable(rs.getBoolean("is_available"));
                } catch (Exception e) {
                    staff.setAvailable(true);
                }
                
                try {
                    staff.setActive(rs.getBoolean("is_active"));
                } catch (Exception e) {
                    staff.setActive(true);
                }
                
                staffList.add(staff);
            }
            
        } catch (Exception e) {
            System.out.println("Error getting available staff list: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignored) {}
            try { if (stmt != null) stmt.close(); } catch (Exception ignored) {}
            try { if (conn != null) conn.close(); } catch (Exception ignored) {}
        }
        
        return staffList;
    }
    
    // Method to get all staff (including unavailable ones) for admin/management purposes
    public static List<Staff> getAllStaffList() {
        List<Staff> staffList = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            String query = "SELECT id, user_email, name, phone, position, service_details, is_available, is_active " +
                          "FROM staff WHERE is_active = 1 ORDER BY name ASC";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Staff staff = new Staff();
                staff.setId(rs.getInt("id"));
                staff.setUserEmail(rs.getString("user_email"));
                staff.setName(rs.getString("name"));
                staff.setPhone(rs.getString("phone"));
                
                // Handle nullable columns
                try {
                    staff.setPosition(rs.getString("position"));
                } catch (Exception e) {
                    staff.setPosition("");
                }
                
                try {
                    staff.setServiceDetails(rs.getString("service_details"));
                } catch (Exception e) {
                    staff.setServiceDetails("");
                }
                
                try {
                    staff.setAvailable(rs.getBoolean("is_available"));
                } catch (Exception e) {
                    staff.setAvailable(true);
                }
                
                try {
                    staff.setActive(rs.getBoolean("is_active"));
                } catch (Exception e) {
                    staff.setActive(true);
                }
                
                staffList.add(staff);
            }
            
        } catch (Exception e) {
            System.out.println("Error getting all staff list: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignored) {}
            try { if (stmt != null) stmt.close(); } catch (Exception ignored) {}
            try { if (conn != null) conn.close(); } catch (Exception ignored) {}
        }
        
        return staffList;
    }
    
    // Method to update staff availability status
    public static boolean updateStaffAvailability(int staffId, boolean isAvailable) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = getConnection();
            String query = "UPDATE staff SET is_available = ? WHERE id = ?";
            stmt = conn.prepareStatement(query);
            stmt.setBoolean(1, isAvailable);
            stmt.setInt(2, staffId);
            
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (Exception e) {
            System.out.println("Error updating staff availability: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try { if (stmt != null) stmt.close(); } catch (Exception ignored) {}
            try { if (conn != null) conn.close(); } catch (Exception ignored) {}
        }
    }
    
    // Method to update staff availability by email
    public static boolean updateStaffAvailabilityByEmail(String userEmail, boolean isAvailable) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = getConnection();
            String query = "UPDATE staff SET is_available = ? WHERE user_email = ?";
            stmt = conn.prepareStatement(query);
            stmt.setBoolean(1, isAvailable);
            stmt.setString(2, userEmail);
            
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (Exception e) {
            System.out.println("Error updating staff availability by email: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try { if (stmt != null) stmt.close(); } catch (Exception ignored) {}
            try { if (conn != null) conn.close(); } catch (Exception ignored) {}
        }
    }
    
    // Helper method to get staff name by email
    public static String getStaffNameByEmail(String staffEmail) {
        if (staffEmail == null || staffEmail.isEmpty()) return null;
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            String query = "SELECT name FROM staff WHERE user_email = ? AND is_active = 1";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, staffEmail);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("name");
            }
            return null;
        } catch (Exception e) {
            System.out.println("Error getting staff name by email: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignored) {}
            try { if (stmt != null) stmt.close(); } catch (Exception ignored) {}
            try { if (conn != null) conn.close(); } catch (Exception ignored) {}
        }
    }
    
    // Helper method to get staff email by name
    public static String getStaffEmailByName(String staffName) {
        if (staffName == null || staffName.isEmpty()) return null;
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            String query = "SELECT user_email FROM staff WHERE name = ? AND is_active = 1";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, staffName);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("user_email");
            }
            return null;
        } catch (Exception e) {
            System.out.println("Error getting staff email by name: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignored) {}
            try { if (stmt != null) stmt.close(); } catch (Exception ignored) {}
            try { if (conn != null) conn.close(); } catch (Exception ignored) {}
        }
    }
    
    // Method to get bookings by staff name (for staff to see their assigned bookings)
    public static List<Booking> getBookingsByStaffName(String staffName) {
        List<Booking> bookings = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            
            // Ensure staff columns exist
            ensureBookingsStaffColumn(conn);
            
            String query = "SELECT b.id, b.user_email, b.service_id, b.appointment_time, b.notes, b.status, " +
                          "b.staff_name, b.staff_email, s.service_name, s.price, s.duration_minutes, " +
                          "u.name as customer_name " +
                          "FROM bookings b " +
                          "LEFT JOIN services s ON b.service_id = s.id " +
                          "LEFT JOIN users u ON b.user_email = u.email " +
                          "WHERE b.staff_name = ? " +
                          "ORDER BY b.appointment_time DESC";
            
            stmt = conn.prepareStatement(query);
            stmt.setString(1, staffName);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Booking booking = new Booking();
                booking.setId(rs.getInt("id"));
                booking.setUserEmail(rs.getString("user_email"));
                booking.setServiceId(rs.getInt("service_id"));
                
                // Parse appointment_time and set booking date/time
                String appointmentTime = rs.getString("appointment_time");
                booking.setAppointmentTime(appointmentTime);
                if (appointmentTime != null && !appointmentTime.isEmpty()) {
                    try {
                        // Format: "yyyy-MM-dd HH:mm:ss" or "yyyy-MM-dd HH:mm"
                        String[] parts = appointmentTime.split(" ");
                        if (parts.length >= 2) {
                            booking.setBookingDate(parts[0]); // Date part
                            booking.setBookingTime(parts[1].substring(0, Math.min(5, parts[1].length()))); // Time part (HH:mm)
                        }
                    } catch (Exception e) {
                        // If parsing fails, use original
                        booking.setBookingDate(appointmentTime);
                        booking.setBookingTime("");
                    }
                }
                
                booking.setNotes(rs.getString("notes"));
                booking.setStatus(rs.getString("status"));
                booking.setStaffName(rs.getString("staff_name"));
                booking.setStaffEmail(rs.getString("staff_email"));
                
                // Set customer info
                booking.setCustomerEmail(rs.getString("user_email"));
                try {
                    booking.setCustomerName(rs.getString("customer_name"));
                } catch (Exception e) {
                    booking.setCustomerName("");
                }
                
                // Set service details if available
                if (rs.getString("service_name") != null) {
                    booking.setServiceName(rs.getString("service_name"));
                    booking.setPrice(rs.getDouble("price"));
                    booking.setDurationMinutes(rs.getInt("duration_minutes"));
                }
                
                bookings.add(booking);
            }
            
        } catch (Exception e) {
            System.out.println("Error getting bookings by staff name: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignored) {}
            try { if (stmt != null) stmt.close(); } catch (Exception ignored) {}
            try { if (conn != null) conn.close(); } catch (Exception ignored) {}
        }
        
        return bookings;
    }
    
    // Method to get bookings by staff email (for backward compatibility)
    public static List<Booking> getBookingsByStaffEmail(String staffEmail) {
        List<Booking> bookings = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            
            // Ensure staff columns exist
            ensureBookingsStaffColumn(conn);
            
            String query = "SELECT b.id, b.user_email, b.service_id, b.appointment_time, b.notes, b.status, " +
                          "b.staff_name, b.staff_email, s.service_name, s.price, s.duration_minutes, " +
                          "u.name as customer_name " +
                          "FROM bookings b " +
                          "LEFT JOIN services s ON b.service_id = s.id " +
                          "LEFT JOIN users u ON b.user_email = u.email " +
                          "WHERE b.staff_email = ? " +
                          "ORDER BY b.appointment_time DESC";
            
            stmt = conn.prepareStatement(query);
            stmt.setString(1, staffEmail);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Booking booking = new Booking();
                booking.setId(rs.getInt("id"));
                booking.setUserEmail(rs.getString("user_email"));
                booking.setServiceId(rs.getInt("service_id"));
                
                // Parse appointment_time and set booking date/time
                String appointmentTime = rs.getString("appointment_time");
                booking.setAppointmentTime(appointmentTime);
                if (appointmentTime != null && !appointmentTime.isEmpty()) {
                    try {
                        // Format: "yyyy-MM-dd HH:mm:ss" or "yyyy-MM-dd HH:mm"
                        String[] parts = appointmentTime.split(" ");
                        if (parts.length >= 2) {
                            booking.setBookingDate(parts[0]); // Date part
                            booking.setBookingTime(parts[1].substring(0, Math.min(5, parts[1].length()))); // Time part (HH:mm)
                        }
                    } catch (Exception e) {
                        // If parsing fails, use original
                        booking.setBookingDate(appointmentTime);
                        booking.setBookingTime("");
                    }
                }
                
                booking.setNotes(rs.getString("notes"));
                booking.setStatus(rs.getString("status"));
                booking.setStaffName(rs.getString("staff_name"));
                booking.setStaffEmail(rs.getString("staff_email"));
                
                // Set customer info
                booking.setCustomerEmail(rs.getString("user_email"));
                try {
                    booking.setCustomerName(rs.getString("customer_name"));
                } catch (Exception e) {
                    booking.setCustomerName("");
                }
                
                // Set service details if available
                if (rs.getString("service_name") != null) {
                    booking.setServiceName(rs.getString("service_name"));
                    booking.setPrice(rs.getDouble("price"));
                    booking.setDurationMinutes(rs.getInt("duration_minutes"));
                }
                
                bookings.add(booking);
            }
            
        } catch (Exception e) {
            System.out.println("Error getting bookings by staff email: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignored) {}
            try { if (stmt != null) stmt.close(); } catch (Exception ignored) {}
            try { if (conn != null) conn.close(); } catch (Exception ignored) {}
        }
        
        return bookings;
    }
    
    // Method to update existing bookings with staff names (migration helper)
    public static boolean updateExistingBookingsWithStaffNames() {
        Connection conn = null;
        PreparedStatement selectStmt = null;
        PreparedStatement updateStmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            
            // Ensure staff columns exist
            ensureBookingsStaffColumn(conn);
            
            // Get bookings that have staff_email but no staff_name
            String selectQuery = "SELECT id, staff_email FROM bookings WHERE staff_email IS NOT NULL AND (staff_name IS NULL OR staff_name = '')";
            selectStmt = conn.prepareStatement(selectQuery);
            rs = selectStmt.executeQuery();
            
            int updatedCount = 0;
            String updateQuery = "UPDATE bookings SET staff_name = ? WHERE id = ?";
            updateStmt = conn.prepareStatement(updateQuery);
            
            while (rs.next()) {
                int bookingId = rs.getInt("id");
                String staffEmail = rs.getString("staff_email");
                
                // Get staff name from email
                String staffName = getStaffNameByEmail(staffEmail);
                if (staffName != null && !staffName.isEmpty()) {
                    updateStmt.setString(1, staffName);
                    updateStmt.setInt(2, bookingId);
                    updateStmt.addBatch();
                    updatedCount++;
                }
            }
            
            if (updatedCount > 0) {
                updateStmt.executeBatch();
                System.out.println("Updated " + updatedCount + " bookings with staff names");
            }
            
            return true;
            
        } catch (Exception e) {
            System.out.println("Error updating existing bookings with staff names: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignored) {}
            try { if (selectStmt != null) selectStmt.close(); } catch (Exception ignored) {}
            try { if (updateStmt != null) updateStmt.close(); } catch (Exception ignored) {}
            try { if (conn != null) conn.close(); } catch (Exception ignored) {}
        }
    }

    // ===== Service Management =====
    public static boolean createService(String category, String serviceName, double price, int durationMinutes, String details, String createdByEmail) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = getConnection();
            // Try insert with created_by_email if column exists
            try {
                ensureServicesCreatorColumn(conn);
                String queryWithCreator = "INSERT INTO services (category, service_name, price, duration_minutes, details, created_by_email) VALUES (?, ?, ?, ?, ?, ?)";
                stmt = conn.prepareStatement(queryWithCreator);
                stmt.setString(1, category);
                stmt.setString(2, serviceName);
                stmt.setDouble(3, price);
                stmt.setInt(4, durationMinutes);
                stmt.setString(5, details);
                stmt.setString(6, createdByEmail);
                int rows = stmt.executeUpdate();
                return rows > 0;
            } catch (Exception withCreatorEx) {
                // Fallback to legacy insert without creator column
                try { if (stmt != null) stmt.close(); } catch (Exception ignored) {}
                String query = "INSERT INTO services (category, service_name, price, duration_minutes, details) VALUES (?, ?, ?, ?, ?)";
                stmt = conn.prepareStatement(query);
                stmt.setString(1, category);
                stmt.setString(2, serviceName);
                stmt.setDouble(3, price);
                stmt.setInt(4, durationMinutes);
                stmt.setString(5, details);
                int rows = stmt.executeUpdate();
                return rows > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try { if (stmt != null) stmt.close(); } catch (Exception ignored) {}
            try { if (conn != null) conn.close(); } catch (Exception ignored) {}
        }
    }

    private static void ensureServicesCreatorColumn(Connection conn) {
        PreparedStatement check = null;
        ResultSet rs = null;
        try {
            String checkSql = "SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'services' AND COLUMN_NAME = 'created_by_email'";
            check = conn.prepareStatement(checkSql);
            rs = check.executeQuery();
            boolean exists = rs.next();
            if (!exists) {
                try (PreparedStatement alter = conn.prepareStatement("ALTER TABLE services ADD created_by_email VARCHAR(255) NULL")) {
                    alter.executeUpdate();
                }
            }
        } catch (Exception ignored) {
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignored) {}
            try { if (check != null) check.close(); } catch (Exception ignored) {}
        }
    }

    // ===== Service History =====
    public static ResultSet getServiceHistory() {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = getConnection();
            String query = "SELECT b.id, u.email AS customer_email, s.service_name AS service_name, b.appointment_time, b.status " +
                    "FROM bookings b " +
                    "JOIN users u ON b.user_email = u.email " +
                    "JOIN services s ON b.service_id = s.id " +
                    "ORDER BY b.appointment_time DESC";
            stmt = conn.prepareStatement(query);
            return stmt.executeQuery();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ===== Feedback/Reviews =====
    public static ResultSet getAllReviews() {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = getConnection();
            String query = "SELECT r.id, u.email AS customer_email, s.service_name AS service_name, r.rating, r.comment, r.created_at " +
                    "FROM reviews r " +
                    "JOIN users u ON r.user_email = u.email " +
                    "JOIN services s ON r.service_id = s.id " +
                    "ORDER BY r.created_at DESC";
            stmt = conn.prepareStatement(query);
            return stmt.executeQuery();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ===== Services (Read) =====
    
    /**
     * Add Bridal Package services if they don't exist
     */
    public static void addBridalPackageServices() {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            System.out.println("=== ADDING BRIDAL PACKAGE SERVICES ===");
            
            conn = getConnection();
            if (conn == null) {
                System.out.println("ERROR: Database connection is null");
                return;
            }
            
            // Check if services already exist
            String checkQuery = "SELECT COUNT(*) FROM services WHERE category = 'Bridal Package'";
            stmt = conn.prepareStatement(checkQuery);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            int existingCount = rs.getInt(1);
            rs.close();
            stmt.close();
            
            if (existingCount > 0) {
                System.out.println("Bridal Package services already exist (" + existingCount + " services)");
                // Update existing Bridal Package price to RM300
                String updateQuery = "UPDATE services SET price = ? WHERE category = 'Bridal Package'";
                stmt = conn.prepareStatement(updateQuery);
                stmt.setDouble(1, 300.00);
                int updatedRows = stmt.executeUpdate();
                System.out.println("Updated " + updatedRows + " Bridal Package services to RM300");
                return;
            }
            
            // Add Bridal Package services
            String[] services = {
                "INSERT INTO services (category, service_name, price, duration_minutes, details, is_active, created_by) VALUES ('Bridal Package', 'Complete Bridal Package', 300.00, 300, 'Complete bridal package including: Normal Facial, Mandian Aura, Mandi Wap + Herba, Mandi Bunga + Susu, Body Scrub Massage', 1, 'admin@mizrahbeauty.com')"
            };
            
            for (String service : services) {
                stmt = conn.prepareStatement(service);
                int rowsAffected = stmt.executeUpdate();
                System.out.println("Added service: " + (rowsAffected > 0 ? "SUCCESS" : "FAILED"));
                stmt.close();
            }
            
            System.out.println("=== BRIDAL PACKAGE SERVICES ADDED SUCCESSFULLY ===");
            
        } catch (Exception e) {
            System.out.println("Error adding Bridal Package services: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { if (stmt != null) stmt.close(); } catch (Exception ignored) {}
            try { if (conn != null) conn.close(); } catch (Exception ignored) {}
        }
    }
    
    /**
     * Add Hair Salon services if they don't exist
     */
    public static void addHairSalonServices() {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            System.out.println("=== ADDING HAIR SALON SERVICES ===");
            
            conn = getConnection();
            if (conn == null) {
                System.out.println("ERROR: Database connection is null");
                return;
            }
            
            // Check if services already exist
            String checkQuery = "SELECT COUNT(*) FROM services WHERE category = 'Hair Salon'";
            stmt = conn.prepareStatement(checkQuery);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            int existingCount = rs.getInt(1);
            rs.close();
            stmt.close();
            
            if (existingCount > 0) {
                System.out.println("Hair Salon services already exist (" + existingCount + " services)");
                return;
            }
            
            // Add Hair Salon services
            String[] services = {
                "INSERT INTO services (category, service_name, price, duration_minutes, details, is_active, created_by) VALUES ('Hair Salon', 'Basic Hair Cut', 20.00, 30, 'Basic hair cutting service', 1, 'admin@mizrahbeauty.com')",
                "INSERT INTO services (category, service_name, price, duration_minutes, details, is_active, created_by) VALUES ('Hair Salon', 'Layer Hair Cut', 25.00, 45, 'Layered hair cutting for volume and style', 1, 'admin@mizrahbeauty.com')",
                "INSERT INTO services (category, service_name, price, duration_minutes, details, is_active, created_by) VALUES ('Hair Salon', 'Hair Wash', 42.50, 30, 'Professional hair washing and conditioning', 1, 'admin@mizrahbeauty.com')",
                "INSERT INTO services (category, service_name, price, duration_minutes, details, is_active, created_by) VALUES ('Hair Salon', 'Inai Asli', 85.00, 120, 'Natural henna hair coloring treatment', 1, 'admin@mizrahbeauty.com')",
                "INSERT INTO services (category, service_name, price, duration_minutes, details, is_active, created_by) VALUES ('Hair Salon', 'Hair Treatment', 85.00, 90, 'Deep conditioning and hair treatment', 1, 'admin@mizrahbeauty.com')",
                "INSERT INTO services (category, service_name, price, duration_minutes, details, is_active, created_by) VALUES ('Hair Salon', 'Rebonding', 250.00, 180, 'Hair straightening treatment', 1, 'admin@mizrahbeauty.com')",
                "INSERT INTO services (category, service_name, price, duration_minutes, details, is_active, created_by) VALUES ('Hair Salon', 'Kids Hair Cut', 15.00, 20, 'Hair cutting service for children', 1, 'admin@mizrahbeauty.com')"
            };
            
            for (String service : services) {
                stmt = conn.prepareStatement(service);
                int rowsAffected = stmt.executeUpdate();
                System.out.println("Added service: " + (rowsAffected > 0 ? "SUCCESS" : "FAILED"));
                stmt.close();
            }
            
            System.out.println("=== HAIR SALON SERVICES ADDED SUCCESSFULLY ===");
            
        } catch (Exception e) {
            System.out.println("Error adding Hair Salon services: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { if (stmt != null) stmt.close(); } catch (Exception ignored) {}
            try { if (conn != null) conn.close(); } catch (Exception ignored) {}
        }
    }
    
    /**
     * Add Face Relaxing services if they don't exist
     */
    public static void addFaceRelaxingServices() {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            System.out.println("=== ADDING FACE RELAXING SERVICES ===");
            
            conn = getConnection();
            if (conn == null) {
                System.out.println("ERROR: Database connection is null");
                return;
            }
            
            // Check if services already exist
            String checkQuery = "SELECT COUNT(*) FROM services WHERE category = 'Face Relaxing'";
            stmt = conn.prepareStatement(checkQuery);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            int existingCount = rs.getInt(1);
            rs.close();
            stmt.close();
            
            if (existingCount > 0) {
                System.out.println("Face Relaxing services already exist (" + existingCount + " services)");
                return;
            }
            
            // Add Face Relaxing services
            String[] services = {
                "INSERT INTO services (category, service_name, price, duration_minutes, details, is_active, created_by) VALUES ('Face Relaxing', 'Normal Facial', 69.00, 60, 'Basic facial treatment for healthy skin maintenance', 1, 'admin@mizrahbeauty.com')",
                "INSERT INTO services (category, service_name, price, duration_minutes, details, is_active, created_by) VALUES ('Face Relaxing', 'Sulam Bedak BB Glow', 179.00, 90, 'BB Glow treatment for natural-looking foundation', 1, 'admin@mizrahbeauty.com')",
                "INSERT INTO services (category, service_name, price, duration_minutes, details, is_active, created_by) VALUES ('Face Relaxing', 'Medium Treatment Facial', 89.00, 75, 'Medium intensity facial treatment for problem skin', 1, 'admin@mizrahbeauty.com')",
                "INSERT INTO services (category, service_name, price, duration_minutes, details, is_active, created_by) VALUES ('Face Relaxing', 'Deep Treatment Facial', 109.00, 90, 'Deep cleansing and treatment facial for severe skin issues', 1, 'admin@mizrahbeauty.com')",
                "INSERT INTO services (category, service_name, price, duration_minutes, details, is_active, created_by) VALUES ('Face Relaxing', 'Sulam Benang', 179.00, 120, 'Thread lift treatment for facial contouring', 1, 'admin@mizrahbeauty.com')",
                "INSERT INTO services (category, service_name, price, duration_minutes, details, is_active, created_by) VALUES ('Face Relaxing', 'Facial Resdung', 79.00, 60, 'Specialized facial treatment for sinus and allergy relief', 1, 'admin@mizrahbeauty.com')",
                "INSERT INTO services (category, service_name, price, duration_minutes, details, is_active, created_by) VALUES ('Face Relaxing', 'Diamond Treatment Facial', 139.00, 90, 'Diamond microdermabrasion for skin exfoliation', 1, 'admin@mizrahbeauty.com')",
                "INSERT INTO services (category, service_name, price, duration_minutes, details, is_active, created_by) VALUES ('Face Relaxing', 'Collagen Face Up', 109.00, 75, 'Collagen treatment for skin firming and anti-aging', 1, 'admin@mizrahbeauty.com')"
            };
            
            for (String service : services) {
                stmt = conn.prepareStatement(service);
                int rowsAffected = stmt.executeUpdate();
                System.out.println("Added service: " + (rowsAffected > 0 ? "SUCCESS" : "FAILED"));
                stmt.close();
            }
            
            System.out.println("=== FACE RELAXING SERVICES ADDED SUCCESSFULLY ===");
            
        } catch (Exception e) {
            System.out.println("Error adding Face Relaxing services: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { if (stmt != null) stmt.close(); } catch (Exception ignored) {}
            try { if (conn != null) conn.close(); } catch (Exception ignored) {}
        }
    }
    
    /**
     * Add Basic Spa services if they don't exist
     */
    public static void addBasicSpaServices() {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            System.out.println("=== ADDING BASIC SPA SERVICES ===");
            
            conn = getConnection();
            if (conn == null) {
                System.out.println("ERROR: Database connection is null");
                return;
            }
            
            // Check if services already exist
            String checkQuery = "SELECT COUNT(*) FROM services WHERE category = 'Basic Spa'";
            stmt = conn.prepareStatement(checkQuery);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            int existingCount = rs.getInt(1);
            rs.close();
            stmt.close();
            
            if (existingCount > 0) {
                System.out.println("Basic Spa services already exist (" + existingCount + " services)");
                return;
            }
            
            // Add Basic Spa services
            String[] services = {
                "INSERT INTO services (category, service_name, price, duration_minutes, details, is_active, created_by) VALUES ('Basic Spa', 'Full Body Massage', 105.00, 60, 'Relaxing full body massage with essential oils', 1, 'admin@mizrahbeauty.com')",
                "INSERT INTO services (category, service_name, price, duration_minutes, details, is_active, created_by) VALUES ('Basic Spa', 'Foot Massage', 105.00, 45, 'Therapeutic foot massage with reflexology', 1, 'admin@mizrahbeauty.com')",
                "INSERT INTO services (category, service_name, price, duration_minutes, details, is_active, created_by) VALUES ('Basic Spa', 'Body Scrub + Massage', 135.00, 90, 'Exfoliating body scrub followed by relaxing massage', 1, 'admin@mizrahbeauty.com')",
                "INSERT INTO services (category, service_name, price, duration_minutes, details, is_active, created_by) VALUES ('Basic Spa', 'Herbal Sauna', 45.00, 30, 'Detoxifying herbal sauna session', 1, 'admin@mizrahbeauty.com')"
            };
            
            for (String service : services) {
                stmt = conn.prepareStatement(service);
                int rowsAffected = stmt.executeUpdate();
                System.out.println("Added service: " + (rowsAffected > 0 ? "SUCCESS" : "FAILED"));
                stmt.close();
            }
            
            System.out.println("=== BASIC SPA SERVICES ADDED SUCCESSFULLY ===");
            
        } catch (Exception e) {
            System.out.println("Error adding Basic Spa services: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { if (stmt != null) stmt.close(); } catch (Exception ignored) {}
            try { if (conn != null) conn.close(); } catch (Exception ignored) {}
        }
    }
    
    /**
     * Get services by category
     */
    public static ResultSet getServicesByCategory(String category) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            System.out.println("=== GET SERVICES BY CATEGORY DEBUG START ===");
            System.out.println("Category: " + category);
            
            conn = getConnection();
            if (conn == null) {
                System.out.println("ERROR: Database connection is null");
                return null;
            }
            System.out.println("Database connection: SUCCESS");
            
            String query = "SELECT id, category, service_name, price, duration_minutes, details FROM services WHERE category = ? AND is_active = 1 ORDER BY service_name ASC";
            System.out.println("SQL Query: " + query);
            
            stmt = conn.prepareStatement(query);
            stmt.setString(1, category);
            System.out.println("Prepared statement created successfully");
            
            ResultSet rs = stmt.executeQuery();
            System.out.println("Query executed successfully, returning ResultSet");
            System.out.println("=== GET SERVICES BY CATEGORY DEBUG END ===");
            
            // Note: We don't close stmt and conn here as they need to stay open for the ResultSet
            // The caller is responsible for closing the ResultSet
            return rs;
        } catch (Exception e) {
            System.out.println("=== GET SERVICES BY CATEGORY DEBUG: EXCEPTION ===");
            System.out.println("Error type: " + e.getClass().getSimpleName());
            System.out.println("Error message: " + e.getMessage());
            e.printStackTrace();
            // Close resources on error
            try { if (stmt != null) stmt.close(); } catch (Exception ignored) {}
            try { if (conn != null) conn.close(); } catch (Exception ignored) {}
            return null;
        }
    }
    
    public static ResultSet getAllServices() {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            System.out.println("=== GET ALL SERVICES DEBUG START ===");
            System.out.println("Getting database connection...");
            
            conn = getConnection();
            if (conn == null) {
                System.out.println("ERROR: Database connection is null");
                return null;
            }
            System.out.println("Database connection: SUCCESS");
            
            String query = "SELECT id, category, service_name, price, duration_minutes, details FROM services WHERE is_active = 1 ORDER BY service_name ASC";
            System.out.println("SQL Query: " + query);
            
            stmt = conn.prepareStatement(query);
            System.out.println("Prepared statement created successfully");
            
            ResultSet rs = stmt.executeQuery();
            System.out.println("Query executed successfully, returning ResultSet");
            System.out.println("=== GET ALL SERVICES DEBUG END ===");
            
            return rs;
        } catch (Exception e) {
            System.out.println("=== GET ALL SERVICES DEBUG: EXCEPTION ===");
            System.out.println("Error type: " + e.getClass().getSimpleName());
            System.out.println("Error message: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // ===== Test Database Connection =====
    public static boolean testConnection() {
        Connection conn = null;
        try {
            System.out.println("=== TEST CONNECTION DEBUG START ===");
            conn = getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println("Database connection test: SUCCESS");
                System.out.println("=== TEST CONNECTION DEBUG: SUCCESS ===");
                return true;
            } else {
                System.out.println("Database connection test: FAILED - Connection is null or closed");
                System.out.println("=== TEST CONNECTION DEBUG: FAILED ===");
                return false;
            }
        } catch (Exception e) {
            System.out.println("=== TEST CONNECTION DEBUG: EXCEPTION ===");
            System.out.println("Error type: " + e.getClass().getSimpleName());
            System.out.println("Error message: " + e.getMessage());
            System.out.println("Stack trace:");
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                    System.out.println("Connection closed successfully");
                }
            } catch (Exception e) {
                System.out.println("Error closing connection: " + e.getMessage());
                e.printStackTrace();
            }
            System.out.println("=== TEST CONNECTION DEBUG END ===");
        }
    }

    // ===== Test Services Table - SIMPLIFIED =====
    public static boolean testServicesTable() {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            System.out.println("=== SIMPLE TEST SERVICES TABLE DEBUG START ===");
            conn = getConnection();
            if (conn == null) {
                System.out.println("ERROR: Database connection is null");
                return false;
            }
            System.out.println("Database connection: SUCCESS");
            
            String query = "SELECT COUNT(*) FROM services";
            System.out.println("Simple SQL Query: " + query);
            stmt = conn.prepareStatement(query);
            java.sql.ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println("Services table test: SUCCESS - Found " + count + " services");
                System.out.println("=== SIMPLE TEST SERVICES TABLE DEBUG: SUCCESS ===");
                return true;
            }
            System.out.println("Services table test: FAILED - No result from COUNT query");
            return false;
        } catch (Exception e) {
            System.out.println("=== SIMPLE TEST SERVICES TABLE DEBUG: EXCEPTION ===");
            System.out.println("Error type: " + e.getClass().getSimpleName());
            System.out.println("Error message: " + e.getMessage());
            System.out.println("Stack trace:");
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                    System.out.println("Statement closed successfully");
                }
                if (conn != null) {
                    conn.close();
                    System.out.println("Connection closed successfully");
                }
            } catch (Exception e) {
                System.out.println("Error closing resources: " + e.getMessage());
                e.printStackTrace();
            }
            System.out.println("=== SIMPLE TEST SERVICES TABLE DEBUG END ===");
        }
    }

    // ===== Services (Create) - Alternative Method =====
    public static boolean addServiceAlternative(String serviceName, String category, double price, int duration, String details) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            System.out.println("=== ALTERNATIVE ADD SERVICE DEBUG START ===");
            
            conn = getConnection();
            if (conn == null) {
                System.out.println("ERROR: Database connection is null");
                return false;
            }
            
            // Try with explicit column names and values - use correct column name
            String query = "INSERT INTO services (category, service_name, price, duration_minutes, details, is_active, created_by) VALUES (?, ?, ?, ?, ?, ?, ?)";
            System.out.println("Alternative SQL Query: " + query);
            
            stmt = conn.prepareStatement(query);
            stmt.setString(1, category);
            stmt.setString(2, serviceName);
            stmt.setDouble(3, price);
            stmt.setInt(4, duration);
            stmt.setString(5, details);
            stmt.setInt(6, 1); // is_active
            stmt.setString(7, "admin@mizrahbeauty.com"); // created_by
            
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Alternative method - Rows affected: " + rowsAffected);
            
            return rowsAffected > 0;
            
        } catch (Exception e) {
            System.out.println("Alternative method error: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // ===== Test Insert Service - SIMPLIFIED =====
    public static boolean testInsertService() {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            System.out.println("=== SIMPLE TEST INSERT DEBUG START ===");
            
            conn = getConnection();
            if (conn == null) {
                System.out.println("ERROR: Database connection is null");
                return false;
            }
            
            // Try to insert a test service with minimal fields - use correct column name
            String query = "INSERT INTO services (category, service_name, price, duration_minutes, details) VALUES (?, ?, ?, ?, ?)";
            System.out.println("Simple Test SQL Query: " + query);
            
            stmt = conn.prepareStatement(query);
            stmt.setString(1, "TEST CATEGORY");
            stmt.setString(2, "TEST SERVICE");
            stmt.setDouble(3, 99.99);
            stmt.setInt(4, 60);
            stmt.setString(5, "TEST DETAILS");
            
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Simple test insert - Rows affected: " + rowsAffected);
            
            if (rowsAffected > 0) {
                System.out.println("=== SIMPLE TEST INSERT DEBUG: SUCCESS ===");
                return true;
            } else {
                System.out.println("=== SIMPLE TEST INSERT DEBUG: NO ROWS AFFECTED ===");
                return false;
            }
            
        } catch (Exception e) {
            System.out.println("=== SIMPLE TEST INSERT DEBUG: EXCEPTION ===");
            System.out.println("Error type: " + e.getClass().getSimpleName());
            System.out.println("Error message: " + e.getMessage());
            System.out.println("Stack trace:");
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("=== SIMPLE TEST INSERT DEBUG END ===");
        }
    }

    // ===== Services (Create) - SIMPLIFIED VERSION =====
    public static boolean addService(String serviceName, String category, double price, int duration, String details) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            System.out.println("=== SIMPLE ADD SERVICE DEBUG START ===");
            System.out.println("Service Name: " + serviceName);
            System.out.println("Category: " + category);
            System.out.println("Price: " + price);
            System.out.println("Duration: " + duration);
            System.out.println("Details: " + details);
            
            conn = getConnection();
            if (conn == null) {
                System.out.println("ERROR: Database connection is null");
                return false;
            }
            System.out.println("Database connection: SUCCESS");
            
            // Use the correct column name - duration_minutes instead of duration_m
            String query = "INSERT INTO services (category, service_name, price, duration_minutes, details) VALUES (?, ?, ?, ?, ?)";
            System.out.println("Simple SQL Query: " + query);
            
            stmt = conn.prepareStatement(query);
            stmt.setString(1, category);
            stmt.setString(2, serviceName);
            stmt.setDouble(3, price);
            stmt.setInt(4, duration);
            stmt.setString(5, details);
            
            System.out.println("Executing simple insert statement...");
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Simple insert - Rows affected: " + rowsAffected);
            
            if (rowsAffected > 0) {
                System.out.println("=== SIMPLE ADD SERVICE DEBUG: SUCCESS ===");
                return true;
            } else {
                System.out.println("=== SIMPLE ADD SERVICE DEBUG: NO ROWS AFFECTED ===");
                return false;
            }
            
        } catch (Exception e) {
            System.out.println("=== SIMPLE ADD SERVICE DEBUG: EXCEPTION ===");
            System.out.println("Error type: " + e.getClass().getSimpleName());
            System.out.println("Error message: " + e.getMessage());
            System.out.println("Stack trace:");
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                    System.out.println("Statement closed successfully");
                }
                if (conn != null) {
                    conn.close();
                    System.out.println("Connection closed successfully");
                }
            } catch (Exception e) {
                System.out.println("Error closing resources: " + e.getMessage());
                e.printStackTrace();
            }
            System.out.println("=== SIMPLE ADD SERVICE DEBUG END ===");
        }
    }

    // ===== Services (Update) =====
    public static boolean updateService(int serviceId, String serviceName, String category, double price, int duration, String details) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            System.out.println("=== UPDATE SERVICE DEBUG START ===");
            System.out.println("Service ID: " + serviceId);
            System.out.println("Service Name: " + serviceName);
            System.out.println("Category: " + category);
            System.out.println("Price: " + price);
            System.out.println("Duration: " + duration);
            System.out.println("Details: " + details);
            
            conn = getConnection();
            if (conn == null) {
                System.out.println("ERROR: Database connection is null");
                return false;
            }
            System.out.println("Database connection: SUCCESS");
            
            String query = "UPDATE services SET category = ?, service_name = ?, price = ?, duration_minutes = ?, details = ?, updated_at = ? WHERE id = ?";
            System.out.println("Update SQL Query: " + query);
            
            stmt = conn.prepareStatement(query);
            stmt.setString(1, category);
            stmt.setString(2, serviceName);
            stmt.setDouble(3, price);
            stmt.setInt(4, duration);
            stmt.setString(5, details);
            stmt.setTimestamp(6, new java.sql.Timestamp(System.currentTimeMillis()));
            stmt.setInt(7, serviceId);
            
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Update - Rows affected: " + rowsAffected);
            
            if (rowsAffected > 0) {
                System.out.println("=== UPDATE SERVICE DEBUG: SUCCESS ===");
                return true;
            } else {
                System.out.println("=== UPDATE SERVICE DEBUG: NO ROWS AFFECTED ===");
                return false;
            }
            
        } catch (Exception e) {
            System.out.println("=== UPDATE SERVICE DEBUG: EXCEPTION ===");
            System.out.println("Error type: " + e.getClass().getSimpleName());
            System.out.println("Error message: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("=== UPDATE SERVICE DEBUG END ===");
        }
    }

    // ===== Services (Delete) =====
    public static boolean deleteService(int serviceId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            System.out.println("=== DELETE SERVICE DEBUG START ===");
            System.out.println("Service ID to delete: " + serviceId);
            
            conn = getConnection();
            if (conn == null) {
                System.out.println("ERROR: Database connection is null");
                return false;
            }
            System.out.println("Database connection: SUCCESS");
            
            String query = "DELETE FROM services WHERE id = ?";
            System.out.println("Delete SQL Query: " + query);
            
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, serviceId);
            
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Delete - Rows affected: " + rowsAffected);
            
            if (rowsAffected > 0) {
                System.out.println("=== DELETE SERVICE DEBUG: SUCCESS ===");
                return true;
            } else {
                System.out.println("=== DELETE SERVICE DEBUG: NO ROWS AFFECTED ===");
                return false;
            }
            
        } catch (Exception e) {
            System.out.println("=== DELETE SERVICE DEBUG: EXCEPTION ===");
            System.out.println("Error type: " + e.getClass().getSimpleName());
            System.out.println("Error message: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("=== DELETE SERVICE DEBUG END ===");
        }
    }

    // Services by creator
    public static ResultSet getServicesByCreator(String creatorEmail) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = getConnection();
            String query = "SELECT id, category, service_name, price, duration_minutes, details FROM services WHERE created_by_email = ? ORDER BY service_name ASC";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, creatorEmail);
            return stmt.executeQuery();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ResultSet getServiceById(int serviceId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = getConnection();
            String query = "SELECT id, category, service_name, price, duration_minutes, details, created_by_email FROM services WHERE id = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, serviceId);
            return stmt.executeQuery();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean updateService(int serviceId, String category, String serviceName, double price, int durationMinutes, String details, String editorEmail) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = getConnection();
            // Prefer update restricted by creator; if column missing, fallback to unrestricted
            try {
                String sql = "UPDATE services SET category = ?, service_name = ?, price = ?, duration_minutes = ?, details = ? WHERE id = ? AND created_by_email = ?";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, category);
                stmt.setString(2, serviceName);
                stmt.setDouble(3, price);
                stmt.setInt(4, durationMinutes);
                stmt.setString(5, details);
                stmt.setInt(6, serviceId);
                stmt.setString(7, editorEmail);
                int rows = stmt.executeUpdate();
                return rows > 0;
            } catch (Exception ex) {
                try { if (stmt != null) stmt.close(); } catch (Exception ignored) {}
                String sql = "UPDATE services SET category = ?, service_name = ?, price = ?, duration_minutes = ?, details = ? WHERE id = ?";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, category);
                stmt.setString(2, serviceName);
                stmt.setDouble(3, price);
                stmt.setInt(4, durationMinutes);
                stmt.setString(5, details);
                stmt.setInt(6, serviceId);
                int rows = stmt.executeUpdate();
                return rows > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try { if (stmt != null) stmt.close(); } catch (Exception ignored) {}
            try { if (conn != null) conn.close(); } catch (Exception ignored) {}
        }
    }
    public static int countServicesByCreator(String creatorEmail) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            String query = "SELECT COUNT(*) AS total FROM services WHERE created_by_email = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, creatorEmail);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        } finally {
            closeResources(rs, stmt, conn);
        }
    }

    public static boolean deleteService(int serviceId, String creatorEmail) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = getConnection();
            // Prefer delete restricted by creator; if column missing this will throw and we fallback
            try {
                String sql = "DELETE FROM services WHERE id = ? AND created_by_email = ?";
                stmt = conn.prepareStatement(sql);
                stmt.setInt(1, serviceId);
                stmt.setString(2, creatorEmail);
                int rows = stmt.executeUpdate();
                return rows > 0;
            } catch (Exception ex) {
                try { if (stmt != null) stmt.close(); } catch (Exception ignored) {}
                String sql = "DELETE FROM services WHERE id = ?";
                stmt = conn.prepareStatement(sql);
                stmt.setInt(1, serviceId);
                int rows = stmt.executeUpdate();
                return rows > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try { if (stmt != null) stmt.close(); } catch (Exception ignored) {}
            try { if (conn != null) conn.close(); } catch (Exception ignored) {}
        }
    }

    // ===== User Profile Management =====
    // Get user profile details including name, email, and phone
    public static String[] getUserProfileDetails(String userEmail) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            ensureNameColumn(conn);
            ensurePhoneColumn(conn);
            
            String query = "SELECT COALESCE(name, '') AS full_name, email, phone FROM users WHERE email = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, userEmail);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                String fullName = rs.getString("full_name");
                String email = rs.getString("email");
                String phone = rs.getString("phone");
                
                if (fullName == null || fullName.trim().isEmpty()) {
                    // Fallback to staff table if available
                    try (PreparedStatement staffStmt = conn.prepareStatement("SELECT name FROM staff WHERE user_email = ?")) {
                        staffStmt.setString(1, userEmail);
                        try (ResultSet staffRs = staffStmt.executeQuery()) {
                            if (staffRs.next()) {
                                String staffName = staffRs.getString("name");
                                if (staffName != null && !staffName.trim().isEmpty()) {
                                    fullName = staffName.trim();
                                }
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }

                if (email == null || email.isEmpty()) {
                    email = userEmail;
                }
                
                if (fullName == null) {
                    fullName = "";
                }

                if (fullName.trim().isEmpty()) {
                    String derivedName = deriveNameFromEmail(email);
                    if (!derivedName.isEmpty()) {
                        fullName = derivedName;
                        try (PreparedStatement updateName = conn.prepareStatement("UPDATE users SET name = ? WHERE email = ?")) {
                            updateName.setString(1, fullName);
                            updateName.setString(2, email);
                            updateName.executeUpdate();
                        } catch (Exception ignored) {
                        }
                    }
                }
                if (phone == null) {
                    phone = "";
                }
                
                return new String[]{fullName, email, phone};
            }
            
            return null;
            
        } catch (Exception e) {
            System.out.println("Error getting user details: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignored) {}
            try { if (stmt != null) stmt.close(); } catch (Exception ignored) {}
            try { if (conn != null) conn.close(); } catch (Exception ignored) {}
        }
    }
    
    // Update user profile phone and password only (name is not editable)
    public static boolean updateUserProfilePhoneAndPassword(String userEmail, String phone, String currentPassword, String newPassword) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = getConnection();
            ensurePhoneColumn(conn);
            
            // First, verify current password if provided
            if (currentPassword != null && !currentPassword.isEmpty()) {
                String verifyQuery = "SELECT password FROM users WHERE email = ?";
                PreparedStatement verifyStmt = conn.prepareStatement(verifyQuery);
                verifyStmt.setString(1, userEmail);
                ResultSet rs = verifyStmt.executeQuery();
                
                if (!rs.next() || !rs.getString("password").equals(currentPassword)) {
                    rs.close();
                    verifyStmt.close();
                    return false; // Invalid current password
                }
                rs.close();
                verifyStmt.close();
            }
            
            // Update phone and optionally password (name remains unchanged)
            String updateQuery;
            if (newPassword != null && !newPassword.isEmpty()) {
                // Update phone and password
                updateQuery = "UPDATE users SET phone = ?, password = ?, updated_at = GETDATE() WHERE email = ?";
            } else {
                // Update only phone
                updateQuery = "UPDATE users SET phone = ?, updated_at = GETDATE() WHERE email = ?";
            }
            
            stmt = conn.prepareStatement(updateQuery);
            stmt.setString(1, phone);
            
            if (newPassword != null && !newPassword.isEmpty()) {
                stmt.setString(2, newPassword);
                stmt.setString(3, userEmail);
            } else {
                stmt.setString(2, userEmail);
            }
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (Exception e) {
            System.out.println("Error updating user profile phone and password: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try { if (stmt != null) stmt.close(); } catch (Exception ignored) {}
            try { if (conn != null) conn.close(); } catch (Exception ignored) {}
        }
    }

    // Update only the user's phone number
    public static boolean updateUserPhone(String userEmail, String phone) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = getConnection();
            ensurePhoneColumn(conn);
            String updateQuery = "UPDATE users SET phone = ?, updated_at = GETDATE() WHERE email = ?";
            stmt = conn.prepareStatement(updateQuery);
            stmt.setString(1, phone);
            stmt.setString(2, userEmail);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (Exception e) {
            System.out.println("Error updating user phone: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try { if (stmt != null) stmt.close(); } catch (Exception ignored) {}
            try { if (conn != null) conn.close(); } catch (Exception ignored) {}
        }
    }

    // Update user profile (name, phone, and optionally password) - kept for other uses
    public static boolean updateUserProfile(String userEmail, String fullName, String phone, String currentPassword, String newPassword) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = getConnection();
            ensureNameColumn(conn);
            
            // First, verify current password if provided
            if (currentPassword != null && !currentPassword.isEmpty()) {
                String verifyQuery = "SELECT password FROM users WHERE email = ?";
                PreparedStatement verifyStmt = conn.prepareStatement(verifyQuery);
                verifyStmt.setString(1, userEmail);
                ResultSet rs = verifyStmt.executeQuery();
                
                if (!rs.next() || !rs.getString("password").equals(currentPassword)) {
                    rs.close();
                    verifyStmt.close();
                    return false; // Invalid current password
                }
                rs.close();
                verifyStmt.close();
            }
            
            String cleanName = fullName != null ? fullName.trim() : "";
            
            String updateQuery;
            if (newPassword != null && !newPassword.isEmpty()) {
                updateQuery = "UPDATE users SET name = ?, phone = ?, password = ?, updated_at = GETDATE() WHERE email = ?";
            } else {
                updateQuery = "UPDATE users SET name = ?, phone = ?, updated_at = GETDATE() WHERE email = ?";
            }
            
            stmt = conn.prepareStatement(updateQuery);
            stmt.setString(1, cleanName);
            stmt.setString(2, phone);
            
            if (newPassword != null && !newPassword.isEmpty()) {
                stmt.setString(3, newPassword);
                stmt.setString(4, userEmail);
            } else {
                stmt.setString(3, userEmail);
            }
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (Exception e) {
            System.out.println("Error updating user profile: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try { if (stmt != null) stmt.close(); } catch (Exception ignored) {}
            try { if (conn != null) conn.close(); } catch (Exception ignored) {}
        }
    }

    // ===== Bookings (Create/Read/Delete) =====
    // Create booking with specific staff
    // Check for booking conflicts at the same date and time
    public static boolean hasBookingConflict(String appointmentTimeIso) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            
            // Check if there's already a booking at the same time
            // We'll check for conflicts within a 1-hour window to prevent overlapping appointments
            String query = "SELECT COUNT(*) FROM bookings WHERE " +
                          "appointment_time = ? AND " +
                          "status IN ('ACTIVE', 'PENDING', 'CONFIRMED')";
            
            stmt = conn.prepareStatement(query);
            stmt.setString(1, appointmentTimeIso);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                int conflictCount = rs.getInt(1);
                return conflictCount > 0; // Return true if there's a conflict
            }
            
            return false;
            
        } catch (Exception e) {
            System.out.println("Error checking booking conflict: " + e.getMessage());
            e.printStackTrace();
            return true; // Return true on error to be safe
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignored) {}
            try { if (stmt != null) stmt.close(); } catch (Exception ignored) {}
            try { if (conn != null) conn.close(); } catch (Exception ignored) {}
        }
    }
    
    // Check for booking conflicts with more detailed information
    public static String getBookingConflictInfo(String appointmentTimeIso) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            
            // Get details of conflicting bookings
            String query = "SELECT b.user_email, s.service_name, b.status " +
                          "FROM bookings b " +
                          "JOIN services s ON b.service_id = s.id " +
                          "WHERE b.appointment_time = ? AND " +
                          "b.status IN ('ACTIVE', 'PENDING', 'CONFIRMED')";
            
            stmt = conn.prepareStatement(query);
            stmt.setString(1, appointmentTimeIso);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                String userEmail = rs.getString("user_email");
                String serviceName = rs.getString("service_name");
                String status = rs.getString("status");
                
                return String.format("Time slot is already booked by %s for %s (Status: %s)", 
                                   userEmail, serviceName, status);
            }
            
            return null; // No conflict found
            
        } catch (Exception e) {
            System.out.println("Error getting booking conflict info: " + e.getMessage());
            e.printStackTrace();
            return "Unable to check for conflicts";
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignored) {}
            try { if (stmt != null) stmt.close(); } catch (Exception ignored) {}
            try { if (conn != null) conn.close(); } catch (Exception ignored) {}
        }
    }

    public static boolean createBookingWithStaff(String userEmail, int serviceId, String appointmentTimeIso, String notes, String staffEmail) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = getConnection();
            
            // Check for booking conflicts first
            if (hasBookingConflict(appointmentTimeIso)) {
                System.out.println("Booking conflict detected for time: " + appointmentTimeIso);
                return false; // Return false to indicate conflict
            }
            
            // Ensure staff columns exist in bookings table
            ensureBookingsStaffColumn(conn);
            
            // Get staff name from email
            String staffName = getStaffNameByEmail(staffEmail);
            
            // Insert booking with specific staff assignment (both email and name)
            String query = "INSERT INTO bookings (user_email, service_id, appointment_time, notes, status, staff_email, staff_name) VALUES (?, ?, ?, ?, 'ACTIVE', ?, ?)";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, userEmail);
            stmt.setInt(2, serviceId);
            stmt.setString(3, appointmentTimeIso);
            stmt.setString(4, notes);
            stmt.setString(5, staffEmail);
            stmt.setString(6, staffName);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (Exception e) {
            System.out.println("Error creating booking with staff: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try { if (stmt != null) stmt.close(); } catch (Exception ignored) {}
            try { if (conn != null) conn.close(); } catch (Exception ignored) {}
        }
    }
    
    // Method to create booking with staff name only (user choice based)
    public static boolean createBookingWithStaffName(String userEmail, int serviceId, String appointmentTimeIso, String notes, String staffName) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = getConnection();
            
            // Check for booking conflicts first
            if (hasBookingConflict(appointmentTimeIso)) {
                System.out.println("Booking conflict detected for time: " + appointmentTimeIso);
                return false; // Return false to indicate conflict
            }
            
            // Ensure staff columns exist in bookings table
            ensureBookingsStaffColumn(conn);
            
            // Get staff email from name (optional, for reference)
            String staffEmail = getStaffEmailByName(staffName);
            
            // Insert booking with staff name (primary) and email (secondary)
            String query = "INSERT INTO bookings (user_email, service_id, appointment_time, notes, status, staff_name, staff_email) VALUES (?, ?, ?, ?, 'ACTIVE', ?, ?)";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, userEmail);
            stmt.setInt(2, serviceId);
            stmt.setString(3, appointmentTimeIso);
            stmt.setString(4, notes);
            stmt.setString(5, staffName);
            stmt.setString(6, staffEmail); // Can be null if staff not found
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (Exception e) {
            System.out.println("Error creating booking with staff name: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try { if (stmt != null) stmt.close(); } catch (Exception ignored) {}
            try { if (conn != null) conn.close(); } catch (Exception ignored) {}
        }
    }
    
    // Method to create booking and return the booking ID
    public static int createBookingWithStaffNameAndGetId(String userEmail, int serviceId, String appointmentTimeIso, String notes, String staffName) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;
        try {
            conn = getConnection();
            
            // Check for booking conflicts first
            if (hasBookingConflict(appointmentTimeIso)) {
                System.out.println("Booking conflict detected for time: " + appointmentTimeIso);
                return -1; // Return -1 to indicate conflict
            }
            
            // Ensure staff columns exist in bookings table
            ensureBookingsStaffColumn(conn);
            
            // Get staff email from name (optional, for reference)
            String staffEmail = getStaffEmailByName(staffName);
            
            // Insert booking with staff name (primary) and email (secondary)
            // Use RETURN_GENERATED_KEYS to get the booking ID
            String query = "INSERT INTO bookings (user_email, service_id, appointment_time, notes, status, staff_name, staff_email) VALUES (?, ?, ?, ?, 'ACTIVE', ?, ?)";
            stmt = conn.prepareStatement(query, java.sql.Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, userEmail);
            stmt.setInt(2, serviceId);
            stmt.setString(3, appointmentTimeIso);
            stmt.setString(4, notes);
            stmt.setString(5, staffName);
            stmt.setString(6, staffEmail); // Can be null if staff not found
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                // Get the generated booking ID
                generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
            return -1; // Return -1 if booking creation failed
            
        } catch (Exception e) {
            System.out.println("Error creating booking with staff name and getting ID: " + e.getMessage());
            e.printStackTrace();
            return -1;
        } finally {
            try { if (generatedKeys != null) generatedKeys.close(); } catch (Exception ignored) {}
            try { if (stmt != null) stmt.close(); } catch (Exception ignored) {}
            try { if (conn != null) conn.close(); } catch (Exception ignored) {}
        }
    }
    
    public static boolean createBooking(String userEmail, int serviceId, String appointmentTimeIso, String notes) {
        Connection conn = null;
        PreparedStatement stmt = null;
        PreparedStatement getStaffStmt = null;
        ResultSet staffRs = null;
        try {
            conn = getConnection();
            
            // Ensure staff_email column exists in bookings table
            ensureBookingsStaffColumn(conn);
            
            // Get a random active staff member
            String assignedStaffEmail = null;
            try {
                String getStaffQuery = "SELECT TOP 1 user_email FROM staff WHERE is_active = 1 ORDER BY NEWID()";
                getStaffStmt = conn.prepareStatement(getStaffQuery);
                staffRs = getStaffStmt.executeQuery();
                if (staffRs.next()) {
                    assignedStaffEmail = staffRs.getString("user_email");
                }
            } catch (Exception e) {
                // If staff table doesn't exist or no active staff, continue without assigning
                System.out.println("Could not assign staff: " + e.getMessage());
            } finally {
                try { if (staffRs != null) staffRs.close(); } catch (Exception ignored) {}
                try { if (getStaffStmt != null) getStaffStmt.close(); } catch (Exception ignored) {}
            }
            
            // Insert booking with staff assignment
            String query;
            if (assignedStaffEmail != null) {
                query = "INSERT INTO bookings (user_email, service_id, appointment_time, notes, status, staff_email) VALUES (?, ?, ?, ?, 'ACTIVE', ?)";
                stmt = conn.prepareStatement(query);
                stmt.setString(1, userEmail);
                stmt.setInt(2, serviceId);
                stmt.setString(3, appointmentTimeIso);
                stmt.setString(4, notes);
                stmt.setString(5, assignedStaffEmail);
            } else {
                query = "INSERT INTO bookings (user_email, service_id, appointment_time, notes, status) VALUES (?, ?, ?, ?, 'ACTIVE')";
                stmt = conn.prepareStatement(query);
                stmt.setString(1, userEmail);
                stmt.setInt(2, serviceId);
                stmt.setString(3, appointmentTimeIso);
                stmt.setString(4, notes);
            }
            
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try { if (stmt != null) stmt.close(); } catch (Exception ignored) {}
            try { if (conn != null) conn.close(); } catch (Exception ignored) {}
        }
    }
    
    private static void ensureBookingsStaffColumn(Connection conn) {
        PreparedStatement check = null;
        ResultSet rs = null;
        try {
            // Check for staff_email column
            String checkSql = "SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'bookings' AND COLUMN_NAME = 'staff_email'";
            check = conn.prepareStatement(checkSql);
            rs = check.executeQuery();
            boolean emailExists = rs.next();
            if (!emailExists) {
                try (PreparedStatement alter = conn.prepareStatement("ALTER TABLE bookings ADD staff_email VARCHAR(255) NULL")) {
                    alter.executeUpdate();
                    System.out.println("Added staff_email column to bookings table");
                    
                    // Assign staff to existing bookings
                    assignStaffToExistingBookings(conn);
                }
            }
            
            // Check for staff_name column
            rs.close();
            check.close();
            String checkNameSql = "SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'bookings' AND COLUMN_NAME = 'staff_name'";
            check = conn.prepareStatement(checkNameSql);
            rs = check.executeQuery();
            boolean nameExists = rs.next();
            if (!nameExists) {
                try (PreparedStatement alter = conn.prepareStatement("ALTER TABLE bookings ADD staff_name VARCHAR(255) NULL")) {
                    alter.executeUpdate();
                    System.out.println("Added staff_name column to bookings table");
                }
            }
        } catch (Exception ignored) {
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignored) {}
            try { if (check != null) check.close(); } catch (Exception ignored) {}
        }
    }
    
    private static void assignStaffToExistingBookings(Connection conn) {
        PreparedStatement getBookings = null;
        PreparedStatement getStaff = null;
        PreparedStatement updateBooking = null;
        ResultSet bookingsRs = null;
        ResultSet staffRs = null;
        
        try {
            // Get all bookings without staff assignment
            String getBookingsQuery = "SELECT id FROM bookings WHERE staff_email IS NULL";
            getBookings = conn.prepareStatement(getBookingsQuery);
            bookingsRs = getBookings.executeQuery();
            
            // Get list of active staff
            String getStaffQuery = "SELECT user_email FROM staff WHERE is_active = 1";
            getStaff = conn.prepareStatement(getStaffQuery);
            staffRs = getStaff.executeQuery();
            
            java.util.List<String> staffEmails = new java.util.ArrayList<>();
            while (staffRs.next()) {
                staffEmails.add(staffRs.getString("user_email"));
            }
            
            if (!staffEmails.isEmpty()) {
                // Update each booking with a random staff
                String updateQuery = "UPDATE bookings SET staff_email = ? WHERE id = ?";
                updateBooking = conn.prepareStatement(updateQuery);
                
                int count = 0;
                while (bookingsRs.next()) {
                    int bookingId = bookingsRs.getInt("id");
                    // Assign staff in round-robin fashion
                    String assignedStaffEmail = staffEmails.get(count % staffEmails.size());
                    updateBooking.setString(1, assignedStaffEmail);
                    updateBooking.setInt(2, bookingId);
                    updateBooking.executeUpdate();
                    count++;
                }
                
                System.out.println("Assigned staff to " + count + " existing bookings");
            }
        } catch (Exception e) {
            System.out.println("Error assigning staff to existing bookings: " + e.getMessage());
        } finally {
            try { if (bookingsRs != null) bookingsRs.close(); } catch (Exception ignored) {}
            try { if (staffRs != null) staffRs.close(); } catch (Exception ignored) {}
            try { if (getBookings != null) getBookings.close(); } catch (Exception ignored) {}
            try { if (getStaff != null) getStaff.close(); } catch (Exception ignored) {}
            try { if (updateBooking != null) updateBooking.close(); } catch (Exception ignored) {}
        }
    }

    public static ResultSet getUserBookings(String userEmail) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = getConnection();
            // Join with staff table to get actual staff name based on assigned staff_email
            String query = "SELECT b.id, b.user_email, COALESCE(u.name, b.user_email) AS customer_name, " +
                    "s.service_name AS service_name, b.appointment_time, b.status, b.notes, b.service_id, " +
                    "COALESCE(st.name, 'Staff') AS staff_name " +
                    "FROM bookings b " +
                    "JOIN services s ON b.service_id = s.id " +
                    "LEFT JOIN staff st ON b.staff_email = st.user_email " +
                    "LEFT JOIN users u ON b.user_email = u.email " +
                    "WHERE b.user_email = ? ORDER BY b.appointment_time DESC";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, userEmail);
            return stmt.executeQuery();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean cancelBooking(int bookingId, String userEmail) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = getConnection();
            String query = "UPDATE bookings SET status = 'CANCELLED' WHERE id = ? AND user_email = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, bookingId);
            stmt.setString(2, userEmail);
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try { if (stmt != null) stmt.close(); } catch (Exception ignored) {}
            try { if (conn != null) conn.close(); } catch (Exception ignored) {}
        }
    }

    // ===== Reviews (Create) =====
    public static boolean addReview(String userEmail, int serviceId, int rating, String comment) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = getConnection();
            String query = "INSERT INTO reviews (user_email, service_id, rating, comment, created_at) VALUES (?, ?, ?, ?, GETDATE())";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, userEmail);
            stmt.setInt(2, serviceId);
            stmt.setInt(3, rating);
            stmt.setString(4, comment);
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try { if (stmt != null) stmt.close(); } catch (Exception ignored) {}
            try { if (conn != null) conn.close(); } catch (Exception ignored) {}
        }
    }

    // ===== Staff Methods for Reading All Bookings and Reviews =====
    
    /**
     * Get all customer bookings for staff to view
     * @return ResultSet containing all bookings with customer and service details
     */
    public static ResultSet getAllBookingsForStaff() {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = getConnection();
            String query = "SELECT b.id, b.user_email, COALESCE(u.name, b.user_email) AS customer_name, " +
                    "s.service_name AS service_name, b.appointment_time, b.status, b.notes, b.service_id, " +
                    "COALESCE(st.name, 'Staff') AS staff_name " +
                    "FROM bookings b " +
                    "JOIN services s ON b.service_id = s.id " +
                    "LEFT JOIN staff st ON b.staff_email = st.user_email " +
                    "LEFT JOIN users u ON b.user_email = u.email " +
                    "ORDER BY b.appointment_time DESC";
            stmt = conn.prepareStatement(query);
            return stmt.executeQuery();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get current/active bookings for staff to view
     * @return ResultSet containing active bookings
     */
    public static ResultSet getCurrentBookingsForStaff() {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = getConnection();
            String query = "SELECT b.id, b.user_email, COALESCE(u.name, b.user_email) AS customer_name, " +
                    "s.service_name AS service_name, b.appointment_time, b.status, b.notes, b.service_id, " +
                    "COALESCE(st.name, 'Staff') AS staff_name, " +
                    "CONVERT(VARCHAR(10), b.appointment_time, 120) AS booking_date, " +
                    "CONVERT(VARCHAR(8), b.appointment_time, 108) AS booking_time " +
                    "FROM bookings b " +
                    "JOIN services s ON b.service_id = s.id " +
                    "LEFT JOIN staff st ON b.staff_email = st.user_email " +
                    "LEFT JOIN users u ON b.user_email = u.email " +
                    "WHERE b.status = 'ACTIVE' " +
                    "ORDER BY b.appointment_time ASC";
            stmt = conn.prepareStatement(query);
            return stmt.executeQuery();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get previous/completed bookings for staff to view
     * @return ResultSet containing completed or cancelled bookings
     */
    public static ResultSet getPreviousBookingsForStaff() {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = getConnection();
            String query = "SELECT b.id, b.user_email, COALESCE(u.name, b.user_email) AS customer_name, " +
                    "s.service_name, b.appointment_time, b.status, b.notes, b.service_id, " +
                    "COALESCE(st.name, 'Staff') AS staff_name, " +
                    "CONVERT(VARCHAR(10), b.appointment_time, 120) AS booking_date, " +
                    "CONVERT(VARCHAR(8), b.appointment_time, 108) AS booking_time " +
                    "FROM bookings b " +
                    "JOIN services s ON b.service_id = s.id " +
                    "LEFT JOIN staff st ON b.staff_email = st.user_email " +
                    "LEFT JOIN users u ON b.user_email = u.email " +
                    "WHERE b.status IN ('COMPLETED', 'CANCELLED') " +
                    "ORDER BY b.appointment_time DESC";
            stmt = conn.prepareStatement(query);
            return stmt.executeQuery();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get all customer reviews for staff to view
     * @return ResultSet containing all reviews with customer and service details
     */
    public static ResultSet getAllReviewsForStaff() {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = getConnection();
            
            // First check if tables exist
            String checkReviews = "SELECT COUNT(*) FROM reviews";
            String checkServices = "SELECT COUNT(*) FROM services";
            
            try (PreparedStatement checkReviewsStmt = conn.prepareStatement(checkReviews);
                 PreparedStatement checkServicesStmt = conn.prepareStatement(checkServices)) {
                checkReviewsStmt.executeQuery();
                checkServicesStmt.executeQuery();
            } catch (Exception e) {
                // Tables don't exist, return empty result
                String emptyQuery = "SELECT 0 as id, '' as user_email, '' as service_name, 0 as rating, '' as comment, '' as created_at, 0 as service_id WHERE 1=0";
                stmt = conn.prepareStatement(emptyQuery);
                return stmt.executeQuery();
            }
            
            // Tables exist, proceed with normal query
            String query = "SELECT r.id, r.user_email, s.service_name, r.rating, r.comment, r.created_at, r.service_id " +
                    "FROM reviews r " +
                    "JOIN services s ON r.service_id = s.id " +
                    "ORDER BY r.created_at DESC";
            stmt = conn.prepareStatement(query);
            return stmt.executeQuery();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Update booking status (for staff to mark as completed, etc.)
     * @param bookingId The booking ID to update
     * @param newStatus The new status (ACTIVE, COMPLETED, CANCELLED)
     * @return true if update was successful
     */
    public static boolean updateBookingStatus(int bookingId, String newStatus) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = getConnection();
            String query = "UPDATE bookings SET status = ? WHERE id = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, newStatus);
            stmt.setInt(2, bookingId);
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try { if (stmt != null) stmt.close(); } catch (Exception ignored) {}
            try { if (conn != null) conn.close(); } catch (Exception ignored) {}
        }
    }
    
    /**
     * Update booking status with staff assignment (for staff to mark as completed, etc.)
     * @param bookingId The booking ID to update
     * @param newStatus The new status (ACTIVE, COMPLETED, CANCELLED)
     * @param staffEmail The email of staff who is updating the status
     * @return true if update was successful
     */
    public static boolean updateBookingStatusWithStaff(int bookingId, String newStatus, String staffEmail) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            System.out.println("=== UPDATE BOOKING WITH STAFF DEBUG ===");
            System.out.println("Booking ID: " + bookingId);
            System.out.println("New Status: " + newStatus);
            System.out.println("Staff Email: " + staffEmail);
            
            conn = getConnection();
            
            // Ensure staff_email column exists
            ensureBookingsStaffColumn(conn);
            
            // Update status and assign staff (especially for COMPLETED status)
            // This will OVERRIDE any previous staff assignment
            String query = "UPDATE bookings SET status = ?, staff_email = ? WHERE id = ?";
            System.out.println("SQL Query: " + query);
            
            stmt = conn.prepareStatement(query);
            stmt.setString(1, newStatus);
            stmt.setString(2, staffEmail);
            stmt.setInt(3, bookingId);
            
            int rows = stmt.executeUpdate();
            System.out.println("Rows updated: " + rows);
            System.out.println("=== UPDATE BOOKING WITH STAFF: " + (rows > 0 ? "SUCCESS" : "FAILED") + " ===");
            
            return rows > 0;
        } catch (Exception e) {
            System.out.println("=== UPDATE BOOKING WITH STAFF: EXCEPTION ===");
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try { if (stmt != null) stmt.close(); } catch (Exception ignored) {}
            try { if (conn != null) conn.close(); } catch (Exception ignored) {}
        }
    }

    // ===== Staff Service Status Management =====
    
    /**
     * Get current service status for a staff member
     * @param staffEmail The staff email
     * @return ResultSet containing staff service status
     */
    public static ResultSet getStaffServiceStatus(String staffEmail) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = getConnection();
            // Try to get from staff table first, if it exists
            try {
                String query = "SELECT name, phone, position, service_details, is_available, updated_at FROM staff WHERE user_email = ?";
                stmt = conn.prepareStatement(query);
                stmt.setString(1, staffEmail);
                return stmt.executeQuery();
            } catch (Exception ex) {
                // If staff table doesn't exist or error, return user info
                try { if (stmt != null) stmt.close(); } catch (Exception ignored) {}
                String query = "SELECT email, name FROM users WHERE email = ?";
                stmt = conn.prepareStatement(query);
                stmt.setString(1, staffEmail);
                return stmt.executeQuery();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Update staff service status and details
     * @param staffEmail The staff email
     * @param serviceDetails Details about current service
     * @param isAvailable Whether staff is available
     * @return true if update was successful
     */
    public static boolean updateStaffServiceStatus(String staffEmail, String serviceDetails, boolean isAvailable) {
        Connection conn = null;
        PreparedStatement checkStmt = null;
        PreparedStatement insertStmt = null;
        PreparedStatement updateStmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            
            // First ensure staff table exists
            ensureStaffTable(conn);
            
            // Check if staff record exists
            String checkQuery = "SELECT COUNT(*) FROM staff WHERE user_email = ?";
            checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setString(1, staffEmail);
            rs = checkStmt.executeQuery();
            
            boolean staffExists = false;
            if (rs.next()) {
                staffExists = rs.getInt(1) > 0;
            }
            try { if (rs != null) rs.close(); } catch (Exception ignored) {}
            
            if (!staffExists) {
                // Insert new staff record
                String insertQuery = "INSERT INTO staff (user_email, service_details, is_available, updated_at) VALUES (?, ?, ?, GETDATE())";
                insertStmt = conn.prepareStatement(insertQuery);
                insertStmt.setString(1, staffEmail);
                insertStmt.setString(2, serviceDetails);
                insertStmt.setBoolean(3, isAvailable);
                int rows = insertStmt.executeUpdate();
                return rows > 0;
            } else {
                // Update existing staff record
                String updateQuery = "UPDATE staff SET service_details = ?, is_available = ?, updated_at = GETDATE() WHERE user_email = ?";
                updateStmt = conn.prepareStatement(updateQuery);
                updateStmt.setString(1, serviceDetails);
                updateStmt.setBoolean(2, isAvailable);
                updateStmt.setString(3, staffEmail);
                int rows = updateStmt.executeUpdate();
                return rows > 0;
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignored) {}
            try { if (checkStmt != null) checkStmt.close(); } catch (Exception ignored) {}
            try { if (insertStmt != null) insertStmt.close(); } catch (Exception ignored) {}
            try { if (updateStmt != null) updateStmt.close(); } catch (Exception ignored) {}
            try { if (conn != null) conn.close(); } catch (Exception ignored) {}
        }
    }

    private static void ensureStaffTable(Connection conn) {
        PreparedStatement check = null;
        ResultSet rs = null;
        try {
            // Check if staff table exists
            String checkSql = "SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'staff'";
            check = conn.prepareStatement(checkSql);
            rs = check.executeQuery();
            boolean exists = rs.next();
            
            if (!exists) {
                // Create staff table
                try (PreparedStatement create = conn.prepareStatement(
                    "CREATE TABLE staff (" +
                    "id INT IDENTITY(1,1) PRIMARY KEY, " +
                    "user_email VARCHAR(255) NOT NULL UNIQUE, " +
                    "name VARCHAR(255), " +
                    "phone VARCHAR(50), " +
                    "position VARCHAR(100), " +
                    "service_details VARCHAR(1000), " +
                    "is_available BIT DEFAULT 1, " +
                    "is_active BIT DEFAULT 1, " +
                    "created_at DATETIME DEFAULT GETDATE(), " +
                    "updated_at DATETIME DEFAULT GETDATE()" +
                    ")"
                )) {
                    create.executeUpdate();
                }
            }
            
            // Ensure service_details and is_available columns exist
            try {
                String addServiceDetails = "ALTER TABLE staff ADD service_details VARCHAR(1000)";
                try (PreparedStatement alter = conn.prepareStatement(addServiceDetails)) {
                    alter.executeUpdate();
                }
            } catch (Exception ignored) {}
            
            try {
                String addIsAvailable = "ALTER TABLE staff ADD is_available BIT DEFAULT 1";
                try (PreparedStatement alter = conn.prepareStatement(addIsAvailable)) {
                    alter.executeUpdate();
                }
            } catch (Exception ignored) {}
            
            try {
                String addUpdatedAt = "ALTER TABLE staff ADD updated_at DATETIME DEFAULT GETDATE()";
                try (PreparedStatement alter = conn.prepareStatement(addUpdatedAt)) {
                    alter.executeUpdate();
                }
            } catch (Exception ignored) {}
            
        } catch (Exception ignored) {
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignored) {}
            try { if (check != null) check.close(); } catch (Exception ignored) {}
        }
    }

    // ===== Staff Management =====
    
    /**
     * Delete a staff member
     * @param staffEmail The staff email to delete
     * @return true if deletion was successful
     */
    public static boolean deleteStaff(String staffEmail) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            System.out.println("=== DELETE STAFF DEBUG START ===");
            System.out.println("Staff Email to delete: " + staffEmail);
            
            conn = getConnection();
            if (conn == null) {
                System.out.println("ERROR: Database connection is null");
                return false;
            }
            System.out.println("Database connection: SUCCESS");
            
            // Delete from both users and staff tables
            // First delete from users table
            String query1 = "DELETE FROM users WHERE email = ? AND role = 'staff'";
            System.out.println("Delete from users SQL Query: " + query1);
            
            stmt = conn.prepareStatement(query1);
            stmt.setString(1, staffEmail);
            int usersRowsAffected = stmt.executeUpdate();
            System.out.println("Delete from users - Rows affected: " + usersRowsAffected);
            
            // Then delete from staff table
            String query2 = "DELETE FROM staff WHERE user_email = ?";
            System.out.println("Delete from staff SQL Query: " + query2);
            
            stmt = conn.prepareStatement(query2);
            stmt.setString(1, staffEmail);
            int staffRowsAffected = stmt.executeUpdate();
            System.out.println("Delete from staff - Rows affected: " + staffRowsAffected);
            
            int totalRowsAffected = usersRowsAffected + staffRowsAffected;
            System.out.println("Total rows affected: " + totalRowsAffected);
            
            if (totalRowsAffected > 0) {
                System.out.println("=== DELETE STAFF DEBUG: SUCCESS ===");
                return true;
            } else {
                System.out.println("=== DELETE STAFF DEBUG: NO ROWS AFFECTED ===");
                return false;
            }
            
        } catch (Exception e) {
            System.out.println("=== DELETE STAFF DEBUG: EXCEPTION ===");
            System.out.println("Error type: " + e.getClass().getSimpleName());
            System.out.println("Error message: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                    System.out.println("Statement closed successfully");
                }
                if (conn != null) {
                    conn.close();
                    System.out.println("Connection closed successfully");
                }
            } catch (Exception e) {
                System.out.println("Error closing resources: " + e.getMessage());
                e.printStackTrace();
            }
            System.out.println("=== DELETE STAFF DEBUG END ===");
        }
    }
    
    /**
     * Update staff information
     * @param staffId The staff ID
     * @param name The new name
     * @param phone The new phone number
     * @return true if update was successful
     */
    public static boolean updateStaffInfo(int staffId, String name, String phone) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = getConnection();
            
            // Update staff table
            String query = "UPDATE staff SET name = ?, phone = ?, updated_at = GETDATE() WHERE id = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, name);
            stmt.setString(2, phone);
            stmt.setInt(3, staffId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try { if (stmt != null) stmt.close(); } catch (Exception ignored) {}
            try { if (conn != null) conn.close(); } catch (Exception ignored) {}
        }
    }
    
    // ==================== FEEDBACK MANAGEMENT ====================
    
    // Create feedback table
    public static boolean createFeedbackTable() {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = getConnection();
            stmt = conn.createStatement();
            
            String createTableSQL = "CREATE TABLE IF NOT EXISTS feedback (" +
                    "id INT IDENTITY(1,1) PRIMARY KEY, " +
                    "user_email VARCHAR(255) NOT NULL, " +
                    "user_name VARCHAR(255) NOT NULL, " +
                    "feedback_text NVARCHAR(MAX) NOT NULL, " +
                    "rating INT NOT NULL, " +
                    "feedback_type VARCHAR(50) NOT NULL, " +
                    "status VARCHAR(20) DEFAULT 'PENDING', " +
                    "created_at DATETIME2 DEFAULT GETDATE(), " +
                    "response NVARCHAR(MAX), " +
                    "responded_at DATETIME2, " +
                    "FOREIGN KEY (user_email) REFERENCES users(email))";
            
            stmt.execute(createTableSQL);
            System.out.println("Feedback table created successfully");
            return true;
        } catch (Exception e) {
            System.out.println("Error creating feedback table: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try { if (stmt != null) stmt.close(); } catch (Exception ignored) {}
            try { if (conn != null) conn.close(); } catch (Exception ignored) {}
        }
    }
    
    // Submit feedback
    public static boolean submitFeedback(String userEmail, String userName, String feedbackText, int rating, String feedbackType) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            System.out.println("=== SUBMIT FEEDBACK DEBUG ===");
            System.out.println("User Email: " + userEmail);
            System.out.println("User Name: " + userName);
            System.out.println("Feedback Text: " + feedbackText);
            System.out.println("Rating: " + rating);
            System.out.println("Feedback Type: " + feedbackType);
            
            conn = getConnection();
            System.out.println("Database connection successful");
            
            // Ensure feedback table exists
            boolean tableCreated = createFeedbackTable();
            System.out.println("Feedback table created/exists: " + tableCreated);
            
            String query = "INSERT INTO feedback (user_email, user_name, feedback_text, rating, feedback_type) VALUES (?, ?, ?, ?, ?)";
            System.out.println("SQL Query: " + query);
            
            stmt = conn.prepareStatement(query);
            stmt.setString(1, userEmail);
            stmt.setString(2, userName);
            stmt.setString(3, feedbackText);
            stmt.setInt(4, rating);
            stmt.setString(5, feedbackType);
            
            int rows = stmt.executeUpdate();
            System.out.println("Rows affected: " + rows);
            System.out.println("=== SUBMIT FEEDBACK: " + (rows > 0 ? "SUCCESS" : "FAILED") + " ===");
            
            return rows > 0;
        } catch (Exception e) {
            System.out.println("=== SUBMIT FEEDBACK: EXCEPTION ===");
            System.out.println("Error submitting feedback: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try { if (stmt != null) stmt.close(); } catch (Exception ignored) {}
            try { if (conn != null) conn.close(); } catch (Exception ignored) {}
        }
    }
    
    // Get user feedback
    public static ResultSet getUserFeedback(String userEmail) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = getConnection();
            String query = "SELECT * FROM feedback WHERE user_email = ? ORDER BY created_at DESC";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, userEmail);
            return stmt.executeQuery();
        } catch (Exception e) {
            System.out.println("Error getting user feedback: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    // Get all feedback (for admin)
    public static ResultSet getAllFeedback() {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = getConnection();
            stmt = conn.createStatement();
            String query = "SELECT * FROM feedback ORDER BY created_at DESC";
            return stmt.executeQuery(query);
        } catch (Exception e) {
            System.out.println("Error getting all feedback: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Get all feedback as List (for staff)
    public static List<Feedback> getAllFeedbackList() {
        List<Feedback> feedbackList = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            String sql = "SELECT * FROM feedback ORDER BY created_at DESC";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Feedback feedback = new Feedback();
                feedback.setId(rs.getInt("id"));
                feedback.setUserEmail(rs.getString("user_email"));
                feedback.setUserName(rs.getString("user_name"));
                feedback.setFeedbackText(rs.getString("feedback_text"));
                feedback.setRating(rs.getInt("rating"));
                feedback.setFeedbackType(rs.getString("feedback_type"));
                feedback.setStatus(rs.getString("status"));
                feedback.setCreatedAt(rs.getTimestamp("created_at") != null ? 
                    rs.getTimestamp("created_at").toString() : "");
                feedback.setResponse(rs.getString("response"));
                feedback.setRespondedAt(rs.getTimestamp("responded_at") != null ? 
                    rs.getTimestamp("responded_at").toString() : "");
                feedbackList.add(feedback);
            }
        } catch (Exception e) {
            System.out.println("Error getting all feedback list: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignored) {}
            try { if (stmt != null) stmt.close(); } catch (Exception ignored) {}
            try { if (conn != null) conn.close(); } catch (Exception ignored) {}
        }
        
        return feedbackList;
    }
    
    // Update feedback status
    public static boolean updateFeedbackStatus(int feedbackId, String status, String response) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = getConnection();
            
            String query;
            if (response != null && !response.isEmpty()) {
                query = "UPDATE feedback SET status = ?, response = ?, responded_at = GETDATE() WHERE id = ?";
            } else {
                query = "UPDATE feedback SET status = ? WHERE id = ?";
            }
            
            stmt = conn.prepareStatement(query);
            stmt.setString(1, status);
            
            if (response != null && !response.isEmpty()) {
                stmt.setString(2, response);
                stmt.setInt(3, feedbackId);
            } else {
                stmt.setInt(2, feedbackId);
            }
            
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (Exception e) {
            System.out.println("Error updating feedback status: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try { if (stmt != null) stmt.close(); } catch (Exception ignored) {}
            try { if (conn != null) conn.close(); } catch (Exception ignored) {}
        }
    }

    // ==================== SERVICE MANAGEMENT ====================

    // Add new service
    public static boolean addService(Service service) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = getConnection();
            String sql = "INSERT INTO services (category, service_name, price, duration_minutes, details) VALUES (?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, service.getCategory());
            stmt.setString(2, service.getServiceName());
            stmt.setDouble(3, service.getPrice());
            stmt.setInt(4, service.getDurationMinutes());
            stmt.setString(5, service.getDetails());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (Exception e) {
            System.out.println("Error adding service: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try { if (stmt != null) stmt.close(); } catch (Exception ignored) {}
            try { if (conn != null) conn.close(); } catch (Exception ignored) {}
        }
    }

} 