package com.example.mizrahbeauty.models;

import java.io.Serializable;

public class Booking implements Serializable {
    private int bookingId;
    private int id; // Database ID
    private String serviceName;
    private String staffName;
    private String staffEmail;
    private String customerName;
    private String customerEmail;
    private String userEmail; // Database field
    private String bookingDate;
    private String bookingTime;
    private String appointmentTime; // Database field
    private String status;
    private String notes;
    private int serviceId;
    private double price;
    private int durationMinutes;

    // Constructor
    public Booking(int bookingId, String serviceName, String staffName, String bookingDate, String bookingTime, String status) {
        this.bookingId = bookingId;
        this.serviceName = serviceName;
        this.staffName = staffName;
        this.bookingDate = bookingDate;
        this.bookingTime = bookingTime;
        this.status = status;
        this.notes = "";
        this.customerName = "";
        this.customerEmail = "";
    }

    // Constructor with notes
    public Booking(int bookingId, String serviceName, String staffName, String bookingDate, String bookingTime, String status, String notes) {
        this.bookingId = bookingId;
        this.serviceName = serviceName;
        this.staffName = staffName;
        this.bookingDate = bookingDate;
        this.bookingTime = bookingTime;
        this.status = status;
        this.notes = notes != null ? notes : "";
        this.customerName = "";
        this.customerEmail = "";
    }

    // Constructor with customer details
    public Booking(int bookingId, String serviceName, String staffName, String bookingDate, String bookingTime,
                   String status, String notes, String customerName, String customerEmail) {
        this.bookingId = bookingId;
        this.serviceName = serviceName;
        this.staffName = staffName;
        this.bookingDate = bookingDate;
        this.bookingTime = bookingTime;
        this.status = status;
        this.notes = notes != null ? notes : "";
        this.customerName = customerName != null ? customerName : "";
        this.customerEmail = customerEmail != null ? customerEmail : "";
    }

    // Getters
    public int getBookingId() {
        return bookingId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getStaffName() {
        return staffName;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public String getBookingDate() {
        return bookingDate;
    }

    public String getBookingTime() {
        return bookingTime;
    }

    public String getStatus() {
        return status;
    }

    public String getNotes() {
        return notes;
    }

    // Setters
    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName != null ? customerName : "";
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail != null ? customerEmail : "";
    }

    public void setBookingDate(String bookingDate) {
        this.bookingDate = bookingDate;
    }

    public void setBookingTime(String bookingTime) {
        this.bookingTime = bookingTime;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setNotes(String notes) {
        this.notes = notes != null ? notes : "";
    }
    
    // Additional getters and setters for new fields
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getStaffEmail() { return staffEmail; }
    public void setStaffEmail(String staffEmail) { this.staffEmail = staffEmail; }
    
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    
    public String getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(String appointmentTime) { this.appointmentTime = appointmentTime; }
    
    public int getServiceId() { return serviceId; }
    public void setServiceId(int serviceId) { this.serviceId = serviceId; }
    
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    
    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
    
    // Default constructor
    public Booking() {
        this.notes = "";
        this.customerName = "";
        this.customerEmail = "";
        this.staffEmail = "";
        this.userEmail = "";
    }
}