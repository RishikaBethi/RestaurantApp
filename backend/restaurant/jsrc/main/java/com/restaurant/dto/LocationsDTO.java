package com.restaurant.dto;

import org.json.JSONObject;

public class LocationsDTO {
    private String id;
    private String address;

    public LocationsDTO() {}

    public LocationsDTO(String id, String address) {
        this.id = id;
        this.address = address;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("address", address);
        return json;
    }
}