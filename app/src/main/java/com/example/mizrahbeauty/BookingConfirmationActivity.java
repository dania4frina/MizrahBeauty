package com.example.mizrahbeauty;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BookingConfirmationActivity extends AppCompatActivity {
    
    private ImageView backButton;
    private TextView serviceNameText, staffNameText, appointmentDateText, serviceIdText, totalAmountText;
    private Button proceedToToyyibPayButton;
    
    private String serviceName, staffName, appointmentDateTime, userEmail;
    private double servicePrice;
    private int serviceId;
    
    // ToyyibPay website URL - replace with actual URL
    private static final String TOYYIBPAY_URL = "https://toyyibpay.com";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_confirmation);
        
        // Get data from intent
        getIntentData();
        
        initializeViews();
        setupClickListeners();
        displayBookingDetails();
    }
    
    private void getIntentData() {
        Intent intent = getIntent();
        serviceName = intent.getStringExtra("SERVICE_NAME");
        staffName = intent.getStringExtra("STAFF_NAME");
        appointmentDateTime = intent.getStringExtra("APPOINTMENT_DATETIME");
        userEmail = intent.getStringExtra("USER_EMAIL");
        servicePrice = intent.getDoubleExtra("SERVICE_PRICE", 0.0);
        serviceId = intent.getIntExtra("SERVICE_ID", 0);
    }
    
    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        serviceNameText = findViewById(R.id.serviceNameText);
        staffNameText = findViewById(R.id.staffNameText);
        appointmentDateText = findViewById(R.id.appointmentDateText);
        serviceIdText = findViewById(R.id.serviceIdText);
        totalAmountText = findViewById(R.id.totalAmountText);
        proceedToToyyibPayButton = findViewById(R.id.proceedToToyyibPayButton);
    }
    
    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());
        
        proceedToToyyibPayButton.setOnClickListener(v -> {
            openToyyibPayWebsite();
        });
    }
    
    private void displayBookingDetails() {
        // Set service name
        serviceNameText.setText(serviceName != null ? serviceName : "Beauty Service");
        
        // Set staff name
        staffNameText.setText(staffName != null ? staffName : "Not specified");
        
        // Format and set appointment date
        if (appointmentDateTime != null && !appointmentDateTime.isEmpty()) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                Date date = inputFormat.parse(appointmentDateTime);
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
                appointmentDateText.setText(outputFormat.format(date));
            } catch (Exception e) {
                appointmentDateText.setText(appointmentDateTime);
            }
        } else {
            appointmentDateText.setText("Not specified");
        }
        
        // Set service ID
        if (serviceId > 0) {
            serviceIdText.setText(String.valueOf(serviceId));
        } else {
            serviceIdText.setText("N/A");
        }
        
        // Set total amount
        totalAmountText.setText(String.format(Locale.getDefault(), "RM %.2f", servicePrice));
    }
    
    private void openToyyibPayWebsite() {
        try {
            // Create intent to open ToyyibPay website
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(TOYYIBPAY_URL));
            
            // Check if there's a browser available
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, "No browser available to open ToyyibPay", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error opening ToyyibPay website", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void navigateToDashboard() {
        Intent intent = new Intent(BookingConfirmationActivity.this, UserDashboardActivity.class);
        intent.putExtra("USER_EMAIL", userEmail);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
    
    @Override
    public void onBackPressed() {
        // Navigate to dashboard instead of going back
        navigateToDashboard();
    }
}

