package com.restaurant.services;

import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.dto.UpdateReservationByWaiterResponseDTO;
import com.restaurant.utils.Helper;
import org.json.JSONObject;
import java.util.*;
import java.util.logging.Logger;


public class UpdateReservationByWaiterService {
    private static final Logger logger = Logger.getLogger(UpdateReservationByWaiterService.class.getName());
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final Table reservationTable;
    private final Table ordersTable;
    private final Table locationTable;
    private final Table tablesTable;
    private final Table waitersTable;
    private final Table usersTable;

    public UpdateReservationByWaiterService(DynamoDB dynamoDB) {
        this.reservationTable = dynamoDB.getTable(System.getenv("RESERVATIONS_TABLE"));
        this.ordersTable = dynamoDB.getTable(System.getenv("ORDERS_TABLE"));
        this.locationTable = dynamoDB.getTable(System.getenv("LOCATIONS_TABLE"));
        this.tablesTable = dynamoDB.getTable(System.getenv("TABLES_TABLE"));
        this.waitersTable = dynamoDB.getTable(System.getenv("WAITERS_TABLE"));
        this.usersTable = dynamoDB.getTable(System.getenv("USERS_TABLE"));
    }

    public APIGatewayProxyResponseEvent handleUpdateReservationByWaiter(APIGatewayProxyRequestEvent request, String path) {
        try {
            logger.info("Updating reservation by waiter");

            String[] pathParts = path.split("/");
            if (pathParts.length < 3)
                return Helper.createErrorResponse(400, "Invalid path");

            String reservationId = pathParts[pathParts.length - 1];
            Map<String, String> requestBody = parseJson(request.getBody());
            if (requestBody == null || requestBody.isEmpty())
                return Helper.createErrorResponse(400, "Empty request body");

            // Extract waiter identity from token
            Map<String, Object> claims = Helper.extractClaims(request);
            String email = (String) claims.get("email");
            if (email == null || email.isEmpty()) {
                return Helper.createErrorResponse(401, "Unauthorized: Missing email claim.");
            }

            // Lookup waiter from GSI
            Index emailIndex = waitersTable.getIndex("email-index");
            Iterator<Item> waiterQuery = emailIndex.query("email", email).iterator();
            if (!waiterQuery.hasNext()) {
                return Helper.createErrorResponse(403, "Unauthorized: Waiter not found.");
            }
            Item waiter = waiterQuery.next();

            // Check reservation exists
            Item reservation = reservationTable.getItem(new GetItemSpec().withPrimaryKey("reservationId", reservationId));
            if (reservation == null)
                return Helper.createErrorResponse(404, "Reservation not found");
            String status = reservation.getString("status");
            if (!"Reserved".equalsIgnoreCase(status)) {
                return Helper.createErrorResponse(400, "Reservation cannot be updated because its status is '" + status + "'.");
            }

            String locationId = reservation.getString("locationId");
            String tableNumber = String.valueOf(reservation.get("tableNumber"));

            // Check table capacity if guestsNumber is updated
            String newGuestsStr = requestBody.get("guestsNumber");
            String newTableNumberStr = requestBody.get("tableNumber");

            if (newTableNumberStr != null) {
                long newTableNumber;
                try {
                    newTableNumber = Long.parseLong(newTableNumberStr);
                } catch (NumberFormatException e) {
                    return Helper.createErrorResponse(400, "Invalid table number format. It should be a number.");
                }

                Item newTableItem = tablesTable.getItem("locationId", locationId, "tableNumber", newTableNumber);
                if (newTableItem == null) {
                    return Helper.createErrorResponse(400, "The specified table number does not exist at this location.");
                }

                if (newGuestsStr != null) {
                    int newGuests;
                    try {
                        newGuests = Integer.parseInt(newGuestsStr);
                    } catch (NumberFormatException e) {
                        return Helper.createErrorResponse(400, "Invalid number of guests.");
                    }

                    int newCapacity = newTableItem.getInt("capacity");
                    if (newGuests > newCapacity) {
                        return Helper.createErrorResponse(400, "The new table cannot accommodate " + newGuests + " guests.");
                    }
                }
            }

            // Update allowed fields
            List<String> editableFields = List.of("date", "timeFrom", "timeTo", "guestsNumber", "tableNumber");
            StringBuilder updateExpr = new StringBuilder("SET ");
            ValueMap valueMap = new ValueMap();
            Map<String, String> nameMap = new HashMap<>();
            int fieldsUpdated = 0;

            for (String field : editableFields) {
                if (requestBody.containsKey(field)) {
                    updateExpr.append("#").append(field).append(" = :").append(field).append(", ");
                    nameMap.put("#" + field, field);
                    String value = requestBody.get(field);
                    if (field.equals("guestsNumber")) {
                        valueMap.withNumber(":" + field, Integer.parseInt(value));
                    } else {
                        valueMap.with(":" + field, value);
                    }
                    fieldsUpdated++;
                }
            }

            if (fieldsUpdated == 0)
                return Helper.createErrorResponse(400, "No updatable fields provided");

            // Trim last comma
            updateExpr.setLength(updateExpr.length() - 2);

            UpdateItemSpec updateSpec = new UpdateItemSpec()
                    .withPrimaryKey("reservationId", reservationId)
                    .withUpdateExpression(updateExpr.toString())
                    .withValueMap(valueMap)
                    .withNameMap(nameMap);

            reservationTable.updateItem(updateSpec);

            // Fetch updated reservation
            Item updatedItem = reservationTable.getItem(new GetItemSpec().withPrimaryKey("reservationId", reservationId));
            if (updatedItem == null)
                return Helper.createErrorResponse(500, "Updated reservation could not be retrieved");

            String orderId = updatedItem.getString("orderId");
            Item orderItem = (orderId != null && !orderId.equals("N/A")) ?
                    ordersTable.getItem("orderId", orderId, "email", email) : null;

            int dishCount = 0;
            if (orderItem != null && orderItem.isPresent("dishItems")) {
                List<String> dishes = orderItem.getList("dishItems");
                dishCount = dishes != null ? dishes.size() : 0;
            }

            String address = "Unknown";
            Item locationItem = locationTable.getItem("locationId", locationId);
            if (locationItem != null && locationItem.isPresent("address"))
                address = locationItem.getString("address");

            String guestsNumberStr = updatedItem.isPresent("guestsNumber") ?
                    String.valueOf(updatedItem.getInt("guestsNumber")) : "0";

            UpdateReservationByWaiterResponseDTO dto = new UpdateReservationByWaiterResponseDTO(
                    updatedItem.getString("reservationId"),
                    updatedItem.getString("status"),
                    address,
                    updatedItem.getString("date"),
                    updatedItem.getString("timeFrom") + " - " + updatedItem.getString("timeTo"),
                    String.valueOf(dishCount),
                    guestsNumberStr,
                    updatedItem.getString("tableNumber"),
                    updatedItem.getString("feedbackId"),
                    "Reservation updated successfully."
            );
            JSONObject response = dto.toJson();

            return Helper.createApiResponse(200, response.toString());

        } catch (Exception e) {
            logger.severe("Error updating reservation: " + e.getMessage());
            return Helper.createErrorResponse(500, "Internal server error: " + e.getMessage());
        }
    }

    private Map<String, String> parseJson(String json) {
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            logger.warning("Failed to parse JSON: " + e.getMessage());
            return Map.of();
        }
    }
}
