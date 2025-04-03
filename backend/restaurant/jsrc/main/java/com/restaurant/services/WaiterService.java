package com.restaurant.services;

import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import java.util.*;
import java.util.logging.Logger;

public class WaiterService {
    private static final Logger logger = Logger.getLogger(WaiterService.class.getName());
    private static final String WAITERS_TABLE = System.getenv("WAITERS_TABLE");
    private static final String RESERVATIONS_TABLE = System.getenv("RESERVATIONS_TABLE");

    private final DynamoDB dynamoDB;
    private final Table waitersTable;
    private final Table reservationsTable;

    public WaiterService(DynamoDB dynamoDB) {
        this.dynamoDB = dynamoDB;
        this.waitersTable = dynamoDB.getTable(WAITERS_TABLE);
        this.reservationsTable = dynamoDB.getTable(RESERVATIONS_TABLE);
    }

    public String assignWaiter(String locationId) {
        Index userIndex = reservationsTable.getIndex("userId-index");
        if (userIndex == null) {
            logger.severe("Index userId-index does not exist in " + RESERVATIONS_TABLE);
            throw new IllegalStateException("userId-index does not exist in " + RESERVATIONS_TABLE);
        }

        Map<String, Integer> waiterLoad = new HashMap<>();

        // Step 1: Scan all waiters for the given location
        ScanSpec scanSpec = new ScanSpec()
                .withFilterExpression("locationId = :v_location")
                .withValueMap(new ValueMap().withString(":v_location", locationId));

        ItemCollection<ScanOutcome> scanResult = waitersTable.scan(scanSpec);

        for (Item item : scanResult) {
            String waiterId = item.getString("waiterId");

            //  Step 2: Count reservations for this waiter
            QuerySpec querySpec = new QuerySpec()
                    .withKeyConditionExpression("userId = :v_user")
                    .withValueMap(new ValueMap().withString(":v_user", waiterId));

            int reservationCount = userIndex.query(querySpec).getAccumulatedItemCount();
            waiterLoad.put(waiterId, reservationCount);
        }

        // Step 3: Assign the least busy waiter
        return waiterLoad.entrySet()
                .stream()
                .min(Comparator.comparingInt(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElseThrow(() -> {
                    logger.severe("No available waiters found for location: " + locationId);
                    return new IllegalStateException("No available waiters.");
                });
    }
}