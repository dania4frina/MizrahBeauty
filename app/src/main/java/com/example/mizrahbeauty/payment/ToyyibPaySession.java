package com.example.mizrahbeauty.payment;

import java.io.Serializable;

/**
 * Holds details about the current ToyyibPay session so we can link callback data
 * with the booking that initiated the payment.
 */
public class ToyyibPaySession implements Serializable {

    private final String bookingReference;
    private final String serviceName;
    private final int serviceId;
    private final String staffName;
    private final String appointmentDateTime;
    private final double amount;
    private final String customerEmail;
    private final String customerName;
    private final String customerPhone;

    private String billCode;
    private String paymentUrl;
    private String lastStatus;
    private String lastRemark;
    private boolean paid;
    private long createdAtMillis;
    private long updatedAtMillis;

    public ToyyibPaySession(String bookingReference,
                            String serviceName,
                            int serviceId,
                            String staffName,
                            String appointmentDateTime,
                            double amount,
                            String customerEmail,
                            String customerName,
                            String customerPhone) {
        this.bookingReference = bookingReference;
        this.serviceName = serviceName;
        this.serviceId = serviceId;
        this.staffName = staffName;
        this.appointmentDateTime = appointmentDateTime;
        this.amount = amount;
        this.customerEmail = customerEmail;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.createdAtMillis = System.currentTimeMillis();
        this.updatedAtMillis = this.createdAtMillis;
    }

    public void assignBillDetails(String billCode, String paymentUrl, String initialStatus) {
        this.billCode = billCode;
        this.paymentUrl = paymentUrl;
        this.lastStatus = initialStatus;
        this.updatedAtMillis = System.currentTimeMillis();
    }

    public void updateStatus(String status, String remark, boolean paid) {
        this.lastStatus = status;
        this.lastRemark = remark;
        this.paid = paid;
        this.updatedAtMillis = System.currentTimeMillis();
    }

    public String getBookingReference() {
        return bookingReference;
    }

    public String getServiceName() {
        return serviceName;
    }

    public int getServiceId() {
        return serviceId;
    }

    public String getStaffName() {
        return staffName;
    }

    public String getAppointmentDateTime() {
        return appointmentDateTime;
    }

    public double getAmount() {
        return amount;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public String getBillCode() {
        return billCode;
    }

    public String getPaymentUrl() {
        return paymentUrl;
    }

    public String getLastStatus() {
        return lastStatus;
    }

    public String getLastRemark() {
        return lastRemark;
    }

    public boolean isPaid() {
        return paid;
    }

    public long getCreatedAtMillis() {
        return createdAtMillis;
    }

    public long getUpdatedAtMillis() {
        return updatedAtMillis;
    }
}

