package com.restaurant.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DishIdDTO {
    @JsonProperty("calories")
    private String calories;

    @JsonProperty("carbohydrates")
    private String carbohydrates;

    @JsonProperty("description")
    private String description;

    @JsonProperty("dishType")
    private String dishType;

    @JsonProperty("fats")
    private String fats;

    @JsonProperty("id")
    private String id;

    @JsonProperty("imageUrl")
    private String imageUrl;

    @JsonProperty("name")
    private String name;

    @JsonProperty("price")
    private String price;

    @JsonProperty("proteins")
    private String proteins;

    @JsonProperty("state")
    private String state;

    @JsonProperty("vitamins")
    private String vitamins;

    @JsonProperty("weight")
    private String weight;

    @JsonIgnore // Exclude dishFrequency from response but use for sorting
    private Integer dishFrequency;

    // Constructors
    public DishIdDTO() {}

    public DishIdDTO(String calories, String carbohydrates, String description, String dishType, String fats,
                     String id, String imageUrl, String name, String price, String proteins, String state,
                     String vitamins, String weight, Integer dishFrequency) {
        this.calories = calories != null ? calories : "";
        this.carbohydrates = carbohydrates != null ? carbohydrates : "";
        this.description = description != null ? description : "";
        this.dishType = dishType != null ? dishType : "";
        this.fats = fats != null ? fats : "";
        this.id = id != null ? id : "";
        this.imageUrl = imageUrl != null ? imageUrl : "";
        this.name = name != null ? name : "";
        this.price = price != null ? price : "";
        this.proteins = proteins != null ? proteins : "";
        this.state = state != null ? state : "Available";
        this.vitamins = vitamins != null ? vitamins : "";
        this.weight = weight != null ? weight : "";
        this.dishFrequency = dishFrequency != null ? dishFrequency : 0;
    }

    // Getters and Setters
    @JsonProperty("calories")
    public String getCalories() {
        return calories != null ? calories : "";
    }

    public void setCalories(String calories) {
        this.calories = calories;
    }

    @JsonProperty("carbohydrates")
    public String getCarbohydrates() {
        return carbohydrates != null ? carbohydrates : "";
    }

    public void setCarbohydrates(String carbohydrates) {
        this.carbohydrates = carbohydrates;
    }

    @JsonProperty("description")
    public String getDescription() {
        return description != null ? description : "";
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("dishType")
    public String getDishType() {
        return dishType != null ? dishType : "";
    }

    public void setDishType(String dishType) {
        this.dishType = dishType;
    }

    @JsonProperty("fats")
    public String getFats() {
        return fats != null ? fats : "";
    }

    public void setFats(String fats) {
        this.fats = fats;
    }

    @JsonProperty("id")
    public String getId() {
        return id != null ? id : "";
    }

    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("imageUrl")
    public String getImageUrl() {
        return imageUrl != null ? imageUrl : "";
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @JsonProperty("name")
    public String getName() {
        return name != null ? name : "";
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("price")
    public String getPrice() {
        return price != null ? price : "";
    }

    public void setPrice(String price) {
        this.price = price;
    }

    @JsonProperty("proteins")
    public String getProteins() {
        return proteins != null ? proteins : "";
    }

    public void setProteins(String proteins) {
        this.proteins = proteins;
    }

    @JsonProperty("state")
    public String getState() {
        return state != null ? state : "Available";
    }

    public void setState(String state) {
        this.state = state;
    }

    @JsonProperty("vitamins")
    public String getVitamins() {
        return vitamins != null ? vitamins : "";
    }

    public void setVitamins(String vitamins) {
        this.vitamins = vitamins;
    }

    @JsonProperty("weight")
    public String getWeight() {
        return weight != null ? weight : "";
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public Integer getDishFrequency() {
        return dishFrequency != null ? dishFrequency : 0;
    }

    public void setDishFrequency(Integer dishFrequency) {
        this.dishFrequency = dishFrequency;
    }
}