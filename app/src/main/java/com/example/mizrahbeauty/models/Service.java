package com.example.mizrahbeauty.models;

import java.io.Serializable;

public class Service implements Serializable {
    private int id;
    private String category;
    private String serviceName;
    private double price;
    private int durationMinutes;
    private String details;

    // Default constructor
    public Service() {
        // Default constructor for easy object creation
    }

    // Constructor with parameters
    public Service(int id, String category, String serviceName, double price, int durationMinutes, String details) {
        this.id = id;
        this.category = category;
        this.serviceName = serviceName;
        this.price = price;
        this.durationMinutes = durationMinutes;
        this.details = details;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public String getServiceName() {
        return serviceName;
    }

    public double getPrice() {
        return price;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public String getDetails() {
        return details;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    // Helper method to format price
    public String getFormattedPrice() {
        return String.format("RM %.2f", price);
    }

    // Helper method to format duration
    public String getFormattedDuration() {
        if (durationMinutes >= 60) {
            int hours = durationMinutes / 60;
            int mins = durationMinutes % 60;
            if (mins == 0) {
                return hours + "h";
            } else {
                return hours + "h " + mins + "m";
            }
        } else {
            return durationMinutes + "m";
        }
    }
}
