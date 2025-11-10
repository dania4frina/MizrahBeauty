# ToyyibPay Payment Flow (Sandbox)

This document summarizes how the Mizrah Beauty Android app will integrate with the ToyyibPay sandbox environment and how the Render-hosted webhook cooperates with it.

## 1. Actors & Components

- **Android app** – collects booking details and launches payments.
- **ToyyibPay sandbox** – payment gateway (`https://dev.toyyibpay.com`).
- **ToyyibPay Service (Render)** – Node/Express webhook located in `toyyibpay-service/`.
- **SQL Server** – internal booking/payment persistence.

## 2. High-level Sequence

1. User confirms a booking inside the app (`BookingConfirmationActivity`).
2. App calls ToyyibPay `createBill` API with booking info.
3. ToyyibPay returns `BillCode`.
4. App opens `https://dev.toyyibpay.com/{BillCode}` in a Custom Tab.
5. User completes (or cancels) payment.
6. ToyyibPay performs two responses:
   - Redirects the user to `billReturnUrl` (deep link back into the app).
   - Sends a server-to-server POST to `billCallbackUrl` (Render webhook).
7. Webhook records payment status and updates SQL Server (current TODO).
8. App checks status (immediate via deep-link extras or later via API/polling).

Reference: [ToyyibPay API Reference](https://toyyibpay.com/apireference/).

## 3. ToyyibPay API Calls

### 3.1 Create Category (one-time)

`POST https://dev.toyyibpay.com/index.php/api/createCategory`  
Fields: `userSecretKey`, `catname`, `catdescription`. Response: `CategoryCode`.

Store the resulting `CategoryCode` in `TOYYIBPAY_CATEGORY_CODE` (env file).

### 3.2 Create Bill (per booking)

`POST https://dev.toyyibpay.com/index.php/api/createBill`

Key fields used by the app:

| Field | Value |
| --- | --- |
| `userSecretKey` | sandbox secret key |
| `categoryCode` | environment variable |
| `billName` | e.g. `Mizrah Booking` |
| `billDescription` | `Booking for {serviceName}` |
| `billAmount` | price in cents (RM 1.35 → `135`) |
| `billPayorInfo` | `1` to capture payer email/phone |
| `billEmail` | user email |
| `billPhone` | user phone (if available) |
| `billReturnUrl` | deep link, e.g. `mizrahbeauty://payment/result` |
| `billCallbackUrl` | Render URL (`https://<service>.onrender.com/toyyibpay/callback`) |
| `billExternalReferenceNo` | internal booking ID |

Response contains `BillCode`. Open `https://dev.toyyibpay.com/{BillCode}`.

## 4. Android Implementation Notes

- Add Retrofit interface `ToyyibPayApi` with `@FormUrlEncoded` `@POST("index.php/api/createBill")`.
- Configure `Retrofit.Builder().baseUrl("https://dev.toyyibpay.com/")`.
- In `BookingConfirmationActivity`:
  - Disable button & show loader while API runs.
  - On success: store `BillCode`, open Custom Tab, keep local state.
  - On fail: toast error and re-enable button.
- Handle deep link returned via `billReturnUrl` to show quick status (success/fail).
- Optionally add polling endpoint to query server once callback processed.

## 5. Render Webhook (`toyyibpay-service`)

- `POST /toyyibpay/callback`: receives ToyyibPay form data.  
  Example payload:
  ```
  {
    "billcode": "abc123",
    "status": "1",
    "order_id": "ORDER-001",
    "billExternalReferenceNo": "BOOKING-42",
    "amount": "13500"
  }
  ```
- Extend handler to:
  1. Validate payload (e.g., check `status == "1"` for success).
  2. Persist into SQL Server or queue for later processing.
  3. Notify the app (future enhancement).

## 6. Environment Variables

| Variable | Purpose |
| --- | --- |
| `TOYYIBPAY_SECRET_KEY` | sandbox secret key used by service (optional if only webhook) |
| `TOYYIBPAY_CATEGORY_CODE` | default category code for bill creation |
| `ALLOW_ORIGINS` | comma-separated list for CORS |
| `PORT` | port for Render & local dev (default 8080) |

For local testing copy `toyyibpay-service/env.sample` to `.env`.

## 7. Outstanding TODOs

- Wire webhook payload to SQL Server.
- Decide on push vs. pull strategy for notifying the Android app.
- Implement signature verification if ToyyibPay provides one (security hardening).

## 8. Useful Links

- ToyyibPay API documentation: [https://toyyibpay.com/apireference/](https://toyyibpay.com/apireference/)
- ToyyibPay sandbox registration: [https://dev.toyyibpay.com](https://dev.toyyibpay.com)
- Render dashboard: [https://dashboard.render.com/](https://dashboard.render.com/)


