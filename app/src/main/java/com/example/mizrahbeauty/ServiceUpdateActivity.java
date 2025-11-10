package com.example.mizrahbeauty;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.sql.ResultSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServiceUpdateActivity extends AppCompatActivity {
    
    private TextView titleText, statusText;
    private RadioGroup availabilityRadioGroup;
    private RadioButton availableRadioButton, notAvailableRadioButton;
    private Button updateButton;
    private ImageView backButton;
    private ExecutorService executorService;
    private String userEmail;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_update);
        
        // Get user info from intent
        Intent intent = getIntent();
        userEmail = intent.getStringExtra("USER_EMAIL");
        
        executorService = Executors.newSingleThreadExecutor();
        initializeViews();
        setupClickListeners();
        loadCurrentServiceStatus();
    }
    
    private void initializeViews() {
        titleText = findViewById(R.id.titleText);
        statusText = findViewById(R.id.statusText);
        availabilityRadioGroup = findViewById(R.id.availabilityRadioGroup);
        availableRadioButton = findViewById(R.id.availableRadioButton);
        notAvailableRadioButton = findViewById(R.id.notAvailableRadioButton);
        updateButton = findViewById(R.id.updateButton);
        backButton = findViewById(R.id.backButton);
    }
    
    private void setupClickListeners() {
        updateButton.setOnClickListener(v -> updateServiceStatus());
        
        backButton.setOnClickListener(v -> goBackToDashboard());
        
        // Listen for availability changes
        availabilityRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.availableRadioButton) {
                statusText.setText("Status: Available - Able to handle a Service");
                statusText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else if (checkedId == R.id.notAvailableRadioButton) {
                statusText.setText("Status: Not Available - Not appear and not able to handle a Service");
                statusText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }
        });
    }
    
    private void loadCurrentServiceStatus() {
        // Load current service status from database
        executorService.execute(() -> {
            try {
                ResultSet rs = ConnectionClass.getStaffServiceStatus(userEmail);
                boolean isAvailable = true; // Default to available
                
                if (rs != null && rs.next()) {
                    try {
                        isAvailable = rs.getBoolean("is_available");
                    } catch (Exception ex) {
                        // Columns might not exist, use defaults
                        isAvailable = true;
                    }
                    rs.close();
                }
                
                final boolean finalIsAvailable = isAvailable;
                
                runOnUiThread(() -> {
                    if (finalIsAvailable) {
                        availableRadioButton.setChecked(true);
                        statusText.setText("Status: Available - Able to handle a Service");
                        statusText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    } else {
                        notAvailableRadioButton.setChecked(true);
                        statusText.setText("Status: Not Available - Not appear and not able to handle a Service");
                        statusText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    }
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    // Set default values if error occurs
                    availableRadioButton.setChecked(true);
                    Toast.makeText(ServiceUpdateActivity.this, "Error loading current status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void updateServiceStatus() {
        boolean isAvailable = availableRadioButton.isChecked();
        
        // Show loading
        updateButton.setEnabled(false);
        updateButton.setText("Updating...");
        
        executorService.execute(() -> {
            try {
                // Update service status in database (without service details)
                boolean success = ConnectionClass.updateStaffServiceStatus(userEmail, "", isAvailable);
                
                runOnUiThread(() -> {
                    updateButton.setEnabled(true);
                    updateButton.setText("Update Service");
                    
                    if (success) {
                        Toast.makeText(ServiceUpdateActivity.this, "Service status updated successfully!", Toast.LENGTH_SHORT).show();
                        // Update status text to reflect current state
                        if (isAvailable) {
                            statusText.setText("Status: Available - Able to handle a Service");
                            statusText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                        } else {
                            statusText.setText("Status: Not Available - Not appear and not able to handle a Service");
                            statusText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                        }
                    } else {
                        Toast.makeText(ServiceUpdateActivity.this, "Update failed. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    updateButton.setEnabled(true);
                    updateButton.setText("Update Service");
                    Toast.makeText(ServiceUpdateActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void clearFields() {
        availableRadioButton.setChecked(true);
    }
    
    private void goBackToDashboard() {
        Intent intent = new Intent(ServiceUpdateActivity.this, StaffDashboardActivity.class);
        intent.putExtra("USER_EMAIL", userEmail);
        intent.putExtra("USER_ROLE", "staff");
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