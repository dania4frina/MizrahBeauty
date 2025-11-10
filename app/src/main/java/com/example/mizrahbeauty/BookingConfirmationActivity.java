package com.example.mizrahbeauty;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;

import com.example.mizrahbeauty.payment.ToyyibPayApi;
import com.example.mizrahbeauty.payment.ToyyibPayClient;
import com.example.mizrahbeauty.payment.ToyyibPaySession;
import com.example.mizrahbeauty.payment.ToyyibPaySessionManager;
import com.example.mizrahbeauty.payment.requests.CreateBillRequest;
import com.example.mizrahbeauty.payment.responses.CreateBillResponse;
import com.example.mizrahbeauty.payment.responses.PaymentStatusResponse;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingConfirmationActivity extends AppCompatActivity {
    
    private ImageView backButton;
    private TextView serviceNameText, staffNameText, appointmentDateText, serviceIdText, totalAmountText, paymentStatusText;
    private Button proceedToToyyibPayButton;
    private TextInputEditText customerNameInput, customerEmailInput, customerPhoneInput;
    
    private String serviceName, staffName, appointmentDateTime, userEmail;
    private String customerName, customerPhone;
    private double servicePrice;
    private int serviceId;
    private boolean isProcessingPayment = false;
    private ToyyibPaySession currentSession;
    
    private static final String TOYYIB_RETURN_URL = "mizrahbeauty://payment/result";
    private static final String TOYYIB_CALLBACK_URL = "https://mizrahbeauty-toyyibpay.onrender.com/toyyibpay/callback";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_confirmation);
        
        initializeViews();
        setupClickListeners();
        handleIncomingIntent(getIntent());
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        android.util.Log.d("ToyyibPay", "onNewIntent called");
        setIntent(intent);
        handleIncomingIntent(intent);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        android.util.Log.d("ToyyibPay", "onResume called - checking for payment updates");
        // Refresh status when returning from payment
        if (currentSession != null && currentSession.getBillCode() != null) {
            android.util.Log.d("ToyyibPay", "Active session found, refreshing status for bill: " + currentSession.getBillCode());
            // Retry status check with delays (ToyyibPay API can be slow to update)
            refreshStatusWithRetry(0);
        }
    }
    
    private void handleIncomingIntent(Intent intent) {
        if (intent == null) {
            android.util.Log.d("ToyyibPay", "handleIncomingIntent: intent is null");
            return;
        }
        
        android.util.Log.d("ToyyibPay", "handleIncomingIntent - Action: " + intent.getAction() + ", Data: " + intent.getData());
        
        boolean fromDeepLink = Intent.ACTION_VIEW.equals(intent.getAction()) && intent.getData() != null;
        
        if (fromDeepLink) {
            android.util.Log.d("ToyyibPay", "Deep link detected! URI: " + intent.getData().toString());
            android.util.Log.d("ToyyibPay", "Query params: " + intent.getData().getQuery());
            
            ToyyibPaySessionManager.updateFromDeepLink(intent.getData());
            currentSession = ToyyibPaySessionManager.getCurrentSession();
            if (currentSession != null) {
                android.util.Log.d("ToyyibPay", "Session updated from deep link. Status: " + currentSession.getLastStatus());
                populateFieldsFromSession(currentSession);
            }
            refreshStatusFromServer();
            
            Toast.makeText(this, "Payment response received", Toast.LENGTH_SHORT).show();
        } else {
            android.util.Log.d("ToyyibPay", "Regular intent (not deep link), clearing session");
            ToyyibPaySessionManager.clearSession();
            populateFieldsFromExtras(intent);
            currentSession = null;
        }
        
        displayBookingDetails();
        populateCustomerInputs();
        updatePaymentStatusText();
    }
    
    private void populateFieldsFromExtras(Intent intent) {
        serviceName = intent.getStringExtra("SERVICE_NAME");
        staffName = intent.getStringExtra("STAFF_NAME");
        appointmentDateTime = intent.getStringExtra("APPOINTMENT_DATETIME");
        userEmail = intent.getStringExtra("USER_EMAIL");
        servicePrice = intent.getDoubleExtra("SERVICE_PRICE", 0.0);
        serviceId = intent.getIntExtra("SERVICE_ID", 0);
        customerName = intent.getStringExtra("CUSTOMER_NAME");
        if (TextUtils.isEmpty(customerName)) {
            customerName = userEmail != null ? userEmail : "Customer";
        }
        customerPhone = intent.getStringExtra("CUSTOMER_PHONE");
        if (customerPhone == null) {
            customerPhone = "";
        }
    }
    
    private void populateFieldsFromSession(ToyyibPaySession session) {
        serviceName = session.getServiceName();
        staffName = session.getStaffName();
        appointmentDateTime = session.getAppointmentDateTime();
        userEmail = session.getCustomerEmail();
        servicePrice = session.getAmount();
        serviceId = session.getServiceId();
        customerName = session.getCustomerName();
        customerPhone = session.getCustomerPhone();
    }

    private void populateCustomerInputs() {
        if (customerNameInput != null) {
            customerNameInput.setText(customerName != null ? customerName : "");
        }
        if (customerEmailInput != null) {
            customerEmailInput.setText(userEmail != null ? userEmail : "");
        }
        if (customerPhoneInput != null) {
            customerPhoneInput.setText(customerPhone != null ? customerPhone : "");
        }
    }
    
    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        serviceNameText = findViewById(R.id.serviceNameText);
        staffNameText = findViewById(R.id.staffNameText);
        appointmentDateText = findViewById(R.id.appointmentDateText);
        serviceIdText = findViewById(R.id.serviceIdText);
        totalAmountText = findViewById(R.id.totalAmountText);
        proceedToToyyibPayButton = findViewById(R.id.proceedToToyyibPayButton);
        paymentStatusText = findViewById(R.id.paymentStatusText);
        customerNameInput = findViewById(R.id.customerNameInput);
        customerEmailInput = findViewById(R.id.customerEmailInput);
        customerPhoneInput = findViewById(R.id.customerPhoneInput);
    }
    
    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());
        
        proceedToToyyibPayButton.setOnClickListener(v -> {
            initiateToyyibPayFlow();
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
    
    private void updatePaymentStatusText() {
        if (paymentStatusText == null) {
            return;
        }
        
        ToyyibPaySession session = ToyyibPaySessionManager.getCurrentSession();
        if (session == null || session.getLastStatus() == null) {
            paymentStatusText.setText("Pending payment - please complete via ToyyibPay.");
        } else {
            String remark = session.getLastRemark();
            String statusMessage = "Status: " + session.getLastStatus();
            if (remark != null && !remark.isEmpty()) {
                statusMessage += "\nDetails: " + remark;
            }
            paymentStatusText.setText(statusMessage);
        }
    }
    
    private void initiateToyyibPayFlow() {
        if (isProcessingPayment) {
            return;
        }
        
        if (servicePrice <= 0) {
            Toast.makeText(this, "Invalid service price.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String inputName = customerNameInput != null ? customerNameInput.getText().toString().trim() : "";
        String inputEmail = customerEmailInput != null ? customerEmailInput.getText().toString().trim() : "";
        String inputPhone = customerPhoneInput != null ? customerPhoneInput.getText().toString().trim() : "";

        if (TextUtils.isEmpty(inputName)) {
            if (customerNameInput != null) {
                customerNameInput.setError("Name is required");
                customerNameInput.requestFocus();
            }
            Toast.makeText(this, "Customer name is required.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(inputEmail) || !Patterns.EMAIL_ADDRESS.matcher(inputEmail).matches()) {
            if (customerEmailInput != null) {
                customerEmailInput.setError("Valid email is required");
                customerEmailInput.requestFocus();
            }
            Toast.makeText(this, "Valid email is required for payment.", Toast.LENGTH_SHORT).show();
            return;
        }

        customerName = inputName;
        userEmail = inputEmail;
        customerPhone = inputPhone;
        
        setProcessingState(true);
        
        String referenceId = generateReferenceId();
        String description = "Booking for " + (serviceName != null ? serviceName : "Service");
        String payerName = customerName;
        
        CreateBillRequest request = new CreateBillRequest(
                servicePrice,
                description,
                payerName,
                userEmail,
                customerPhone,
                referenceId,
                TOYYIB_RETURN_URL,
                TOYYIB_CALLBACK_URL
        );
        
        ToyyibPaySession session = new ToyyibPaySession(
                referenceId,
                serviceName,
                serviceId,
                staffName,
                appointmentDateTime,
                servicePrice,
                userEmail,
                payerName,
                customerPhone
        );
        
        android.util.Log.d("ToyyibPay", "Initiating createBill request...");
        android.util.Log.d("ToyyibPay", "Request details - Amount: " + servicePrice + ", Email: " + userEmail + ", Name: " + payerName);
        
        ToyyibPayApi api = ToyyibPayClient.getApi();
        api.createBill(request).enqueue(new Callback<CreateBillResponse>() {
            @Override
            public void onResponse(Call<CreateBillResponse> call, Response<CreateBillResponse> response) {
                android.util.Log.d("ToyyibPay", "Response received - Code: " + response.code() + ", Success: " + response.isSuccessful());
                setProcessingState(false);
                
                if (!response.isSuccessful() || response.body() == null) {
                    android.util.Log.e("ToyyibPay", "Failed response or null body. Code: " + response.code());
                    Toast.makeText(BookingConfirmationActivity.this, "Failed to create ToyyibPay bill (HTTP " + response.code() + ")", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                CreateBillResponse body = response.body();
                String billCode = body.getBillCode();
                String paymentUrl = body.getPaymentUrl();
                String status = body.getStatus() != null ? body.getStatus() : "CREATED";
                
                android.util.Log.d("ToyyibPay", "Bill created successfully - BillCode: " + billCode + ", PaymentUrl: " + paymentUrl);
                
                session.assignBillDetails(billCode, paymentUrl, status);
                ToyyibPaySessionManager.startSession(session);
                currentSession = session;
                updatePaymentStatusText();
                
                if (paymentUrl != null && !paymentUrl.isEmpty()) {
                    openPaymentUrl(paymentUrl);
                } else {
                    android.util.Log.e("ToyyibPay", "Payment URL is null or empty");
                    Toast.makeText(BookingConfirmationActivity.this, "Invalid payment URL.", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<CreateBillResponse> call, Throwable t) {
                android.util.Log.e("ToyyibPay", "Network failure: " + t.getMessage(), t);
                setProcessingState(false);
                Toast.makeText(BookingConfirmationActivity.this, "ToyyibPay error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void refreshStatusWithRetry(int attemptNumber) {
        final int maxAttempts = 5;
        final long[] delayMillis = {0, 2000, 3000, 5000, 8000}; // 0s, 2s, 3s, 5s, 8s
        
        if (attemptNumber >= maxAttempts) {
            android.util.Log.d("ToyyibPay", "Max retry attempts reached");
            Toast.makeText(this, "Payment status check timed out. Please refresh manually.", Toast.LENGTH_LONG).show();
            return;
        }
        
        android.util.Log.d("ToyyibPay", "Status check attempt " + (attemptNumber + 1) + "/" + maxAttempts);
        
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            refreshStatusFromServer(attemptNumber);
        }, delayMillis[attemptNumber]);
    }
    
    private void refreshStatusFromServer(final int attemptNumber) {
        ToyyibPaySession session = ToyyibPaySessionManager.getCurrentSession();
        if (session == null || session.getBillCode() == null || session.getBillCode().isEmpty()) {
            android.util.Log.d("ToyyibPay", "refreshStatusFromServer: no active session or billCode");
            return;
        }
        
        String billCode = session.getBillCode();
        android.util.Log.d("ToyyibPay", "Fetching payment status for billCode: " + billCode);
        
        ToyyibPayClient.getApi().getPaymentStatus(billCode)
                .enqueue(new Callback<PaymentStatusResponse>() {
                    @Override
                    public void onResponse(Call<PaymentStatusResponse> call, Response<PaymentStatusResponse> response) {
                        android.util.Log.d("ToyyibPay", "Status response - Code: " + response.code() + ", Success: " + response.isSuccessful());
                        
                        if (!response.isSuccessful() || response.body() == null) {
                            android.util.Log.e("ToyyibPay", "Failed to fetch status. Code: " + response.code());
                            refreshStatusWithRetry(attemptNumber + 1);
                            return;
                        }
                        PaymentStatusResponse body = response.body();
                        android.util.Log.d("ToyyibPay", "Payment status: " + body.getStatus() + ", Paid: " + body.isPaid() + ", Remark: " + body.getRemark());
                        
                        ToyyibPaySession updatedSession = ToyyibPaySessionManager.getCurrentSession();
                        if (updatedSession != null) {
                            updatedSession.updateStatus(
                                    body.getStatus(),
                                    body.getRemark(),
                                    body.isPaid()
                            );
                            updatePaymentStatusText();
                            
                            if (body.isPaid()) {
                                android.util.Log.d("ToyyibPay", "Payment confirmed as PAID!");
                                Toast.makeText(BookingConfirmationActivity.this, "Payment confirmed! ✓", Toast.LENGTH_LONG).show();
                            } else if ("UNKNOWN".equals(body.getStatus()) || body.getStatus() == null || body.getStatus().isEmpty()) {
                                // Status not ready yet, retry
                                android.util.Log.d("ToyyibPay", "Status still pending, will retry...");
                                refreshStatusWithRetry(attemptNumber + 1);
                            }
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<PaymentStatusResponse> call, Throwable t) {
                        android.util.Log.e("ToyyibPay", "Status check failed: " + t.getMessage(), t);
                        refreshStatusWithRetry(attemptNumber + 1);
                    }
                });
    }
    
    private String generateReferenceId() {
        return "BOOK-" + (serviceId > 0 ? serviceId : "GEN") + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    private void openPaymentUrl(String url) {
        android.util.Log.d("ToyyibPay", "Opening payment URL: " + url);
        
        try {
            CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder().build();
            customTabsIntent.launchUrl(this, android.net.Uri.parse(url));
            android.util.Log.d("ToyyibPay", "Custom Tab launched successfully");
        } catch (Exception e) {
            android.util.Log.e("ToyyibPay", "Custom Tab failed, using fallback intent: " + e.getMessage());
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url));
            if (browserIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(browserIntent);
            } else {
                android.util.Log.e("ToyyibPay", "No browser available");
                Toast.makeText(this, "No browser available to open ToyyibPay.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void setProcessingState(boolean processing) {
        isProcessingPayment = processing;
        proceedToToyyibPayButton.setEnabled(!processing);
        proceedToToyyibPayButton.setAlpha(processing ? 0.6f : 1f);
        proceedToToyyibPayButton.setText(processing ? "Connecting to ToyyibPay..." : "Proceed to ToyyibPay");
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

