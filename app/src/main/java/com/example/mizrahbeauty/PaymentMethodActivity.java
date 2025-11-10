package com.example.mizrahbeauty;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PaymentMethodActivity extends AppCompatActivity {
    
    private ImageView backButton;
    private TextView serviceNameText, staffNameText, appointmentDateText, totalAmountText;
    private Button proceedPaymentButton;
    
    private String serviceName, staffName, appointmentDateTime, userEmail;
    private double servicePrice;
    private int serviceId, bookingId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_method);
        
        // Get data from intent
        getIntentData();
        
        initializeViews();
        setupClickListeners();
        displayPaymentSummary();
    }
    
    private void getIntentData() {
        Intent intent = getIntent();
        serviceName = intent.getStringExtra("SERVICE_NAME");
        staffName = intent.getStringExtra("STAFF_NAME");
        appointmentDateTime = intent.getStringExtra("APPOINTMENT_DATETIME");
        userEmail = intent.getStringExtra("USER_EMAIL");
        servicePrice = intent.getDoubleExtra("SERVICE_PRICE", 0.0);
        serviceId = intent.getIntExtra("SERVICE_ID", 0);
        bookingId = intent.getIntExtra("BOOKING_ID", 0);
    }
    
    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        serviceNameText = findViewById(R.id.serviceNameText);
        staffNameText = findViewById(R.id.staffNameText);
        appointmentDateText = findViewById(R.id.appointmentDateText);
        totalAmountText = findViewById(R.id.totalAmountText);
        proceedPaymentButton = findViewById(R.id.proceedPaymentButton);
    }
    
    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());
        
        proceedPaymentButton.setOnClickListener(v -> {
            proceedToPayment();
        });
    }
    
    
    private void displayPaymentSummary() {
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
        
        // Set total amount
        totalAmountText.setText(String.format(Locale.getDefault(), "RM %.2f", servicePrice));
    }
    
    private void proceedToPayment() {
        // Navigate to booking confirmation page
        Intent confirmationIntent = new Intent(PaymentMethodActivity.this, BookingConfirmationActivity.class);
        confirmationIntent.putExtra("SERVICE_NAME", serviceName);
        confirmationIntent.putExtra("STAFF_NAME", staffName);
        confirmationIntent.putExtra("APPOINTMENT_DATETIME", appointmentDateTime);
        confirmationIntent.putExtra("USER_EMAIL", userEmail);
        confirmationIntent.putExtra("SERVICE_PRICE", servicePrice);
        confirmationIntent.putExtra("SERVICE_ID", serviceId);
        confirmationIntent.putExtra("BOOKING_ID", bookingId);
        startActivity(confirmationIntent);
        finish();
    }
}
