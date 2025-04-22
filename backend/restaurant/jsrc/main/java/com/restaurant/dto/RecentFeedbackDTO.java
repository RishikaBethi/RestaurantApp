package com.restaurant.dto;

import org.json.JSONObject;
import software.amazon.awssdk.services.cognitoidentityprovider.endpoints.internal.Value;

public class RecentFeedbackDTO {

    private String serviceComment;
    private String cuisineComment;
    private Double serviceRating;
    private Double cuisineRating;
    private String waiterName;

    public RecentFeedbackDTO(String serviceComment, String cuisineComment, Double serviceRating, Double cuisineRating, String waiterName) {
        this.serviceComment = serviceComment;
        this.cuisineComment = cuisineComment;
        this.serviceRating = serviceRating;
        this.cuisineRating = cuisineRating;
        this.waiterName = waiterName;
    }

    public String getServiceComment() {
        return serviceComment;
    }

    public String getCuisineComment() {
        return cuisineComment;
    }

    public void setCuisineComment(String cuisineComment) {
        this.cuisineComment = cuisineComment;
    }

    public Double getServiceRating() {
        return serviceRating;
    }

    public void setServiceRating(Double serviceRating) {
        this.serviceRating = serviceRating;
    }

    public String getWaiterName() {
        return waiterName;
    }

    public void setWaiterName(String waiterName) {
        this.waiterName = waiterName;
    }

    public Double getCuisineRating() {
        return cuisineRating;
    }

    public void setCuisineRating(Double cuisineRating) {
        this.cuisineRating = cuisineRating;
    }

    public void setServiceComment(String serviceComment) {
        this.serviceComment = serviceComment;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("serviceRating", serviceRating);
        json.put("serviceComment", serviceComment);
        json.put("cuisineRating", cuisineRating);
        json.put("cuisineComment", cuisineComment);
        json.put("waiterName", waiterName);
        return json;
    }
}
