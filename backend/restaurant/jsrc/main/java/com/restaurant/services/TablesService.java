package com.restaurant.services;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.restaurant.dto.AvailableSlotsDTO;
import org.json.JSONArray;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import static com.restaurant.utils.Helper.createApiResponse;
import static com.restaurant.utils.Helper.createErrorResponse;

public class TablesService {
    private final DynamoDB dynamoDB;
    private final String tableName = System.getenv("TABLES_TABLE");
    private final String reservationsTableName = System.getenv("RESERVATIONS_TABLE");
    private final String locationsTable = System.getenv("LOCATIONS_TABLE");
    //private final ObjectMapper objectMapper;
    private Map<String, String> queryParams = new HashMap<>();
    private final List<String> timeSlots = List.of("10:30-12:00","12:15-13:45","14:00-15:30","15:45-17:15","17:30-19:00","19:15-20:45","21:00-22:30");

    @Inject
    public TablesService(DynamoDB dynamoDB) {
        this.dynamoDB = dynamoDB;
    }

    public APIGatewayProxyResponseEvent returnAvailableTablesFilteredByGivenCriteria(
            APIGatewayProxyRequestEvent event, Context context) {
        try {
            queryParams = event.getQueryStringParameters();
            if (queryParams == null) {
                return createApiResponse(200, Collections.emptyList());
            }

            String locationId = queryParams.get("locationId");
            String date = queryParams.get("date");
            String guestsStr = queryParams.get("guests");
            String time = queryParams.get("time");

            // Validate guests parameter
            int guests;
            try {
                guests = guestsStr != null ? Integer.parseInt(guestsStr) : -1;
                if (guestsStr != null && guests <= 0) {
                    return createErrorResponse(400, "Guest capacity must be a positive integer");
                }
            } catch (NumberFormatException e) {
                return createErrorResponse(400, "Invalid guest capacity format. Must be an integer");
            }
            LocalDate selectedDate = null;
            if (date != null) {
                try {
                    selectedDate = LocalDate.parse(date);
                } catch (DateTimeParseException e) {
                    return createErrorResponse(400, "Invalid date format. Use YYYY-MM-DD");
                }
            }

            LocalTime userTime = null;
            if (time != null) {
                try {
                    userTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"));
                } catch (DateTimeParseException e) {
                    return createErrorResponse(400, "Invalid time format. Use HH:MM");
                }
            }

            LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));

            if (date != null && time != null) {
                LocalDateTime selectedDateTime = LocalDateTime.of(selectedDate, userTime);
                if (selectedDateTime.isBefore(now)) {
                    return createErrorResponse(400, "Date/time cannot be selected in the past");
                }
            } else if (date != null) {
                if (selectedDate.isBefore(now.toLocalDate())) {
                    return createErrorResponse(400, "Date cannot be selected in the past");
                }
            } else if (time != null) {
                LocalDateTime selectedDateTime = LocalDateTime.of(now.toLocalDate(), userTime);
                if (selectedDateTime.isBefore(now)) {
                    return createErrorResponse(400, "Time cannot be selected in the past");
                }
            }
            

            if(locationId!=null) {
                Table locationsData = dynamoDB.getTable(locationsTable);
                boolean locationExists = false;
                ScanSpec scanSpecLocations = new ScanSpec();
                ItemCollection<ScanOutcome> loctionsItem = locationsData.scan(scanSpecLocations);
                for (Item location : loctionsItem) {
                    String address = location.getString("locationId");
                    if (address.equals(locationId)) {
                        locationExists = true;
                        break;
                    }
                }

                if (!locationExists) {
                    return createErrorResponse(404, "Location not found");
                }
            }

            List<Item> availableTables = getAvailableTablesByLocationAndCapacity(locationId, guests);
            if(availableTables.isEmpty()) {
                return createErrorResponse(400, "We are sorry! We couldn't find tables as per your criteria :(");
            }
            List<AvailableSlotsDTO> availableTimeSlots = getAvailableTimeSlots(availableTables, date, time, context);

            JSONArray jsonArray = new JSONArray();
            for (AvailableSlotsDTO dto : availableTimeSlots) {
                jsonArray.put(dto.toJson());
            }
            return createApiResponse(200, jsonArray);

        } catch (Exception e) {
            context.getLogger().log("Error: " + e.getMessage());
            return createErrorResponse(500, "Internal server error");
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

        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
        LocalDate currentDate = now.toLocalDate();
        LocalTime currentTime = now.toLocalTime();
        LocalDate effectiveDate = (date != null) ? LocalDate.parse(date) : currentDate;

        for (Item table : tablesList) {
            String locationId = table.getString("locationId");

            // the function returns a list of slots that are not available for that table and removes it from the available slots
            List<String> notAvailable = returnNotAvailableSlots(locationId, table.getString("tableNumber"), date, time, context);

            ArrayList<String> availableSlots = new ArrayList<>(timeSlots);
            availableSlots.removeAll(notAvailable);

            //to filter time slots after user selected time slots
            if(time!=null) {
                List<String> timeSlotsBefore = new ArrayList<>();
                for (String slot : availableSlots) {
                    LocalTime timeFrom = LocalTime.parse(slot.split("-")[0], DateTimeFormatter.ofPattern("HH:mm"));
                    LocalTime userTime = LocalTime.parse(time);
                    if (timeFrom.isBefore(userTime)) {
                        timeSlotsBefore.add(slot);
                    }
                }
                availableSlots.removeAll(timeSlotsBefore);
            }

            //to remove time slots from today that have passed
            if (effectiveDate.equals(currentDate)) {
                availableSlots.removeIf(slot -> {
                    String[] times = slot.split("-");
                    LocalTime slotStartTime = LocalTime.parse(times[0], DateTimeFormatter.ofPattern("HH:mm"));
                    return slotStartTime.isBefore(currentTime);
                });
            }

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
}