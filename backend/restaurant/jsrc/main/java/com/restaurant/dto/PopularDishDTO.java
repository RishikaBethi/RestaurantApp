package com.restaurant.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PopularDishDTO {
    @JsonProperty("name")
    private String name;

    @JsonProperty("price")
    private String price;

    @JsonProperty("weight")
    private String weight;

    @JsonProperty("imageUrl")
    private String imageUrl;

    @JsonIgnore // Exclude dishFrequency from response but use for sorting
    private Integer dishFrequency;

    // Constructors
    public PopularDishDTO() {}

    public PopularDishDTO(String name, String price, String weight, String imageUrl, Integer dishFrequency) {
        this.name = name != null ? name : "";
        this.price = price != null ? price : "";
        this.weight = weight != null ? weight : "";
        this.imageUrl = imageUrl != null ? imageUrl : "";
        this.dishFrequency = dishFrequency != null ? dishFrequency : 0;
    }

    // Getters and Setters
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

    @JsonProperty("weight")
    public String getWeight() {
        return weight != null ? weight : "";
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    @JsonProperty("imageUrl")
    public String getImageUrl() {
        return imageUrl != null ? imageUrl : "";
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getDishFrequency() {
        return dishFrequency != null ? dishFrequency : 0;
    }

    public void setDishFrequency(Integer dishFrequency) {
        this.dishFrequency = dishFrequency;
    }
}