package com.restaurant.dto;

import java.util.ArrayList;
import java.util.List;

public class LocationDTO {
    private String id;
    private String address;
    private String description;
    private String totalCapacity;
    private String averageOccupancy;
    private String imageUrl;
    private String rating;

    public LocationDTO(String id, String address, String description, String totalCapacity,
                       String averageOccupancy, String imageUrl, String rating) {
        this.id = id;
        this.address = address;
        this.description = description;
        this.totalCapacity = totalCapacity;
        this.averageOccupancy = averageOccupancy;
        this.imageUrl = imageUrl;
        this.rating = rating;
    }

    // Getters
    public String getId() { return id; }
    public String getAddress() { return address; }
    public String getDescription() { return description; }
    public String getTotalCapacity() { return totalCapacity; }
    public String getAverageOccupancy() { return averageOccupancy; }
    public String getImageUrl() { return imageUrl; }
    public String getRating() { return rating; }

 }