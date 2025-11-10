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
        if (currentSession == null || data == null) {
            return;
        }

        String billCode = data.getQueryParameter("billcode");
        String status = data.getQueryParameter("status");
        if (TextUtils.isEmpty(status)) {
            status = data.getQueryParameter("status_id");
        }
        String message = data.getQueryParameter("msg");
        String amount = data.getQueryParameter("amount");
        String paid = data.getQueryParameter("paid");

        if (!TextUtils.isEmpty(billCode) && !billCode.equalsIgnoreCase(currentSession.getBillCode())) {
            // Deep link is for a different bill; ignore for now.
            return;
        }

        boolean paidFlag = "1".equals(status) || "true".equalsIgnoreCase(paid);
        String remark = message != null ? message : ("Amount: " + (amount != null ? amount : "N/A"));

        currentSession.updateStatus(
                !TextUtils.isEmpty(status) ? status : "UNKNOWN",
                remark,
                paidFlag
        );
    }
}

