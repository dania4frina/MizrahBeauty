package com.example.mizrahbeauty;

import android.util.Log;
import java.sql.ResultSet;

/**
 * Helper class for admin operations
 * This class demonstrates how to use the admin functionality from ConnectionClass
 */
public class AdminHelper {
    private static final String TAG = "AdminHelper";

    /**
     * Create a new admin user
     */
    public static boolean createAdmin(String email, String password, String name) {
        try {
            boolean success = ConnectionClass.createAdminUser(email, password, name);
            if (success) {
                Log.i(TAG, "Admin user created successfully: " + email);
            } else {
                Log.e(TAG, "Failed to create admin user: " + email);
            }
            return success;
        } catch (Exception e) {
            Log.e(TAG, "Error creating admin user: " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if a user is an admin
     */
    public static boolean isUserAdmin(String email) {
        try {
            boolean isAdmin = ConnectionClass.isAdminUser(email);
            Log.i(TAG, "User " + email + " admin status: " + isAdmin);
            return isAdmin;
        } catch (Exception e) {
            Log.e(TAG, "Error checking admin status: " + e.getMessage());
            return false;
        }
    }

    /**
     * Promote a regular user to admin
     */
    public static boolean promoteUserToAdmin(String email) {
        try {
            boolean success = ConnectionClass.promoteToAdmin(email);
            if (success) {
                Log.i(TAG, "User promoted to admin successfully: " + email);
            } else {
                Log.e(TAG, "Failed to promote user to admin: " + email);
            }
            return success;
        } catch (Exception e) {
            Log.e(TAG, "Error promoting user to admin: " + e.getMessage());
            return false;
        }
    }

    /**
     * Demote an admin user to regular user
     */
    public static boolean demoteAdminToUser(String email) {
        try {
            boolean success = ConnectionClass.demoteFromAdmin(email);
            if (success) {
                Log.i(TAG, "Admin demoted to user successfully: " + email);
            } else {
                Log.e(TAG, "Failed to demote admin to user: " + email);
            }
            return success;
        } catch (Exception e) {
            Log.e(TAG, "Error demoting admin to user: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get all admin users
     */
    public static void listAllAdminUsers() {
        try {
            ResultSet rs = ConnectionClass.getAllAdminUsers();
            if (rs != null) {
                Log.i(TAG, "=== All Admin Users ===");
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    String email = rs.getString("email");
                    
                    Log.i(TAG, String.format("Admin ID: %d - Name: %s - Email: %s", id, name, email));
                }
                rs.close();
            } else {
                Log.e(TAG, "Failed to get admin users");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error listing admin users: " + e.getMessage());
        }
    }

    /**
     * Get user details
     */
    public static void getUserDetails(String email) {
        try {
            ResultSet rs = ConnectionClass.getUserDetails(email);
            if (rs != null && rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                boolean isAdmin = rs.getBoolean("is_admin");
                
                Log.i(TAG, "=== User Details ===");
                Log.i(TAG, "ID: " + id);
                Log.i(TAG, "Name: " + name);
                Log.i(TAG, "Email: " + email);
                Log.i(TAG, "Admin: " + isAdmin);
                
                rs.close();
            } else {
                Log.e(TAG, "User not found: " + email);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting user details: " + e.getMessage());
        }
    }

    /**
     * Update user profile
     */
    public static boolean updateProfile(String email, String name) {
        try {
            boolean success = ConnectionClass.updateUserProfile(email, name);
            if (success) {
                Log.i(TAG, "Profile updated successfully for: " + email);
            } else {
                Log.e(TAG, "Failed to update profile for: " + email);
            }
            return success;
        } catch (Exception e) {
            Log.e(TAG, "Error updating profile: " + e.getMessage());
            return false;
        }
    }

    /**
     * Change user password
     */
    public static boolean changePassword(String email, String oldPassword, String newPassword) {
        try {
            boolean success = ConnectionClass.changePassword(email, oldPassword, newPassword);
            if (success) {
                Log.i(TAG, "Password changed successfully for: " + email);
            } else {
                Log.e(TAG, "Failed to change password for: " + email);
            }
            return success;
        } catch (Exception e) {
            Log.e(TAG, "Error changing password: " + e.getMessage());
            return false;
        }
    }

    /**
     * List all users (for admin management)
     */
    public static void listAllUsers() {
        try {
            ResultSet rs = ConnectionClass.getAllUsers();
            if (rs != null) {
                Log.i(TAG, "=== All Users ===");
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    String email = rs.getString("email");
                    boolean isAdmin = rs.getBoolean("is_admin");
                    
                    String userType = isAdmin ? "ADMIN" : "USER";
                    Log.i(TAG, String.format("ID: %d - %s - Name: %s - Email: %s", id, userType, name, email));
                }
                rs.close();
            } else {
                Log.e(TAG, "Failed to get users");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error listing users: " + e.getMessage());
        }
    }

    /**
     * Example usage of admin functionality
     */
    public static void demonstrateAdminFeatures() {
        Log.i(TAG, "=== Demonstrating Admin Features ===");
        
        // 1. Create a new admin user
        createAdmin("newadmin@mizrahbeauty.com", "adminpass123", "New Admin");
        
        // 2. Check if user is admin
        isUserAdmin("newadmin@mizrahbeauty.com");
        
        // 3. List all admin users
        listAllAdminUsers();
        
        // 4. Get user details
        getUserDetails("newadmin@mizrahbeauty.com");
        
        // 5. Update profile
        updateProfile("newadmin@mizrahbeauty.com", "Updated Admin Name");
        
        // 6. Change password
        changePassword("newadmin@mizrahbeauty.com", "adminpass123", "newpass456");
        
        // 7. List all users
        listAllUsers();
        
        Log.i(TAG, "=== Admin Features Demo Complete ===");
    }
} 