package com.restaurant.services;

import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import static com.restaurant.utils.Helper.*;
import java.util.*;
import java.util.logging.Logger;
import com.restaurant.dto.ReservationResponseDTO;
import java.time.*;
import java.time.format.DateTimeFormatter;


public class BookingService {
    private static final Logger logger = Logger.getLogger(BookingService.class.getName());
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final Table reservationTable;
    private final Table ordersTable;
    private final Table tablesTable;
    private final Table locationTable;
    private final WaiterService waiterService;

    public BookingService(DynamoDB dynamoDB, WaiterService waiterService) {
        this.reservationTable = dynamoDB.getTable(System.getenv("RESERVATIONS_TABLE"));
        this.ordersTable = dynamoDB.getTable(System.getenv("ORDERS_TABLE"));
        this.tablesTable = dynamoDB.getTable(System.getenv("TABLES_TABLE"));
        this.locationTable = dynamoDB.getTable(System.getenv("LOCATIONS_TABLE"));
        this.waiterService = waiterService;
    }

    public APIGatewayProxyResponseEvent handleCreateReservation(APIGatewayProxyRequestEvent request) {
        try {
            logger.info("Handling reservation booking request");

            // Parse request body
            Map<String, String> requestBody = parseJson(request.getBody());
            if (requestBody == null || requestBody.isEmpty()) {
                return createErrorResponse(400, "Invalid request data: Empty request body.");
            }

            // Extract user ID from JWT claims
            Map<String, Object> claims = extractClaims(request);
            logger.info("Extracted claims: " + claims); // Debugging purpose
            String userId = (String) claims.get("sub");
            String email = (String) claims.get("email");

            if (userId == null || userId.isEmpty()) {
                return createErrorResponse(401, "Unauthorized: Missing or invalid token.");
            }

            // Validate required fields in request body
            List<String> requiredFields = List.of("locationId", "tableNumber", "date", "guestsNumber", "timeFrom", "timeTo");
            for (String field : requiredFields) {
                if (!requestBody.containsKey(field) || requestBody.get(field).trim().isEmpty()) {
                    return createErrorResponse(400, "Missing required field: " + field);
                }
            }

            // Convert guestsNumber to Integer
            int guestsNumber;
            try {
                guestsNumber = Integer.parseInt(requestBody.get("guestsNumber"));
                if (guestsNumber <= 0) {
                    return createErrorResponse(400, "Invalid guestsNumber: Must be a positive integer.");
                }
            } catch (NumberFormatException e) {
                return createErrorResponse(400, "Invalid guestsNumber: Must be an integer.");
            }

            String locationId = requestBody.get("locationId");
            String tableNumber = requestBody.get("tableNumber");
            String date = requestBody.get("date");
            String timeFrom = requestBody.get("timeFrom");
            String timeTo = requestBody.get("timeTo");

            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                LocalDateTime reservationStart = LocalDateTime.parse(date + " " + timeFrom, formatter);
                ZonedDateTime reservationStartIST = reservationStart.atZone(ZoneId.of("Asia/Kolkata"));
                ZonedDateTime nowIST = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));

                if (reservationStartIST.isBefore(nowIST)) {
                    return createErrorResponse(400, "You cannot book a reservation in the past.");
                }
            } catch (Exception e) {
                logger.severe("Error parsing reservation time: " + e.getMessage());
                return createErrorResponse(400, "Invalid date or time format. Use yyyy-MM-dd for date and HH:mm for time");
            }

            // Check if table exists in Tables table
            Item tableItem = tablesTable.getItem("locationId", locationId, "tableNumber", tableNumber);
            if (tableItem == null) {
                return createErrorResponse(400, "The specified table number does not exist for the given location.");
            }

            // Check for existing overlapping reservations for same table/date
            ItemCollection<ScanOutcome> existingReservations = reservationTable.scan(
                    new ScanFilter("locationId").eq(locationId),
                    new ScanFilter("tableNumber").eq(Integer.parseInt(tableNumber)),
                    new ScanFilter("date").eq(date),
                    new ScanFilter("status").ne("Cancelled")
            );

            for (Item reservation : existingReservations) {
                String existingFrom = reservation.getString("timeFrom");
                String existingTo = reservation.getString("timeTo");
                String reservedBy = reservation.getString("email");

                // Time overlap check
                if (!(timeTo.compareTo(existingFrom) <= 0 || timeFrom.compareTo(existingTo) >= 0)) {
                    if (reservedBy.equals(email)) {
                        return createErrorResponse(409, "You have already booked this table for the same time slot.");
                    }
                    return createErrorResponse(409, "The specified table is already booked for the given date and time.");
                }
            }

            String waiterId = waiterService.assignWaiter(requestBody.get("locationId"));
            String reservationId = UUID.randomUUID().toString();
            String orderId = UUID.randomUUID().toString();
            String timeSlot = timeFrom + " - " + timeTo;

            // Save reservation
            reservationTable.putItem(new PutItemSpec().withItem(new Item()
                    .withPrimaryKey("reservationId", reservationId)
                    .withString("email", email)
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

            // Save order
            ordersTable.putItem(new PutItemSpec().withItem(new Item()
                    .withPrimaryKey("orderId", orderId, "email", email)
                    .withString("reservationId", reservationId)
                    .withString("locationId", locationId)
                    .withString("date", date)
                    .withString("state", "Submitted")
                    .withString("timeSlot", timeSlot)
                    .withList("dishItems", new ArrayList<String>())
            ));

            // Fetch location address
            Item locationItem = locationTable.getItem("locationId", locationId);
            String locationAddress = locationItem != null ? locationItem.getString("address") : null;

            ReservationResponseDTO dto = new ReservationResponseDTO(
                    reservationId,
                    "Reserved",
                    locationAddress,
                    date,
                    timeSlot,
                    "0",
                    String.valueOf(guestsNumber),
                    null // feedbackId
            );

            return createApiResponse(201, dto.toJson());

        } catch (Exception e) {
            logger.severe("Error creating reservation: " + e.getMessage());
            return createErrorResponse(500, "Error creating reservation: " + e.getMessage());
        }
    }

    public Map<String, String> parseJson(String json) {
        try {
            logger.info("Parsing JSON: " + json);  // Log incoming JSON
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            logger.severe("Error parsing JSON: " + e.getMessage());
            return Map.of();
        }
    }
}
