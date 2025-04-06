package com.restaurant.services;

import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.dto.DishDTO;
import com.restaurant.dto.LocationDTO;

import javax.inject.Inject;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

//import static org.openjdk.tools.sjavac.Util.getStackTrace;

public class LocationService {
    private static final Logger logger = Logger.getLogger(LocationService.class.getName());
    private final DynamoDB dynamoDB;
    private final ObjectMapper objectMapper;
    private static final String LOCATIONS_TABLE = System.getenv("LOCATIONS_TABLE");
    private static final String DISHES_TABLE = System.getenv("DISHES_TABLE");

    @Inject
    public LocationService(DynamoDB dynamoDB, ObjectMapper objectMapper) {
        this.dynamoDB = dynamoDB;
        this.objectMapper = objectMapper;
    }
    public APIGatewayProxyResponseEvent getLocations(APIGatewayProxyRequestEvent request) {
        try {
            if (LOCATIONS_TABLE == null || LOCATIONS_TABLE.isEmpty()) {
                logger.severe("LOCATIONS_TABLE environment variable is not set");
                return createResponse(500, "{\"message\":\"Server configuration error: LOCATIONS_TABLE not set\"}");
            }

            logger.info("Fetching locations from table: " + LOCATIONS_TABLE);
            Table table = dynamoDB.getTable(LOCATIONS_TABLE);
            List<LocationDTO> locations = new ArrayList<>();

            for (Item item : table.scan()) {
                LocationDTO location = new LocationDTO(
                        item.getString("locationId") != null ? item.getString("locationId") : "", // Changed from "id" to "locationId"
                        item.getString("address") != null ? item.getString("address") : "",
                        item.getString("description") != null ? item.getString("description") : "",
                        item.isPresent("totalCapacity") ? String.valueOf(item.getInt("totalCapacity")) : "0",
                        item.isPresent("averageOccupancy") ? String.valueOf(item.getDouble("averageOccupancy")) : "0.0",
                        item.getString("imageUrl") != null ? item.getString("imageUrl") : "",
                        item.isPresent("rating") ? String.valueOf(item.getDouble("rating")) : "0.0"
                );
                locations.add(location);
            }

            return createResponse(200, objectMapper.writeValueAsString(locations));
        } catch (Exception e) {
            //logger.severe("Error in getLocations: " + e.getMessage() + ", Stacktrace: " + getStackTrace(e));
            return createResponse(500, "{\"message\":\"Internal Server Error: " + e.getMessage() + "\"}");
        }
    }
    public APIGatewayProxyResponseEvent getSpecialityDishes(APIGatewayProxyRequestEvent request) {
        try {
            String locationId = Optional.ofNullable(request.getQueryStringParameters())
                    .map(params -> params.get("locationId"))
                    .filter(id -> !id.trim().isEmpty())
                    .orElseThrow(() -> {
                        logger.warning("Missing or empty locationId in query parameters");
                        return new IllegalArgumentException("Location ID is required and cannot be empty");
                    });

            logger.info("Fetching speciality dishes for locationId: " + locationId);
            List<DishDTO> dishes = fetchSpecialityDishes(locationId);

            return createResponse(200, objectMapper.writeValueAsString(dishes));
        } catch (IllegalArgumentException e) {
            return createResponse(404, "{\"message\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.severe("Error fetching speciality dishes: " + e.getMessage());
            return createResponse(500, "{\"message\":\"Internal Server Error\"}");
        }
    }

    private List<DishDTO> fetchSpecialityDishes(String locationId) {
        validateTableNames();

        Table locationsTable = dynamoDB.getTable(LOCATIONS_TABLE);
        Item locationItem = locationsTable.getItem(new PrimaryKey("locationId", locationId));

        if (locationItem == null) {
            logger.warning("Location not found for locationId: " + locationId);
            throw new IllegalArgumentException("Location not found");
        }

        // Safely handle the specialDishes attribute
        List<String> specialDishIds;
        try {
            Object specialDishesObj = locationItem.get("specialDishes");

            if (specialDishesObj == null) {
                logger.info("No special dishes found for locationId: " + locationId);
                return Collections.emptyList();
            }

            if (specialDishesObj instanceof List<?>) {
                @SuppressWarnings("unchecked")
                List<Object> rawList = (List<Object>) specialDishesObj;
                if (rawList.isEmpty()) {
                    logger.info("Empty special dishes list for locationId: " + locationId);
                    return Collections.emptyList();
                }

                // Convert to List<String> and handle potential type mismatches
                specialDishIds = rawList.stream()
                        .filter(Objects::nonNull)
                        .map(Object::toString)
                        .collect(Collectors.toList());
            } else {
                logger.warning("specialDishes is not a list for locationId: " + locationId);
                return Collections.emptyList();
            }
        } catch (Exception e) {
            logger.severe("Error processing specialDishes for locationId: " + locationId + ": " + e.getMessage());
            return Collections.emptyList();
        }

        if (specialDishIds.isEmpty()) {
            logger.info("No valid special dish IDs found for locationId: " + locationId);
            return Collections.emptyList();
        }

        return fetchDishesByIds(specialDishIds);
    }

    private List<DishDTO> fetchDishesByIds(List<String> dishIds) {
        Table dishesTable = dynamoDB.getTable(DISHES_TABLE);

        return dishIds.stream()
                .map(dishId -> {
                    try {
                        Item dishItem = dishesTable.getItem(new PrimaryKey("dishId", dishId));
                        if (dishItem == null) {
                            logger.warning("Dish not found for dishId: " + dishId);
                            return null;
                        }

                        // Log the raw item for debugging
                        logger.fine("Processing dish item: " + dishItem.toJSON());

                        return new DishDTO(
                                dishItem.getString("dishName") != null ? dishItem.getString("dishName") : "",
                                dishItem.getString("dishPrice") != null ? dishItem.getString("dishPrice") : "0",
                                dishItem.getString("weight") != null ? dishItem.getString("weight") : "",
                                dishItem.getString("dishImageUrl") != null ? dishItem.getString("dishImageUrl") : ""
                        );
                    } catch (Exception e) {
                        logger.warning("Error processing dishId " + dishId + ": " + e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private void validateTableNames() {
        if (LOCATIONS_TABLE == null || LOCATIONS_TABLE.trim().isEmpty()) {
            logger.severe("LOCATIONS_TABLE environment variable not set");
            throw new IllegalStateException("Locations table configuration missing");
        }
        if (DISHES_TABLE == null || DISHES_TABLE.trim().isEmpty()) {
            logger.severe("DISHES_TABLE environment variable not set");
            throw new IllegalStateException("Dishes table configuration missing");
        }
    }

    private APIGatewayProxyResponseEvent createResponse(int statusCode, String body) {
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withBody(body)
                .withHeaders(Map.of(
                        "Content-Type", "application/json",
                        "Access-Control-Allow-Origin", "*"
                ));
    }
}