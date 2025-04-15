package com.restaurant.dto;

public class FeedbackDTO {
    private String id;
    private String comment;
    private String cuisineComment;
    private String cuisineRating;
    private String serviceComment;
    private String serviceRating;
    private String date;
    private String email;
    private String reservationId;
    private String type;
    private String userAvatarUrl;
    private String userName;
    private String locationId;

    // Getters and setters
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

    public String getCuisineRating() {
        return cuisineRating;
    }

    public void setCuisineRating(String cuisineRating) {
        this.cuisineRating = cuisineRating;
    }

    public String getServiceComment() {
        return serviceComment;
    }

    public void setServiceComment(String serviceComment) {
        this.serviceComment = serviceComment;
    }

    public String getServiceRating() {
        return serviceRating;
    }

    public void setServiceRating(String serviceRating) {
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
}