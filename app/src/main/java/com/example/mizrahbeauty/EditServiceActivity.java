package com.example.mizrahbeauty;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mizrahbeauty.models.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EditServiceActivity extends AppCompatActivity {
    private String adminEmail, adminName;
    private int serviceId;
    private TextView titleText;
    private ImageView backButton;
    private EditText serviceNameEditText, priceEditText, durationEditText, detailsEditText, categoryEditText;
    private Button saveButton, deleteButton;
    private ExecutorService executorService;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_service);

        // Get data from intent
        adminEmail = getIntent().getStringExtra("USER_EMAIL");
        adminName = getIntent().getStringExtra("USER_NAME");
        serviceId = getIntent().getIntExtra("SERVICE_ID", 0);

        initializeViews();
        setupClickListeners();
        loadServiceData();
    }

    private void initializeViews() {
        titleText = findViewById(R.id.titleText);
        backButton = findViewById(R.id.backButton);
        serviceNameEditText = findViewById(R.id.serviceNameEditText);
        priceEditText = findViewById(R.id.priceEditText);
        durationEditText = findViewById(R.id.durationEditText);
        detailsEditText = findViewById(R.id.detailsEditText);
        categoryEditText = findViewById(R.id.categoryEditText);
        saveButton = findViewById(R.id.saveButton);
        deleteButton = findViewById(R.id.deleteButton);

        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        // Set title
        titleText.setText("Edit Service");
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());

        saveButton.setOnClickListener(v -> updateService());

        deleteButton.setOnClickListener(v -> {
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
            builder.setTitle("Delete Service")
                    .setMessage("Are you sure you want to delete this service?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        deleteService();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }


    private void loadServiceData() {
        // Pre-fill form with existing data
        String serviceName = getIntent().getStringExtra("SERVICE_NAME");
        double servicePrice = getIntent().getDoubleExtra("SERVICE_PRICE", 0.0);
        int serviceDuration = getIntent().getIntExtra("SERVICE_DURATION", 0);
        String serviceDetails = getIntent().getStringExtra("SERVICE_DETAILS");
        String serviceCategory = getIntent().getStringExtra("SERVICE_CATEGORY");

        if (serviceName != null) {
            serviceNameEditText.setText(serviceName);
        }
        if (servicePrice > 0) {
            priceEditText.setText(String.valueOf(servicePrice));
        }
        if (serviceDuration > 0) {
            durationEditText.setText(String.valueOf(serviceDuration));
        }
        if (serviceDetails != null) {
            detailsEditText.setText(serviceDetails);
        }
        if (serviceCategory != null) {
            categoryEditText.setText(serviceCategory);
        }
    }

    private void updateService() {
        String serviceName = serviceNameEditText.getText().toString().trim();
        String priceText = priceEditText.getText().toString().trim();
        String durationText = durationEditText.getText().toString().trim();
        String details = detailsEditText.getText().toString().trim();
        String category = categoryEditText.getText().toString().trim();

        // Validation
        if (serviceName.isEmpty()) {
            Toast.makeText(this, "Please enter service name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (priceText.isEmpty()) {
            Toast.makeText(this, "Please enter price", Toast.LENGTH_SHORT).show();
            return;
        }

        if (durationText.isEmpty()) {
            Toast.makeText(this, "Please enter duration", Toast.LENGTH_SHORT).show();
            return;
        }

        if (category.isEmpty()) {
            Toast.makeText(this, "Please enter category", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double price = Double.parseDouble(priceText);
            int duration = Integer.parseInt(durationText);

            if (price <= 0) {
                Toast.makeText(this, "Harga mesti lebih daripada 0", Toast.LENGTH_SHORT).show();
                return;
            }

            if (duration <= 0) {
                Toast.makeText(this, "Tempoh masa mesti lebih daripada 0", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update service in database
            executorService.execute(() -> {
                boolean success = ConnectionClass.updateService(serviceId, serviceName, category, price, duration, details);
                mainHandler.post(() -> {
                    if (success) {
                        Toast.makeText(this, "Service updated successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to update service", Toast.LENGTH_SHORT).show();
                    }
                });
            });

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteService() {
        executorService.execute(() -> {
            boolean success = ConnectionClass.deleteService(serviceId);
            mainHandler.post(() -> {
                if (success) {
                    Toast.makeText(this, "Service deleted successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Failed to delete service", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}