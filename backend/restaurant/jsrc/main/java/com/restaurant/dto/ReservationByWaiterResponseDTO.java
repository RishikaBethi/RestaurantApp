package com.restaurant.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReservationByWaiterResponseDTO {

    @JsonProperty("id")
    private String id;

    @JsonProperty("status")
    private String status;

    @JsonProperty("locationAddress")
    private String locationAddress;

    @JsonProperty("date")
    private String date;

    @JsonProperty("timeSlot")
    private String timeSlot;

    @JsonProperty("preOrder")
    private String preOrder;

    @JsonProperty("guestsNumber")
    private String guestsNumber;

    @JsonProperty("feedbackId")
    private String feedbackId;

    @JsonProperty("tableNumber")
    private String tableNumber;

    @JsonProperty("userInfo")
    private String userInfo;

    // Constructor
    public ReservationByWaiterResponseDTO(String id, String status, String locationAddress, String date, String timeSlot,
                                  String preOrder, String guestsNumber, String feedbackId,
                                  String tableNumber, String userInfo) {
        this.id = id;
        this.status = status;
        this.locationAddress = locationAddress;
        this.date = date;
        this.timeSlot = timeSlot;
        this.preOrder = preOrder;
        this.guestsNumber = guestsNumber;
        this.feedbackId = feedbackId;
        this.tableNumber = tableNumber;
        this.userInfo = userInfo;
    }

    public ReservationByWaiterResponseDTO() {
    }

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

    public String getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(String tableNumber) {
        this.tableNumber = tableNumber;
    }

    public String getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(String userInfo) {
        this.userInfo = userInfo;
    }

    public String toJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert DTO to JSON", e);
        }
    }
}
