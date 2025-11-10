package com.example.mizrahbeauty;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;

import com.example.mizrahbeauty.payment.ToyyibPayRepository;
import com.example.mizrahbeauty.payment.requests.CreateBillRequest;
import com.example.mizrahbeauty.payment.responses.CreateBillResponse;
import com.example.mizrahbeauty.payment.responses.PaymentStatusResponse;

import java.util.UUID;

/**
 * Example screen that demonstrates how to kick off a ToyyibPay payment from the app.
 * This activity expects a backend endpoint (see ToyyibPayClient) to be available.
 */
public class PaymentActivity extends AppCompatActivity {

    public static final String EXTRA_AMOUNT = "EXTRA_AMOUNT";
    public static final String EXTRA_DESCRIPTION = "EXTRA_DESCRIPTION";
    public static final String EXTRA_CUSTOMER_NAME = "EXTRA_CUSTOMER_NAME";
    public static final String EXTRA_CUSTOMER_EMAIL = "EXTRA_CUSTOMER_EMAIL";

    private EditText amountInput;
    private EditText descriptionInput;
    private EditText nameInput;
    private EditText emailInput;
    private EditText billCodeInput;
    private Button createBillButton;
    private Button checkStatusButton;
    private ProgressBar progressBar;
    private TextView resultText;

    private ToyyibPayRepository repository;
    @Nullable
    private String lastBillCode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        repository = new ToyyibPayRepository();

        amountInput = findViewById(R.id.amountInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        billCodeInput = findViewById(R.id.billCodeInput);
        createBillButton = findViewById(R.id.createBillButton);
        checkStatusButton = findViewById(R.id.checkStatusButton);
        progressBar = findViewById(R.id.paymentProgressBar);
        resultText = findViewById(R.id.resultText);

        // Populate inputs from extras if provided
        double amountFromIntent = getIntent().getDoubleExtra(EXTRA_AMOUNT, 0);
        if (amountFromIntent > 0) {
            amountInput.setText(String.valueOf(amountFromIntent));
        }
        String descriptionFromIntent = getIntent().getStringExtra(EXTRA_DESCRIPTION);
        if (!TextUtils.isEmpty(descriptionFromIntent)) {
            descriptionInput.setText(descriptionFromIntent);
        }
        String nameFromIntent = getIntent().getStringExtra(EXTRA_CUSTOMER_NAME);
        if (!TextUtils.isEmpty(nameFromIntent)) {
            nameInput.setText(nameFromIntent);
        }
        String emailFromIntent = getIntent().getStringExtra(EXTRA_CUSTOMER_EMAIL);
        if (!TextUtils.isEmpty(emailFromIntent)) {
            emailInput.setText(emailFromIntent);
        }

        createBillButton.setOnClickListener(v -> triggerCreateBill());
        checkStatusButton.setOnClickListener(v -> triggerCheckStatus());
    }

    private void triggerCreateBill() {
        String amountText = amountInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        String customerName = nameInput.getText().toString().trim();
        String customerEmail = emailInput.getText().toString().trim();

        if (TextUtils.isEmpty(amountText) || TextUtils.isEmpty(description)
                || TextUtils.isEmpty(customerName) || TextUtils.isEmpty(customerEmail)) {
            Toast.makeText(this, "Please provide amount, description, name and email", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        toggleLoading(true);
        resultText.setText("");

        CreateBillRequest request = new CreateBillRequest(
                amount,
                description,
                customerName,
                customerEmail,
                UUID.randomUUID().toString()
        );

        repository.createBill(request, new ToyyibPayRepository.CreateBillCallback() {
            @Override
            public void onSuccess(CreateBillResponse response) {
                toggleLoading(false);
                lastBillCode = response.getBillCode();
                billCodeInput.setText(lastBillCode);
                resultText.setText("Bill created: " + lastBillCode);

                if (!TextUtils.isEmpty(response.getPaymentUrl())) {
                    openPaymentPage(response.getPaymentUrl());
                }
            }

            @Override
            public void onError(Throwable t) {
                toggleLoading(false);
                Toast.makeText(PaymentActivity.this, "Error creating bill: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void triggerCheckStatus() {
        String billCode = billCodeInput.getText().toString().trim();
        if (TextUtils.isEmpty(billCode)) {
            Toast.makeText(this, "Enter bill code to check status", Toast.LENGTH_SHORT).show();
            return;
        }

        toggleLoading(true);
        repository.getPaymentStatus(billCode, new ToyyibPayRepository.StatusCallback() {
            @Override
            public void onSuccess(PaymentStatusResponse response) {
                toggleLoading(false);
                String statusMessage = "Status: " + response.getStatus() +
                        "\nPaid: " + response.isPaid() +
                        (response.getRemark() != null ? "\nRemark: " + response.getRemark() : "");
                resultText.setText(statusMessage);
            }

            @Override
            public void onError(Throwable t) {
                toggleLoading(false);
                Toast.makeText(PaymentActivity.this, "Error checking status: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void toggleLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        createBillButton.setEnabled(!loading);
        checkStatusButton.setEnabled(!loading);
    }

    private void openPaymentPage(String url) {
        try {
            Uri uri = Uri.parse(url);
            CustomTabsIntent intent = new CustomTabsIntent.Builder().build();
            intent.launchUrl(this, uri);
        } catch (Exception e) {
            Toast.makeText(this, "Unable to open payment page", Toast.LENGTH_SHORT).show();
        }
    }
}

