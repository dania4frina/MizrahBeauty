package com.example.mizrahbeauty;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.sql.ResultSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {
    
    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView registerPrompt, forgotPasswordText;
    private ProgressBar progressBar;
    private ExecutorService executorService;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        executorService = Executors.newSingleThreadExecutor();
        initializeViews();
        setupClickListeners();
    }
    
    private void initializeViews() {
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerPrompt = findViewById(R.id.registerPrompt);
        forgotPasswordText = findViewById(R.id.forgotPasswordText);
        // new fix as date on 09 nov 2025 -7.01PM
        emailEditText.setFocusable(true);
        emailEditText.setFocusableInTouchMode(true);
        emailEditText.setClickable(true);
        passwordEditText.setFocusable(true);
        passwordEditText.setFocusableInTouchMode(true);
        passwordEditText.setClickable(true);
        emailEditText.requestFocus();
        
        // Create a simple progress bar since it's not in the layout
        progressBar = new ProgressBar(this);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.GONE);
    }
    
    private void setupClickListeners() {
        loginButton.setOnClickListener(v -> performLogin());
        
        registerPrompt.setOnClickListener(v -> {
            // Navigate to registration
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        forgotPasswordText.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
    }
    
    private void performLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        
        showProgress(true);
        
        executorService.execute(() -> {
            try {
                ResultSet resultSet = ConnectionClass.validateUser(email, password);
                
                runOnUiThread(() -> {
                    showProgress(false);
                    
                    try {
                        if (resultSet != null && resultSet.next()) {
                            // Get user information from result set
                            String userName = email; // Use email as fallback for name
                            String userRole = "user"; // Default to regular user
                            
                            // Get role from the result set
                            String role = resultSet.getString("role");
                            if (role != null && !role.isEmpty()) {
                                userRole = role;
                            }
                            
                            // Try to get name if the column exists (for future use)
                            try {
                                String name = resultSet.getString("name");
                                if (name != null && !name.isEmpty()) {
                                    userName = name;
                                }
                            } catch (Exception e) {
                                // name column doesn't exist, use email as fallback
                            }
                            
                            // Navigate based on user role
                            navigateToDashboard(userRole, userName, email);
                            
                        } else {
                            Toast.makeText(LoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                        }
                    } catch (java.sql.SQLException e) {
                        e.printStackTrace();
                        Toast.makeText(LoginActivity.this, "Database error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    showProgress(false);
                    Toast.makeText(LoginActivity.this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void navigateToDashboard(String userRole, String userName, String userEmail) {
        Intent intent;
        
        switch (userRole.toLowerCase()) {
            case "admin":
                intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
                break;
            case "staff":
                intent = new Intent(LoginActivity.this, StaffDashboardActivity.class);
                break;
            case "user":
            default:
                intent = new Intent(LoginActivity.this, UserDashboardActivity.class);
                break;
        }
        
        // Pass user information to dashboard
        intent.putExtra("USER_NAME", userName);
        intent.putExtra("USER_EMAIL", userEmail);
        intent.putExtra("USER_ROLE", userRole);
        
        startActivity(intent);
        finish(); // Close login activity
    }
    
    private void showProgress(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        loginButton.setEnabled(!show);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
