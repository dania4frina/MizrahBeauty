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

    @SerializedName("referenceId")
    private final String referenceId;

    public CreateBillRequest(double amount, String description, String customerName, String customerEmail, String referenceId) {
        this.amount = amount;
        this.description = description;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.referenceId = referenceId;
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

    public String getReferenceId() {
        return referenceId;
    }
}

