package com.restaurant.services;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.restaurant.dto.AvailableSlotsDTO;
import static com.restaurant.utils.Helper.createApiResponse;
import javax.inject.Inject;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;


public class TablesService {
    private final DynamoDB dynamoDB;
    private final String tableName = System.getenv("TABLES_TABLE");
    private final String reservationsTableName = System.getenv("RESERVATIONS_TABLE");
    private final String locationsTable = System.getenv("LOCATIONS_TABLE");
    private final ObjectMapper objectMapper;
    private Map<String, String> queryParams = new HashMap<>();
    private final List<String> timeSlots = List.of("10:30-11:00","12:15-1:45","14:00-3:30","15:45-17:15","17:30-19:00","19:15-20:45","21:00-22:30");

    @Inject
    public TablesService(DynamoDB dynamoDB, ObjectMapper objectMapper) {
        this.dynamoDB = dynamoDB;
        this.objectMapper = objectMapper;
    }

    public APIGatewayProxyResponseEvent returnAvailableTablesFilteredByGivenCriteria(
            APIGatewayProxyRequestEvent event, Context context) {
        try {
            queryParams = event.getQueryStringParameters();

            String locationId = queryParams.get("locationId");
            String date = queryParams.get("date");
            String guestsStr = queryParams.get("guests");
            String time = queryParams.get("time");

            if (queryParams == null) {
                return createApiResponse(200, Collections.emptyList());
            }

            LocalDate selectedDate = date != null ? LocalDate.parse(date) : LocalDate.now();
            LocalTime userTime = time != null ? LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm")) : null;

            //check if the date and time entered by user are before the current date and time
            if ((selectedDate.isBefore(LocalDate.now())) ||
                    (selectedDate.isEqual(LocalDate.now()) && userTime != null && userTime.isBefore(LocalTime.now()))) {
                return createApiResponse(400, "Date/Time cannot be in the past");
            }

            // Validate guests parameter
            int guests;
            try {
                guests = guestsStr != null ? Integer.parseInt(guestsStr) : -1;
                if (guestsStr != null && guests <= 0) {
                    return errorResponseHandler(400, "Guest capacity must be a positive integer");
                }
            } catch (NumberFormatException e) {
                return errorResponseHandler(400, "Invalid guest capacity format. Must be an integer");
            }

            // Validate date parameter
            LocalDate parsedDate;
            try {
                parsedDate = date != null ? LocalDate.parse(date) : LocalDate.now();
            } catch (DateTimeParseException e) {
                return errorResponseHandler(400, "Invalid date format. Use YYYY-MM-DD");
            }

            // Validate time parameter
            LocalTime parsedTime = null;
            if (time != null) {
                try {
                    parsedTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"));
                } catch (DateTimeParseException e) {
                    return errorResponseHandler(400, "Invalid time format. Use HH:MM");
                }
            }

            List<Item> availableTables = getAvailableTablesByLocationAndCapacity(locationId, guests);
            List<AvailableSlotsDTO> availableTimeSlots = getAvailableTimeSlots(availableTables, date, time, context);

            return createApiResponse(200, availableTimeSlots);

        } catch (Exception e) {
            context.getLogger().log("Error: " + e.getMessage());
            return errorResponseHandler(500, "Internal server error");
        }
    }

    //filter tables initially by capacity and locations sent
    //return list of tables that satisfy the 2 criteria from the tables table
    public List<Item> getAvailableTablesByLocationAndCapacity(String locationId, int guests) {
        Table table = dynamoDB.getTable(tableName);
        List<Item> tablesList = new ArrayList<>();

        if (locationId == null && guests == -1) {
            ScanSpec scanSpec = new ScanSpec();
            ItemCollection<ScanOutcome> items = table.scan(scanSpec);
            for (Item item : items) {
                tablesList.add(item);
            }
            return tablesList;
        }
        else if (locationId == null && guests != -1) {
            ScanSpec scanSpec = new ScanSpec();
            ItemCollection<ScanOutcome> items = table.scan(scanSpec);
            for (Item item : items) {
                if (guests <= item.getInt("capacity")) {
                    tablesList.add(item);
                }
            }
            return tablesList;
        }
        else if (locationId != null && guests == -1) {
            ScanSpec scanSpec = new ScanSpec()
                    .withFilterExpression("locationId = :locId")
                    .withValueMap(new ValueMap().withString(":locId", locationId));
            ItemCollection<ScanOutcome> items = table.scan(scanSpec);
            for (Item item : items) {
                tablesList.add(item);
            }
            return tablesList;
        }
        else {
            ScanSpec scanSpec = new ScanSpec()
                    .withFilterExpression("locationId = :locId")
                    .withValueMap(new ValueMap().withString(":locId", locationId));
            ItemCollection<ScanOutcome> items = table.scan(scanSpec);
            for (Item item : items) {
                if (guests <= item.getInt("capacity")) {
                    tablesList.add(item);
                }
            }
            return tablesList;
        }
    }

    //return a list of available slots for every table the list fetched from the previous function
    private List<AvailableSlotsDTO> getAvailableTimeSlots(List<Item> tablesList, String date, String time, Context context) {
        List<AvailableSlotsDTO> responseList = new ArrayList<>();


        for (Item table : tablesList) {
            String locationId = table.getString("locationId");

            // the function returns a list of slots that are not available for that table and removes it from the available slots
            List<String> notAvailable = returnNotAvailableSlots(locationId, table.getString("tableNumber"), date, time, context);

            ArrayList<String> availableSlots = new ArrayList<>(timeSlots);
            availableSlots.removeAll(notAvailable);

            Table locTable = dynamoDB.getTable(locationsTable);
            ScanSpec scanSpec = new ScanSpec()
                    .withFilterExpression("locationId = :locId")
                    .withValueMap(new ValueMap().withString(":locId", locationId));
            ItemCollection<ScanOutcome> items = locTable.scan(scanSpec);
            String locationAddress = items.iterator().hasNext() ? items.iterator().next().getString("address") : null;

            AvailableSlotsDTO dto = new AvailableSlotsDTO(
                    locationId,
                    locationAddress,
                    table.getString("tableNumber"),
                    table.getInt("capacity"),
                    availableSlots
            );
            responseList.add(dto);
        }
        return responseList;
    }

    // to return a list of time slots that are not available for that particular table
    private List<String> returnNotAvailableSlots(String locationId, String tableNumber, String date, String time, Context context) {
        Table reservationsTable = dynamoDB.getTable(reservationsTableName);
        ScanSpec scanSpec = new ScanSpec()
                .withFilterExpression("locationId = :locId and #dt = :date and tableNumber = :tableNumber")
                .withNameMap(Map.of("#dt", "date"))
                .withValueMap(new ValueMap()
                        .withString(":locId", locationId)
                        .withString(":date", date)
                        .withNumber(":tableNumber", Integer.parseInt(tableNumber)));

        ItemCollection<ScanOutcome> reservations = reservationsTable.scan(scanSpec);
        List<String> notAvailableSlots = new ArrayList<>();

        for (Item reservation : reservations) {
            if ("Reserved".equals(reservation.getString("status"))) {
                notAvailableSlots.add(reservation.getString("timeFrom")+"-"+reservation.getString("timeTo"));
            }
        }

        context.getLogger().log("All reserved slots for " + locationId + " on " + date + ": " + notAvailableSlots);
        return notAvailableSlots;
    }


    private Map<String, String> createCorsHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Access-Control-Allow-Origin", "*");
        headers.put("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        headers.put("Access-Control-Allow-Headers", "Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token");
        return Collections.unmodifiableMap(headers);
    }

    private APIGatewayProxyResponseEvent errorResponseHandler(int statusCode, String message) {
        try {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", message);

            APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
            response.setHeaders(createCorsHeaders());
            response.setStatusCode(statusCode);
            response.setBody(objectMapper.writeValueAsString(errorResponse));
            return response;
        } catch (Exception e) {
            APIGatewayProxyResponseEvent fallbackResponse = new APIGatewayProxyResponseEvent();
            fallbackResponse.setHeaders(createCorsHeaders());
            fallbackResponse.setStatusCode(500);
            fallbackResponse.setBody("{\"error\":\"Internal server error\"}");
            return fallbackResponse;
        }
    }
}