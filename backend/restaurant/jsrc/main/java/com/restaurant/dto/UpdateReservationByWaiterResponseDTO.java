package com.restaurant.dto;

import org.json.JSONObject;

public class UpdateReservationByWaiterResponseDTO {
    private String reservationId;
    private String status;
    private String address;
    private String date;
    private String timeSlot;
    private String dishCount;
    private String guestsNumber;
    private String tableNumber;
    private String feedbackId;
    private String message;

    public UpdateReservationByWaiterResponseDTO() {
    }

    public UpdateReservationByWaiterResponseDTO(String reservationId, String status, String address, String date,
                                                String timeSlot, String dishCount, String guestsNumber,
                                                String tableNumber, String feedbackId, String message) {
        this.reservationId = reservationId;
        this.status = status;
        this.address = address;
        this.date = date;
        this.timeSlot = timeSlot;
        this.dishCount = dishCount;
        this.guestsNumber = guestsNumber;
        this.tableNumber = tableNumber;
        this.feedbackId = feedbackId;
        this.message = message;
    }

    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public String getDishCount() {
        return dishCount;
    }

    public void setDishCount(String dishCount) {
        this.dishCount = dishCount;
    }

    public String getGuestsNumber() {
        return guestsNumber;
    }

    public void setGuestsNumber(String guestsNumber) {
        this.guestsNumber = guestsNumber;
    }

    public String getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(String tableNumber) {
        this.tableNumber = tableNumber;
    }

    public String getFeedbackId() {
        return feedbackId;
    }

    public void setFeedbackId(String feedbackId) {
        this.feedbackId = feedbackId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("reservationId", reservationId);
        json.put("status", status);
        json.put("address", address);
        json.put("date", date);
        json.put("timeSlot", timeSlot);
        json.put("dishCount", dishCount);
        json.put("guestsNumber", guestsNumber);
        json.put("tableNumber", tableNumber);
        json.put("feedbackId", feedbackId);
        json.put("message", message);
        return json;
    }

}
