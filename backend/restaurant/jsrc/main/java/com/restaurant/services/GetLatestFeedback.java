package com.restaurant.services;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AmazonDynamoDBException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.dto.RecentFeedbackDTO;

import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Logger;
import static com.restaurant.utils.Helper.*;

public class GetLatestFeedback {
    private static final Logger logger = Logger.getLogger(GetLatestFeedback.class.getName());
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final Table feedbackTable;
    private final Table reservationTable;
    private final Table waitersTable;

    public GetLatestFeedback(DynamoDB dynamoDB) {
        this.feedbackTable = dynamoDB.getTable(System.getenv("FEEDBACKS_TABLE"));
        this.reservationTable = dynamoDB.getTable(System.getenv("RESERVATIONS_TABLE"));
        this.waitersTable = dynamoDB.getTable(System.getenv("WAITERS_TABLE"));
    }

    public APIGatewayProxyResponseEvent returnLatestFeedback(APIGatewayProxyRequestEvent request, Context context) {

        try {
            context.getLogger().log("In get a feedback function");
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
            if(reservationId!=null) {
                context.getLogger().log("Fetching data from DB");
                Item getReservation = reservationTable.getItem("reservationId", reservationId);

                String waiterId = getReservation.getString("waiterId");
                context.getLogger().log("Waiter Id : " + waiterId);

                String waiterName = waitersTable.getItem("waiterId", waiterId).getString("waiterName");
                context.getLogger().log(waiterName+" ");

                String feedbackId = getReservation.getString("feedbackId");
                if(feedbackId==null) {
                    return createApiResponse(200, Map.of("waiterName", waiterName));
                }
                String locationId = getReservation.getString("locationId");
                context.getLogger().log(feedbackId);

                Item feedback = feedbackTable.getItem("feedbackId", feedbackId, "locationId", locationId);
                context.getLogger().log(feedback.getString("serviceComment"));
                Double serviceRating = feedback.getNumber("serviceRating").doubleValue();
                Double cuisineRating = feedback.getNumber("cuisineRating").doubleValue();

                RecentFeedbackDTO dto = new RecentFeedbackDTO(
                        feedback.getString("serviceComment"),
                        feedback.getString("cuisineComment"),
                        serviceRating,
                        cuisineRating,
                        waiterName
                );

                return createApiResponse(200, dto);
            }
            else {
                return createErrorResponse(400, "Reservation not found");

            }
        } catch (AmazonDynamoDBException e) {
            logger.severe("Failed to update reservation with feedbackId: " + e.getMessage());
            throw new RuntimeException("Error updating reservation", e);
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
}