package com.example.mizrahbeauty;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AdminDashboardActivity extends AppCompatActivity {
    
    private TextView welcomeText;
    private LinearLayout serviceManagementCard, staffRegistrationCard, staffStatusCard, profileCard, staffManagementCard;
    private Button logoutButton;
    private String userName, userEmail;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);
        
        // Get user info from intent
        Intent intent = getIntent();
        userName = intent.getStringExtra("USER_NAME");
        userEmail = intent.getStringExtra("USER_EMAIL");
        
        initializeViews();
        setupClickListeners();
        displayWelcomeMessage();
    }
    
    private void initializeViews() {
        welcomeText = findViewById(R.id.welcomeText);
        serviceManagementCard = findViewById(R.id.serviceManagementCard);
        staffRegistrationCard = findViewById(R.id.staffRegistrationCard);
        staffStatusCard = findViewById(R.id.staffStatusCard);
        profileCard = findViewById(R.id.profileCard);
        staffManagementCard = findViewById(R.id.staffManagementCard);
        logoutButton = findViewById(R.id.logoutButton);
    }
    
    private void setupClickListeners() {
        serviceManagementCard.setOnClickListener(v -> {
            // Navigate to new admin service dashboard
            Intent intent = new Intent(AdminDashboardActivity.this, AdminServiceDashboardActivity.class);
            intent.putExtra("USER_EMAIL", userEmail);
            intent.putExtra("USER_NAME", userName);
            startActivity(intent);
        });
        
        staffRegistrationCard.setOnClickListener(v -> {
            // Navigate to staff registration
            Intent intent = new Intent(AdminDashboardActivity.this, StaffRegistrationActivity.class);
            startActivity(intent);
        });
        
        staffStatusCard.setOnClickListener(v -> {
            // Navigate to staff status preview
            Intent intent = new Intent(AdminDashboardActivity.this, StaffStatusPreviewActivity.class);
            startActivity(intent);
        });
        
        staffManagementCard.setOnClickListener(v -> {
            // Navigate to staff management
            Intent intent = new Intent(AdminDashboardActivity.this, StaffManagementActivity.class);
            startActivity(intent);
        });
        
        profileCard.setOnClickListener(v -> {
            // Navigate to profile
            Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show();
            // Intent to ProfileActivity
            Intent intent = new Intent(AdminDashboardActivity.this, ProfileActivity.class);
            intent.putExtra("USER_NAME", userName);
            intent.putExtra("USER_EMAIL", userEmail);
            startActivity(intent);
        });
        
        logoutButton.setOnClickListener(v -> {
            // Return to login
            Intent intent = new Intent(AdminDashboardActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
    
    private void displayWelcomeMessage() {
        welcomeText.setText("Welcome!\nAdmin Dashboard");
    }
    
    @Override
    public void onBackPressed() {
        // Prevent going back to login
        Toast.makeText(this, "Please use logout button", Toast.LENGTH_SHORT).show();
    }
} 