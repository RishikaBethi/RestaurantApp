package com.restaurant.services;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.document.utils.NameMap;
import com.fasterxml.jackson.databind.JsonNode;
import com.amazonaws.services.dynamodbv2.document.Item;
import javax.inject.Inject;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class ReportService {

    private static final Logger logger = Logger.getLogger(ReportService.class.getName());

    private final DynamoDB dynamoDB;
    private final Table waiterStatsTable;
    private final Table locationStatsTable;

    private static final String WAITER_DAILY_STATS_TABLE = System.getenv("WAITER_DAILY_STATS_TABLE");
    private static final String LOCATION_DAILY_STATS_TABLE = System.getenv("LOCATION_DAILY_STATS_TABLE");

    @Inject
    public ReportService(DynamoDB dynamoDB) {
        this.dynamoDB = dynamoDB;
        this.waiterStatsTable = dynamoDB.getTable(WAITER_DAILY_STATS_TABLE);
        this.locationStatsTable = dynamoDB.getTable(LOCATION_DAILY_STATS_TABLE);
    }

    public void processOrderFinished(JsonNode orderEvent) {
        String waiterId = orderEvent.get("waiterId").asText();
        String locationId = orderEvent.get("locationId").asText();
        String date = LocalDate.now(ZoneId.of("Asia/Kolkata")).toString();
        double revenue = orderEvent.get("revenue").asDouble();
        String timeSlot = orderEvent.get("timeSlot").asText();

        updateWaiterStats(waiterId, locationId, date, 1, timeSlot, 0);
        updateLocationStats(locationId, date, 1, revenue, 0);
    }

    public void processFeedbackGiven(JsonNode feedbackEvent) {
        String waiterId = feedbackEvent.get("waiterId").asText();
        String locationId = feedbackEvent.get("locationId").asText();
        String date = LocalDate.now(ZoneId.of("Asia/Kolkata")).toString();
        double serviceRating = feedbackEvent.get("serviceFeedback").asDouble();
        double cuisineRating = feedbackEvent.get("cuisineFeedback").asDouble();

        updateWaiterStats(waiterId, locationId, date, 0, null, serviceRating);
        updateLocationStats(locationId, date, 0, 0, cuisineRating);
    }

    private void updateWaiterStats(String waiterId, String locationId, String date, int orderIncrement, String newSlot, double serviceRating) {
        try {
            Item existingItem = waiterStatsTable.getItem("waiterId", waiterId, "date", date);

            int updatedOrders = orderIncrement;
            Set<String> updatedSlots = new HashSet<>();
            double totalServiceFeedback = serviceRating;
            int serviceFeedbackCount = serviceRating > 0 ? 1 : 0;
            double minServiceFeedback = serviceRating > 0 ? serviceRating : Double.MAX_VALUE;

            if (newSlot != null) {
                updatedSlots.add(newSlot);
            }

            if (existingItem != null) {
                updatedOrders += existingItem.getInt("ordersProcessed");

                List<String> existingSlots = existingItem.getList("workedSlots");
                if (existingSlots != null) {
                    updatedSlots.addAll(existingSlots);
                }

                double existingTotalFeedback = existingItem.getDouble("totalServiceFeedback");
                int existingFeedbackCount = existingItem.getInt("serviceFeedbackCount");
                double existingMinFeedback = existingItem.getDouble("minServiceFeedback");

                if (serviceRating > 0) {
                    totalServiceFeedback += existingTotalFeedback;
                    serviceFeedbackCount += existingFeedbackCount;
                    if (existingMinFeedback != 0) {
                        minServiceFeedback = Math.min(existingMinFeedback, serviceRating);
                    }
                } else {
                    totalServiceFeedback = existingTotalFeedback;
                    serviceFeedbackCount = existingFeedbackCount;
                    minServiceFeedback = existingMinFeedback;
                }

                // Update existing item
                UpdateItemSpec updateItemSpec = new UpdateItemSpec()
                        .withPrimaryKey("waiterId", waiterId, "date", date)
                        .withNameMap(new NameMap()
                                .with("#loc", "locationId")
                                .with("#op", "ordersProcessed")
                                .with("#ws", "workedSlots")
                                .with("#tf", "totalServiceFeedback")
                                .with("#fc", "serviceFeedbackCount")
                                .with("#mf", "minServiceFeedback"))
                        .withUpdateExpression("SET #loc = :loc, #op = :op, #ws = :ws, #tf = :tf, #fc = :fc, #mf = :mf")
                        .withValueMap(new ValueMap()
                                .withString(":loc", locationId)
                                .withNumber(":op", updatedOrders)
                                .withStringSet(":ws", updatedSlots)
                                .withNumber(":tf", totalServiceFeedback)
                                .withNumber(":fc", serviceFeedbackCount)
                                .withNumber(":mf", minServiceFeedback == Double.MAX_VALUE ? 0 : minServiceFeedback));

                waiterStatsTable.updateItem(updateItemSpec);
                logger.info("Updated waiter stats for " + waiterId + " on " + date);
            } else {
                // Create new item
                Item newItem = new Item()
                        .withPrimaryKey("waiterId", waiterId, "date", date)
                        .withString("locationId", locationId)
                        .withNumber("ordersProcessed", updatedOrders)
                        .withNumber("totalServiceFeedback", totalServiceFeedback)
                        .withNumber("serviceFeedbackCount", serviceFeedbackCount)
                        .withNumber("minServiceFeedback", minServiceFeedback == Double.MAX_VALUE ? 0 : minServiceFeedback);

                if (!updatedSlots.isEmpty()) {
                    newItem.withStringSet("workedSlots", updatedSlots);
                }

                waiterStatsTable.putItem(newItem);
                logger.info("Created new waiter stats for " + waiterId + " on " + date);
            }

        } catch (Exception e) {
            logger.severe("Error updating waiter stats for " + waiterId + " on " + date + ": " + e.getMessage());
        }
    }


    private void updateLocationStats(String locationId, String date, int orderIncrement, double revenueIncrement, double cuisineRating) {
        try {
            Item existingItem = locationStatsTable.getItem("locationId", locationId, "date", date);

            int updatedOrders = orderIncrement;
            double updatedRevenue = revenueIncrement;
            double newTotalCuisineFeedback = cuisineRating;
            int newFeedbackCount = cuisineRating > 0 ? 1 : 0;
            double minCuisineFeedback = cuisineRating > 0 ? cuisineRating : Double.MAX_VALUE;

            if (existingItem != null) {
                updatedOrders += existingItem.getInt("ordersProcessed");
                updatedRevenue += existingItem.getDouble("revenue");

                if (cuisineRating > 0) {
                    newTotalCuisineFeedback += existingItem.getDouble("totalCuisineFeedback");
                    newFeedbackCount += existingItem.getInt("cuisineFeedbackCount");
                    if (existingItem.getDouble("minCuisineFeedback") != 0) {
                        minCuisineFeedback = Math.min(existingItem.getDouble("minCuisineFeedback"), cuisineRating);
                    }
                } else {
                    newTotalCuisineFeedback = existingItem.getDouble("totalCuisineFeedback");
                    newFeedbackCount = existingItem.getInt("cuisineFeedbackCount");
                    minCuisineFeedback = existingItem.getDouble("minCuisineFeedback");
                }
            }

            UpdateItemSpec updateItemSpec = new UpdateItemSpec()
                    .withPrimaryKey("locationId", locationId, "date", date)
                    .withNameMap(new NameMap()
                            .with("#op", "ordersProcessed")
                            .with("#rev", "revenue")
                            .with("#tf", "totalCuisineFeedback")
                            .with("#fc", "cuisineFeedbackCount")
                            .with("#mf", "minCuisineFeedback"))
                    .withUpdateExpression("SET #op = :op, #rev = :rev, #tf = :tf, #fc = :fc, #mf = :mf")
                    .withValueMap(new ValueMap()
                            .withNumber(":op", updatedOrders)
                            .withNumber(":rev", updatedRevenue)
                            .withNumber(":tf", newTotalCuisineFeedback)
                            .withNumber(":fc", newFeedbackCount)
                            .withNumber(":mf", minCuisineFeedback == Double.MAX_VALUE ? 0 : minCuisineFeedback));

            locationStatsTable.updateItem(updateItemSpec);
        } catch (Exception e) {
            logger.severe("Error updating location stats: " + e.getMessage());
        }
    }

}
