package com.example.mizrahbeauty;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StaffRegistrationActivity extends AppCompatActivity {
    
    private EditText emailEditText, passwordEditText, nameEditText, phoneEditText;
    private Button registerButton;
    private ImageView backButton;
    private TextView titleText;
    private ExecutorService executorService;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_registration);
        
        executorService = Executors.newSingleThreadExecutor();
        
        initializeViews();
        setupClickListeners();
    }
    
    private void initializeViews() {
        try {
            backButton = findViewById(R.id.backButton);
            titleText = findViewById(R.id.titleText);
            emailEditText = findViewById(R.id.emailEditText);
            passwordEditText = findViewById(R.id.passwordEditText);
            nameEditText = findViewById(R.id.nameEditText);
            phoneEditText = findViewById(R.id.phoneEditText);
            registerButton = findViewById(R.id.registerButton);
            
            titleText.setText("Staff Registration");
            
            backButton.setOnClickListener(v -> finish());
            
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error initializing views: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void setupClickListeners() {
        registerButton.setOnClickListener(v -> registerStaff());
    }
    
    private void registerStaff() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String name = nameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        
        if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }
        
        executorService.execute(() -> {
            try {
                boolean success = ConnectionClass.registerStaff(email, password, name, phone);
                
                runOnUiThread(() -> {
                    if (success) {
                        Toast.makeText(this, "Staff registered successfully", Toast.LENGTH_SHORT).show();
                        clearFields();
                    } else {
                        Toast.makeText(this, "Failed to register staff", Toast.LENGTH_SHORT).show();
                    }
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void clearFields() {
        emailEditText.setText("");
        passwordEditText.setText("");
        nameEditText.setText("");
        phoneEditText.setText("");
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
