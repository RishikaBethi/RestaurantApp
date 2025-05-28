package com.restaurant.services;

import com.amazonaws.services.lambda.runtime.Context;  // Correct import
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.restaurant.dto.LocationsDTO;
import javax.inject.Inject;
import java.util.*;

public class LocationsService {
    private final DynamoDB dynamoDB;
    private final String tableName = System.getenv("LOCATIONS_TABLE");
    private final ObjectMapper objectMapper;

    @Inject
    public LocationsService(DynamoDB dynamoDB, ObjectMapper objectMapper) {
        this.dynamoDB = dynamoDB;
        this.objectMapper = objectMapper;
    }

    public APIGatewayProxyResponseEvent allAvailableLocations(APIGatewayProxyRequestEvent event, Context context) {
        try {
            Table table = dynamoDB.getTable(tableName);
            ScanSpec scanSpec = new ScanSpec();
            ItemCollection<ScanOutcome> locations = table.scan(scanSpec);

            List<LocationsDTO> locationsData = new ArrayList<>();
            for (Item location : locations) {
                LocationsDTO locationDTO = new LocationsDTO(
                        location.getString("locationId"),
                        location.getString("address")
                );
                locationsData.add(locationDTO);
            }

            APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
            response.setHeaders(createCorsHeaders());
            response.setStatusCode(200);
            response.setBody(objectMapper.writeValueAsString(locationsData));
            return response;

        } catch (Exception e) {
            context.getLogger().log("Error retrieving locations: " + e.getMessage());
            APIGatewayProxyResponseEvent errorResponse = new APIGatewayProxyResponseEvent();
            errorResponse.setHeaders(createCorsHeaders());
            errorResponse.setStatusCode(500);
            errorResponse.setBody("[]"); // Return empty array in case of error
            return errorResponse;
        }
    }

    private Map<String, String> createCorsHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Access-Control-Allow-Origin", "*");
        headers.put("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        headers.put("Access-Control-Allow-Headers", "Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token");
        return Collections.unmodifiableMap(headers);
    }
}