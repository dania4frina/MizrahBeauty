package com.example.mizrahbeauty;

import android.content.Intent;
import android.os.Bundle;
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
        setIntent(intent);
        handleIncomingIntent(intent);
    }
    
    private void handleIncomingIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        
        boolean fromDeepLink = Intent.ACTION_VIEW.equals(intent.getAction()) && intent.getData() != null;
        
        if (fromDeepLink) {
            ToyyibPaySessionManager.updateFromDeepLink(intent.getData());
            currentSession = ToyyibPaySessionManager.getCurrentSession();
            if (currentSession != null) {
                populateFieldsFromSession(currentSession);
            }
            refreshStatusFromServer();
        } else {
            ToyyibPaySessionManager.clearSession();
            populateFieldsFromExtras(intent);
            currentSession = null;
        }
        
        displayBookingDetails();
        updatePaymentStatusText();
    }
    
    private void populateFieldsFromExtras(Intent intent) {
        serviceName = intent.getStringExtra("SERVICE_NAME");
        staffName = intent.getStringExtra("STAFF_NAME");
        appointmentDateTime = intent.getStringExtra("APPOINTMENT_DATETIME");
        userEmail = intent.getStringExtra("USER_EMAIL");
        servicePrice = intent.getDoubleExtra("SERVICE_PRICE", 0.0);
        serviceId = intent.getIntExtra("SERVICE_ID", 0);
        customerName = userEmail != null ? userEmail : "Customer";
        customerPhone = "";
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
    
    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        serviceNameText = findViewById(R.id.serviceNameText);
        staffNameText = findViewById(R.id.staffNameText);
        appointmentDateText = findViewById(R.id.appointmentDateText);
        serviceIdText = findViewById(R.id.serviceIdText);
        totalAmountText = findViewById(R.id.totalAmountText);
        proceedToToyyibPayButton = findViewById(R.id.proceedToToyyibPayButton);
        paymentStatusText = findViewById(R.id.paymentStatusText);
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
        
        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(this, "User email is required for payment.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        setProcessingState(true);
        
        String referenceId = generateReferenceId();
        String description = "Booking for " + (serviceName != null ? serviceName : "Service");
        String payerName = (customerName != null && !customerName.isEmpty()) ? customerName : userEmail;
        
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
        
        ToyyibPayApi api = ToyyibPayClient.getApi();
        api.createBill(request).enqueue(new Callback<CreateBillResponse>() {
            @Override
            public void onResponse(Call<CreateBillResponse> call, Response<CreateBillResponse> response) {
                setProcessingState(false);
                
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(BookingConfirmationActivity.this, "Failed to create ToyyibPay bill.", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                CreateBillResponse body = response.body();
                String billCode = body.getBillCode();
                String paymentUrl = body.getPaymentUrl();
                String status = body.getStatus() != null ? body.getStatus() : "CREATED";
                
                session.assignBillDetails(billCode, paymentUrl, status);
                ToyyibPaySessionManager.startSession(session);
                currentSession = session;
                updatePaymentStatusText();
                
                if (paymentUrl != null && !paymentUrl.isEmpty()) {
                    openPaymentUrl(paymentUrl);
                } else {
                    Toast.makeText(BookingConfirmationActivity.this, "Invalid payment URL.", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<CreateBillResponse> call, Throwable t) {
                setProcessingState(false);
                Toast.makeText(BookingConfirmationActivity.this, "ToyyibPay error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void refreshStatusFromServer() {
        ToyyibPaySession session = ToyyibPaySessionManager.getCurrentSession();
        if (session == null || session.getBillCode() == null || session.getBillCode().isEmpty()) {
            return;
        }
        
        ToyyibPayClient.getApi().getPaymentStatus(session.getBillCode())
                .enqueue(new Callback<PaymentStatusResponse>() {
                    @Override
                    public void onResponse(Call<PaymentStatusResponse> call, Response<PaymentStatusResponse> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            return;
                        }
                        PaymentStatusResponse body = response.body();
                        ToyyibPaySession updatedSession = ToyyibPaySessionManager.getCurrentSession();
                        if (updatedSession != null) {
                            updatedSession.updateStatus(
                                    body.getStatus(),
                                    body.getRemark(),
                                    body.isPaid()
                            );
                            updatePaymentStatusText();
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<PaymentStatusResponse> call, Throwable t) {
                        // Silent fail; user can retry later.
                    }
                });
    }
    
    private String generateReferenceId() {
        return "BOOK-" + (serviceId > 0 ? serviceId : "GEN") + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    private void openPaymentUrl(String url) {
        try {
            CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder().build();
            customTabsIntent.launchUrl(this, android.net.Uri.parse(url));
        } catch (Exception e) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url));
            if (browserIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(browserIntent);
            } else {
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

