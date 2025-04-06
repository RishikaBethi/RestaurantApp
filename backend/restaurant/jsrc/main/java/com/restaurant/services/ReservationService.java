package com.restaurant.services;

import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.restaurant.services.WaiterService;
import java.util.logging.Logger;
import java.util.*;
import com.restaurant.utils.Helper;

public class ReservationService {
    private static final Logger logger = Logger.getLogger(ReservationService.class.getName());
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String RESERVATIONS_TABLE = System.getenv("RESERVATIONS_TABLE");
    private static final String LOCATIONS_TABLE = System.getenv("LOCATIONS_TABLE");
    private static final String ORDERS_TABLE = System.getenv("ORDERS_TABLE");
    private static final String TABLES_TABLE = System.getenv("TABLES_TABLE");

    private static final String EMAIL_NAME = "email-index";
    private final Table reservationTable;
    private final Table locationTable;
    private final Table ordersTable;
    private final Table tablesTable;
    private final WaiterService waiterService;
    private final DynamoDB dynamoDB;

    public ReservationService(DynamoDB dynamoDB, WaiterService waiterService) {
        this.dynamoDB = dynamoDB;
        this.waiterService = waiterService;
        this.reservationTable = dynamoDB.getTable(RESERVATIONS_TABLE);
        this.locationTable = dynamoDB.getTable(LOCATIONS_TABLE);
        this.ordersTable = dynamoDB.getTable(ORDERS_TABLE);
        this.tablesTable = dynamoDB.getTable(TABLES_TABLE);
    }

    public APIGatewayProxyResponseEvent handleCreateReservation(APIGatewayProxyRequestEvent request) {
        try {
            logger.info("Handling reservation booking request");
            Map<String, String> requestBody = parseJson(request.getBody());
            if (requestBody == null || requestBody.isEmpty()) {
                return Helper.createErrorResponse(400, "Invalid request data: Empty request body.");
            }
            Map<String, Object> claims = Helper.extractClaims(request);
            logger.info("Extracted claims: " + claims);
            String userId = (String) claims.get("sub");
            String email = (String) claims.get("email");
            if (userId == null || userId.isEmpty()) {
                return Helper.createErrorResponse(401, "Unauthorized: Missing or invalid token.");
            }
            List<String> requiredFields = List.of("locationId", "tableNumber", "date", "guestsNumber", "timeFrom", "timeTo");
            for (String field : requiredFields) {
                if (!requestBody.containsKey(field) || requestBody.get(field).trim().isEmpty()) {
                    return Helper.createErrorResponse(400, "Missing required field: " + field);
                }
            }
            int guestsNumber;
            try {
                guestsNumber = Integer.parseInt(requestBody.get("guestsNumber"));
                if (guestsNumber <= 0) {
                    return Helper.createErrorResponse(400, "Invalid guestsNumber: Must be a positive integer.");
                }
            } catch (NumberFormatException e) {
                return Helper.createErrorResponse(400, "Invalid guestsNumber: Must be an integer.");
            }


            //check if a table exists or not
            if (!isTableAvailable(requestBody.get("locationId"), requestBody.get("tableNumber"))) {
                return Helper.createErrorResponse(404, "Table " + requestBody.get("tableNumber") + " not found at the specified location.");
            }

            if (isDuplicateReservation(email, requestBody.get("locationId"), Integer.parseInt(requestBody.get("tableNumber")),
                    requestBody.get("date"), requestBody.get("timeFrom"), requestBody.get("timeTo"))) {
                return Helper.createErrorResponse(409, "You already have a reservation for this table during the selected time.");
            }

            if (isTableAlreadyBooked(requestBody.get("locationId"),
                    Integer.parseInt(requestBody.get("tableNumber")),
                    requestBody.get("date"),
                    requestBody.get("timeFrom"),
                    requestBody.get("timeTo"))) {
                return Helper.createErrorResponse(409, "This table is already booked for the selected time.");
            }



            String waiterId = waiterService.assignWaiter(requestBody.get("locationId"));
            String reservationId = UUID.randomUUID().toString();
            String orderId = UUID.randomUUID().toString();
            String timeSlot = requestBody.get("timeFrom") + " - " + requestBody.get("timeTo");
            reservationTable.putItem(new PutItemSpec().withItem(new Item()
                    .withPrimaryKey("reservationId", reservationId)
                    .withString("email", email)
                    .withString("waiterId", waiterId)
                    .withString("locationId", requestBody.get("locationId"))
                    .withNumber("tableNumber", Integer.parseInt(requestBody.get("tableNumber")))
                    .withString("date", requestBody.get("date"))
                    .withNumber("guestsNumber", guestsNumber)
                    .withString("timeFrom", requestBody.get("timeFrom"))
                    .withString("timeTo", requestBody.get("timeTo"))
                    .withString("status", "Reserved")
                    .withString("orderId", orderId)));
            ordersTable.putItem(new PutItemSpec().withItem(new Item()
                    .withPrimaryKey("orderId", orderId, "email", email)
                    .withString("reservationId", reservationId)
                    .withString("locationId", requestBody.get("locationId"))
                    .withString("date", requestBody.get("date"))
                    .withString("state", "Submitted")
                    .withString("timeSlot", timeSlot)));
            Item reservationItem = reservationTable.getItem("reservationId", reservationId);
            Item locationItem = locationTable.getItem("locationId", reservationItem.getString("locationId"));
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("id", reservationId);
            responseData.put("status", reservationItem.getString("status"));
            responseData.put("locationAddress", locationItem.getString("address"));
            responseData.put("date", reservationItem.getString("date"));
            responseData.put("timeSlot", timeSlot);
            responseData.put("preOrder", "0");
            responseData.put("guestsNumber", String.valueOf(reservationItem.getInt("guestsNumber")));
            responseData.put("feedbackId", reservationItem.hasAttribute("feedbackId") ? reservationItem.getString("feedbackId") : null);
            return Helper.createApiResponse(200, responseData);
        } catch (Exception e) {
            logger.severe("Error creating reservation: " + e.getMessage());
            return Helper.createErrorResponse(500, "Error creating reservation: " + e.getMessage());
        }
    }

    public APIGatewayProxyResponseEvent handleGetReservations(APIGatewayProxyRequestEvent request) {
        try {
            Map<String, Object> claims = Helper.extractClaims(request);
            String email = (String) claims.get("email");
            logger.info("Extracted email: " + email);
            if (email == null || email.isEmpty()) {
                return Helper.createErrorResponse(401, "Unauthorized: Email not found in token.");
            }
            List<Map<String, Object>> userReservations = new ArrayList<>();
            Index emailIndex = reservationTable.getIndex("email-index");
            ItemCollection<QueryOutcome> items = emailIndex.query(new QuerySpec()
                    .withKeyConditionExpression("email = :email")
                    .withValueMap(new ValueMap().withString(":email", email)));
            for (Item item : items) {
                Map<String, Object> reservation = item.asMap();
                String reservationId = reservation.getOrDefault("reservationId", "null").toString();
                String locationId = reservation.getOrDefault("locationId", "null").toString();
                String orderId = reservation.getOrDefault("orderId", "null").toString();
                String timeSlot = reservation.get("timeFrom") + " - " + reservation.get("timeTo");
                Item loc = (locationId.equals("null")) ? null : locationTable.getItem(new GetItemSpec().withPrimaryKey("locationId", locationId));
                String address = (loc != null && loc.isPresent("address")) ? loc.getString("address") : "Unknown";
                Item orderItem = (orderId.equals("N/A")) ? null : ordersTable.getItem(new GetItemSpec().withPrimaryKey("orderId", orderId, "email", email));
                int dishCount = 0;
                if (orderItem != null && orderItem.isPresent("dishItems")) {
                    List<String> dishes = orderItem.getList("dishItems");
                    dishCount = dishes != null ? dishes.size() : 0;
                }
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("id", reservationId);
                responseData.put("status", reservation.get("status"));
                responseData.put("locationAddress", address);
                responseData.put("date", reservation.get("date"));
                responseData.put("timeSlot", timeSlot);
                responseData.put("preOrder", String.valueOf(dishCount));
                responseData.put("guestsNumber", reservation.get("guestsNumber"));
                responseData.put("feedbackId", reservation.get("feedbackId"));
                userReservations.add(responseData);
            }
            return Helper.createApiResponse(200, userReservations);
        } catch (Exception e) {
            return Helper.createErrorResponse(500, "Error fetching reservations: " + e.getMessage());
        }
    }



    public APIGatewayProxyResponseEvent handleCancelReservation(APIGatewayProxyRequestEvent request, String path) {
        try {
            String[] pathParts = path.split("/");
            if (pathParts.length < 3) {
                return Helper.createErrorResponse(400, "Invalid reservation cancellation request.");
            }

            String reservationId = pathParts[pathParts.length - 1];
            Map<String, Object> claims = Helper.extractClaims(request);
            String email = (String) claims.get("email");

            if (email == null || email.isEmpty()) {
                return Helper.createErrorResponse(400, "Missing email.");
            }

            // Directly fetch reservation item using the reservationId (PK)
            Item item = reservationTable.getItem("reservationId", reservationId);

            if (item == null) {
                return Helper.createErrorResponse(404, "Reservation not found.");
            }

            // Verify reservation belongs to the user
            if (!email.equals(item.getString("userId"))) {
                return Helper.createErrorResponse(403, "You are not authorized to cancel this reservation.");
            }

            // Only cancel if it's not already cancelled
            if ("Cancelled".equalsIgnoreCase(item.getString("status"))) {
                return Helper.createApiResponse(200, Map.of("message", "Reservation is already cancelled."));
            }

            cancelReservationStatus(reservationId, "Cancelled");

            return Helper.createApiResponse(204, null); // No Content

        } catch (Exception e) {
            logger.severe("Error canceling reservation: " + e.getMessage());
            return Helper.createErrorResponse(500, "Error canceling reservation: " + e.getMessage());
        }
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

    public String cancelReservationStatus(String reservationId, String status) {
        try {
            UpdateItemSpec updateItemSpec = new UpdateItemSpec()
                    .withPrimaryKey("reservationId", reservationId)
                    .withUpdateExpression("set #s = :status")
                    .withNameMap(Collections.singletonMap("#s", "status"))
                    .withValueMap(new ValueMap().withString(":status", status));
            reservationTable.updateItem(updateItemSpec);
            return "Reservation updated to: " + status;
        } catch (Exception e) {
            System.err.println("Error updating reservation: " + e.getMessage());
            return "Failed to update reservation.";
        }
    }

    public List<Map<String, Object>> getReservations() {
        ItemCollection<ScanOutcome> items = reservationTable.scan();
        List<Map<String, Object>> reservations = new ArrayList<>();
        for (Item item : items) {
            reservations.add(item.asMap());
        }
        return reservations;
    }

    public boolean isTableAvailable(String locationId, String tableNumber) {
        try {
            GetItemSpec getItemSpec = new GetItemSpec()
                    .withPrimaryKey("locationId", locationId, "tableNumber", tableNumber);
            Item item = tablesTable.getItem(getItemSpec);
            return item != null;
        } catch (Exception e) {
            logger.severe("Error checking table existence: " + e.getMessage());
            return false;
        }
    }

    private boolean isDuplicateReservation(String email, String locationId, int tableNumber, String date, String timeFrom, String timeTo) {
        Index emailIndex = reservationTable.getIndex("email-index");

        QuerySpec spec = new QuerySpec()
                .withKeyConditionExpression("email = :email")
                .withValueMap(new ValueMap().withString(":email", email));

        ItemCollection<QueryOutcome> items = emailIndex.query(spec);

        for (Item item : items) {
            String existingDate = item.getString("date");
            String existingLocationId = item.getString("locationId");
            int existingTableNumber = item.getInt("tableNumber");
            String existingTimeFrom = item.getString("timeFrom");
            String existingTimeTo = item.getString("timeTo");
            String status = item.getString("status");

            // If location, tableNumber, date and overlapping time matches
            if (existingDate.equals(date) &&
                    existingLocationId.equals(locationId) &&
                    existingTableNumber == tableNumber &&
                    !status.equalsIgnoreCase("Cancelled") &&
                    isTimeOverlap(timeFrom, timeTo, existingTimeFrom, existingTimeTo)) {
                return true;
            }
        }
        return false;
    }

    public boolean isTableAlreadyBooked(String locationId, int tableNumber, String date, String timeFrom, String timeTo) {
        try {
            Map<String, String> nameMap = new HashMap<>();
            nameMap.put("#d", "date");
            nameMap.put("#s", "status");

            QuerySpec querySpec = new QuerySpec()
                    .withKeyConditionExpression("locationId = :locId")
                    .withFilterExpression("tableNumber = :tblNum AND #d = :dt AND ((timeFrom < :to AND timeTo > :from) AND #s <> :cancelled)")
                    .withNameMap(nameMap)
                    .withValueMap(new ValueMap()
                            .withString(":locId", locationId)
                            .withNumber(":tblNum", tableNumber)
                            .withString(":dt", date)
                            .withString(":from", timeFrom)
                            .withString(":to", timeTo)
                            .withString(":cancelled", "Cancelled"));

            ItemCollection<QueryOutcome> items = reservationTable.query(querySpec);
            return items.iterator().hasNext(); // true means there's already a booking in this slot
        } catch (Exception e) {
            logger.severe("Error checking if table is already booked: " + e.getMessage());
            return true; // be safe, assume it's booked in case of error
        }
    }




    private boolean isTimeOverlap(String start1, String end1, String start2, String end2) {
        try {
            java.time.LocalTime s1 = java.time.LocalTime.parse(start1);
            java.time.LocalTime e1 = java.time.LocalTime.parse(end1);
            java.time.LocalTime s2 = java.time.LocalTime.parse(start2);
            java.time.LocalTime e2 = java.time.LocalTime.parse(end2);

            return !(e1.isBefore(s2) || s1.isAfter(e2));
        } catch (Exception e) {
            logger.warning("Time parsing error in isTimeOverlap: " + e.getMessage());
            return false;
        }
    }


}
