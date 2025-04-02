package com.restaurant.services;

import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;

import java.util.*;

public class ReservationService {
    private static final String TABLE_NAME = "RESERVATIONS_TABLE";
    private final DynamoDB dynamoDB;

    public ReservationService(DynamoDB dynamoDB) {
        this.dynamoDB = dynamoDB;
    }

    public String createReservation(Map<String, String> requestBody, String userId, String waiterId) {
        UUID reservationId = UUID.randomUUID();
        Table table = dynamoDB.getTable(TABLE_NAME);

        table.putItem(new PutItemSpec().withItem(new Item()
                .withPrimaryKey("reservationId", reservationId.toString())
                .withString("userId", userId)
                .withString("waiterId", waiterId)
                .withString("locationId", requestBody.get("locationId"))
                .withString("tableNumber", requestBody.get("tableNumber"))
                .withString("date", requestBody.get("date"))
                .withNumber("guestsNumber", Integer.parseInt(requestBody.get("guestsNumber")))
                .withString("timeFrom", requestBody.get("timeFrom"))
                .withString("timeTo", requestBody.get("timeTo"))
                .withString("status", "Pending")));

        return reservationId.toString();
    }

    public String modifyReservation(String reservationId, String userId, String status) {
        Table table = dynamoDB.getTable(TABLE_NAME);
        UpdateItemSpec updateItemSpec = new UpdateItemSpec()
                .withPrimaryKey("reservationId", reservationId, "userId", userId)
                .withUpdateExpression("set #s = :status")
                .withNameMap(Map.of("#s", "status"))
                .withValueMap(Map.of(":status", status));

        table.updateItem(updateItemSpec);
        return "Reservation updated to: " + status;
    }

    public List<Map<String, Object>> getReservationsByUser(String userId) {
        Table table = dynamoDB.getTable(TABLE_NAME);
        Index userIndex = table.getIndex("userId-index");

        QuerySpec querySpec = new QuerySpec()
                .withKeyConditionExpression("userId = :uid")
                .withValueMap(Map.of(":uid", userId));

        ItemCollection<QueryOutcome> items = userIndex.query(querySpec);
        List<Map<String, Object>> reservations = new ArrayList<>();

        for (Item item : items) {
            reservations.add(item.asMap());
        }
        return reservations;
    }

    public List<Map<String, Object>> getReservations() {
        Table table = dynamoDB.getTable(TABLE_NAME);
        ItemCollection<ScanOutcome> items = table.scan();
        List<Map<String, Object>> reservations = new ArrayList<>();

        for (Item item : items) {
            reservations.add(item.asMap());
        }
        return reservations;
    }
}