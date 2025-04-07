package com.restaurant.dto;
import org.json.JSONObject;

public class ReservationResponseDTO {
    private String id;
    private String status;
    private String locationAddress;
    private String date;
    private String timeSlot;
    private String preOrder;
    private String guestsNumber;
    private String feedbackId;

    // Constructors
    public ReservationResponseDTO() {}

    public ReservationResponseDTO(String id, String status, String locationAddress, String date,
                               String timeSlot, String preOrder, String guestsNumber, String feedbackId) {
        this.id = id;
        this.status = status;
        this.locationAddress = locationAddress;
        this.date = date;
        this.timeSlot = timeSlot;
        this.preOrder = preOrder;
        this.guestsNumber = guestsNumber;
        this.feedbackId = feedbackId;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLocationAddress() {
        return locationAddress;
    }

    public void setLocationAddress(String locationAddress) {
        this.locationAddress = locationAddress;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
    }

    public String getPreOrder() {
        return preOrder;
    }

    public void setPreOrder(String preOrder) {
        this.preOrder = preOrder;
    }

    public String getGuestsNumber() {
        return guestsNumber;
    }

    public void setGuestsNumber(String guestsNumber) {
        this.guestsNumber = guestsNumber;
    }

    public String getFeedbackId() {
        return feedbackId;
    }

    public void setFeedbackId(String feedbackId) {
        this.feedbackId = feedbackId;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("status", status);
        json.put("locationAddress", locationAddress);
        json.put("date", date);
        json.put("timeSlot", timeSlot);
        json.put("preOrder", preOrder);
        json.put("guestsNumber", guestsNumber);
        json.put("feedbackId", feedbackId);
        return json;
    }
}
