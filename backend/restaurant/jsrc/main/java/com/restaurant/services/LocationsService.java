package com.restaurant.services;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.restaurant.dto.LocationsDTO;
import org.json.JSONArray;

import javax.inject.Inject;
import java.util.*;
import static com.restaurant.utils.Helper.*;

public class LocationsService {
    private final DynamoDB dynamoDB;
    private final String tableName = System.getenv("LOCATIONS_TABLE");
    //private final ObjectMapper objectMapper;

    @Inject
    public LocationsService(DynamoDB dynamoDB) {
        this.dynamoDB = dynamoDB;
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

            JSONArray jsonArray = new JSONArray();
            for (LocationsDTO dto : locationsData) {
                jsonArray.put(dto.toJson());
            }

            return createApiResponse(200, jsonArray);

        } catch (Exception e) {
            return createErrorResponse(500, "Error retrieving locations: " + e.getMessage());
        }
    }
}