package com.restaurant.services;

import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.restaurant.dto.ReservationByWaiterResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import static com.restaurant.utils.Helper.*;
import com.restaurant.utils.Helper;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Logger;

public class BookingsByWaiterService {
    private static final Logger logger = Logger.getLogger(BookingsByWaiterService.class.getName());
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final Table reservationsTable;
    private final Table ordersTable;
    private final Table tablesTable;
    private final Table locationTable;
    private final Table waitersTable;
    private final Table usersTable;
    private final Table feedbacksTable;

    public BookingsByWaiterService(DynamoDB dynamoDB) {
        this.reservationsTable = dynamoDB.getTable(System.getenv("RESERVATIONS_TABLE"));
        this.ordersTable = dynamoDB.getTable(System.getenv("ORDERS_TABLE"));
        this.tablesTable = dynamoDB.getTable(System.getenv("TABLES_TABLE"));
        this.locationTable = dynamoDB.getTable(System.getenv("LOCATIONS_TABLE"));
        this.waitersTable = dynamoDB.getTable(System.getenv("WAITERS_TABLE"));
        this.usersTable = dynamoDB.getTable(System.getenv("USERS_TABLE"));
        this.feedbacksTable = dynamoDB.getTable(System.getenv("FEEDBACKS_TABLE"));
    }

    private static final Map<String, Integer> waiterVisitorCount = new HashMap<>();

    public APIGatewayProxyResponseEvent handleReservationByWaiter(APIGatewayProxyRequestEvent request) {
        try {
            logger.info("Handling reservation by waiter");

            Map<String, String> requestBody = parseJson(request.getBody());
            if (requestBody == null || requestBody.isEmpty()) {
                return createErrorResponse(400, "Invalid request body.");
            }

            // Extract waiter identity
            Map<String, Object> claims = Helper.extractClaims(request);
            String email = (String) claims.get("email");
            if (email == null || email.isEmpty()) {
                return Helper.createErrorResponse(401, "Unauthorized: Missing email claim.");
            }

            // Lookup waiter using GSI (email)
            Index emailIndex = waitersTable.getIndex("email-index");
            Item waiter = null;
            ItemCollection<QueryOutcome> items = emailIndex.query("email", email);
            Iterator<Item> iterator = items.iterator();
            if (iterator.hasNext()) {
                waiter = iterator.next();
            }

            if (waiter == null) {
                return Helper.createErrorResponse(403, "Unauthorized: Waiter not found.");
            }

            String waiterId = waiter.getString("waiterId");
            String waiterLocationId = waiter.getString("locationId");
            String locationId = waiterLocationId;

            // Remove locationId from required fields
            List<String> requiredFields = new ArrayList<>(List.of("tableNumber", "date", "guestsNumber", "timeFrom", "timeTo", "clientType"));
            for (String field : requiredFields) {
                if (!requestBody.containsKey(field) || requestBody.get(field).trim().isEmpty()) {
                    return Helper.createErrorResponse(400, "Missing required field: " + field);
                }
            }


            String clientType = requestBody.get("clientType");
            String customerEmail = null;
            String userEmail;
            String fullName;

            if ("CUSTOMER".equalsIgnoreCase(clientType)) {
                requiredFields.add("customerEmail");
                customerEmail = requestBody.get("customerEmail");

                if (!isCustomerRegistered(customerEmail)) {
                    return Helper.createErrorResponse(400, "Customer is not registered. Please ask the customer to sign up.");
                }
                fullName = getUserFullName(customerEmail);
                userEmail = customerEmail;

            } else if ("VISITOR".equalsIgnoreCase(clientType)) {
                userEmail = email;
                customerEmail = email;
                fullName = getUserFullName(userEmail);
            } else {
                return Helper.createErrorResponse(400, "Invalid clientType. Must be either 'CUSTOMER' or 'VISITOR'");
            }

            if (fullName == null || fullName.isBlank()) {
                return Helper.createErrorResponse(500, "Could not fetch user name for " + userEmail);
            }

            // Parse and validate guestsNumber
            int guestsNumber;
            try {
                guestsNumber = Integer.parseInt(requestBody.get("guestsNumber"));
                if (guestsNumber <= 0) {
                    return Helper.createErrorResponse(400, "guestsNumber must be a positive integer.");
                }
            } catch (NumberFormatException e) {
                return Helper.createErrorResponse(400, "guestsNumber must be an integer.");
            }

            // Time and table details
            String tableNumber = requestBody.get("tableNumber");
            String date = requestBody.get("date");
            String timeFrom = requestBody.get("timeFrom");
            String timeTo = requestBody.get("timeTo");

            // Time validation
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                LocalDateTime reservationStart = LocalDateTime.parse(date + " " + timeFrom, formatter);
                ZonedDateTime reservationStartIST = reservationStart.atZone(ZoneId.of("Asia/Kolkata"));
                ZonedDateTime nowIST = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
                if (reservationStartIST.isBefore(nowIST)) {
                    return Helper.createErrorResponse(400, "Reservation cannot be made for a past time.");
                }
            } catch (Exception e) {
                return Helper.createErrorResponse(400, "Invalid date/time format. Use yyyy-MM-dd and HH:mm");
            }

            try {
                int tableNumberInt = Integer.parseInt(tableNumber);
                PrimaryKey tableKey = new PrimaryKey("locationId", locationId, "tableNumber", tableNumberInt);
                Item tableItem = tablesTable.getItem(tableKey);

                if (tableItem == null) {
                    return Helper.createErrorResponse(400, "Specified table does not exist.");
                }

                int tableCapacity = tableItem.getInt("capacity");
                if(guestsNumber > tableCapacity){
                    return Helper.createErrorResponse(400, "guestsNumber exceeds the table's capacity of " + tableCapacity + ".");
                }
            } catch (NumberFormatException e) {
                return Helper.createErrorResponse(400, "Invalid table number format. Must be a number.");
            }catch (Exception e) {
                return Helper.createErrorResponse(400, "Error retrieving table capacity");
            }

            // Overlapping reservation check
            ItemCollection<ScanOutcome> existingReservations = reservationsTable.scan(
                    new ScanFilter("locationId").eq(locationId),
                    new ScanFilter("tableNumber").eq(Integer.parseInt(tableNumber)),
                    new ScanFilter("date").eq(date),
                    new ScanFilter("status").ne("Cancelled")
            );

            for (Item reservation : existingReservations) {
                String existingFrom = reservation.getString("timeFrom");
                String existingTo = reservation.getString("timeTo");

                if (!(timeTo.compareTo(existingFrom) <= 0 || timeFrom.compareTo(existingTo) >= 0)) {
                    return Helper.createErrorResponse(409, "Table is already booked for the selected time.");
                }
            }

            // Create reservation and order
            String reservationId = UUID.randomUUID().toString();
            String orderId = UUID.randomUUID().toString();
            String timeSlot = timeFrom + " - " + timeTo;

            reservationsTable.putItem(new PutItemSpec().withItem(new Item()
                    .withPrimaryKey("reservationId", reservationId)
                    .withString("email", customerEmail)
                    .withString("waiterId", waiterId)
                    .withString("locationId", locationId)
                    .withNumber("tableNumber", Integer.parseInt(tableNumber))
                    .withString("date", date)
                    .withNumber("guestsNumber", guestsNumber)
                    .withString("timeFrom", timeFrom)
                    .withString("timeTo", timeTo)
                    .withString("status", "Reserved")
                    .withString("orderId", orderId)
            ));

            ordersTable.putItem(new PutItemSpec().withItem(new Item()
                    .withPrimaryKey("orderId", orderId, "email", userEmail)
                    .withString("reservationId", reservationId)
                    .withString("locationId", locationId)
                    .withString("status", "Submitted")
            ));

            Item orderItem = ordersTable.getItem("orderId", orderId, "email", userEmail);
            int preOrderCount = 0;
            if (orderItem != null && orderItem.hasAttribute("dishItems")) {
                Map<String, Number> dishItems = orderItem.getMap("dishItems");
                preOrderCount = dishItems != null ? dishItems.size() : 0;
            }

            Item locationItem = locationTable.getItem("locationId", locationId);
            String locationAddress = locationItem != null ? locationItem.getString("address") : null;

            ZonedDateTime nowIST = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
            ZonedDateTime fromTimeIST = LocalDateTime.parse(date + " " + timeFrom, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).atZone(ZoneId.of("Asia/Kolkata"));
            ZonedDateTime toTimeIST = LocalDateTime.parse(date + " " + timeTo, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")).atZone(ZoneId.of("Asia/Kolkata"));

            String status;
            if (nowIST.isBefore(fromTimeIST)) {
                status = "reserved";
            } else if (nowIST.isAfter(toTimeIST)) {
                status = "finished";
            } else {
                status = "in-progress";
            }

            String userInfo;
            if (clientType.equalsIgnoreCase("VISITOR")) {
                int currentCount = waiterVisitorCount.getOrDefault(waiterId, 0) + 1;
                waiterVisitorCount.put(waiterId, currentCount);
                userInfo = "WAITER " + fullName + " (Visitor " + currentCount + ")";
            } else {
                userInfo = "CUSTOMER " + fullName;
            }

            ReservationByWaiterResponseDTO responseDTO = new ReservationByWaiterResponseDTO(
                    reservationId,
                    status,
                    locationAddress,
                    date,
                    timeFrom + " - " + timeTo,
                    String.valueOf(preOrderCount),
                    String.valueOf(guestsNumber),
                    "",
                    tableNumber,
                    userInfo
            );

            String responseBody = objectMapper.writeValueAsString(responseDTO);
            return Helper.createApiResponse(201, responseBody);

        } catch (Exception e) {
            logger.severe("Error processing reservation by waiter: " + e.getMessage());
            return Helper.createErrorResponse(500, "Internal error: " + e.getMessage());
        }
    }


    private String getUserFullName(String email) {
        try {
            Item item = usersTable.getItem("email", email);
            if (item == null) return null;
            return item.getString("firstName") + " " + item.getString("lastName");
        } catch (Exception e) {
            logger.severe("Error fetching user name: " + e.getMessage());
            return null;
        }
    }

    private boolean isCustomerRegistered(String email) {
        try {
            Item item = usersTable.getItem("email", email);
            return item != null;
        } catch (Exception e) {
            logger.severe("Failed to query users table: " + e.getMessage());
            return false;
        }
    }

    private Map<String, String> parseJson(String json) {
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            logger.severe("JSON parsing error: " + e.getMessage());
            return Map.of();
        }
    }
}



