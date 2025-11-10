package com.example.mizrahbeauty.payment.requests;

import com.google.gson.annotations.SerializedName;

/**
 * Request payload sent to your backend to generate a ToyyibPay bill.
 */
public class CreateBillRequest {

    @SerializedName("amount")
    private final double amount;

    @SerializedName("description")
    private final String description;

    @SerializedName("customerName")
    private final String customerName;

    @SerializedName("customerEmail")
    private final String customerEmail;

    @SerializedName("customerPhone")
    private final String customerPhone;

    @SerializedName("referenceId")
    private final String referenceId;

    @SerializedName("returnUrl")
    private final String returnUrl;

    @SerializedName("callbackUrl")
    private final String callbackUrl;

    public CreateBillRequest(double amount,
                             String description,
                             String customerName,
                             String customerEmail,
                             String customerPhone,
                             String referenceId,
                             String returnUrl,
                             String callbackUrl) {
        this.amount = amount;
        this.description = description;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone;
        this.referenceId = referenceId;
        this.returnUrl = returnUrl;
        this.callbackUrl = callbackUrl;
    }

    public double getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }
}

