package com.example.mizrahbeauty.models;

import java.io.Serializable;

public class Staff implements Serializable {
    private int id;
    private String userEmail;
    private String name;
    private String phone;
    private String position;
    private String serviceDetails;
    private boolean isAvailable;
    private boolean isActive;

    // Default constructor
    public Staff() {}

    // Constructor
    public Staff(int id, String userEmail, String name, String phone, String position) {
        this.id = id;
        this.userEmail = userEmail;
        this.name = name;
        this.phone = phone;
        this.position = position;
        this.isAvailable = true;
        this.isActive = true;
    }

    // Full constructor
    public Staff(int id, String userEmail, String name, String phone, String position, 
                String serviceDetails, boolean isAvailable, boolean isActive) {
        this.id = id;
        this.userEmail = userEmail;
        this.name = name;
        this.phone = phone;
        this.position = position;
        this.serviceDetails = serviceDetails;
        this.isAvailable = isAvailable;
        this.isActive = isActive;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public String getServiceDetails() { return serviceDetails; }
    public void setServiceDetails(String serviceDetails) { this.serviceDetails = serviceDetails; }

    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    // Helper methods
    public String getDisplayName() {
        if (position != null && !position.isEmpty()) {
            return name + " (" + position + ")";
        }
        return name;
    }

    public String getAvailabilityStatus() {
        if (!isActive) return "Inactive";
        if (!isAvailable) return "Not Available";
        return "Available";
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}
