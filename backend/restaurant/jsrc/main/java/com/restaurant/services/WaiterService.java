package com.restaurant.services;

import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.document.utils.NameMap;
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
        Map<String, Integer> waiterLoad = new HashMap<>();

        // Step 1: Scan all waiters at this location
        ScanSpec scanSpec = new ScanSpec()
                .withFilterExpression("locationId = :v_location")
                .withValueMap(new ValueMap().withString(":v_location", locationId));
        ItemCollection<ScanOutcome> scanResult = waitersTable.scan(scanSpec);

        for (Item waiterItem : scanResult) {
            String waiterId = waiterItem.getString("waiterId");
            if (waiterId == null) continue;

            // Step 2: Count how many reservations this waiter has
            ScanSpec reservationScan = new ScanSpec()
                    .withFilterExpression("waiterId = :v_waiterId AND locationId = :v_location AND #st <> :v_cancelled")
                    .withNameMap(new NameMap().with("#st", "status"))
                    .withValueMap(new ValueMap()
                            .withString(":v_waiterId", waiterId)
                            .withString(":v_location", locationId)
                            .withString(":v_cancelled", "Cancelled")
                    );

            int reservationCount = 0;
            for (Item ignored : reservationsTable.scan(reservationScan)) {
                reservationCount++;
            }

            waiterLoad.put(waiterId, reservationCount);
        }

        // Step 3: Pick the least busy waiter
        return waiterLoad.entrySet().stream()
                .min(Comparator.comparingInt(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElseThrow(() -> {
                    logger.severe("No available waiters found for location: " + locationId);
                    return new IllegalStateException("No available waiters.");
                });
    }
}