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
    private final Table usersTable;

    public PostAFeedbackService(DynamoDB dynamoDB) {
        this.reservationTable = dynamoDB.getTable(System.getenv("RESERVATIONS_TABLE"));
        this.ordersTable = dynamoDB.getTable(System.getenv("ORDERS_TABLE"));
        this.feedbackTable = dynamoDB.getTable(System.getenv("FEEDBACKS_TABLE"));
        this.usersTable = dynamoDB.getTable(System.getenv("USERS_TABLE"));
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

            Item userDetails = usersTable.getItem("email",email);
            String userAvatarUrl = userDetails.getString("imageUrl");
            String userName = userDetails.getString("firstName")+" "+userDetails.getString("lastName");


            String reservationId = requestBody.get("reservationId");
            ZoneId zoneId = ZoneId.of("Asia/Kolkata");

            String dateString = LocalDate.now(zoneId)
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            if(reservationId!=null) {
                Item requiredReservation = reservationTable.getItem("reservationId", reservationId);
                if(requiredReservation==null) {
                    return createErrorResponse(404, "Reservation not found");
                }
                String locationId = requiredReservation.getString("locationId");
                String statusOfReservation = requiredReservation.getString("status");
                String feedbackId = requiredReservation.getString("feedbackId");
                if(statusOfReservation.equals("In Progress") || statusOfReservation.equals("Finished")) {
                    String serviceRating = requestBody.get("serviceRating");
                    String serviceComment = requestBody.get("serviceComment");
                    Double serviceRatingDouble = null; // Allow null if not provided
                    // Require serviceRating if serviceComment is non-empty
                    if (serviceComment != null && !serviceComment.trim().isEmpty() &&
                            (serviceRating == null || serviceRating.trim().isEmpty())) {
                        return createErrorResponse(400, "Service rating is required when service comment is provided");
                    }
                    if (serviceRating != null && !serviceRating.trim().isEmpty()) {
                        try {
                            serviceRatingDouble = Double.parseDouble(serviceRating);
                            if(serviceRatingDouble<=0.0) {
                                return createErrorResponse(400, "Service rating is mandatory");
                            }
                        } catch (NumberFormatException e) {
                            return createErrorResponse(400, "Service rating must be a number");
                        }
                    }

                    String orderId = requiredReservation.getString("orderId");
                    String userMail = requiredReservation.getString("email");
                    Item order = ordersTable.getItem("orderId", orderId, "email", userMail);
                    if(feedbackId==null) feedbackId = UUID.randomUUID().toString();
                    if((order.getString("status")).equals("Finished") && !order.getMap("dishItems").isEmpty()) {
                        String cuisineRating = requestBody.get("cuisineRating");
                        String cuisineComment = requestBody.get("cuisineComment");
                        Double cuisineRatingDouble = null; // Allow null if not provided
                        // Only require cuisineRating if cuisineComment is non-empty
                        if (cuisineComment != null && !cuisineComment.trim().isEmpty() &&
                                (cuisineRating == null || cuisineRating.trim().isEmpty())) {
                            return createErrorResponse(400, "Cuisine rating is required when cuisine comment is provided");
                        }
                        if (cuisineRating != null && !cuisineRating.trim().isEmpty()) {
                            try {
                                cuisineRatingDouble = Double.parseDouble(cuisineRating);
                                if(cuisineRatingDouble<=0.0) {
                                    return createErrorResponse(400, "Cuisine rating is mandatory");
                                }
                            } catch (NumberFormatException e) {
                                return createErrorResponse(400, "Cuisine rating must be a number");
                            }
                        }
                        logger.info("Adding to tables");
                        addItemTOTable(feedbackId, locationId, cuisineComment, cuisineRatingDouble, serviceComment, serviceRatingDouble, dateString, email, reservationId, userAvatarUrl, userName);
                        return createApiResponse(201, Map.of("message", "Feedback has been created"));
                    }
                    else {
                        String cuisineRating = requestBody.get("cuisineRating");
                        String cuisineComment = requestBody.get("cuisineComment");
                        addItemTOTable(feedbackId, locationId, null, null, serviceComment, serviceRatingDouble, dateString, email, reservationId, userAvatarUrl, userName);
                        if (cuisineRating != null && !cuisineRating.trim().isEmpty() ||
                                cuisineComment != null && !cuisineComment.trim().isEmpty()) {
                            return createErrorResponse(400,"Service feedback saved, but cuisine feedback cannot be provided before ordering");
                        }
                        return createApiResponse(201, Map.of("message", "Feedback has been created"));
                    }
                }
                else if(statusOfReservation.equals("Reserved") || statusOfReservation.equals("Cancelled")){
                    return createErrorResponse(400, "Feedback can be provided only once your reservation is in progress or has finished");
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
    
    public void addItemTOTable(String feedbackId, String locationId, String cuisineComment, Number cuisineRating, String serviceComment, Number serviceRating, String date, String email, String reservationId, String userAvatarUrl, String userName) {
        try {

            logger.info("Adding items to tables");


            Item item = new Item()
                    .withPrimaryKey("feedbackId", feedbackId, "locationId", locationId)
                    .withString("date", date)
                    .withString("email", email)
                    .withString("reservationId", reservationId)
                    .withString("userName", userName);

            // Add optional fields only if non-null
            if (cuisineComment != null) {
                item.withString("cuisineComment", cuisineComment);
            }
            if (cuisineRating != null) {
                item.withNumber("cuisineRating", cuisineRating);
            }
            if (serviceComment != null) {
                item.withString("serviceComment", serviceComment);
            }
            if (serviceRating != null) {
                item.withNumber("serviceRating", serviceRating);
            }

            if(userAvatarUrl != null) {
                item.withString("userAvatarUrl", userAvatarUrl);
            }

            feedbackTable.putItem(new PutItemSpec().withItem(item));

            logger.info("Adding items to reservations table");

            reservationTable.updateItem(new UpdateItemSpec()
                    .withPrimaryKey("reservationId", reservationId)
                    .withUpdateExpression("SET feedbackId = :feedbackId")
                    .withValueMap(new ValueMap().withString(":feedbackId", feedbackId))
            );

        }
        catch(AmazonDynamoDBException e) {
            logger.severe("Failed to update reservation with feedbackId: " + e.getMessage());
            throw new RuntimeException("Error updating reservation", e);
        }
    }
}
