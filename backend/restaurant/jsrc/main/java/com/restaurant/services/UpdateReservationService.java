package com.restaurant.services;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import static com.restaurant.utils.Helper.*;
import java.util.logging.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;

public class UpdateReservationService {
    private static final Logger logger = Logger.getLogger(UpdateReservationService.class.getName());
    private final Table reservationTable;
    private final DynamoDB dynamoDB;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public UpdateReservationService(DynamoDB dynamoDB) {
        this.dynamoDB = dynamoDB;
        this.reservationTable = dynamoDB.getTable(System.getenv("RESERVATIONS_TABLE"));
    }

    public APIGatewayProxyResponseEvent handleUpdateReservation(APIGatewayProxyRequestEvent request, String path) {
        try {
            String[] pathParts = path.split("/");
            if (pathParts.length < 3)
                return createErrorResponse(400, "Invalid path");

            String reservationId = pathParts[pathParts.length - 1];

            Map<String, String> requestBody = parseJson(request.getBody());
            if (requestBody == null || requestBody.isEmpty())
                return createErrorResponse(400, "Empty body");

            UpdateItemSpec updateSpec = new UpdateItemSpec().withPrimaryKey("reservationId", reservationId);

            StringBuilder updateExpr = new StringBuilder("set ");
            ValueMap values = new ValueMap();
            Map<String, String> names = new HashMap<>();

            List<String> editableFields = List.of("tableNumber", "date", "guestsNumber", "timeFrom", "timeTo", "locationId");
            for (String key : editableFields) {
                if (requestBody.containsKey(key)) {
                    updateExpr.append("#").append(key).append(" = :").append(key).append(", ");
                    names.put("#" + key, key);
                    values.with(":" + key, requestBody.get(key));
                }
            }

            if (values.isEmpty())
                return createErrorResponse(400, "No editable fields provided");

            updateExpr.setLength(updateExpr.length() - 2); // remove trailing comma and space
            updateSpec.withUpdateExpression(updateExpr.toString()).withValueMap(values).withNameMap(names);

            reservationTable.updateItem(updateSpec);

            // Fetch updated reservation
            Item updatedItem = reservationTable.getItem(new GetItemSpec().withPrimaryKey("reservationId", reservationId));
            if (updatedItem == null) {
                return createErrorResponse(404, "Updated reservation not found");
            }

            String timeSlot = updatedItem.getString("timeFrom") + " - " + updatedItem.getString("timeTo");

            return createApiResponse(200, Map.of("message", "The reservation has been updated."));

        } catch (Exception e) {
            return createErrorResponse(500, "Error updating reservation: " + e.getMessage());
        }
    }

    private Map<String, String> parseJson(String json) {
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            return Map.of();
        }
    }
}
