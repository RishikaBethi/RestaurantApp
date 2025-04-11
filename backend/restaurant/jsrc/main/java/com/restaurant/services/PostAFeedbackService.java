package com.restaurant.services;

import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.AmazonDynamoDBException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.std.StdKeySerializers;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import static com.restaurant.utils.Helper.*;

public class PostAFeedbackService {

    private static final Logger logger = Logger.getLogger(PostAFeedbackService.class.getName());
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final Table reservationTable;
    private final Table ordersTable;
    private final Table feedbackTable;

    public PostAFeedbackService(DynamoDB dynamoDB) {
        this.reservationTable = dynamoDB.getTable(System.getenv("RESERVATIONS_TABLE"));
        this.ordersTable = dynamoDB.getTable(System.getenv("ORDERS_TABLE"));
        this.feedbackTable = dynamoDB.getTable(System.getenv("FEEDBACKS_TABLE"));
    }

    public APIGatewayProxyResponseEvent handlePostAFeedback(APIGatewayProxyRequestEvent request, Context context) {

        try {
            context.getLogger().log("In post a feedback function");

            Map<String, String> requestBody = parseJson(request.getBody());
            if (requestBody == null || requestBody.isEmpty()) {
                return createErrorResponse(400, "Invalid request data: Empty request body.");
            }

            Map<String, Object> claims = extractClaims(request);
            logger.info("Extracted claims: " + claims); // Debugging purpose
            String userId = (String) claims.get("sub");
            String email = (String) claims.get("email");

            if (userId == null || userId.isEmpty()) {
                return createErrorResponse(401, "Unauthorized: Missing or invalid token.");
            }

            String reservationId = requestBody.get("reservationId");
            ZoneId zoneId = ZoneId.of("Asia/Kolkata");

            String dateString = LocalDate.now(zoneId)
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            logger.info(reservationId);
            if(reservationId!=null) {
                logger.info("Inside if");
//                ScanSpec scanSpec = new ScanSpec()
//                        .withFilterExpression("reservationId = :resId")
//                        .withValueMap(new ValueMap().withString(":resId", reservationId));
//                Item requiredReservation = reservationTable.scan(scanSpec).iterator().next();
                Item requiredReservation = reservationTable.getItem("reservationId", reservationId);
                logger.info(requiredReservation.getString("locationId"));
                String locationId = requiredReservation.getString("locationId");
                if((requiredReservation.getString("status")).equals("In Progress")) {
                    String serviceRating = requestBody.get("serviceRating");
                    String serviceComment = requestBody.get("serviceComment");
                    double serviceRatingDouble;
                    if(serviceRating!=null) {
                        try {
                            serviceRatingDouble = Double.parseDouble(serviceRating);
                        }
                        catch(NumberFormatException e) {
                            return createErrorResponse(400, "Rating needs to be a number");
                        }
                    }
                    else {
                        return createErrorResponse(400,"Rating cannot be empty");
                    }

                    String orderId = requiredReservation.getString("orderId");
//                    ScanSpec scanOrdersTable = new ScanSpec()
//                            .withFilterExpression("orderId = :ordId")
//                            .withValueMap(new ValueMap().withString(":ordId", orderId));
//                    Item order = ordersTable.scan(scanOrdersTable).iterator().next();
                    Item order = ordersTable.getItem("orderId", orderId);
                    String feedbackId = UUID.randomUUID().toString();
                    if((order.getString("status")).equals("Finished") && !order.getMap("dishItems").isEmpty()) {
                        String cuisineRating = requestBody.get("cuisineRating");
                        String cuisineComment = requestBody.get("cuisineComment");
                        double cuisineRatingDouble;
                        if(cuisineRating!=null) {
                            try {
                                cuisineRatingDouble = Double.parseDouble(cuisineRating);
                            }
                            catch(NumberFormatException e) {
                                return createErrorResponse(400, "Rating needs to be an integer");
                            }
                        }
                        else {
                            return createErrorResponse(400, "Rating cannot be empty");
                        }
                        logger.info("Adding to tables");
                        addItemTOTable(feedbackId, locationId, cuisineComment, cuisineRatingDouble, serviceComment, serviceRatingDouble, dateString, email, reservationId);
                        return createApiResponse(200, "Your feedback is valuable to us");
                    }
                    else {
                        String cuisineRating = requestBody.get("cuisineRating");
                        String cuisineComment = requestBody.get("cuisineComment");
                        if(cuisineRating!=null || cuisineComment!=null) {
                            return createErrorResponse(400,"Cuisine feedback cannot be provided before ordering");
                        }
                        addItemTOTable(feedbackId, locationId, null, null, serviceComment, serviceRatingDouble, dateString, email, reservationId);
                        return createApiResponse(200, "Your feedback is valuable to us");
                    }
                }
            }
            return createErrorResponse(400, "Reservation ID cannot be null");
        }
        catch (Exception e) {
            logger.severe("Error creating reservation: " + e.getMessage());
            return createErrorResponse(500, "Error creating reservation: " + e.getMessage());
        }
    }

    public Map<String, String> parseJson(String json) {
        try {
            logger.info("Parsing JSON: " + json);  // Log incoming JSON
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            logger.severe("Error parsing JSON: " + e.getMessage());
            return Map.of();
        }
    }
    
    public void addItemTOTable(String feedbackId, String locationId, String cuisineComment, Double cuisineRating, String serviceComment, Double serviceRating, String date, String email, String reservationId) {
        try {

            logger.info("Adding items to tables");
            logger.info("ReservationId "+reservationId);
            logger.info("FeedbdackId"+feedbackId);

            feedbackTable.putItem(new PutItemSpec().withItem(new Item()
                    .withPrimaryKey("feedbackId", feedbackId)
                    .withString("locationId", locationId)
                    .withString("cuisineComment", cuisineComment)
                    .withNumber("cuisineRating", cuisineRating)
                    .withString("date", date)
                    .withString("email", email)
                    .withString("reservationId", reservationId)
                    .withString("serviceComment", serviceComment)
                    .withNumber("serviceRating", serviceRating)
            ));

            logger.info("Adding items to reservations table");

            reservationTable.updateItem(new UpdateItemSpec()
                    .withPrimaryKey("reservationId", reservationId)
                    .withUpdateExpression("feedbackId = :feedbackId")
                    .withValueMap(new ValueMap().withString(":feedbackId", feedbackId))
            );

            logger.info("Done");
        }
        catch(AmazonDynamoDBException e) {
            logger.severe("Failed to update reservation with feedbackId: " + e.getMessage());
            throw new RuntimeException("Error updating reservation", e);
        }
    }
}
