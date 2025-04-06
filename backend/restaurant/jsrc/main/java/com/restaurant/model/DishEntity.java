package com.restaurant.model;

import org.json.JSONObject;

public class DishEntity {
    private String dishId;
    private Double calories;
    private Double carbohydrates;
    private String dishDescription;
    private Integer dishFrequency;
    private String dishImageUrl;
    private String dishName;
    private Double dishPrice;
    private Integer dishQuantity;
    private String dishType;
    private Double fats;
    private Double proteins;
    private String state;
    private String vitamins;
    private Double weight;

    public DishEntity() {}

    public DishEntity(String dishId, Double calories, Double carbohydrates, String dishDescription, Integer dishFrequency, String dishImageUrl, String dishName, Double dishPrice, Integer dishQuantity, String dishType, Double fats, Double proteins, String state, String vitamins, Double weight) {
        this.dishId = dishId;
        this.calories = calories;
        this.carbohydrates = carbohydrates;
        this.dishDescription = dishDescription;
        this.dishFrequency = dishFrequency;
        this.dishImageUrl = dishImageUrl;
        this.dishName = dishName;
        this.dishPrice = dishPrice;
        this.dishQuantity = dishQuantity;
        this.dishType = dishType;
        this.fats = fats;
        this.proteins = proteins;
        this.state = state;
        this.vitamins = vitamins;
        this.weight = weight;
    }

    // Getters and Setters
    public String getDishId() {
        return dishId;
    }

    public void setDishId(String dishId) {
        this.dishId = dishId;
    }

    public Double getCalories() {
        return calories;
    }

    public void setCalories(Double calories) {
        this.calories = calories;
    }

    public Double getCarbohydrates() {
        return carbohydrates;
    }

    public void setCarbohydrates(Double carbohydrates) {
        this.carbohydrates = carbohydrates;
    }

    public String getDishDescription() {
        return dishDescription;
    }

    public void setDishDescription(String dishDescription) {
        this.dishDescription = dishDescription;
    }

    public Integer getDishFrequency() {
        return dishFrequency;
    }

    public void setDishFrequency(Integer dishFrequency) {
        this.dishFrequency = dishFrequency;
    }

    public String getDishImageUrl() {
        return dishImageUrl;
    }

    public void setDishImageUrl(String dishImageUrl) {
        this.dishImageUrl = dishImageUrl;
    }

    public String getDishName() {
        return dishName;
    }

    public void setDishName(String dishName) {
        this.dishName = dishName;
    }

    public Double getDishPrice() {
        return dishPrice;
    }

    public void setDishPrice(Double dishPrice) {
        this.dishPrice = dishPrice;
    }

    public Integer getDishQuantity() {
        return dishQuantity;
    }

    public void setDishQuantity(Integer dishQuantity) {
        this.dishQuantity = dishQuantity;
    }

    public String getDishType() {
        return dishType;
    }

    public void setDishType(String dishType) {
        this.dishType = dishType;
    }

    public Double getFats() {
        return fats;
    }

    public void setFats(Double fats) {
        this.fats = fats;
    }

    public Double getProteins() {
        return proteins;
    }

    public void setProteins(Double proteins) {
        this.proteins = proteins;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getVitamins() {
        return vitamins;
    }

    public void setVitamins(String vitamins) {
        this.vitamins = vitamins;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public static DishEntity fromJson(String jsonString) {
        JSONObject json = new JSONObject(jsonString);
        return new DishEntity(
                json.optString("dishId", null),
                json.optDouble("calories", 0.0),
                json.optDouble("carbohydrates", 0.0),
                json.optString("dishDescription", null),
                json.optInt("dishFrequency", 0),
                json.optString("dishImageUrl", null),
                json.optString("dishName", null),
                json.optDouble("dishPrice", 0.0),
                json.optInt("dishQuantity", 0),
                json.optString("dishType", null),
                json.optDouble("fats", 0.0),
                json.optDouble("proteins", 0.0),
                json.optString("state", null),
                json.optString("vitamins", null),
                json.optDouble("weight", 0.0)
        );
    }
}