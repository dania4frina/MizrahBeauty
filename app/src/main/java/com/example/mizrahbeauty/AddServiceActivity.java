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

public class AddServiceActivity extends AppCompatActivity {
    private String adminEmail, adminName, preSelectedCategory;
    private TextView titleText;
    private ImageView backButton;
    private EditText serviceNameEditText, priceEditText, durationEditText, detailsEditText, categoryEditText;
    private TextView categoryTextView;
    private Button saveButton;
    private ExecutorService executorService;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_service);

        // Get admin info from intent
        adminEmail = getIntent().getStringExtra("USER_EMAIL");
        adminName = getIntent().getStringExtra("USER_NAME");
        preSelectedCategory = getIntent().getStringExtra("CATEGORY_NAME");

        initializeViews();
        setupClickListeners();
        setupCategoryField();
        loadData();
    }

    private void initializeViews() {
        titleText = findViewById(R.id.titleText);
        backButton = findViewById(R.id.backButton);
        serviceNameEditText = findViewById(R.id.serviceNameEditText);
        priceEditText = findViewById(R.id.priceEditText);
        durationEditText = findViewById(R.id.durationEditText);
        detailsEditText = findViewById(R.id.detailsEditText);
        categoryEditText = findViewById(R.id.categoryEditText);
        categoryTextView = findViewById(R.id.categoryTextView);
        saveButton = findViewById(R.id.saveButton);

        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        // Set title
        titleText.setText(getString(R.string.add_service));
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());

        saveButton.setOnClickListener(v -> saveService());
    }

    private void setupCategoryField() {
        if (preSelectedCategory != null && !preSelectedCategory.isEmpty()) {
            // If category is pre-selected, show TextView (non-editable)
            categoryTextView.setText(preSelectedCategory);
            categoryTextView.setVisibility(View.VISIBLE);
            categoryEditText.setVisibility(View.GONE);
        } else {
            // If no category pre-selected, show EditText (editable)
            categoryTextView.setVisibility(View.GONE);
            categoryEditText.setVisibility(View.VISIBLE);
        }
    }

    private void loadData() {
        // Pre-fill form if editing (for future use)
    }

    private void saveService() {
        String serviceName = serviceNameEditText.getText().toString().trim();
        String priceText = priceEditText.getText().toString().trim();
        String durationText = durationEditText.getText().toString().trim();
        String details = detailsEditText.getText().toString().trim();
        String category;
        if (preSelectedCategory != null && !preSelectedCategory.isEmpty()) {
            // Use pre-selected category
            category = preSelectedCategory;
        } else {
            // Get category from EditText
            category = categoryEditText.getText().toString().trim();
        }

        // Validation
        if (serviceName.isEmpty()) {
            Toast.makeText(this, getString(R.string.please_enter_service_name), Toast.LENGTH_SHORT).show();
            return;
        }

        if (priceText.isEmpty()) {
            Toast.makeText(this, getString(R.string.please_enter_price), Toast.LENGTH_SHORT).show();
            return;
        }

        if (durationText.isEmpty()) {
            Toast.makeText(this, getString(R.string.please_enter_duration), Toast.LENGTH_SHORT).show();
            return;
        }

        if (category.isEmpty()) {
            Toast.makeText(this, getString(R.string.please_enter_category), Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double price = Double.parseDouble(priceText);
            int duration = Integer.parseInt(durationText);

            if (price <= 0) {
                Toast.makeText(this, getString(R.string.price_must_be_positive), Toast.LENGTH_SHORT).show();
                return;
            }

            if (duration <= 0) {
                Toast.makeText(this, getString(R.string.duration_must_be_positive), Toast.LENGTH_SHORT).show();
                return;
            }

            // Create service object
            Service service = new Service();
            service.setServiceName(serviceName);
            service.setCategory(category);
            service.setPrice(price);
            service.setDurationMinutes(duration);
            service.setDetails(details);

            // Save to database
            executorService.execute(() -> {
                boolean success = ConnectionClass.addService(service);
                mainHandler.post(() -> {
                    if (success) {
                        Toast.makeText(this, getString(R.string.service_added_successfully), Toast.LENGTH_SHORT).show();
                        clearForm();
                    } else {
                        Toast.makeText(this, getString(R.string.failed_to_add_service), Toast.LENGTH_SHORT).show();
                    }
                });
            });

        } catch (NumberFormatException e) {
            Toast.makeText(this, getString(R.string.please_enter_valid_number), Toast.LENGTH_SHORT).show();
        }
    }

    private void clearForm() {
        serviceNameEditText.setText("");
        priceEditText.setText("");
        durationEditText.setText("");
        detailsEditText.setText("");
        if (categoryEditText.getVisibility() == View.VISIBLE) {
            categoryEditText.setText("");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}