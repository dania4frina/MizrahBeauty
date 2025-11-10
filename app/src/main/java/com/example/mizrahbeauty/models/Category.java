package com.example.mizrahbeauty.models;

import java.io.Serializable;
import java.util.List;

public class Category implements Serializable {
    private String categoryName;
    private List<Service> services;
    private int imageResource;

    // Constructor
    public Category(String categoryName, List<Service> services) {
        this.categoryName = categoryName;
        this.services = services;
    }

    public Category(String categoryName, List<Service> services, int imageResource) {
        this.categoryName = categoryName;
        this.services = services;
        this.imageResource = imageResource;
    }

    // Getters and Setters
    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public List<Service> getServices() {
        return services;
    }

    public void setServices(List<Service> services) {
        this.services = services;
    }

    public int getImageResource() {
        return imageResource;
    }

    public void setImageResource(int imageResource) {
        this.imageResource = imageResource;
    }

    public int getServiceCount() {
        return services != null ? services.size() : 0;
    }
}

