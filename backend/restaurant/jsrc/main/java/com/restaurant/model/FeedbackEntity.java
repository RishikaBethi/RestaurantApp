package com.restaurant.model;

import org.json.JSONObject;

public class FeedbackEntity {
    private String id;
    private Double rate;
    private String comment;
    private String userName;
    private String userAvatarUrl;
    private String date;
    private String type;
    private String locationId;

    public FeedbackEntity() {}

    public FeedbackEntity(String id, Double rate, String comment, String userName, String userAvatarUrl, String date, String type, String locationId) {
        this.id = id;
        this.rate = rate;
        this.comment = comment;
        this.userName = userName;
        this.userAvatarUrl = userAvatarUrl;
        this.date = date;
        this.type = type;
        this.locationId = locationId;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Double getRate() {
        return rate;
    }

    public void setRate(Double rate) {
        this.rate = rate;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserAvatarUrl() {
        return userAvatarUrl;
    }

    public void setUserAvatarUrl(String userAvatarUrl) {
        this.userAvatarUrl = userAvatarUrl;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public static FeedbackEntity fromJson(String jsonString) {
        JSONObject json = new JSONObject(jsonString);
        return new FeedbackEntity(
                json.optString("id", null),
                json.optDouble("rate", 0.0),
                json.optString("comment", null),
                json.optString("userName", null),
                json.optString("userAvatarUrl", null),
                json.optString("date", null),
                json.optString("type", null),
                json.optString("locationId", null)
        );
    }
}