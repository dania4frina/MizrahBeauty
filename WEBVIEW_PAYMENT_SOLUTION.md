# ToyyibPay Payment Integration - WebView Solution

## 🎯 Problem Summary

### Previous Issues with Custom Tabs Approach:
1. **Server Bug**: Status API returned `"UNKNOWN"` instead of actual payment status
   - Root cause: Server looked for `latest.status` but ToyyibPay returns `billpaymentStatus`
   
2. **Deep Link Failure**: Custom Tabs don't reliably trigger deep links
   - Users stayed on ToyyibPay success page
   - No automatic return to app after payment

### Solution: WebView + Fixed Server Status Mapping
Based on [Android best practices](https://stackoverflow.com/questions/31024951/getting-callback-from-payment-gateway-in-webview) and [official documentation](https://developer.android.com/develop/ui/views/layout/webapps/webview), WebView is the standard approach for payment gateway integration.

---

## 🔧 Changes Made

### 1. Server Fix (toyyibpay-service)

**File**: `toyyibpay-service/src/routes/toyyibpay.js`

**Fixed Status Mapping**:
```javascript
// OLD (BROKEN):
status: latest?.status ?? 'UNKNOWN',  // ❌ latest.status doesn't exist!

// NEW (FIXED):
const paymentStatus = latest?.billpaymentStatus;  // ✅ Correct field
const isPaid = paymentStatus === '1';

if (isPaid) {
  status = 'SUCCESS';
  remark = `Payment successful via ${latest.billpaymentChannel}`;
}
```

**ToyyibPay API Field Mapping**:
- `billpaymentStatus: "1"` → `SUCCESS` (paid)
- `billpaymentStatus: "2"` → `PENDING` (pending verification)
- `billpaymentStatus: "3"` → `FAILED` (failed/cancelled)
- `billStatus: "0"` + no payment → `PENDING` (awaiting payment)

---

### 2. Android WebView Implementation

#### New Activity: `PaymentWebViewActivity.java`

**Key Features**:
- ✅ Loads ToyyibPay payment URL in WebView
- ✅ Intercepts return URL (`mizrahbeauty://payment/result`)
- ✅ Parses payment result from URL parameters
- ✅ Returns result to calling activity
- ✅ Handles back button navigation
- ✅ Shows loading progress

**WebViewClient URL Interception**:
```java
@Override
public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
    String url = request.getUrl().toString();
    
    // Intercept the return URL (deep link)
    if (returnUrl != null && url.startsWith(returnUrl)) {
        handleReturnUrl(url, billCode);
        return true; // Prevent WebView from loading deep link
    }
    
    return false; // Allow other URLs
}
```

#### Updated: `BookingConfirmationActivity.java`

**Changes**:
- ✅ Replaced `CustomTabsIntent` with `PaymentWebViewActivity`
- ✅ Added `ActivityResultLauncher` to receive payment result
- ✅ Removed deep link handling (now handled by WebView)
- ✅ Simplified `onResume()` and `handleIncomingIntent()`

**Payment Flow**:
```java
// Launch WebView for payment
Intent intent = new Intent(this, PaymentWebViewActivity.class);
intent.putExtra(PaymentWebViewActivity.EXTRA_PAYMENT_URL, paymentUrl);
intent.putExtra(PaymentWebViewActivity.EXTRA_RETURN_URL, returnUrl);
intent.putExtra(PaymentWebViewActivity.EXTRA_BILL_CODE, billCode);
paymentLauncher.launch(intent);

// Receive result when WebView returns
paymentLauncher = registerForActivityResult(
    new ActivityResultContracts.StartActivityForResult(),
    result -> {
        if (result.getResultCode() == RESULT_OK) {
            String status = data.getStringExtra("PAYMENT_STATUS");
            // Handle success/failure
        }
    }
);
```

#### Layout: `activity_payment_webview.xml`

Simple layout with:
- `WebView` (full screen)
- `ProgressBar` (centered, shows during page load)

---

## 📱 Payment Flow (New)

1. **User enters payment details** in `BookingConfirmationActivity`
2. **App creates bill** via server → receives `billCode` + `paymentUrl`
3. **App launches `PaymentWebViewActivity`** with payment URL
4. **User completes payment** in WebView (ToyyibPay page)
5. **ToyyibPay redirects** to `mizrahbeauty://payment/result?status_id=1&...`
6. **WebView intercepts** return URL in `shouldOverrideUrlLoading()`
7. **WebView parses** query parameters (`status_id`, `msg`, `reason`)
8. **WebView returns** result to `BookingConfirmationActivity`
9. **App refreshes** payment status from server API
10. **App shows** success/failure message

---

## 🚀 Deployment Steps

### Server (Render.com)

1. **Commit and push** server changes:
   ```bash
   cd toyyibpay-service
   git add src/routes/toyyibpay.js
   git commit -m "Fix: Map billpaymentStatus correctly for payment status API"
   git push origin main
   ```

2. **Render will auto-deploy** (or manually trigger deploy)

3. **Verify fix** by checking logs after payment:
   - Should see `status: 'SUCCESS'` instead of `'UNKNOWN'`
   - Should see `paid: true` for successful payments

### Android App

1. **Build and run**:
   ```bash
   ./gradlew clean assembleDebug
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

2. **Test payment flow**:
   - Create booking
   - Click "Proceed to ToyyibPay"
   - Complete payment in WebView
   - Verify automatic return to app
   - Check payment status shows "SUCCESS"

---

## ✅ Testing Checklist

### Server Tests

- [ ] Call `/toyyibpay/payment/status?billCode=XXX` with paid bill
  - Should return: `{"status": "SUCCESS", "paid": true, "remark": "..."}`
  - NOT: `{"status": "UNKNOWN", "paid": false}`

- [ ] Check Render logs show correct field mapping:
  ```
  ToyyibPay transactions response: [{"billpaymentStatus":"1",...}]
  ```

### Android Tests

- [ ] Payment WebView opens successfully
- [ ] Payment form loads in WebView
- [ ] Can complete test payment (use ToyyibPay sandbox)
- [ ] **WebView automatically closes** after payment success
- [ ] **App shows success message** immediately
- [ ] Payment status updates to "SUCCESS"
- [ ] Back button in WebView navigates within payment pages
- [ ] Back button on first page cancels payment

### Edge Cases

- [ ] Handle payment cancellation (user presses back)
- [ ] Handle payment failure (invalid card, etc.)
- [ ] Handle network errors during payment
- [ ] Handle slow ToyyibPay response
- [ ] Verify retry logic still works for status check

---

## 🐛 Debugging

### Server Logs (Render)

Check for these log entries:
```
POST /toyyibpay/callback 200
  Body: { "status": "1", "reason": "Payment Approved", "billpaymentStatus":"1" }

GET /toyyibpay/payment/status?billCode=XXX 200
  Response: { "status": "SUCCESS", "paid": true }
```

### Android Logs (adb logcat)

```bash
adb logcat -s ToyyibPay:D
```

Expected flow:
```
ToyyibPay: Opening payment URL in WebView
ToyyibPay: WebView loading: https://dev.toyyibpay.com/xxxxx
ToyyibPay: Return URL detected! Parsing result...
ToyyibPay: URL params - status_id: 1, msg: ok, reason: Payment Approved
ToyyibPay: Payment result - Status: SUCCESS
ToyyibPay: Payment confirmed as PAID!
```

---

## 📚 References

1. [Stack Overflow: Payment Gateway Callback in WebView](https://stackoverflow.com/questions/31024951/getting-callback-from-payment-gateway-in-webview)
2. [Android Developer Guide: WebView](https://developer.android.com/develop/ui/views/layout/webapps/webview)
3. [ToyyibPay API Documentation](https://toyyibpay.com/apireference/)

---

## 🔄 Comparison: Custom Tabs vs WebView

| Feature | Custom Tabs (OLD) | WebView (NEW) |
|---------|------------------|---------------|
| URL Interception | ❌ Unreliable | ✅ Reliable |
| Deep Link Trigger | ❌ Doesn't work | ✅ Not needed |
| User Experience | Browser opens | ❌ In-app |
| Return to App | ❌ Manual | ✅ Automatic |
| Result Parsing | Via deep link | ✅ Via WebViewClient |
| Standard Practice | Not for payments | ✅ Yes |

---

## 💡 Key Learnings

1. **WebView is standard** for payment gateways in mobile apps
2. **Always verify API field names** - don't assume they match your expectations
3. **Test with real ToyyibPay sandbox** to catch integration issues early
4. **ActivityResultLauncher** is modern Android way to get activity results
5. **Deep links are flaky** with Custom Tabs for payment flows

---

## 🎉 Expected Results

After these fixes:

✅ Server correctly maps payment status  
✅ Android app receives "SUCCESS" status  
✅ WebView automatically returns to app after payment  
✅ No more "UNKNOWN" status  
✅ No more stuck on ToyyibPay page  
✅ Smooth user experience  

**Status**: Ready for testing! 🚀

