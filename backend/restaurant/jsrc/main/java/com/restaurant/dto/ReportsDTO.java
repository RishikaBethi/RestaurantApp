package com.restaurant.dto;

import java.util.List;

public class ReportsDTO {
    private String reportName;
    private String reportDescription;
    private String generatedDate;
    private List<WaiterReportDTO> waiterReports;
    private List<LocationReportDTO> locationReports;
    private String downloadLink;

    // Getters and setters

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public String getReportDescription() {
        return reportDescription;
    }

    public void setReportDescription(String reportDescription) {
        this.reportDescription = reportDescription;
    }

    public String getGeneratedDate() {
        return generatedDate;
    }

    public void setGeneratedDate(String generatedDate) {
        this.generatedDate = generatedDate;
    }

    public List<WaiterReportDTO> getWaiterReports() {
        return waiterReports;
    }

    public void setWaiterReports(List<WaiterReportDTO> waiterReports) {
        this.waiterReports = waiterReports;
    }

    public List<LocationReportDTO> getLocationReports() {
        return locationReports;
    }

    public void setLocationReports(List<LocationReportDTO> locationReports) {
        this.locationReports = locationReports;
    }

    public String getDownloadLink() {
        return downloadLink;
    }

    public void setDownloadLink(String downloadLink) {
        this.downloadLink = downloadLink;
    }
}
