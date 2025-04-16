//package com.restaurant.dto;
//
//import com.fasterxml.jackson.annotation.JsonIgnore;
//import com.fasterxml.jackson.annotation.JsonProperty;
//
//public class DishDTO {
//    @JsonProperty("id")
//    private String id;
//
//    @JsonProperty("name")
//    private String name;
//
//    @JsonProperty("previewImageUrl")
//    private String previewImageUrl;
//
//    @JsonProperty("price")
//    private String price;
//
//    @JsonProperty("state")
//    private String state;
//
//    @JsonProperty("weight")
//    private String weight;
//
//    @JsonIgnore // Exclude dishFrequency from response but use for sorting
//    private Integer dishFrequency;
//
//    @JsonIgnore // Exclude dishFrequency from response but use for sorting
//    private String dishType;
//    // Constructors
//    public DishDTO() {}
//    public DishDTO(String id, String name, String previewImageUrl, String price) {
//        this.id = id != null ? id : "";
//        this.name = name != null ? name : "";
//        this.previewImageUrl = previewImageUrl != null ? previewImageUrl : "";
//        this.price = price != null ? price : "";
//
//    }
//    public DishDTO(String id, String name, String previewImageUrl, String price, String state, String weight, Integer dishFrequency) {
//        this.id = id != null ? id : "";
//        this.name = name != null ? name : "";
//        this.previewImageUrl = previewImageUrl != null ? previewImageUrl : "";
//        this.price = price != null ? price : "";
//        this.state = state != null ? state : "Available";
//        this.weight = weight != null ? weight : "";
//        this.dishFrequency = dishFrequency != null ? dishFrequency : 0;
//    }
//
//    // Getters and Setters
//    @JsonProperty("id")
//    public String getId() {
//        return id != null ? id : "";
//    }
//
//    public void setId(String id) {
//        this.id = id;
//    }
//
//    @JsonProperty("name")
//    public String getName() {
//        return name != null ? name : "";
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    @JsonProperty("previewImageUrl")
//    public String getPreviewImageUrl() {
//        return previewImageUrl != null ? previewImageUrl : "";
//    }
//
//    public void setPreviewImageUrl(String previewImageUrl) {
//        this.previewImageUrl = previewImageUrl;
//    }
//
//    @JsonProperty("price")
//    public String getPrice() {
//        return price != null ? price : "";
//    }
//
//    public void setPrice(String price) {
//        this.price = price;
//    }
//
//    @JsonProperty("state")
//    public String getState() {
//        return state != null ? state : "Available";
//    }
//
//    public void setState(String state) {
//        this.state = state;
//    }
//
//    @JsonProperty("weight")
//    public String getWeight() {
//        return weight != null ? weight : "";
//    }
//
//    public void setWeight(String weight) {
//        this.weight = weight;
//    }
//
//    public Integer getDishFrequency() {
//        return dishFrequency != null ? dishFrequency : 0;
//    }
//
//    public void setDishFrequency(Integer dishFrequency) {
//        this.dishFrequency = dishFrequency;
//    }
//
//    public String getDishType() {
//        return dishType!=null ? dishType : "";
//    }
//}
package com.restaurant.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a dish data transfer object for API responses.
 *
 * <h3>API Response Metadata</h3>
 * <table>
 *   <tr><th>Code</th><th>Description</th><th>Links</th></tr>
 *   <tr><td>200</td><td>Successful operation</td><td></td></tr>
 *   <tr><td></td><td>Example Description: Successful menu retrieval, in service</td><td></td></tr>
 * </table>
 */
public class DishDTO {
    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("previewImageUrl")
    private String previewImageUrl;

    @JsonProperty("price")
    private String price;

    @JsonProperty("state")
    private String state;

    @JsonProperty("weight")
    private String weight;

    @JsonIgnore // Exclude dishFrequency from response but use for sorting
    private Integer dishFrequency;

    @JsonIgnore // Exclude dishType from response but use for filtering
    private String dishType;

    // Constructors
    public DishDTO() {}

    public DishDTO(String id, String name, String previewImageUrl, String price) {
        this.id = id != null ? id : "";
        this.name = name != null ? name : "";
        this.previewImageUrl = previewImageUrl != null ? previewImageUrl : "";
        this.price = price != null ? price : "";
    }

    public DishDTO(String id, String name, String previewImageUrl, String price, String state, String weight, String dishType, Integer dishFrequency) {
        this.id = id != null ? id : "";
        this.name = name != null ? name : "";
        this.previewImageUrl = previewImageUrl != null ? previewImageUrl : "";
        this.price = price != null ? price : "";
        this.state = state != null ? state : "Available";
        this.weight = weight != null ? weight : "";
        this.dishType = dishType != null ? dishType : "";
        this.dishFrequency = dishFrequency != null ? dishFrequency : 0;
    }

    // Getters and Setters
    @JsonProperty("id")
    public String getId() {
        return id != null ? id : "";
    }

    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("name")
    public String getName() {
        return name != null ? name : "";
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("previewImageUrl")
    public String getPreviewImageUrl() {
        return previewImageUrl != null ? previewImageUrl : "";
    }

    public void setPreviewImageUrl(String previewImageUrl) {
        this.previewImageUrl = previewImageUrl;
    }

    @JsonProperty("price")
    public String getPrice() {
        return price != null ? price : "";
    }

    public void setPrice(String price) {
        this.price = price;
    }

    @JsonProperty("state")
    public String getState() {
        return state != null ? state : "Available";
    }

    public void setState(String state) {
        this.state = state;
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

    public String getDishType() {
        return dishType != null ? dishType : "";
    }

    public void setDishType(String dishType) {
        this.dishType = dishType;
    }
}