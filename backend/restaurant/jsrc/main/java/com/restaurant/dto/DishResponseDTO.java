package com.restaurant.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DishResponseDTO {
    @JsonProperty("content")
    private DishDTO[] content;

    // Constructors
    public DishResponseDTO() {}

    public DishResponseDTO(DishDTO[] content) {
        this.content = content;
    }

    // Getters and Setters
    @JsonProperty("content")
    public DishDTO[] getContent() {
        return content;
    }

    public void setContent(DishDTO[] content) {
        this.content = content;
    }
}