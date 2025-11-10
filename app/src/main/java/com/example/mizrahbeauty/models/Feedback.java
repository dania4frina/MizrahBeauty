package com.example.mizrahbeauty.models;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Feedback implements Serializable {
    private int id;
    private String userEmail;
    private String userName;
    private String feedbackText;
    private int rating;
    private String feedbackType;
    private String status;
    private String createdAt;
    private String response;
    private String respondedAt;

    public Feedback() {}

    public Feedback(String userEmail, String userName, String feedbackText, int rating, String feedbackType) {
        this.userEmail = userEmail;
        this.userName = userName;
        this.feedbackText = feedbackText;
        this.rating = rating;
        this.feedbackType = feedbackType;
        this.status = "PENDING";
        this.createdAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getFeedbackText() { return feedbackText; }
    public void setFeedbackText(String feedbackText) { this.feedbackText = feedbackText; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getFeedbackType() { return feedbackType; }
    public void setFeedbackType(String feedbackType) { this.feedbackType = feedbackType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }

    public String getRespondedAt() { return respondedAt; }
    public void setRespondedAt(String respondedAt) { this.respondedAt = respondedAt; }

    // Helper methods
    public String getFormattedCreatedAt() {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
            Date date = inputFormat.parse(createdAt);
            return outputFormat.format(date);
        } catch (Exception e) {
            return createdAt;
        }
    }

    public String getFormattedRespondedAt() {
        if (respondedAt == null || respondedAt.isEmpty()) return "";
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
            Date date = inputFormat.parse(respondedAt);
            return outputFormat.format(date);
        } catch (Exception e) {
            return respondedAt;
        }
    }

    public String getStatusText() {
        switch (status) {
            case "PENDING": return "Menunggu Respons";
            case "REVIEWED": return "Ditinjau";
            case "RESPONDED": return "Direspons";
            default: return status;
        }
    }

    public int getStatusColor() {
        switch (status) {
            case "PENDING": return 0xFFFF9800; // Orange
            case "REVIEWED": return 0xFF2196F3; // Blue
            case "RESPONDED": return 0xFF4CAF50; // Green
            default: return 0xFF757575; // Grey
        }
    }
}
