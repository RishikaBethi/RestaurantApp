package com.restaurant.services;

import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;

import java.util.*;

public class ReservationService {
    private static final String TABLE_NAME = System.getenv("RESERVATIONS_TABLE");
    private static final String USER_INDEX_NAME = "userId-index"; // Ensure this exists in DynamoDB
    private final Table reservationTable;

    private final DynamoDB dynamoDB;

    public ReservationService(DynamoDB dynamoDB) {
        this.dynamoDB = dynamoDB;
        this.reservationTable = dynamoDB.getTable(TABLE_NAME);
    }

    // Create a reservation
    public String createReservation(Map<String, String> requestBody, String userId, String waiterId) {
        UUID reservationId = UUID.randomUUID();
        try {
            reservationTable.putItem(new PutItemSpec().withItem(new Item()
                    .withPrimaryKey("reservationId", reservationId.toString())
                    .withString("userId", userId)
                    .withString("waiterId", waiterId)
                    .withString("locationId", requestBody.get("locationId"))
                    .withNumber("tableNumber", Integer.parseInt(requestBody.get("tableNumber")))  // Store as Number
                    .withString("date", requestBody.get("date")) // Ensure ISO-8601 format (YYYY-MM-DD)
                    .withNumber("guestsNumber", Integer.parseInt(requestBody.get("guestsNumber"))) // Store as Number
                    .withString("timeFrom", requestBody.get("timeFrom")) // Store as String
                    .withString("timeTo", requestBody.get("timeTo"))
                    .withString("status", "Pending") // Default status
            ));
            return reservationId.toString();
        } catch (Exception e) {
            System.err.println("Error creating reservation: " + e.getMessage());
            return null;
        }
    }

    // Modify reservation status
    public String modifyReservation(String reservationId, String status) {
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

    // Get all reservations for a user
    public List<Map<String, Object>> getReservationsByUser(String userId) {
        Index userIndex = reservationTable.getIndex(USER_INDEX_NAME); // Ensure this exists

        QuerySpec querySpec = new QuerySpec()
                .withKeyConditionExpression("userId = :uid")
                .withValueMap(new ValueMap().withString(":uid", userId));

        ItemCollection<QueryOutcome> items = userIndex.query(querySpec);
        List<Map<String, Object>> reservations = new ArrayList<>();
        for (Item item : items) {
            reservations.add(item.asMap());
        }
        return reservations;
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
