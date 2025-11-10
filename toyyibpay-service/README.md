# ToyyibPay Service

Lightweight Node/Express service that exposes webhook endpoints for ToyyibPay sandbox payments. Deploy it to Render (or any Node host) and use its public URL as the `billCallbackUrl` when creating ToyyibPay bills.

## Features

- `POST /toyyibpay/callback` – receives official ToyyibPay payment callbacks (form or JSON payloads).
- `POST /toyyibpay/payment/bill` – proxy endpoint that creates ToyyibPay bills using your secret key.
- `GET /toyyibpay/payment/status?billCode=XXXX` – fetches ToyyibPay transactions for a bill.
- `POST /toyyibpay/simulate` – helper to test payload handling without calling the real gateway.
- CORS, logging, and environment-driven configuration ready for Render deployment.

## Local Setup

```bash
cd toyyibpay-service
npm install
cp env.sample .env   # adjust values
npm run dev
```

Visit `http://localhost:8080/` to confirm it is running.

## Deploying on Render

1. Commit this folder to GitHub (already inside the main repo).
2. In Render, create a new **Web Service**, choose your GitHub repo, and set the root directory to `toyyibpay-service`.
3. Use the default Node environment. Set environment variables (at least `PORT`, `TOYYIBPAY_SECRET_KEY`, `TOYYIBPAY_CATEGORY_CODE`, optionally `TOYYIBPAY_BASE_URL`).
4. Render provides a URL like `https://your-service.onrender.com`. The callback endpoint becomes `https://your-service.onrender.com/toyyibpay/callback`.

## Linking with Android App

1. When creating a bill with ToyyibPay, set:
   - `billCallbackUrl` → `https://your-service.onrender.com/toyyibpay/callback`
   - `billReturnUrl` → `mizrahbeauty://payment/result` (deep-link back to the app)
2. Store `billExternalReferenceNo` with your local booking ID. The webhook payload includes it, so you can reconcile payments later.
3. Extend `src/routes/toyyibpay.js` to persist payloads in your database or trigger email notifications.

See `../payment-toyyibpay-flows.md` for the full integration flow (created alongside this service).

