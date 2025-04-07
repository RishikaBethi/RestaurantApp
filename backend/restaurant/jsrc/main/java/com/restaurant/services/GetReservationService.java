package com.restaurant.services;

import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.restaurant.dto.ReservationResponseDTO;
import org.json.JSONArray;
import java.util.logging.Logger;
import java.util.*;
import com.restaurant.utils.Helper;

public class GetReservationService {
    private static final Logger logger = Logger.getLogger(GetReservationService.class.getName());
    private final Table reservationTable;
    private final Table locationTable;
    private final Table ordersTable;
    private final DynamoDB dynamoDB;

    public GetReservationService(DynamoDB dynamoDB) {
        this.dynamoDB = dynamoDB;
        this.reservationTable = dynamoDB.getTable(System.getenv("RESERVATIONS_TABLE"));
        this.ordersTable = dynamoDB.getTable(System.getenv("ORDERS_TABLE"));
        this.locationTable = dynamoDB.getTable(System.getenv("LOCATIONS_TABLE"));
    }

    public APIGatewayProxyResponseEvent handleGetReservations(APIGatewayProxyRequestEvent request) {
        try {
            Map<String, Object> claims = Helper.extractClaims(request);
            String email = (String) claims.get("email");

            logger.info("Extracted email: " + email);
            if (email == null || email.isEmpty()) {
                return Helper.createErrorResponse(401, "Unauthorized: Email not found in token.");
            }

            Index emailIndex = reservationTable.getIndex("email-index");
            if (emailIndex == null) {
                logger.severe("DynamoDB index 'email-index' does not exist.");
                return Helper.createErrorResponse(500, "Server error: Email index not found.");
            }

            ItemCollection<QueryOutcome> items = emailIndex.query(
                    new QuerySpec().withKeyConditionExpression("email = :email")
                            .withValueMap(new ValueMap().withString(":email", email))
            );

            List<ReservationResponseDTO> reservationDTOs = new ArrayList<>();
            for (Item item : items) {
                Map<String, Object> reservation = item.asMap();

                String reservationId = reservation.getOrDefault("reservationId", "null").toString();
                String locationId = reservation.getOrDefault("locationId", "null").toString();
                String orderId = reservation.getOrDefault("orderId", "null").toString();
                String timeSlot = reservation.get("timeFrom") + " - " + reservation.get("timeTo");

                // Get location address
                Item loc = (locationId.equals("null")) ? null : locationTable.getItem(new GetItemSpec().withPrimaryKey("locationId", locationId));
                String address = (loc != null && loc.isPresent("address")) ? loc.getString("address") : "Unknown";

                // Get dish count
                Item orderItem = (orderId.equals("N/A")) ? null : ordersTable.getItem(new GetItemSpec().withPrimaryKey("orderId", orderId, "email", email));
                int dishCount = 0;
                if (orderItem != null && orderItem.isPresent("dishItems")) {
                    List<String> dishes = orderItem.getList("dishItems");
                    dishCount = dishes != null ? dishes.size() : 0;
                }

                ReservationResponseDTO dto = new ReservationResponseDTO(
                        reservationId,
                        String.valueOf(reservation.get("status")),
                        address,
                        String.valueOf(reservation.get("date")),
                        timeSlot,
                        String.valueOf(dishCount),
                        String.valueOf(reservation.get("guestsNumber")),
                        reservation.getOrDefault("feedbackId", null) != null ?
                                reservation.get("feedbackId").toString() : null
                );

                reservationDTOs.add(dto);
            }

            // Convert list of DTOs to JSONArray
            JSONArray jsonArray = new JSONArray();
            for (ReservationResponseDTO dto : reservationDTOs) {
                jsonArray.put(dto.toJson());
            }

            return Helper.createApiResponse(200, jsonArray);
        } catch (Exception e) {
            return Helper.createErrorResponse(500, "Error fetching reservations: " + e.getMessage());
        }
    }
}
