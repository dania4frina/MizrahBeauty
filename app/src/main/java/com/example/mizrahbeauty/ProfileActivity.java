package com.example.mizrahbeauty;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfileActivity extends AppCompatActivity {
    
    private EditText nameEditText, emailEditText, phoneEditText;
    private Button updateButton, logoutButton;
    private ImageView backButton;
    private ExecutorService executorService;
    private String userEmail, userRole;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        
        // Get user info from intent
        Intent intent = getIntent();
        userEmail = intent.getStringExtra("USER_EMAIL");
        userRole = intent.getStringExtra("USER_ROLE");
        
        executorService = Executors.newSingleThreadExecutor();
        initializeViews();
        setupClickListeners();
        loadUserProfile();
    }
    
    private void initializeViews() {
        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        updateButton = findViewById(R.id.updateButton);
        backButton = findViewById(R.id.backButton);
        logoutButton = findViewById(R.id.logoutButton);
    }
    
    private void setupClickListeners() {
        updateButton.setOnClickListener(v -> updateProfile());
        
        backButton.setOnClickListener(v -> goBackToDashboard());
        
        logoutButton.setOnClickListener(v -> logout());
    }
    
    private void loadUserProfile() {
        // Set email (read-only)
        emailEditText.setText(userEmail);
        
        // Load user profile data including phone number
        executorService.execute(() -> {
            try {
                // Get user details from database
                String[] userDetails = ConnectionClass.getUserProfileDetails(userEmail);
                
                runOnUiThread(() -> {
                    if (userDetails != null && userDetails.length >= 3) {
                        String fullName = userDetails[0] != null ? userDetails[0] : "";
                        String emailFromDb = userDetails[1] != null ? userDetails[1] : "";
                        String phone = userDetails[2] != null ? userDetails[2] : "";

                        if (!fullName.isEmpty()) {
                            nameEditText.setText(fullName);
                        } else {
                            nameEditText.setText("");
                            nameEditText.setHint("Enter your full name");
                        }

                        if (!emailFromDb.isEmpty()) {
                            emailEditText.setText(emailFromDb);
                        } else {
                            emailEditText.setText(userEmail);
                        }

                        phoneEditText.setText(phone);
                    } else {
                        // Set default hints if no data found
                        nameEditText.setHint("Enter your full name");
                        emailEditText.setText(userEmail);
                        phoneEditText.setHint("Enter your phone number");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    nameEditText.setHint("Enter your full name");
                    phoneEditText.setHint("Enter your phone number");
                });
            }
        });
    }
    
    private void updateProfile() {
        String phone = phoneEditText.getText().toString().trim();
        // Validate inputs
        if (phone.isEmpty()) {
            phoneEditText.setError("Phone number is required");
            return;
        }
        
        // Basic phone number validation
        if (!isValidPhoneNumber(phone)) {
            phoneEditText.setError("Please enter a valid phone number");
            return;
        }
        
        // Show loading
        updateButton.setEnabled(false);
        updateButton.setText("Updating...");
        
        executorService.execute(() -> {
            try {
                boolean success = ConnectionClass.updateUserPhone(userEmail, phone);
                
                final boolean finalSuccess = success;
                runOnUiThread(() -> {
                    updateButton.setEnabled(true);
                    updateButton.setText("Update Profile");
                    
                    if (finalSuccess) {
                        Toast.makeText(ProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ProfileActivity.this, "Update failed. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    updateButton.setEnabled(true);
                    updateButton.setText("Update Profile");
                    Toast.makeText(ProfileActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private boolean isValidPhoneNumber(String phone) {
        // Basic phone number validation - allows digits, spaces, hyphens, parentheses, and plus sign
        // Should be at least 7 digits and at most 15 digits
        String cleanPhone = phone.replaceAll("[^0-9]", "");
        return cleanPhone.length() >= 7 && cleanPhone.length() <= 15;
    }
    
    private void goBackToDashboard() {
        // Navigate back to appropriate dashboard based on user role
        Intent intent;
        
        switch (userRole.toLowerCase()) {
            case "admin":
                intent = new Intent(ProfileActivity.this, AdminDashboardActivity.class);
                break;
            case "staff":
                intent = new Intent(ProfileActivity.this, StaffDashboardActivity.class);
                break;
            case "user":
            default:
                intent = new Intent(ProfileActivity.this, UserDashboardActivity.class);
                break;
        }
        
        // Pass user information back to dashboard
        intent.putExtra("USER_EMAIL", userEmail);
        intent.putExtra("USER_ROLE", userRole);
        
        startActivity(intent);
        finish();
    }
    
    private void logout() {
        // Return to login screen
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
} 