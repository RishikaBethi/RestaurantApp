package com.restaurant.services;

import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.document.utils.NameMap;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.restaurant.dto.ReservationResponseDTO;
import org.json.JSONArray;
import java.util.logging.Logger;
import java.util.*;
import static com.restaurant.utils.Helper.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GetReservationService {
    private static final Logger logger = Logger.getLogger(GetReservationService.class.getName());
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
    static {
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata")); // Set formatter to IST
    }
    private final Table reservationTable;
    private final Table locationTable;
    private final Table ordersTable;
    private final Table feedbacksTable;
    private final DynamoDB dynamoDB;

    public GetReservationService(DynamoDB dynamoDB) {
        this.dynamoDB = dynamoDB;
        this.reservationTable = dynamoDB.getTable(System.getenv("RESERVATIONS_TABLE"));
        this.ordersTable = dynamoDB.getTable(System.getenv("ORDERS_TABLE"));
        this.locationTable = dynamoDB.getTable(System.getenv("LOCATIONS_TABLE"));
        this.feedbacksTable = dynamoDB.getTable(System.getenv("FEEDBACKS_TABLE"));
    }

    public APIGatewayProxyResponseEvent handleGetReservations(APIGatewayProxyRequestEvent request) {
        try {
            Date now = new Date();
            Map<String, Object> claims = extractClaims(request);
            String email = (String) claims.get("email");

            logger.info("Extracted email: " + email);
            if (email == null || email.isEmpty()) {
                return createErrorResponse(401, "Unauthorized: Email not found in token.");
            }

            Index emailIndex = reservationTable.getIndex("email-index");
            if (emailIndex == null) {
                logger.severe("DynamoDB index 'email-index' does not exist.");
                return createErrorResponse(500, "Server error: Email index not found.");
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
                String reservationDate = String.valueOf(reservation.get("date"));
                String timeFromStr = String.valueOf(reservation.get("timeFrom"));
                String timeToStr = String.valueOf(reservation.get("timeTo"));
                String guestsNumber = String.valueOf(reservation.get("guestsNumber"));
                String currentStatus = String.valueOf(reservation.get("status"));
                String feedbackId = reservation.get("feedbackId") != null ? reservation.get("feedbackId").toString() : null;

                String timeSlot = timeFromStr + " - " + timeToStr;

                Date timeFrom = sdf.parse(reservationDate + "T" + timeFromStr);
                Date timeTo = sdf.parse(reservationDate + "T" + timeToStr);

                // Handle status transitions
                if (now.after(timeFrom) && now.before(timeTo)) {
                    if (!"In progress".equals(currentStatus)) {
                        updateReservationStatus(reservationId, "In progress", feedbackId);
                        currentStatus = "In progress";
                    }
                } else if (now.after(timeTo)) {
                    if (!"Finished".equals(currentStatus)) {
                        updateReservationStatus(reservationId, "Finished", feedbackId);
                        currentStatus = "Finished";
                    }
                }

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
                        currentStatus,
                        address,
                        reservationDate,
                        timeSlot,
                        String.valueOf(dishCount),
                        guestsNumber,
                        feedbackId
                );

                reservationDTOs.add(dto);
            }

            // Convert list of DTOs to JSONArray
            JSONArray jsonArray = new JSONArray();
            for (ReservationResponseDTO dto : reservationDTOs) {
                jsonArray.put(dto.toJson());
            }

            return createApiResponse(200, jsonArray);
        } catch (Exception e) {
            return createErrorResponse(500, "Error fetching reservations: " + e.getMessage());
        }
    }

    private void updateReservationStatus(String reservationId, String status, String feedbackId) {
        UpdateItemSpec updateSpec = new UpdateItemSpec()
                .withPrimaryKey("reservationId", reservationId)
                .withUpdateExpression("set #s = :s, feedbackId = :f")
                .withNameMap(new NameMap().with("#s", "status"))
                .withValueMap(new ValueMap().withString(":s", status).withString(":f", feedbackId));
        reservationTable.updateItem(updateSpec);
    }
}
