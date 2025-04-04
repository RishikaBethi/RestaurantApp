package com.restaurant.services;

import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;

import java.util.*;

public class ReservationService {
    private static final String TABLE_NAME = System.getenv("RESERVATIONS_TABLE");
    private static final String TABLE_NAME_LOC = System.getenv("LOCATIONS_TABLE");
    private static final String TABLE_NAME_ORDERS = System.getenv("ORDERS_TABLE");

    private final DynamoDB dynamoDB;
    private final Table reservationTable;
    private final Table locationTable;
    private final Table ordersTable;

    public ReservationService(DynamoDB dynamoDB) {
        this.dynamoDB = dynamoDB;
        this.reservationTable = dynamoDB.getTable(TABLE_NAME);
        this.locationTable = dynamoDB.getTable(TABLE_NAME_LOC);
        this.ordersTable = dynamoDB.getTable(TABLE_NAME_ORDERS);
    }

    public String createReservation(Map<String, String> requestBody, String email, String waiterId) {
        UUID reservationId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        try {
            reservationTable.putItem(new PutItemSpec().withItem(new Item()
                    .withPrimaryKey("reservationId", reservationId.toString())
                    .withString("email", email)
                    .withString("waiterId", waiterId)
                    .withString("locationId", requestBody.get("locationId"))
                    .withString("tableNumber", requestBody.get("tableNumber"))
                    .withString("date", requestBody.get("date"))
                    .withNumber("guestsNumber", parseInteger(requestBody.get("guestsNumber")))
                    .withString("timeFrom", requestBody.get("timeFrom"))
                    .withString("timeTo", requestBody.get("timeTo"))
                    .withString("status", "Reserved")
                    .withString("orderId", orderId.toString())));
        } catch (Exception e) {
            throw new RuntimeException("Error creating reservation: " + e.getMessage(), e);
        }

        return reservationId.toString();
    }

    public String modifyReservation(String reservationId, String status) {
        try {
            UpdateItemSpec updateItemSpec = new UpdateItemSpec()
                    .withPrimaryKey("reservationId", reservationId)
                    .withUpdateExpression("SET #s = :status")
                    .withNameMap(Collections.singletonMap("#s", "status"))
                    .withValueMap(Collections.singletonMap(":status", status));

            reservationTable.updateItem(updateItemSpec);
            return "Reservation updated successfully: " + status;

        } catch (Exception e) {
            throw new RuntimeException("Error updating reservation: " + e.getMessage(), e);
        }
    }

    public List<Map<String, Object>> getReservationsByUser(String email) {
        QuerySpec querySpec = new QuerySpec()
                .withKeyConditionExpression("email = :e")
                .withValueMap(Collections.singletonMap(":e", email));

        ItemCollection<QueryOutcome> items = reservationTable.query(querySpec);
        List<Map<String, Object>> reservations = new ArrayList<>();

        for (Item item : items) {
            reservations.add(item.asMap());
        }
        return reservations;
    }

    public List<Map<String, Object>> getReservations() {
        ItemCollection<ScanOutcome> items = reservationTable.scan();
        List<Map<String, Object>> reservations = new ArrayList<>();

        for (Item item : items) {
            reservations.add(item.asMap());
        }
        return reservations;
    }

    public Optional<Map<String, Object>> getReservationById(String reservationId) {
        Item item = reservationTable.getItem("reservationId", reservationId);

        if (item == null) {
            return Optional.empty();
        }

        // Construct a map from the DynamoDB item attributes
        Map<String, Object> reservationDetails = new HashMap<>();
        reservationDetails.put("id", item.getString("reservationId"));
        reservationDetails.put("status", item.getString("status"));
        reservationDetails.put("date", item.getString("date"));
        reservationDetails.put("timeSlot", item.getString("timeFrom") + " - " + item.getString("timeTo"));
        reservationDetails.put("guestsNumber", item.getNumber("guestsNumber"));

        // Fetch orderId and dish count
        String orderId = item.getString("orderId");
        int preOrderCount = 0;

        if (orderId != null) {
            Item orderItem = ordersTable.getItem("orderId", orderId);
            if (orderItem != null && orderItem.isPresent("dishItems")) {
                preOrderCount = orderItem.getList("dishItems").size();
            }
        }

        reservationDetails.put("preOrder", preOrderCount);

        String locationId = item.getString("locationId");
        Item locationItem = locationTable.getItem("locationId", locationId);

        if (locationItem != null) {
            reservationDetails.put("locationAddress", locationItem.getString("address"));
        } else {
            reservationDetails.put("locationAddress", "Unknown");
        }



        reservationDetails.put("feedbackId", "None");
        return Optional.of(reservationDetails);
    }


    private int parseInteger(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format: " + value, e);
        }
    }
}