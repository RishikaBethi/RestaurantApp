package com.restaurant.services;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.restaurant.dto.ReservationResponseDTO;
import static com.restaurant.utils.Helper.*;
import java.util.logging.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import org.json.JSONObject;

public class UpdateReservationService {
    private static final Logger logger = Logger.getLogger(UpdateReservationService.class.getName());
    private final Table reservationTable;
    private final Table locationTable;
    private final Table ordersTable;
    private final Table tablesTable;
    private final DynamoDB dynamoDB;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public UpdateReservationService(DynamoDB dynamoDB) {
        this.dynamoDB = dynamoDB;
        this.reservationTable = dynamoDB.getTable(System.getenv("RESERVATIONS_TABLE"));
        this.ordersTable = dynamoDB.getTable(System.getenv("ORDERS_TABLE"));
        this.locationTable = dynamoDB.getTable(System.getenv("LOCATIONS_TABLE"));
        this.tablesTable = dynamoDB.getTable(System.getenv("TABLES_TABLE"));
    }

    public APIGatewayProxyResponseEvent handleUpdateReservation(APIGatewayProxyRequestEvent request, String path) {
        try {
            String[] pathParts = path.split("/");
            if (pathParts.length < 3)
                return createErrorResponse(400, "Invalid path");

            String reservationId = pathParts[pathParts.length - 1];

            Map<String, String> requestBody = parseJson(request.getBody());
            if (requestBody == null || requestBody.isEmpty())
                return createErrorResponse(400, "Empty body");

            Map<String, Object> claims = extractClaims(request);
            String email = (String) claims.get("email");

            logger.info("Extracted email: " + email);
            if (email == null || email.isEmpty()) {
                return createErrorResponse(401, "Unauthorized: Email not found in token.");
            }

            Item existingReservation = reservationTable.getItem(new GetItemSpec().withPrimaryKey("reservationId", reservationId));
            if (existingReservation == null) {
                return createErrorResponse(404, "Reservation not found");
            }

            String newGuestsNumber = requestBody.get("guestsNumber");
            if (newGuestsNumber != null) {
                String locationId = existingReservation.getString("locationId");
                Number tableNumber = existingReservation.getNumber("tableNumber");

                if (locationId != null && tableNumber != null) {
                    tableNumber = tableNumber.intValue();
                    Item tableItem = tablesTable.getItem(new GetItemSpec()
                            .withPrimaryKey("locationId", locationId, "tableNumber", tableNumber));

                    if (tableItem != null && tableItem.isPresent("capacity")) {
                        int capacity = tableItem.getInt("capacity");
                        int newGuests = Integer.parseInt(newGuestsNumber);

                        if (newGuests > capacity) {
                            return createErrorResponse(400, "The selected table cannot accommodate " + newGuests +  " guests. Please cancel the current reservation and book a new one.");
                        }
                    }
                }
            }

            UpdateItemSpec updateSpec = new UpdateItemSpec().withPrimaryKey("reservationId", reservationId);
            StringBuilder updateExpr = new StringBuilder("set ");
            ValueMap values = new ValueMap();
            Map<String, String> names = new HashMap<>();

            List<String> editableFields = List.of("date", "guestsNumber", "timeFrom", "timeTo");
            for (String key : editableFields) {
                if (requestBody.containsKey(key)) {
                    updateExpr.append("#").append(key).append(" = :").append(key).append(", ");
                    names.put("#" + key, key);
                    values.with(":" + key, requestBody.get(key));
                }
            }

            if (values.isEmpty())
                return createErrorResponse(400, "No editable fields provided");

            updateExpr.setLength(updateExpr.length() - 2); // remove trailing comma and space
            updateSpec.withUpdateExpression(updateExpr.toString()).withValueMap(values).withNameMap(names);
            reservationTable.updateItem(updateSpec);

            // Fetch updated reservation
            Item updatedItem = reservationTable.getItem(new GetItemSpec().withPrimaryKey("reservationId", reservationId));
            if (updatedItem == null) {
                return createErrorResponse(404, "Updated reservation not found");
            }

            String timeSlot = updatedItem.getString("timeFrom") + " - " + updatedItem.getString("timeTo");

            // Get location address
            String locationId = updatedItem.getString("locationId");
            Item loc = (locationId == null || locationId.equals("null")) ? null : locationTable.getItem(new GetItemSpec().withPrimaryKey("locationId", locationId));
            String address = (loc != null && loc.isPresent("address")) ? loc.getString("address") : "Unknown";

            // Get dish count
            String orderId = updatedItem.getString("orderId");
            Item orderItem = (orderId == null || orderId.equals("N/A")) ? null : ordersTable.getItem(new GetItemSpec().withPrimaryKey("orderId", orderId, "email", email));
            int dishCount = 0;
            if (orderItem != null && orderItem.isPresent("dishItems")) {
                List<String> dishes = orderItem.getList("dishItems");
                dishCount = dishes != null ? dishes.size() : 0;
            }

            ReservationResponseDTO dto = new ReservationResponseDTO(
                    updatedItem.getString("reservationId"),
                    updatedItem.getString("status"),
                    address,
                    updatedItem.getString("date"),
                    timeSlot,
                    String.valueOf(dishCount),
                    String.valueOf(updatedItem.get("guestsNumber")),
                    updatedItem.getString("feedbackId")
            );

            JSONObject jsonResponse = dto.toJson();
            jsonResponse.put("message", "The reservation has been updated.");
            return createApiResponse(200, jsonResponse.toString());

        } catch (Exception e) {
            return createErrorResponse(500, "Error updating reservation: " + e.getMessage());
        }
    }

    private Map<String, String> parseJson(String json) {
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            return Map.of();
        }
    }
}
