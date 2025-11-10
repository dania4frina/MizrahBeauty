# MizrahBeauty Admin User Setup Guide

## Overview
This guide explains how to set up and use admin functionality in your MizrahBeauty Android app with a simplified users table structure.

## Prerequisites
- SQL Server database running (dania database)
- Android Studio project with database connection configured
- JTDS JDBC driver dependency added to your project

## Database Setup

### Step 1: Add Admin Column to Database
Run the `add_admin_column_simple.sql` script in your SQL Server Management Studio or any SQL client:

```sql
-- This will add the is_admin column to your users table
USE dania;
GO

ALTER TABLE users ADD is_admin BIT DEFAULT 0;
GO

-- Create a new admin user if none exists
INSERT INTO users (name, email, password, is_admin) 
VALUES ('Admin User', 'admin@mizrahbeauty.com', 'admin123', 1);
GO
```

### Step 2: Verify Database Structure
Your users table should now have this structure:
- `id` - Primary key (auto-increment)
- `name` - User's full name
- `email` - User email (unique)
- `password` - User password
- `is_admin` - Admin privileges (NEW!)

## Android Code Setup

### Step 1: Updated ConnectionClass.java
The `ConnectionClass.java` has been enhanced with admin methods for your simple table structure:
- `createAdminUser(name, email, password)` - Create new admin users
- `isAdminUser(email)` - Check if user is admin
- `promoteToAdmin(email)` - Promote user to admin
- `demoteFromAdmin(email)` - Remove admin privileges
- `getAllAdminUsers()` - List all admin users
- `getUserDetails(email)` - Get detailed user information
- `updateUserProfile(email, name)` - Update user profile
- `changePassword(email, oldPassword, newPassword)` - Change user password
- `getAllUsers()` - List all users for admin management

### Step 2: AdminHelper.java
A helper class that demonstrates how to use admin functionality:
- Wraps database calls with proper error handling
- Provides logging for debugging
- Includes example usage methods

## Usage Examples

### Creating an Admin User
```java
// In your Android activity or service
boolean success = AdminHelper.createAdmin(
    "newadmin@mizrahbeauty.com",
    "securepassword123",
    "John Admin"
);

if (success) {
    Toast.makeText(this, "Admin user created successfully!", Toast.LENGTH_SHORT).show();
} else {
    Toast.makeText(this, "Failed to create admin user", Toast.LENGTH_SHORT).show();
}
```

### Checking Admin Status
```java
boolean isAdmin = AdminHelper.isUserAdmin("user@example.com");
if (isAdmin) {
    // Show admin features
    showAdminDashboard();
} else {
    // Show regular user features
    showUserDashboard();
}
```

### Promoting a User to Admin
```java
boolean promoted = AdminHelper.promoteUserToAdmin("regularuser@example.com");
if (promoted) {
    Log.i("Admin", "User promoted to admin successfully");
}
```

### Listing All Admin Users
```java
AdminHelper.listAllAdminUsers(); // Results will be logged
```

### Listing All Users (Admin Management)
```java
AdminHelper.listAllUsers(); // Shows all users with admin status
```

## Default Admin Users

After running the database script, you'll have this admin user:

**admin@mizrahbeauty.com**
- Password: admin123
- Name: Admin User

## Security Considerations

1. **Password Security**: In production, never store plain text passwords. Use hashing (BCrypt, SHA-256).
2. **Admin Access Control**: Implement proper authentication before allowing admin operations.
3. **Input Validation**: Always validate user input before database operations.
4. **Connection Security**: Use SSL/TLS for database connections in production.

## Testing Admin Features

You can test all admin functionality using the `AdminHelper.demonstrateAdminFeatures()` method:

```java
// Run this in a background thread
new Thread(() -> {
    AdminHelper.demonstrateAdminFeatures();
}).start();
```

## Troubleshooting

### Common Issues:

1. **Database Connection Error**
   - Check if SQL Server is running
   - Verify connection string in ConnectionClass.java
   - Ensure JTDS driver is in your project dependencies

2. **Column Not Found Error**
   - Make sure you ran the `add_admin_column_simple.sql` script
   - Verify the `is_admin` column exists in your users table

3. **Permission Denied**
   - Ensure your database user (sa) has ALTER TABLE permissions
   - Check if the users table exists and is accessible

### Debug Tips:
- Check Android Logcat for detailed error messages
- Use `AdminHelper` methods which include comprehensive logging
- Verify database operations in SQL Server Management Studio

## Next Steps

1. **UI Implementation**: Create admin screens in your Android app
2. **Role-Based Access**: Implement different UI based on admin status
3. **Admin Dashboard**: Build admin-specific features (user management, reports, etc.)
4. **Audit Logging**: Track admin actions for security purposes

## Support

If you encounter issues:
1. Check the Android Logcat for error messages
2. Verify database connectivity
3. Ensure all SQL scripts have been executed
4. Check that the `is_admin` column exists in your users table 