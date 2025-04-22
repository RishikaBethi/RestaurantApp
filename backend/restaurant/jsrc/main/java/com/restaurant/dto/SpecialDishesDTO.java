package com.restaurant.dto;


public class SpecialDishesDTO {
    private String name;
    private String price;
    private String weight;
    private String imageUrl;

    // Default constructor for Jackson deserialization
    public SpecialDishesDTO() {}

    // Parameterized constructor
    public SpecialDishesDTO(String name, String price, String weight, String imageUrl) {
        this.name = name;
        this.price = price;
        this.weight = weight;
        this.imageUrl = imageUrl;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
