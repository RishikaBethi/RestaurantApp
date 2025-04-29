package com.restaurant.services;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.logging.Logger;

import static com.restaurant.utils.Helper.*;

public class GenerateReportsService {
    private final ReportsDispatchService reportsDispatchService;
    private static final Logger logger = Logger.getLogger(GenerateReportsService.class.getName());
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final DynamoDB dynamoDB;
    private final AmazonS3 s3Client;
    private final Table waiterStatsTable;
    private final Table locationStatsTable;
    private final Table reportsTable;
    private static final String S3_BUCKET = System.getenv("S3_BUCKET");
    private static final String WAITER_DAILY_STATS_TABLE = System.getenv("WAITER_DAILY_STATS_TABLE");
    private static final String LOCATION_DAILY_STATS_TABLE = System.getenv("LOCATION_DAILY_STATS_TABLE");
    private static final String REPORTS_TABLE = System.getenv("REPORTS_TABLE");

    @Inject
    public GenerateReportsService(ReportsDispatchService reportsDispatchService, DynamoDB dynamoDB, AmazonS3 s3Client) {
        this.reportsDispatchService = reportsDispatchService;
        this.dynamoDB = dynamoDB;
        this.s3Client = s3Client;
        this.waiterStatsTable = dynamoDB.getTable(WAITER_DAILY_STATS_TABLE);
        this.locationStatsTable = dynamoDB.getTable(LOCATION_DAILY_STATS_TABLE);
        this.reportsTable = dynamoDB.getTable(REPORTS_TABLE);
    }

    public APIGatewayProxyResponseEvent generateReports(APIGatewayProxyRequestEvent request) {
        try {
            logger.info("Received request: " + request.getBody());
            Map<String, String> requestBody = parseJson(request.getBody());

            String locationId = requestBody.get("locationId");
            String waiterId = requestBody.get("waiterId");
            String startDateStr = requestBody.get("startDate");
            String endDateStr = requestBody.get("endDate");

            logger.info("Parsed params - locationId: " + locationId + ", waiterId: " + waiterId + ", startDate: " + startDateStr + ", endDate: " + endDateStr);

            LocalDate startDate = startDateStr != null ? LocalDate.parse(startDateStr) : null;
            LocalDate endDate = endDateStr != null ? LocalDate.parse(endDateStr) : null;

            ZoneId istZone = ZoneId.of("Asia/Kolkata");
            ZonedDateTime today = ZonedDateTime.now(istZone);
            LocalDate todayLocal = today.toLocalDate();

            LocalDate defaultEndDate = endDate == null ? todayLocal : endDate;
            LocalDate defaultStartDate = startDate == null ? defaultEndDate.minusWeeks(1) : startDate;
            LocalDate prevStartDate = defaultStartDate.minusWeeks(1);
            LocalDate prevEndDate = defaultEndDate.minusWeeks(1);

            logger.info("Resolved date range - Current: " + defaultStartDate + " to " + defaultEndDate + ", Previous: " + prevStartDate + " to " + prevEndDate);

            if (waiterId != null && locationId != null) {
                List<Item> waiterItems = fetchWaiterStats(waiterId, defaultStartDate, defaultEndDate);
                String finalLocationId = locationId;
                boolean waiterBelongsToLocation = waiterItems.stream()
                        .anyMatch(item -> finalLocationId.equals(item.getString("locationId")));

                logger.info("Waiter belongs to location? " + waiterBelongsToLocation);

                if (!waiterBelongsToLocation) {
                    return createErrorResponse(400, "Waiter does not belong to the specified location.");
                }
            }

            if (locationId != null && waiterId == null) {
                logger.info("Generating report for location: " + locationId);

                List<Item> currentLocationItems = fetchLocationStats(locationId, defaultStartDate, defaultEndDate);
                List<Item> previousLocationItems = fetchLocationStats(locationId, prevStartDate, prevEndDate);

                String locationCsv = reportsDispatchService.generateLocationCSV(currentLocationItems, previousLocationItems, defaultStartDate, defaultEndDate);
                String locationReportUrl = reportsDispatchService.uploadReportToS3(locationCsv, "Location_Report_" + locationId + ".csv");

                reportsDispatchService.storeReportMetadata("N/A", "Location Report", "Location Performance Summary", locationReportUrl, locationId, "Location", defaultStartDate, defaultEndDate);
            } else if (waiterId != null) {
                logger.info("Generating report for waiter: " + waiterId);

                List<Item> currentWaiterItems = fetchWaiterStats(waiterId, defaultStartDate, defaultEndDate);
                List<Item> previousWaiterItems = fetchWaiterStats(waiterId, prevStartDate, prevEndDate);

                locationId = "N/A";
                if (!currentWaiterItems.isEmpty()) {
                    locationId = currentWaiterItems.get(0).getString("locationId");
                }

                String waiterCsv = reportsDispatchService.generateWaiterCSV(currentWaiterItems, previousWaiterItems, defaultStartDate, defaultEndDate);
                String waiterReportUrl = reportsDispatchService.uploadReportToS3(waiterCsv, "Waiter_Report_" + waiterId + ".csv");

                reportsDispatchService.storeReportMetadata(waiterId, "Waiter Report", "Staff Performance Report", waiterReportUrl, locationId, "Staff", defaultStartDate, defaultEndDate);
            } else {
                logger.info("Generating general reports for all waiters and locations...");

                List<Item> currentWaiterItems = reportsDispatchService.scanTable(waiterStatsTable, defaultStartDate, defaultEndDate);
                List<Item> previousWaiterItems = reportsDispatchService.scanTable(waiterStatsTable, prevStartDate, prevEndDate);
                List<Item> currentLocationItems = reportsDispatchService.scanTable(locationStatsTable, defaultStartDate, defaultEndDate);
                List<Item> previousLocationItems = reportsDispatchService.scanTable(locationStatsTable, prevStartDate, prevEndDate);

                String waiterCsv = reportsDispatchService.generateWaiterCSV(currentWaiterItems, previousWaiterItems, defaultStartDate, defaultEndDate);
                String locationCsv = reportsDispatchService.generateLocationCSV(currentLocationItems, previousLocationItems, defaultStartDate, defaultEndDate);

                String waiterReportUrl = reportsDispatchService.uploadReportToS3(waiterCsv, "All_Waiter_Report.csv");
                String locationReportUrl = reportsDispatchService.uploadReportToS3(locationCsv, "All_Location_Report.csv");

                reportsDispatchService.storeReportMetadata("N/A", "Staff Performance Report", "Weekly performance of waiters", waiterReportUrl, "N/A", "Staff", defaultStartDate, defaultEndDate);
                reportsDispatchService.storeReportMetadata("N/A", "Location Comparison Report", "Weekly performance by location", locationReportUrl, "N/A", "Location", defaultStartDate, defaultEndDate);
            }
            logger.info("Report generation completed successfully.");
            return createApiResponse(200, Map.of("message","Report created successfully"));
        } catch (Exception e){
            logger.severe("Error creating report: " + e.getMessage());
            return createErrorResponse(500, "Error creating report: " + e.getMessage());
        }
    }

    public List<Item> fetchWaiterStats(String waiterId, LocalDate from, LocalDate to) {
        List<Item> results = new ArrayList<>();
        ItemCollection<ScanOutcome> items = waiterStatsTable.scan();

        for (Item item : items) {
            String itemWaiterId = item.getString("waiterId");
            LocalDate itemDate = LocalDate.parse(item.getString("date"));

            boolean matchWaiter = waiterId == null || waiterId.equals(itemWaiterId);
            boolean inRange = !itemDate.isBefore(from) && !itemDate.isAfter(to);

            if (matchWaiter && inRange) {
                results.add(item);
            }
        }
        return results;
    }

    public List<Item> fetchLocationStats(String locationId, LocalDate from, LocalDate to) {
        List<Item> results = new ArrayList<>();
        ItemCollection<ScanOutcome> items = locationStatsTable.scan();

        for (Item item : items) {
            String itemLocationId = item.getString("locationId");
            LocalDate itemDate = LocalDate.parse(item.getString("date"));

            boolean matchLocation = locationId == null || locationId.equals(itemLocationId);
            boolean inRange = !itemDate.isBefore(from) && !itemDate.isAfter(to);

            if (matchLocation && inRange) {
                results.add(item);
            }
        }
        return results;
    }

    public Map<String, String> parseJson(String json) {
        try {
            logger.info("Parsing JSON: " + json);
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            logger.severe("Error parsing JSON: " + e.getMessage());
            return Map.of();
        }
    }
}
