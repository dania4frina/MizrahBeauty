package com.example.mizrahbeauty.payment.responses;

import com.google.gson.annotations.SerializedName;

public class PaymentStatusResponse {

    @SerializedName("billCode")
    private String billCode;

    @SerializedName("status")
    private String status;

    @SerializedName("paid")
    private boolean paid;

    @SerializedName("remark")
    private String remark;

    public String getBillCode() {
        return billCode;
    }

    public String getStatus() {
        return status;
    }

    public boolean isPaid() {
        return paid;
    }

    public String getRemark() {
        return remark;
    }
}

