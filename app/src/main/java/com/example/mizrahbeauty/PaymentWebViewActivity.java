package com.example.mizrahbeauty;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * WebView-based payment activity for ToyyibPay
 * This approach is standard for payment gateway integration in Android
 * Reference: https://stackoverflow.com/questions/31024951/getting-callback-from-payment-gateway-in-webview
 */
public class PaymentWebViewActivity extends AppCompatActivity {
    
    private WebView webView;
    private ProgressBar progressBar;
    private String returnUrl;
    
    public static final String EXTRA_PAYMENT_URL = "PAYMENT_URL";
    public static final String EXTRA_RETURN_URL = "RETURN_URL";
    public static final String EXTRA_BILL_CODE = "BILL_CODE";
    
    public static final String RESULT_EXTRA_STATUS = "PAYMENT_STATUS";
    public static final String RESULT_EXTRA_BILL_CODE = "BILL_CODE";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_webview);
        
        webView = findViewById(R.id.paymentWebView);
        progressBar = findViewById(R.id.progressBar);
        
        String paymentUrl = getIntent().getStringExtra(EXTRA_PAYMENT_URL);
        returnUrl = getIntent().getStringExtra(EXTRA_RETURN_URL);
        String billCode = getIntent().getStringExtra(EXTRA_BILL_CODE);
        
        if (paymentUrl == null || paymentUrl.isEmpty()) {
            Toast.makeText(this, "Invalid payment URL", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        setupWebView(billCode);
        webView.loadUrl(paymentUrl);
        
        android.util.Log.d("ToyyibPay", "WebView loading payment URL: " + paymentUrl);
        android.util.Log.d("ToyyibPay", "Return URL to intercept: " + returnUrl);
    }
    
    private void setupWebView(String billCode) {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setSupportZoom(false);
        
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
                android.util.Log.d("ToyyibPay", "WebView loading: " + url);
                
                // Check if this is the return URL
                if (returnUrl != null && url.startsWith(returnUrl)) {
                    android.util.Log.d("ToyyibPay", "Return URL detected! Parsing result...");
                    handleReturnUrl(url, billCode);
                }
            }
            
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
                android.util.Log.d("ToyyibPay", "WebView page finished: " + url);
            }
            
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                android.util.Log.e("ToyyibPay", "WebView error: " + error.getDescription());
                
                // If error on return URL, still try to handle it
                String url = request.getUrl().toString();
                if (returnUrl != null && url.startsWith(returnUrl)) {
                    handleReturnUrl(url, billCode);
                }
            }
            
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                android.util.Log.d("ToyyibPay", "shouldOverrideUrlLoading: " + url);
                
                // Intercept the return URL (deep link)
                if (returnUrl != null && url.startsWith(returnUrl)) {
                    android.util.Log.d("ToyyibPay", "Intercepting return URL");
                    handleReturnUrl(url, billCode);
                    return true; // Prevent WebView from loading the deep link
                }
                
                // Allow WebView to handle all other URLs
                return false;
            }
        });
    }
    
    private void handleReturnUrl(String url, String billCode) {
        android.util.Log.d("ToyyibPay", "Processing return URL: " + url);
        
        // Parse query parameters from the return URL
        Uri uri = Uri.parse(url);
        String statusCode = uri.getQueryParameter("status_id");
        String msg = uri.getQueryParameter("msg");
        String reason = uri.getQueryParameter("reason");
        String transactionId = uri.getQueryParameter("transaction_id");
        
        android.util.Log.d("ToyyibPay", "URL params - status_id: " + statusCode + 
                          ", msg: " + msg + ", reason: " + reason + 
                          ", transaction_id: " + transactionId);
        
        // Determine payment status
        String status = "UNKNOWN";
        if ("1".equals(statusCode)) {
            status = "SUCCESS";
        } else if ("2".equals(statusCode)) {
            status = "PENDING";
        } else if ("3".equals(statusCode)) {
            status = "FAILED";
        }
        
        // Return result to calling activity
        Intent resultIntent = new Intent();
        resultIntent.putExtra(RESULT_EXTRA_STATUS, status);
        resultIntent.putExtra(RESULT_EXTRA_BILL_CODE, billCode);
        resultIntent.putExtra("status_id", statusCode);
        resultIntent.putExtra("msg", msg);
        resultIntent.putExtra("reason", reason);
        resultIntent.putExtra("transaction_id", transactionId);
        
        setResult(RESULT_OK, resultIntent);
        
        Toast.makeText(this, 
            status.equals("SUCCESS") ? "Payment successful!" : "Payment " + status.toLowerCase(), 
            Toast.LENGTH_SHORT).show();
        
        // Close the WebView and return to the booking activity
        finish();
    }
    
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            // User cancelled payment
            Intent resultIntent = new Intent();
            resultIntent.putExtra(RESULT_EXTRA_STATUS, "CANCELLED");
            setResult(RESULT_CANCELED, resultIntent);
            super.onBackPressed();
        }
    }
}

