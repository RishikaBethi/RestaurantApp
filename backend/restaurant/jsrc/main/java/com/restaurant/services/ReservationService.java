package com.restaurant.services;

import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
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

    private static final String USER_INDEX_NAME = "userId-index"; // Ensure this exists in DynamoDB
    private final Table reservationTable;
    private final Table locationTable;
    private final Table ordersTable;
    private final WaiterService waiterService;
    private final DynamoDB dynamoDB;

    public ReservationService(DynamoDB dynamoDB, WaiterService waiterService) {
        this.dynamoDB = dynamoDB;
        this.waiterService = waiterService;
        this.reservationTable = dynamoDB.getTable(RESERVATIONS_TABLE);
        this.locationTable = dynamoDB.getTable(LOCATIONS_TABLE);
        this.ordersTable = dynamoDB.getTable(ORDERS_TABLE);
    }

    public APIGatewayProxyResponseEvent handleCreateReservation(APIGatewayProxyRequestEvent request) {
        try {
            logger.info("Handling reservation booking request");

            // Parse request body
            Map<String, String> requestBody = parseJson(request.getBody());
            if (requestBody == null || requestBody.isEmpty()) {
                return Helper.createErrorResponse(400, "Invalid request data: Empty request body.");
            }

            // Extract user ID from JWT claims
            Map<String, Object> claims = Helper.extractClaims(request);
            logger.info("Extracted claims: " + claims); // Debugging purpose

            String userId = (String) claims.get("sub");
            String email = (String) claims.get("email");

            if (userId == null || userId.isEmpty()) {
                return Helper.createErrorResponse(401, "Unauthorized: Missing or invalid token.");
            }

            // Validate required fields in request body
            List<String> requiredFields = List.of("locationId", "tableNumber", "date", "guestsNumber", "timeFrom", "timeTo");
            for (String field : requiredFields) {
                if (!requestBody.containsKey(field) || requestBody.get(field).trim().isEmpty()) {
                    return Helper.createErrorResponse(400, "Missing required field: " + field);
                }
            }

            // Convert guestsNumber to Integer
            int guestsNumber;
            try {
                guestsNumber = Integer.parseInt(requestBody.get("guestsNumber"));
                if (guestsNumber <= 0) {
                    return Helper.createErrorResponse(400, "Invalid guestsNumber: Must be a positive integer.");
                }
            } catch (NumberFormatException e) {
                return Helper.createErrorResponse(400, "Invalid guestsNumber: Must be an integer.");
            }

            String waiterId = waiterService.assignWaiter(requestBody.get("locationId"));
            String reservationId = UUID.randomUUID().toString();
            String orderId = UUID.randomUUID().toString();
            String timeSlot = requestBody.get("timeFrom") + " - " + requestBody.get("timeTo");

            // Save reservation
            reservationTable.putItem(new PutItemSpec().withItem(new Item()
                    .withPrimaryKey("reservationId", reservationId)
                    .withString("userId", userId)
                    .withString("waiterId", waiterId)
                    .withString("locationId", requestBody.get("locationId"))
                    .withNumber("tableNumber", Integer.parseInt(requestBody.get("tableNumber")))
                    .withString("date", requestBody.get("date"))
                    .withNumber("guestsNumber", guestsNumber)
                    .withString("timeFrom", requestBody.get("timeFrom"))
                    .withString("timeTo", requestBody.get("timeTo"))
                    .withString("status", "Reserved")
                    .withString("orderId", orderId)
            ));

            // Save order
            ordersTable.putItem(new PutItemSpec().withItem(new Item()
                    .withPrimaryKey("orderId", orderId, "SK", email)
                    .withString("reservationId", reservationId)
                    .withString("locationId", requestBody.get("locationId"))
                    .withString("date", requestBody.get("date"))
                    .withString("state", "Submitted")
                    .withString("timeSlot", timeSlot)
            ));

            // Fetch reservation and location details
            Item reservationItem = reservationTable.getItem("reservationId", reservationId);
            Table locationsTable = dynamoDB.getTable(System.getenv("LOCATIONS_TABLE"));
            Item locationItem = locationsTable.getItem("locationId", reservationItem.getString("locationId"));

            // Prepare response
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("id", reservationId);
            responseData.put("status", reservationItem.getString("status"));
            responseData.put("locationAddress", locationItem.getString("address"));
            responseData.put("date", reservationItem.getString("date"));
            responseData.put("timeSlot", timeSlot);
            responseData.put("preOrder", "0");
            responseData.put("guestsNumber", String.valueOf(reservationItem.getInt("guestsNumber")));
            responseData.put("feedbackId", reservationItem.isPresent("feedbackId") ? reservationItem.getString("feedbackId") : null);

            return Helper.createApiResponse(200, responseData);

        } catch (Exception e) {
            logger.severe("Error creating reservation: " + e.getMessage());
            return Helper.createErrorResponse(500, "Error creating reservation: " + e.getMessage());
        }
    }

    public APIGatewayProxyResponseEvent handleGetReservations(APIGatewayProxyRequestEvent request) {
        try {
            Map<String, Object> claims = Helper.extractClaims(request);
            String userId = (String) claims.get("sub");
            String email = (String) claims.get("email");

            if (userId == null || userId.isEmpty()) {
                return Helper.createErrorResponse(401, "Unauthorized: Missing or invalid token.");
            }

            List<Map<String, Object>> userReservations = new ArrayList<>();
            Index userIndex = reservationTable.getIndex(USER_INDEX_NAME);

            ItemCollection<QueryOutcome> items = userIndex.query(
                    new QuerySpec().withKeyConditionExpression("userId = :uid")
                            .withValueMap(new ValueMap().withString(":uid", userId))
            );

            for (Item item : items) {
                Map<String, Object> reservation = item.asMap();
                String reservationId = (String) reservation.get("reservationId");
                String locationId = (String) reservation.get("locationId");
                String orderId = (String) reservation.get("orderId");

                String timeSlot = reservation.get("timeFrom") + " - " + reservation.get("timeTo");

                // Get location address
                Item loc = locationTable.getItem("locationId", locationId);
                String address = loc != null ? loc.getString("address") : "Unknown";

                // Get dish count
                Item orderItem = ordersTable.getItem("orderId", orderId, "SK", email);
                int dishCount = 0;
                if (orderItem != null && orderItem.isPresent("dishItems")) {
                    List<String> dishes = orderItem.getList("dishItems");
                    dishCount = dishes != null ? dishes.size() : 0;
                }

                userReservations.add(Map.of(
                        "id", reservationId,
                        "status", reservation.get("status"),
                        "locationAddress", address,
                        "date", reservation.get("date"),
                        "timeSlot", timeSlot,
                        "preOrder", String.valueOf(dishCount),
                        "guestsNumber", reservation.get("guestsNumber").toString(),
                        "feedbackId", null
                ));
            }

            return Helper.createApiResponse(200, userReservations);
        } catch (Exception e) {
            return Helper.createErrorResponse(500, "Error fetching reservations: " + e.getMessage());
        }
    }

    public APIGatewayProxyResponseEvent handleUpdateReservation(APIGatewayProxyRequestEvent request, String path) {
        try {
            String[] pathParts = path.split("/");
            if (pathParts.length < 3) return Helper.createErrorResponse(400, "Invalid path");

            String reservationId = pathParts[pathParts.length - 1];
            Map<String, String> requestBody = parseJson(request.getBody());
            if (requestBody == null || requestBody.isEmpty()) return Helper.createErrorResponse(400, "Empty body");

            UpdateItemSpec updateSpec = new UpdateItemSpec().withPrimaryKey("reservationId", reservationId);
            StringBuilder updateExpr = new StringBuilder("set ");
            ValueMap values = new ValueMap();
            Map<String, String> names = new HashMap<>();

            List<String> editableFields = List.of("tableNumber", "date", "guestsNumber", "timeFrom", "timeTo", "locationId");
            for (String key : editableFields) {
                if (requestBody.containsKey(key)) {
                    updateExpr.append("#").append(key).append(" = :").append(key).append(", ");
                    names.put("#" + key, key);
                    values.with(":" + key, requestBody.get(key));
                }
            }

            if (values.isEmpty()) return Helper.createErrorResponse(400, "No editable fields provided");

            updateExpr.setLength(updateExpr.length() - 2); // remove last comma
            updateSpec.withUpdateExpression(updateExpr.toString()).withValueMap(values).withNameMap(names);

            reservationTable.updateItem(updateSpec);
            return Helper.createApiResponse(200, Map.of("message", "Reservation updated successfully"));

        } catch (Exception e) {
            return Helper.createErrorResponse(500, "Error updating reservation: " + e.getMessage());
        }
    }

    public APIGatewayProxyResponseEvent handleCancelReservation(APIGatewayProxyRequestEvent request, String path) {
        try {
            String[] pathParts = path.split("/");
            if (pathParts.length < 3) {
                return Helper.createErrorResponse(400, "Invalid reservation cancellation request.");
            }
            String reservationId = pathParts[pathParts.length - 1];
            Map<String, String> requestBody = parseJson(request.getBody());
            // Extract user ID from JWT claims
            Map<String, Object> claims = Helper.extractClaims(request);
            String userId = (String) claims.get("sub");

            if (userId == null || userId.isEmpty()) {
                return Helper.createErrorResponse(400, "Missing userId.");
            }

            List<Map<String, Object>> reservations = getReservations();
            boolean exists = reservations.stream()
                    .anyMatch(res -> res.get("reservationId").equals(reservationId));

            if (!exists) {
                return Helper.createErrorResponse(404, "Reservation not found.");
            }
            cancelReservationStatus(reservationId, "Cancelled");
            return Helper.createApiResponse(200, Map.of("message", "Reservation Canceled"));
        } catch (Exception e) {
            logger.severe("Error canceling reservation: " + e.getMessage());
            return Helper.createErrorResponse(500, "Error canceling reservation: " + e.getMessage());
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

    // Modify reservation status
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

    // Get all reservations (admin functionality)
    public List<Map<String, Object>> getReservations() {
        ItemCollection<ScanOutcome> items = reservationTable.scan();

        List<Map<String, Object>> reservations = new ArrayList<>();
        for (Item item : items) {
            reservations.add(item.asMap());
        }
        return reservations;
    }
}
