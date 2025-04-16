package com.restaurant.services;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.NameMap;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.utils.Helper;

import javax.inject.Inject;
import java.util.Map;
import java.util.logging.Logger;

public class WaiterOrderService {
    private static final Logger logger = Logger.getLogger(WaiterOrderService.class.getName());
    private final DynamoDB dynamoDB;
    private final Table reservationsTable;
    private final Table ordersTable;
    private final ObjectMapper objectMapper;

    @Inject
    public WaiterOrderService(DynamoDB dynamoDB, ObjectMapper objectMapper) {
        this.dynamoDB = dynamoDB;
        this.reservationsTable = dynamoDB.getTable(System.getenv("RESERVATIONS_TABLE"));
        this.ordersTable = dynamoDB.getTable(System.getenv("ORDERS_TABLE"));
        this.objectMapper = objectMapper;
    }

    public APIGatewayProxyResponseEvent handleCreateOrUpdateOrder(APIGatewayProxyRequestEvent request) {
        try {
            logger.info("Processing create or update order request: " + request.getBody());
            // Parse request body
            String body = request.getBody();
            if (body == null || body.trim().isEmpty()) {
                logger.warning("Request body is null or empty");
                return Helper.createErrorResponse(400, "Request body is required");
            }

            // Extract reservationId and dishItems from body
            Map<String, Object> bodyMap = objectMapper.readValue(body, Map.class);
            String reservationId = (String) bodyMap.get("reservationId");
            @SuppressWarnings("unchecked")
            Map<String, Integer> dishItems = (Map<String, Integer>) bodyMap.get("dishItems");

            if (reservationId == null || dishItems == null) {
                logger.warning("Missing required fields: reservationId=" + reservationId + ", dishItems=" + dishItems);
                return Helper.createErrorResponse(400, "reservationId and dishItems are required");
            }

            logger.info("Extracted reservationId: " + reservationId + ", dishItems: " + dishItems);
            // Fetch reservation to get orderId and email
            Item reservation = reservationsTable.getItem("reservationId", reservationId);
            if (reservation == null) {
                logger.warning("Reservation not found for reservationId: " + reservationId);
                return Helper.createErrorResponse(404, "Reservation not found");
            }
            String orderId = reservation.getString("orderId");
            String email = reservation.getString("email");
            if (orderId == null || email == null) {
                logger.severe("OrderId or email not found in reservation: " + reservationId);
                return Helper.createErrorResponse(500, "OrderId or email not found in reservation");
            }

            logger.info("Retrieved orderId: " + orderId + " and email: " + email + " for reservationId: " + reservationId);
            // Fetch existing order with composite key
            GetItemSpec getItemSpec = new GetItemSpec()
                    .withPrimaryKey("orderId", orderId, "email", email);
            Item existingOrder = ordersTable.getItem(getItemSpec);
            logger.info("Fetched existing order: " + (existingOrder != null ? existingOrder.toJSON() : "null"));

            // Handle dishItems correctly
            Map<String, Integer> existingDishItems = null;
            if (existingOrder != null && existingOrder.isPresent("dishItems")) {
                Object dishItemsObj = existingOrder.get("dishItems");
                if (dishItemsObj instanceof Map) {
                    existingDishItems = (Map<String, Integer>) dishItemsObj;
                } else {
                    logger.severe("Unsupported dishItems type: " + dishItemsObj.getClass().getName());
                    return Helper.createErrorResponse(500, "Unsupported dishItems data type");
                }
            }

            logger.info("Existing dishItems: " + (existingDishItems != null ? existingDishItems.toString() : "null"));
            // Update or create order using UpdateItemSpec
            if (existingDishItems == null || existingDishItems.isEmpty()) {
                logger.info("Creating new order for orderId: " + orderId + ", email: " + email);
                // New order creation with UpdateItemSpec
                UpdateItemSpec updateItemSpec = new UpdateItemSpec()
                        .withPrimaryKey("orderId", orderId, "email", email)
                        .withUpdateExpression("set dishItems = :di, #st = :s")
                        .withNameMap(new NameMap().with("#st", "status"))
                        .withValueMap(new ValueMap()
                                .withMap(":di", dishItems)
                                .withString(":s", "InProgress"))
                        .withReturnValues("NONE");
                ordersTable.updateItem(updateItemSpec);
                logger.info("New order created successfully for orderId: " + orderId + ", email: " + email);
                return Helper.createApiResponse(201, Map.of("message", "Success: New order has been created successfully"));
            } else {
                if (existingOrder == null) {
                    logger.warning("Existing order not found for orderId: " + orderId + ", email: " + email);
                    return Helper.createErrorResponse(404, "Existing order not found");
                }
                logger.info("Updating existing order for orderId: " + orderId + ", email: " + email);
                // Update existing order with only dishItems
                existingDishItems.putAll(dishItems); // Merge new dishItems
                UpdateItemSpec updateItemSpec = new UpdateItemSpec()
                        .withPrimaryKey("orderId", orderId, "email", email)
                        .withUpdateExpression("set dishItems = :di")
                        .withValueMap(new ValueMap()
                                .withMap(":di", existingDishItems))
                        .withReturnValues("NONE");
                ordersTable.updateItem(updateItemSpec);
                logger.info("Order updated successfully for orderId: " + orderId + ", email: " + email);
                return Helper.createApiResponse(200, Map.of("message", "Success: All changes have been saved successfully"));
            }
        } catch (Exception e) {
            logger.severe("Error handling order: " + e.getMessage());
            return Helper.createErrorResponse(500, "Error processing order: " + e.getMessage());
        }
    }
}