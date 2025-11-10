package com.example.mizrahbeauty;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class StaffDashboardActivity extends AppCompatActivity {
    
    private TextView welcomeText;
    private LinearLayout serviceUpdateCard, bookingManagementCard, customerReviewCard, profileCard;
    private Button logoutButton;
    private String userName, userEmail;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_dashboard);
        
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
        serviceUpdateCard = findViewById(R.id.serviceUpdateCard);
        bookingManagementCard = findViewById(R.id.bookingManagementCard);
        customerReviewCard = findViewById(R.id.customerReviewCard);
        profileCard = findViewById(R.id.profileCard);
        logoutButton = findViewById(R.id.logoutButton);
    }
    
    private void setupClickListeners() {
        serviceUpdateCard.setOnClickListener(v -> {
            // Navigate to ServiceUpdateActivity
            Intent intent = new Intent(StaffDashboardActivity.this, ServiceUpdateActivity.class);
            intent.putExtra("USER_NAME", userName);
            intent.putExtra("USER_EMAIL", userEmail);
            startActivity(intent);
        });
        
        bookingManagementCard.setOnClickListener(v -> {
            // Navigate to Staff Booking Activity to see bookings assigned by name
            Intent intent = new Intent(StaffDashboardActivity.this, StaffBookingActivity.class);
            intent.putExtra("STAFF_NAME", userName);
            intent.putExtra("STAFF_EMAIL", userEmail);
            startActivity(intent);
        });
        
        customerReviewCard.setOnClickListener(v -> {
            // Navigate to StaffFeedbackActivity
            Intent intent = new Intent(StaffDashboardActivity.this, StaffFeedbackActivity.class);
            intent.putExtra("USER_NAME", userName);
            intent.putExtra("USER_EMAIL", userEmail);
            startActivity(intent);
        });
        
        profileCard.setOnClickListener(v -> {
            // Navigate to ProfileActivity
            Intent intent = new Intent(StaffDashboardActivity.this, ProfileActivity.class);
            intent.putExtra("USER_NAME", userName);
            intent.putExtra("USER_EMAIL", userEmail);
            startActivity(intent);
        });
        
        logoutButton.setOnClickListener(v -> {
            // Return to login
            Intent intent = new Intent(StaffDashboardActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
    
    private void displayWelcomeMessage() {
        welcomeText.setText("Welcome!\nStaff Dashboard");
    }
    
    @Override
    public void onBackPressed() {
        // Prevent going back to login
        Toast.makeText(this, "Please use logout button", Toast.LENGTH_SHORT).show();
    }
} 