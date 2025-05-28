package com.restaurant.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DishDTO {
    @JsonProperty("name")  // Matches API response spec
    private String dishName;  // Matches DynamoDB field name

    @JsonProperty("price")  // Matches API response spec
    private String dishPrice;  // Matches DynamoDB field name

    @JsonProperty("weight")  // Matches API response spec
    private String weight;    // Matches DynamoDB field name

    @JsonProperty("imageUrl")  // Matches API response spec
    private String dishImageUrl;  // Matches DynamoDB field name

    // Constructors
    public DishDTO() {}

    public DishDTO(String dishName, String dishPrice, String weight, String dishImageUrl) {
        this.dishName = dishName;
        this.dishPrice = dishPrice;
        this.weight = weight;
        this.dishImageUrl = dishImageUrl;
    }

    // Getters and Setters
    public String getDishName() {
        return dishName;
    }

    public void setDishName(String dishName) {
        this.dishName = dishName;
    }

    public String getDishPrice() {
        return dishPrice;
    }

    public void setDishPrice(String dishPrice) {
        this.dishPrice = dishPrice;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getDishImageUrl() {
        return dishImageUrl;
    }

    public void setDishImageUrl(String dishImageUrl) {
        this.dishImageUrl = dishImageUrl;
    }
}