package com.restaurant.dto;

import com.amazonaws.services.dynamodbv2.document.Item;
import org.json.JSONObject;

public class ReportResponseDTO {
    private String id;
    private String description;
    private String downloadLink;
    private String location;
    private String waiterId;
    private String name;
    private String fromDate;
    private String toDate;

    public ReportResponseDTO() {
    }

    public ReportResponseDTO(Item item) {
        this.id = item.getString("reportId");
        this.description = item.getString("reportDescription");
        this.downloadLink = item.getString("downloadLink");
        this.location = item.getString("locationId");
        this.waiterId = item.getString("waiterId");
        this.name = item.getString("reportName");
        this.fromDate = item.getString("startPeriod");
        this.toDate = item.getString("endPeriod");
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("description", description);
        json.put("downloadLink", downloadLink);
        json.put("location", location);
        json.put("waiterId", waiterId);
        json.put("name", name);
        json.put("fromDate", fromDate);
        json.put("toDate", toDate);
        return json;
    }

    // Getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDownloadLink() {
        return downloadLink;
    }

    public void setDownloadLink(String downloadLink) {
        this.downloadLink = downloadLink;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getWaiterId() {
        return waiterId;
    }

    public void setWaiterId(String waiterId) {
        this.waiterId = waiterId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFromDate() {
        return fromDate;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public String getToDate() {
        return toDate;
    }

    public void setToDate(String toDate) {
        this.toDate = toDate;
    }
}
