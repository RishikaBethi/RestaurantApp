package com.restaurant.services;

import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import java.util.*;
import java.util.logging.Logger;

public class WaiterService {
    private static final Logger logger = Logger.getLogger(WaiterService.class.getName());
    private final DynamoDB dynamoDB;

    public WaiterService(DynamoDB dynamoDB) {
        this.dynamoDB = dynamoDB;
    }

    public String assignWaiter(String locationId) {
        Table reservationsTable = dynamoDB.getTable("RESERVATIONS_TABLE");
        Index locationIndex = reservationsTable.getIndex("locationId-index");

        QuerySpec querySpec = new QuerySpec()
                .withKeyConditionExpression("locationId = :v_location")
                .withValueMap(new ValueMap().withString(":v_location", locationId));

        ItemCollection<QueryOutcome> items = locationIndex.query(querySpec);
        Map<String, Integer> waiterReservationCount = new HashMap<>();

        for (Item item : items) {
            String waiterId = item.getString("waiterId");
            waiterReservationCount.put(waiterId, waiterReservationCount.getOrDefault(waiterId, 0) + 1);
        }

        if (!waiterReservationCount.isEmpty()) {
            return waiterReservationCount.entrySet()
                    .stream()
                    .min(Comparator.comparingInt(Map.Entry::getValue))
                    .map(Map.Entry::getKey)
                    .orElseGet(() -> assignLeastBusyWaiter(locationId));
        }

        return assignLeastBusyWaiter(locationId);
    }

    private String assignLeastBusyWaiter(String locationId) {
        Table waitersTable = dynamoDB.getTable("WAITERS_TABLE");
        Table reservationsTable = dynamoDB.getTable("RESERVATIONS_TABLE");

        Map<String, Integer> waiterLoad = new HashMap<>();
        ItemCollection<ScanOutcome> waiters = waitersTable.scan();

        for (Item waiter : waiters) {
            String waiterId = waiter.getString("waiterId");

            QuerySpec querySpec = new QuerySpec()
                    .withKeyConditionExpression("waiterId = :v_waiter AND locationId = :v_location")
                    .withValueMap(new ValueMap().withString(":v_waiter", waiterId).withString(":v_location", locationId));

            int reservationCount = reservationsTable.query(querySpec).getAccumulatedItemCount();
            waiterLoad.put(waiterId, reservationCount);
        }

        return waiterLoad.entrySet()
                .stream()
                .min(Comparator.comparingInt(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElseThrow(() -> {
                    logger.severe("No available waiters found.");
                    return new IllegalStateException("No available waiters.");
                });
    }
}
