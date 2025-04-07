package com.restaurant.dto;

import java.util.List;

public class AvailableSlotsDTO {
    private String locationId;
    private String locationAddress;
    private String tableNumber;
    private int capacity;
    private List<String> availableSlots;

    // Default constructor
    public AvailableSlotsDTO() {}

    // Parameterized constructor
    public AvailableSlotsDTO(String locationId, String locationAddress,
                             String tableNumber, int capacity,
                             List<String> availableSlots) {
        this.locationId = locationId;
        this.locationAddress = locationAddress;
        this.tableNumber = tableNumber;
        this.capacity = capacity;
        this.availableSlots = availableSlots;
    }

    // Getters and Setters
    public String getLocationId() { return locationId; }
    public void setLocationId(String locationId) { this.locationId = locationId; }
    public String getLocationAddress() { return locationAddress; }
    public void setLocationAddress(String locationAddress) { this.locationAddress = locationAddress; }
    public String getTableNumber() { return tableNumber; }
    public void setTableNumber(String tableNumber) { this.tableNumber = tableNumber; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public List<String> getAvailableSlots() { return availableSlots; }
    public void setAvailableSlots(List<String> availableSlots) { this.availableSlots = availableSlots; }
}