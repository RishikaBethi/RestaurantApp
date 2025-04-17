package com.restaurant.dto;

public class WaiterReportDTO {
    private String waiterId;
    private int ordersProcessed;
    private int totalServiceFeedback;
    private int serviceFeedbackCount;
    private int minServiceFeedback;
    private double workingHours;

    // Getters and setters

    public String getWaiterId() {
        return waiterId;
    }

    public void setWaiterId(String waiterId) {
        this.waiterId = waiterId;
    }

    public int getOrdersProcessed() {
        return ordersProcessed;
    }

    public void setOrdersProcessed(int ordersProcessed) {
        this.ordersProcessed = ordersProcessed;
    }

    public int getTotalServiceFeedback() {
        return totalServiceFeedback;
    }

    public void setTotalServiceFeedback(int totalServiceFeedback) {
        this.totalServiceFeedback = totalServiceFeedback;
    }

    public int getServiceFeedbackCount() {
        return serviceFeedbackCount;
    }

    public void setServiceFeedbackCount(int serviceFeedbackCount) {
        this.serviceFeedbackCount = serviceFeedbackCount;
    }

    public int getMinServiceFeedback() {
        return minServiceFeedback;
    }

    public void setMinServiceFeedback(int minServiceFeedback) {
        this.minServiceFeedback = minServiceFeedback;
    }

    public double getWorkingHours() {
        return workingHours;
    }

    public void setWorkingHours(double workingHours) {
        this.workingHours = workingHours;
    }
}
