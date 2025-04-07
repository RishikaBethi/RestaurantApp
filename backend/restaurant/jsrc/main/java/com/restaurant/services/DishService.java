package com.restaurant.services;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.ScanOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.dto.DishDTO;
import static com.restaurant.utils.Helper.*;

import javax.inject.Inject;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class DishService {
    private static final Logger logger = Logger.getLogger(DishService.class.getName());
    private final DynamoDB dynamoDB;
    private final ObjectMapper objectMapper;
    private static final String DISHES_TABLE = System.getenv("DISHES_TABLE");
    private static final int POPULAR_DISH_LIMIT = 4;

    @Inject
    public DishService(DynamoDB dynamoDB, ObjectMapper objectMapper) {
        this.dynamoDB = dynamoDB;
        this.objectMapper = objectMapper;
    }

    public APIGatewayProxyResponseEvent getPopularDishes(APIGatewayProxyRequestEvent request) {
        try {
            if (DISHES_TABLE == null || DISHES_TABLE.trim().isEmpty()) {
                logger.severe("DISHES_TABLE environment variable not set");
                return createErrorResponse(500, "DISHES_TABLE not set");
            }

            Table table = dynamoDB.getTable(DISHES_TABLE);
            ItemCollection<ScanOutcome> items = table.scan();

            // Convert ItemCollection to Stream using StreamSupport
            List<DishDTO> popularDishes = StreamSupport.stream(items.spliterator(), false)
                    .sorted(Comparator.comparingInt(item -> {
                        try {
                            // Check if dishFrequency exists and get its value
                            if (item.hasAttribute("dishFrequency")) {
                                return -item.getInt("dishFrequency"); // Negative for descending order
                            }
                            return 0;
                        } catch (Exception e) {
                            logger.warning("Error parsing dishFrequency for item " +
                                    (item.hasAttribute("dishId") ? item.getString("dishId") : "unknown") +
                                    ": " + e.getMessage());
                            return 0;
                        }
                    }))
                    .limit(POPULAR_DISH_LIMIT)
                    .map(item -> {
                        try {
                            return new DishDTO(
                                    item.hasAttribute("dishName") ? item.getString("dishName") : "",
                                    item.hasAttribute("dishPrice") ? String.valueOf(item.get("dishPrice")) : "0",
                                    item.hasAttribute("weight") ? String.valueOf(item.get("weight")) : "",
                                    item.hasAttribute("dishImageUrl") ? item.getString("dishImageUrl") : ""
                            );
                        } catch (Exception e) {
                            logger.warning("Error mapping dish " +
                                    (item.hasAttribute("dishId") ? item.getString("dishId") : "unknown") +
                                    ": " + e.getMessage());
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            logger.info("Retrieved " + popularDishes.size() + " popular dishes");

            String responseBody = objectMapper.writeValueAsString(popularDishes);
            return createApiResponse(200, responseBody);

        } catch (Exception e) {
            logger.severe("Error fetching popular dishes: " + e.getMessage());
            return createErrorResponse(500, e.getMessage());
        }
    }
}