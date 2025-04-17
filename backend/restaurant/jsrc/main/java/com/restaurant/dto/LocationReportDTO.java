package com.restaurant.dto;

public class LocationReportDTO {
    private String locationId;
    private int ordersProcessed;
    private int totalCuisineFeedback;
    private int cuisineFeedbackCount;
    private int minCuisineFeedback;
    private double revenue;

    // Getters and setters

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public int getOrdersProcessed() {
        return ordersProcessed;
    }

    public void setOrdersProcessed(int ordersProcessed) {
        this.ordersProcessed = ordersProcessed;
    }

    public int getTotalCuisineFeedback() {
        return totalCuisineFeedback;
    }

    public void setTotalCuisineFeedback(int totalCuisineFeedback) {
        this.totalCuisineFeedback = totalCuisineFeedback;
    }

    public int getCuisineFeedbackCount() {
        return cuisineFeedbackCount;
    }

    public void setCuisineFeedbackCount(int cuisineFeedbackCount) {
        this.cuisineFeedbackCount = cuisineFeedbackCount;
    }

    public int getMinCuisineFeedback() {
        return minCuisineFeedback;
    }

    public void setMinCuisineFeedback(int minCuisineFeedback) {
        this.minCuisineFeedback = minCuisineFeedback;
    }

    public double getRevenue() {
        return revenue;
    }

    public void setRevenue(double revenue) {
        this.revenue = revenue;
    }
}
