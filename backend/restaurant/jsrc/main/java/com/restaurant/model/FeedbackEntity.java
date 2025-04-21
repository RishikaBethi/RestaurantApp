package com.restaurant.model;

import org.json.JSONObject;

public class FeedbackEntity {
    private String id;
    private String comment;
    private String cuisineComment;
    private Double cuisineRating;
    private String serviceComment;
    private Double serviceRating;
    private String date;
    private String email;
    private String reservationId;
    private String type;
    private String userAvatarUrl;
    private String userName;
    private String locationId;

    public FeedbackEntity() {}

    public FeedbackEntity(String id, String comment, String cuisineComment, Double cuisineRating, String serviceComment,
                          Double serviceRating, String date, String email, String reservationId, String type,
                          String userAvatarUrl, String userName, String locationId) {
        this.id = id;
        this.comment = comment;
        this.cuisineComment = cuisineComment;
        this.cuisineRating = cuisineRating;
        this.serviceComment = serviceComment;
        this.serviceRating = serviceRating;
        this.date = date;
        this.email = email;
        this.reservationId = reservationId;
        this.type = type;
        this.userAvatarUrl = userAvatarUrl;
        this.userName = userName;
        this.locationId = locationId;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCuisineComment() {
        return cuisineComment;
    }

    public void setCuisineComment(String cuisineComment) {
        this.cuisineComment = cuisineComment;
    }

    public Double getCuisineRating() {
        return cuisineRating;
    }

    public void setCuisineRating(Double cuisineRating) {
        this.cuisineRating = cuisineRating;
    }

    public String getServiceComment() {
        return serviceComment;
    }

    public void setServiceComment(String serviceComment) {
        this.serviceComment = serviceComment;
    }

    public Double getServiceRating() {
        return serviceRating;
    }

    public void setServiceRating(Double serviceRating) {
        this.serviceRating = serviceRating;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUserAvatarUrl() {
        return userAvatarUrl;
    }

    public void setUserAvatarUrl(String userAvatarUrl) {
        this.userAvatarUrl = userAvatarUrl;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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
                json.optString("comment", null),
                json.optString("cuisineComment", null),
                json.optDouble("cuisineRating", 0.0),
                json.optString("serviceComment", null),
                json.optDouble("serviceRating", 0.0),
                json.optString("date", null),
                json.optString("email", null),
                json.optString("reservationId", null),
                json.optString("type", null),
                json.optString("userAvatarUrl", null),
                json.optString("userName", null),
                json.optString("locationId", null)
        );
    }
}