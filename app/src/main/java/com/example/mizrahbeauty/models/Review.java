package com.example.mizrahbeauty.models;

public class Review {
    private int id;
    private String userEmail;
    private String serviceName;
    private int rating;
    private String comment;
    private String createdAt;
    private int serviceId;

    // Constructor
    public Review(int id, String userEmail, String serviceName, int rating, String comment, String createdAt, int serviceId) {
        this.id = id;
        this.userEmail = userEmail;
        this.serviceName = serviceName;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
        this.serviceId = serviceId;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getServiceName() {
        return serviceName;
    }

    public int getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public int getServiceId() {
        return serviceId;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }
}
