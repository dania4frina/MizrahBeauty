package com.example.mizrahbeauty.payment;

import android.net.Uri;
import android.text.TextUtils;

/**
 * Simple in-memory holder for the current ToyyibPay payment session. This
 * enables us to match callbacks and deep links to the booking initiated earlier.
 */
public final class ToyyibPaySessionManager {

    private static ToyyibPaySession currentSession;

    private ToyyibPaySessionManager() {
        // no-op
    }

    public static void startSession(ToyyibPaySession session) {
        currentSession = session;
    }

    public static ToyyibPaySession getCurrentSession() {
        return currentSession;
    }

    public static void clearSession() {
        currentSession = null;
    }

    public static void updateFromDeepLink(Uri data) {
        android.util.Log.d("ToyyibPay", "updateFromDeepLink called");
        
        if (currentSession == null) {
            android.util.Log.e("ToyyibPay", "No active session to update");
            return;
        }
        
        if (data == null) {
            android.util.Log.e("ToyyibPay", "Deep link data is null");
            return;
        }

        android.util.Log.d("ToyyibPay", "Deep link full URI: " + data.toString());
        android.util.Log.d("ToyyibPay", "Deep link query string: " + data.getQuery());

        String billCode = data.getQueryParameter("billcode");
        String status = data.getQueryParameter("status");
        if (TextUtils.isEmpty(status)) {
            status = data.getQueryParameter("status_id");
        }
        String message = data.getQueryParameter("msg");
        String amount = data.getQueryParameter("amount");
        String paid = data.getQueryParameter("paid");

        android.util.Log.d("ToyyibPay", "Extracted params - billCode: " + billCode + ", status: " + status + ", paid: " + paid + ", msg: " + message);

        if (!TextUtils.isEmpty(billCode) && !billCode.equalsIgnoreCase(currentSession.getBillCode())) {
            android.util.Log.w("ToyyibPay", "BillCode mismatch. Expected: " + currentSession.getBillCode() + ", Got: " + billCode);
            return;
        }

        boolean paidFlag = "1".equals(status) || "true".equalsIgnoreCase(paid);
        String remark = message != null ? message : ("Amount: " + (amount != null ? amount : "N/A"));

        android.util.Log.d("ToyyibPay", "Updating session with status: " + status + ", paid: " + paidFlag);

        currentSession.updateStatus(
                !TextUtils.isEmpty(status) ? status : "UNKNOWN",
                remark,
                paidFlag
        );
    }
}

