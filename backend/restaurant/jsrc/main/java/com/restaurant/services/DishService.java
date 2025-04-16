////package com.restaurant.services;
////
////import com.amazonaws.services.dynamodbv2.document.*;
////import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
////import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
////import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
////import com.fasterxml.jackson.databind.ObjectMapper;
////import com.restaurant.dto.DishDTO;
////import com.restaurant.dto.DishResponseDTO;
////import com.restaurant.dto.DishIdDTO;
////import com.restaurant.dto.PopularDishDTO;
////
////import javax.inject.Inject;
////import java.util.*;
////import java.util.logging.Logger;
////import java.util.stream.Collectors;
////import java.util.stream.StreamSupport;
////
////import static com.restaurant.utils.Helper.createErrorResponse;
////import static com.restaurant.utils.Helper.extractClaims;
////
////public class DishService {
////    private static final Logger logger = Logger.getLogger(DishService.class.getName());
////    private final DynamoDB dynamoDB;
////    private final ObjectMapper objectMapper;
////    private static final String DISHES_TABLE = System.getenv("DISHES_TABLE");
////    private static final int POPULAR_DISH_LIMIT = 4;
////
////    @Inject
////    public DishService(DynamoDB dynamoDB, ObjectMapper objectMapper) {
////        this.dynamoDB = dynamoDB;
////        this.objectMapper = objectMapper;
////    }
////
////    public APIGatewayProxyResponseEvent getPopularDishes(APIGatewayProxyRequestEvent request) {
////        try {
////            if (DISHES_TABLE == null || DISHES_TABLE.trim().isEmpty()) {
////                logger.severe("DISHES_TABLE environment variable not set");
////                return createResponse(500, "{\"message\":\"Server configuration error: DISHES_TABLE not set\"}", null);
////            }
////
////            Table table = dynamoDB.getTable(DISHES_TABLE);
////            ItemCollection<ScanOutcome> items = table.scan();
////
////            List<PopularDishDTO> popularDishes = StreamSupport.stream(items.spliterator(), false)
////                    .sorted(Comparator.comparingInt(item -> {
////                        try {
////                            if (item.hasAttribute("dishFrequency")) {
////                                return -item.getInt("dishFrequency"); // Negative for descending order
////                            }
////                            return 0;
////                        } catch (Exception e) {
////                            logger.warning("Error parsing dishFrequency for item " +
////                                    (item.hasAttribute("dishId") ? item.getString("dishId") : "unknown") +
////                                    ": " + e.getMessage());
////                            return 0;
////                        }
////                    }))
////                    .limit(POPULAR_DISH_LIMIT)
////                    .map(item -> mapItemToPopularDishDTO(item))
////                    .filter(Objects::nonNull)
////                    .collect(Collectors.toList());
////
////            logger.info("Retrieved " + popularDishes.size() + " popular dishes");
////            String responseBody = objectMapper.writeValueAsString(popularDishes);
////            return createResponse(200,responseBody.substring(1), popularDishes.toArray(new PopularDishDTO[0]));
////
////        } catch (Exception e) {
////            logger.severe("Error fetching popular dishes: " + e.getMessage());
////            return createResponse(500, "{\"message\":\"Internal Server Error: " + e.getMessage() + "\"}", null);
////        }
////    }
////
////    public APIGatewayProxyResponseEvent getAllDishes(APIGatewayProxyRequestEvent request) {
////        try {
////            // Extract user ID from JWT claims for authentication
////            Map<String, Object> claims = extractClaims(request);
////            logger.info("Extracted claims: " + claims); // Debugging purpose
////            String userId = (String) claims.get("sub");
////            String email = (String) claims.get("email");
////
////            if (userId == null || userId.isEmpty()) {
////                return createResponse(401, "{\"message\":\"Unauthorized: Missing or invalid token.\"}", null);
////            }
////
////            if (DISHES_TABLE == null || DISHES_TABLE.trim().isEmpty()) {
////                logger.severe("DISHES_TABLE environment variable not set");
////                return createResponse(500, "{\"message\":\"Server configuration error: DISHES_TABLE not set\"}", null);
////            }
////
////            Table table = dynamoDB.getTable(DISHES_TABLE);
////            ItemCollection<ScanOutcome> items = table.scan();
////
////            List<DishDTO> dishes = StreamSupport.stream(items.spliterator(), false)
////                    .map(item -> mapItemToDishDTO(item))
////                    .filter(Objects::nonNull)
////                    .collect(Collectors.toList());
////
////            // Apply filtering and sorting based on query parameters
////            Map<String, String> queryParams = request.getQueryStringParameters();
////            if (queryParams != null) {
////                dishes = filterAndSortDishes(dishes, queryParams);
////            }
////
////            logger.info("Retrieved " + dishes.size() + " dishes");
////            String responseBody = objectMapper.writeValueAsString(new DishResponseDTO(dishes.toArray(new DishDTO[0])));
////            return createResponse(200, "{\"message\":\"Successful operation\", \"description\":\"Successful menu retrieval\", \"content\":" + responseBody.substring(1), dishes.toArray(new DishDTO[0]));
////
////        } catch (Exception e) {
////            logger.severe("Error fetching all dishes: " + e.getMessage());
////            return createResponse(500, "{\"message\":\"Internal Server Error: " + e.getMessage() + "\"}", null);
////        }
////    }
////
////    public APIGatewayProxyResponseEvent getDishById(APIGatewayProxyRequestEvent request, String path) {
////        try {
////            String[] pathParts = path.split("/");
////            if (pathParts.length < 3) {
////                return createErrorResponse(400, "Invalid reservation cancellation request.");
////            }
////            String dishId = pathParts[pathParts.length - 1];
////            Map<String, Object> claims = extractClaims(request);
////            logger.info("Extracted claims: " + claims); // Debugging purpose
////            String userId = (String) claims.get("sub");
////            String email = (String) claims.get("email");
////
////            if (userId == null || userId.isEmpty()) {
////                return createResponse(401, "{\"message\":\"Unauthorized: Missing or invalid token.\"}", null);
////            }
////
////            if (DISHES_TABLE == null || DISHES_TABLE.trim().isEmpty()) {
////                logger.severe("DISHES_TABLE environment variable not set");
////                return createResponse(500, "{\"message\":\"Server configuration error: DISHES_TABLE not set\"}", null);
////            }
////
////            // Extract dishId from path parameters
////          // String dishId = request.getPathParameters().get("id");
////            if (dishId == null || dishId.trim().isEmpty()) {
////                return createResponse(400, "{\"message\":\"Bad Request: Missing or invalid dish ID\"}", null);
////            }
////
////            Table table = dynamoDB.getTable(DISHES_TABLE);
////            Map<String, Object> expressionAttributeValues = new HashMap<>();
////            expressionAttributeValues.put(":id", dishId);
////            QuerySpec querySpec = new QuerySpec()
////                    .withKeyConditionExpression("dishId = :id")
////                    .withValueMap(expressionAttributeValues);
////
////            ItemCollection<QueryOutcome> items = table.query(querySpec);
////            DishIdDTO dish = StreamSupport.stream(items.spliterator(), false)
////                    .findFirst()
////                    .map(item -> mapItemToDishIdDTO(item))
////                    .orElse(null);
////
////            if (dish == null) {
////                return createResponse(404, "{\"message\":\"Not Found: Dish with ID " + dishId + " not found\"}", null);
////            }
////
////            logger.info("Retrieved dish with ID: " + dishId);
////            String responseBody = objectMapper.writeValueAsString(dish);
////            return createResponse(200, "{\"message\":\"Successful operation\", \"description\":\"Successful dish retrieval\", \"content\":" + responseBody.substring(1), new DishIdDTO[]{dish});
////
////        } catch (Exception e) {
////            logger.severe("Error fetching dish by ID: " + e.getMessage());
////            return createResponse(500, "{\"message\":\"Internal Server Error: " + e.getMessage() + "\"}", null);
////        }
////    }
////
////    private DishDTO mapItemToDishDTO(Item item) {
////        try {
////            return new DishDTO(
////                    item.getString("dishId"),
////                    item.getString("dishName"),
////                    item.getString("dishImageUrl"),
////                    item.getString("dishPrice"),
////                    item.getString("state"),
////                    item.getString("weight"),
////                    item.hasAttribute("dishFrequency") ? item.getInt("dishFrequency") : 0
////            );
////        } catch (Exception e) {
////            logger.warning("Error mapping dish " +
////                    (item.hasAttribute("dishId") ? item.getString("dishId") : "unknown") +
////                    ": " + e.getMessage());
////            return null;
////        }
////    }
////
////    private DishIdDTO mapItemToDishIdDTO(Item item) {
////        try {
////            return new DishIdDTO(
////                    item.getString("calories"),
////                    item.getString("carbohydrates"),
////                    item.getString("dishDescription"),
////                    item.getString("dishType"),
////                    item.getString("fats"),
////                    item.getString("dishId"),
////                    item.getString("dishImageUrl"),
////                    item.getString("dishName"),
////                    item.getString("dishPrice"),
////                    item.getString("proteins"),
////                    item.getString("state"),
////                    item.getString("vitamins"),
////                    item.getString("weight"),
////                    item.hasAttribute("dishFrequency") ? item.getInt("dishFrequency") : 0
////            );
////        } catch (Exception e) {
////            logger.warning("Error mapping detailed dish " +
////                    (item.hasAttribute("dishId") ? item.getString("dishId") : "unknown") +
////                    ": " + e.getMessage());
////            return null;
////        }
////    }
////
////    private PopularDishDTO mapItemToPopularDishDTO(Item item) {
////        try {
////            return new PopularDishDTO(
////                    item.getString("dishName"),
////                    item.getString("dishPrice"),
////                    item.getString("weight"),
////                    item.getString("dishImageUrl"),
////                    item.hasAttribute("dishFrequency") ? item.getInt("dishFrequency") : 0
////            );
////        } catch (Exception e) {
////            logger.warning("Error mapping popular dish " +
////                    (item.hasAttribute("dishId") ? item.getString("dishId") : "unknown") +
////                    ": " + e.getMessage());
////            return null;
////        }
////    }
////
////    private List<DishDTO> filterAndSortDishes(List<DishDTO> dishes, Map<String, String> queryParams) {
////        List<DishDTO> filteredDishes = new ArrayList<>(dishes);
////
////        // Sort by price or popularity
////        if (queryParams != null && queryParams.containsKey("sortBy")) {
////            String sortBy = queryParams.get("sortBy").toLowerCase();
////            if ("price-asc".equals(sortBy)) {
////                filteredDishes.sort(Comparator.comparing(DishDTO::getPrice, (p1, p2) -> {
////                    try {
////                        return Double.compare(Double.parseDouble(p1.replace("$", "").trim()), Double.parseDouble(p2.replace("$", "").trim()));
////                    } catch (NumberFormatException e) {
////                        logger.warning("Failed to parse price for sorting: p1=" + p1 + ", p2=" + p2);
////                        return 0;
////                    }
////                }));
////            } else if ("price-desc".equals(sortBy)) {
////                filteredDishes.sort(Comparator.comparing(DishDTO::getPrice, (p1, p2) -> {
////                    try {
////                        return Double.compare(Double.parseDouble(p2.replace("$", "").trim()), Double.parseDouble(p1.replace("$", "").trim()));
////                    } catch (NumberFormatException e) {
////                        logger.warning("Failed to parse price for sorting: p1=" + p2 + ", p2=" + p1);
////                        return 0;
////                    }
////                }));
////            } else if ("popularity-asc".equals(sortBy)) {
////                filteredDishes.sort(Comparator.comparingInt(DishDTO::getDishFrequency));
////            } else if ("popularity-desc".equals(sortBy)) {
////                filteredDishes.sort(Comparator.comparingInt(DishDTO::getDishFrequency).reversed());
////            }
////        }
////
////        return filteredDishes;
////    }
////
////    private APIGatewayProxyResponseEvent createResponse(int statusCode, String body, Object[] content) {
////        String finalBody;
////        if (statusCode == 200 && content != null) {
////            finalBody = body; // Already includes message and description for 200
////        } else {
////            finalBody = body; // Use the provided error message for other codes
////        }
////        return new APIGatewayProxyResponseEvent()
////                .withStatusCode(statusCode)
////                .withBody(finalBody)
////                .withHeaders(Map.of(
////                        "Content-Type", "application/json",
////                        "Access-Control-Allow-Origin", "*"
////                ));
////    }
////}
////package com.restaurant.services;
////
////import com.amazonaws.services.dynamodbv2.document.*;
////import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
////import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
////import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
////import com.fasterxml.jackson.databind.ObjectMapper;
////import com.restaurant.dto.DishDTO;
////import com.restaurant.dto.DishResponseDTO;
////import com.restaurant.dto.DishIdDTO;
////import com.restaurant.dto.PopularDishDTO;
////import com.restaurant.utils.Helper;
////
////import javax.inject.Inject;
////import java.util.*;
////import java.util.logging.Logger;
////import java.util.stream.Collectors;
////import java.util.stream.StreamSupport;
////
////public class DishService {
////    private static final Logger logger = Logger.getLogger(DishService.class.getName());
////    private final DynamoDB dynamoDB;
////    private final ObjectMapper objectMapper;
////    private static final String DISHES_TABLE = System.getenv("DISHES_TABLE");
////    private static final int POPULAR_DISH_LIMIT = 4;
////    private static final Set<String> VALID_DISH_TYPES = Set.of("MAIN COURSES", "DESSERTS", "APPETIZERS");
////    private static final Set<String> VALID_SORT_FIELDS = Set.of("price", "popularity");
////    private static final Set<String> VALID_SORT_DIRECTIONS = Set.of("asc", "desc");
////
////    @Inject
////    public DishService(DynamoDB dynamoDB, ObjectMapper objectMapper) {
////        this.dynamoDB = dynamoDB;
////        this.objectMapper = objectMapper;
////    }
////
////    public APIGatewayProxyResponseEvent getPopularDishes(APIGatewayProxyRequestEvent request) {
////        try {
////            if (DISHES_TABLE == null || DISHES_TABLE.trim().isEmpty()) {
////                logger.severe("DISHES_TABLE environment variable not set");
////                return Helper.createErrorResponse(500, "Server configuration error: DISHES_TABLE not set");
////            }
////
////            Table table = dynamoDB.getTable(DISHES_TABLE);
////            ItemCollection<ScanOutcome> items = table.scan();
////
////            List<PopularDishDTO> popularDishes = StreamSupport.stream(items.spliterator(), false)
////                    .sorted(Comparator.comparingInt(item -> {
////                        try {
////                            if (item.hasAttribute("dishFrequency")) {
////                                return -item.getInt("dishFrequency"); // Negative for descending order
////                            }
////                            return 0;
////                        } catch (Exception e) {
////                            logger.warning("Error parsing dishFrequency for item " +
////                                    (item.hasAttribute("dishId") ? item.getString("dishId") : "unknown") +
////                                    ": " + e.getMessage());
////                            return 0;
////                        }
////                    }))
////                    .limit(POPULAR_DISH_LIMIT)
////                    .map(item -> mapItemToPopularDishDTO(item))
////                    .filter(Objects::nonNull)
////                    .collect(Collectors.toList());
////
////            logger.info("Retrieved " + popularDishes.size() + " popular dishes");
////            String responseBody = objectMapper.writeValueAsString(popularDishes);
////            return Helper.createApiResponse(200, responseBody.substring(1));
////
////        } catch (Exception e) {
////            logger.severe("Error fetching popular dishes: " + e.getMessage());
////            return Helper.createErrorResponse(500, "Internal Server Error: " + e.getMessage());
////        }
////    }
////
////    public APIGatewayProxyResponseEvent getAllDishes(APIGatewayProxyRequestEvent request) {
////        try {
////            // Extract user ID from JWT claims for authentication
////            Map<String, Object> claims = Helper.extractClaims(request);
////            logger.info("Extracted claims: " + claims);
////            String userId = (String) claims.get("sub");
////            String email = (String) claims.get("email");
////
////            if (userId == null || userId.isEmpty()) {
////                return Helper.createErrorResponse(401, "Unauthorized: Missing or invalid token.");
////            }
////
////            if (DISHES_TABLE == null || DISHES_TABLE.trim().isEmpty()) {
////                logger.severe("DISHES_TABLE environment variable not set");
////                return Helper.createErrorResponse(500, "Server configuration error: DISHES_TABLE not set");
////            }
////
////            Table table = dynamoDB.getTable(DISHES_TABLE);
////            ItemCollection<ScanOutcome> items = table.scan();
////
////            List<DishDTO> dishes = StreamSupport.stream(items.spliterator(), false)
////                    .map(item -> mapItemToDishDTO(item))
////                    .filter(Objects::nonNull)
////                    .collect(Collectors.toList());
////
////            // Apply filtering and sorting based on query parameters
////            Map<String, String> queryParams = request.getQueryStringParameters();
////            if (queryParams != null) {
////                dishes = filterAndSortDishes(dishes, queryParams);
////            }
////
////            logger.info("Retrieved " + dishes.size() + " dishes");
////            String responseBody = objectMapper.writeValueAsString(new DishResponseDTO(dishes.toArray(new DishDTO[0])));
////            return Helper.createApiResponse(200,"{\"message\":\"Successful operation\", \"description\":\"Successful menu retrieval\", \"content\":" + responseBody);
////
////        } catch (Exception e) {
////            logger.severe("Error fetching all dishes: " + e.getMessage());
////            return Helper.createErrorResponse(500, "Internal Server Error: " + e.getMessage());
////        }
////    }
////
////    public APIGatewayProxyResponseEvent getDishById(APIGatewayProxyRequestEvent request, String path) {
////        try {
////            String[] pathParts = path.split("/");
////            if (pathParts.length < 3) {
////                return Helper.createErrorResponse(400, "Invalid dish ID request format.");
////            }
////            String dishId = pathParts[pathParts.length - 1];
////            Map<String, Object> claims = Helper.extractClaims(request);
////            logger.info("Extracted claims: " + claims);
////            String userId = (String) claims.get("sub");
////            String email = (String) claims.get("email");
////
////            if (userId == null || userId.isEmpty()) {
////                return Helper.createErrorResponse(401, "Unauthorized: Missing or invalid token.");
////            }
////
////            if (DISHES_TABLE == null || DISHES_TABLE.trim().isEmpty()) {
////                logger.severe("DISHES_TABLE environment variable not set");
////                return Helper.createErrorResponse(500, "Server configuration error: DISHES_TABLE not set");
////            }
////
////            if (dishId == null || dishId.trim().isEmpty()) {
////                return Helper.createErrorResponse(400, "Bad Request: Missing or invalid dish ID");
////            }
////
////            Table table = dynamoDB.getTable(DISHES_TABLE);
////            Map<String, Object> expressionAttributeValues = new HashMap<>();
////            expressionAttributeValues.put(":id", dishId);
////            QuerySpec querySpec = new QuerySpec()
////                    .withKeyConditionExpression("dishId = :id")
////                    .withValueMap(expressionAttributeValues);
////
////            ItemCollection<QueryOutcome> items = table.query(querySpec);
////            DishIdDTO dish = StreamSupport.stream(items.spliterator(), false)
////                    .findFirst()
////                    .map(item -> mapItemToDishIdDTO(item))
////                    .orElse(null);
////
////            if (dish == null) {
////                return Helper.createErrorResponse(404, "Not Found: Dish with ID " + dishId + " not found");
////            }
////
////            logger.info("Retrieved dish with ID: " + dishId);
////            String responseBody = objectMapper.writeValueAsString(dish);
////            return Helper.createApiResponse(200, "{\"message\":\"Successful operation\", \"description\":\"Successful menu retrieval\", \"content\":" +responseBody);
////
////        } catch (Exception e) {
////            logger.severe("Error fetching dish by ID: " + e.getMessage());
////            return Helper.createErrorResponse(500, "Internal Server Error: " + e.getMessage());
////        }
////    }
////
////    private DishDTO mapItemToDishDTO(Item item) {
////        try {
////            return new DishDTO(
////                    item.getString("dishId"),
////                    item.getString("dishName"),
////                    item.getString("dishImageUrl"),
////                    item.getString("dishPrice"),
////                    item.getString("state"),
////                    item.getString("weight"),
////                    item.getString("dishType"), // Added to match updated DishDTO
////                    item.hasAttribute("dishFrequency") ? item.getInt("dishFrequency") : 0
////            );
////        } catch (Exception e) {
////            logger.warning("Error mapping dish " +
////                    (item.hasAttribute("dishId") ? item.getString("dishId") : "unknown") +
////                    ": " + e.getMessage());
////            return null;
////        }
////    }
////
////    private DishIdDTO mapItemToDishIdDTO(Item item) {
////        try {
////            return new DishIdDTO(
////                    item.getString("calories"),
////                    item.getString("carbohydrates"),
////                    item.getString("dishDescription"),
////                    item.getString("dishType"),
////                    item.getString("fats"),
////                    item.getString("dishId"),
////                    item.getString("dishImageUrl"),
////                    item.getString("dishName"),
////                    item.getString("dishPrice"),
////                    item.getString("proteins"),
////                    item.getString("state"),
////                    item.getString("vitamins"),
////                    item.getString("weight"),
////                    item.hasAttribute("dishFrequency") ? item.getInt("dishFrequency") : 0
////            );
////        } catch (Exception e) {
////            logger.warning("Error mapping detailed dish " +
////                    (item.hasAttribute("dishId") ? item.getString("dishId") : "unknown") +
////                    ": " + e.getMessage());
////            return null;
////        }
////    }
////
////    private PopularDishDTO mapItemToPopularDishDTO(Item item) {
////        try {
////            return new PopularDishDTO(
////                    item.getString("dishName"),
////                    item.getString("dishPrice"),
////                    item.getString("weight"),
////                    item.getString("dishImageUrl"),
////                    item.hasAttribute("dishFrequency") ? item.getInt("dishFrequency") : 0
////            );
////        } catch (Exception e) {
////            logger.warning("Error mapping popular dish " +
////                    (item.hasAttribute("dishId") ? item.getString("dishId") : "unknown") +
////                    ": " + e.getMessage());
////            return null;
////        }
////    }
////
////    private List<DishDTO> filterAndSortDishes(List<DishDTO> dishes, Map<String, String> queryParams) {
////        List<DishDTO> filteredDishes = new ArrayList<>(dishes);
////
////        // Filter by dishType
////        if (queryParams != null && queryParams.containsKey("dishType")) {
////            String dishType = queryParams.get("dishType").toUpperCase();
////            if (!VALID_DISH_TYPES.contains(dishType)) {
////                logger.warning("Invalid dishType: " + dishType + ". Must be MAIN COURSES, DESSERTS, or APPETIZERS");
////                return filteredDishes; // Return unfiltered list for invalid dishType
////            }
////            filteredDishes = filteredDishes.stream()
////                    .filter(dish -> dish.getDishType() != null && dish.getDishType().toUpperCase().equals(dishType))
////                    .collect(Collectors.toList());
////        }
////
////        // Sort by price or popularity
////        if (queryParams != null && queryParams.containsKey("sortBy")) {
////            String sortBy = queryParams.get("sortBy").toLowerCase();
////            String[] sortParts = sortBy.split("-");
////            if (sortParts.length != 2) {
////                logger.warning("Invalid sortBy format: " + sortBy + ". Use format field-direction (e.g., price-asc)");
////                return filteredDishes; // Return unfiltered/sorted list for invalid format
////            }
////
////            String sortField = sortParts[0];
////            String sortDirection = sortParts[1];
////
////            if (!VALID_SORT_FIELDS.contains(sortField)) {
////                logger.warning("Invalid sort field: " + sortField + ". Must be price or popularity");
////                return filteredDishes; // Return unfiltered/sorted list for invalid field
////            }
////
////            if (!VALID_SORT_DIRECTIONS.contains(sortDirection)) {
////                logger.warning("Invalid sort direction: " + sortDirection + ". Must be asc or desc");
////                return filteredDishes; // Return unfiltered/sorted list for invalid direction
////            }
////
////            if ("price".equals(sortField)) {
////                filteredDishes.sort(Comparator.comparing(DishDTO::getPrice, (p1, p2) -> {
////                    try {
////                        return "asc".equals(sortDirection) ?
////                                Double.compare(Double.parseDouble(p1.replace("$", "").trim()), Double.parseDouble(p2.replace("$", "").trim())) :
////                                Double.compare(Double.parseDouble(p2.replace("$", "").trim()), Double.parseDouble(p1.replace("$", "").trim()));
////                    } catch (NumberFormatException e) {
////                        logger.warning("Failed to parse price for sorting: p1=" + p1 + ", p2=" + p2);
////                        return 0;
////                    }
////                }));
////            } else if ("popularity".equals(sortField)) {
////                filteredDishes.sort(Comparator.comparingInt(DishDTO::getDishFrequency)
////                        .reversed()); // Default to descending for popularity
////                if ("asc".equals(sortDirection)) {
////                    filteredDishes.sort(Comparator.comparingInt(DishDTO::getDishFrequency)); // Ascending if specified
////                }
////            }
////        }
////
////        return filteredDishes;
////    }
////}
////
//package com.restaurant.services;
//
//import com.amazonaws.services.dynamodbv2.document.*;
//import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
//import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
//import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.restaurant.dto.DishDTO;
//import com.restaurant.dto.DishResponseDTO;
//import com.restaurant.dto.DishIdDTO;
//import com.restaurant.dto.PopularDishDTO;
//import com.restaurant.utils.Helper;
//
//import javax.inject.Inject;
//import java.util.*;
//import java.util.logging.Logger;
//import java.util.stream.Collectors;
//import java.util.stream.StreamSupport;
//
//public class DishService {
//    private static final Logger logger = Logger.getLogger(DishService.class.getName());
//    private final DynamoDB dynamoDB;
//    private final ObjectMapper objectMapper;
//    private static final String DISHES_TABLE = System.getenv("DISHES_TABLE");
//    private static final int POPULAR_DISH_LIMIT = 4;
//    private static final Set<String> VALID_DISH_TYPES = Set.of("MAIN COURSES", "DESSERTS", "APPETIZERS");
//    private static final Set<String> VALID_SORT_FIELDS = Set.of("price", "popularity");
//    private static final Set<String> VALID_SORT_DIRECTIONS = Set.of("asc", "desc");
//    private static final String DEFAULT_SORT_BY = "popularity-asc";
//
//    @Inject
//    public DishService(DynamoDB dynamoDB, ObjectMapper objectMapper) {
//        this.dynamoDB = dynamoDB;
//        this.objectMapper = objectMapper;
//    }
//
//    public APIGatewayProxyResponseEvent getPopularDishes(APIGatewayProxyRequestEvent request) {
//        try {
//            if (DISHES_TABLE == null || DISHES_TABLE.trim().isEmpty()) {
//                logger.severe("DISHES_TABLE environment variable not set");
//                return Helper.createErrorResponse(500, "Server configuration error: DISHES_TABLE not set");
//            }
//
//            Table table = dynamoDB.getTable(DISHES_TABLE);
//            ItemCollection<ScanOutcome> items = table.scan();
//
//            List<PopularDishDTO> popularDishes = StreamSupport.stream(items.spliterator(), false)
//                    .sorted(Comparator.comparingInt(item -> {
//                        try {
//                            if (item.hasAttribute("dishFrequency")) {
//                                return -item.getInt("dishFrequency"); // Negative for descending order
//                            }
//                            return 0;
//                        } catch (Exception e) {
//                            logger.warning("Error parsing dishFrequency for item " +
//                                    (item.hasAttribute("dishId") ? item.getString("dishId") : "unknown") +
//                                    ": " + e.getMessage());
//                            return 0;
//                        }
//                    }))
//                    .limit(POPULAR_DISH_LIMIT)
//                    .map(item -> mapItemToPopularDishDTO(item))
//                    .filter(Objects::nonNull)
//                    .collect(Collectors.toList());
//
//            logger.info("Retrieved " + popularDishes.size() + " popular dishes");
//            String responseBody = objectMapper.writeValueAsString(popularDishes);
//            return Helper.createApiResponse(200, responseBody.substring(1));
//
//        } catch (Exception e) {
//            logger.severe("Error fetching popular dishes: " + e.getMessage());
//            return Helper.createErrorResponse(500, "Internal Server Error: " + e.getMessage());
//        }
//    }
//
//    public APIGatewayProxyResponseEvent getAllDishes(APIGatewayProxyRequestEvent request) {
//        try {
//            // Extract user ID from JWT claims for authentication
//            Map<String, Object> claims = Helper.extractClaims(request);
//            logger.info("Extracted claims: " + claims);
//            String userId = (String) claims.get("sub");
//            String email = (String) claims.get("email");
//
//            if (userId == null || userId.isEmpty()) {
//                return Helper.createErrorResponse(401, "Unauthorized: Missing or invalid token.");
//            }
//
//            if (DISHES_TABLE == null || DISHES_TABLE.trim().isEmpty()) {
//                logger.severe("DISHES_TABLE environment variable not set");
//                return Helper.createErrorResponse(500, "Server configuration error: DISHES_TABLE not set");
//            }
//
//            Table table = dynamoDB.getTable(DISHES_TABLE);
//            ItemCollection<ScanOutcome> items = table.scan();
//
//            List<DishDTO> dishes = StreamSupport.stream(items.spliterator(), false)
//                    .map(item -> mapItemToDishDTO(item))
//                    .filter(Objects::nonNull)
//                    .collect(Collectors.toList());
//
//            // Apply filtering and sorting based on query parameters
//            Map<String, String> queryParams = request.getQueryStringParameters();
//            List<DishDTO> filteredDishes = new ArrayList<>(dishes); // Start with all dishes
//
//            if (queryParams != null) {
//                String dishType = queryParams.get("dishType");
//                if (dishType != null && !dishType.trim().isEmpty()) {
//                    if (!VALID_DISH_TYPES.contains(dishType.toUpperCase())) {
//                        return Helper.createErrorResponse(400, "Invalid dishType: " + dishType + ". Must be MAIN COURSES, DESSERTS, or APPETIZERS");
//                    }
//                    String finalDishType = dishType;
//                    filteredDishes = filteredDishes.stream()
//                            .filter(dish -> dish.getDishType() != null && dish.getDishType().toUpperCase().equals(finalDishType.toUpperCase()))
//                            .collect(Collectors.toList());
//                } else {
//                    logger.info("dishType not provided, returning all dishes.");
//                }
//
//                String sortBy = queryParams.get("sortBy");
//                if (sortBy == null || sortBy.trim().isEmpty()) {
//                    logger.warning("sortBy not provided, using default: " + DEFAULT_SORT_BY);
//                    sortBy = DEFAULT_SORT_BY;
//                }
//
//                String[] sortParts = sortBy.toLowerCase().split("-");
//                if (sortParts.length != 2) {
//                    return Helper.createErrorResponse(400, "Invalid sortBy format: " + sortBy + ". Use format field-direction (e.g., price-asc or popularity-desc)");
//                }
//
//                String sortField = sortParts[0];
//                String sortDirection = sortParts[1];
//
//                if (!VALID_SORT_FIELDS.contains(sortField)) {
//                    return Helper.createErrorResponse(400, "Invalid sort field: " + sortField + ". Must be price or popularity");
//                }
//                if (!VALID_SORT_DIRECTIONS.contains(sortDirection)) {
//                    return Helper.createErrorResponse(400, "Invalid sort direction: " + sortDirection + ". Must be asc or desc");
//                }
//
//                // Apply sorting
//                filteredDishes = filterAndSortDishes(filteredDishes, queryParams);
//            }
//
//            logger.info("Retrieved " + filteredDishes.size() + " dishes");
//            String responseBody = objectMapper.writeValueAsString(new DishResponseDTO(filteredDishes.toArray(new DishDTO[0])));
//            return Helper.createApiResponse(200, "{\"message\":\"Successful operation\", \"description\":\"Successful menu retrieval\", \"content\":" + responseBody);
//
//        } catch (Exception e) {
//            logger.severe("Error fetching all dishes: " + e.getMessage());
//            return Helper.createErrorResponse(500, "Internal Server Error: " + e.getMessage());
//        }
//    }
//
//    public APIGatewayProxyResponseEvent getDishById(APIGatewayProxyRequestEvent request, String path) {
//        try {
//            String[] pathParts = path.split("/");
//            if (pathParts.length < 3) {
//                return Helper.createErrorResponse(400, "Invalid dish ID request format.");
//            }
//            String dishId = pathParts[pathParts.length - 1];
//            Map<String, Object> claims = Helper.extractClaims(request);
//            logger.info("Extracted claims: " + claims);
//            String userId = (String) claims.get("sub");
//            String email = (String) claims.get("email");
//
//            if (userId == null || userId.isEmpty()) {
//                return Helper.createErrorResponse(401, "Unauthorized: Missing or invalid token.");
//            }
//
//            if (DISHES_TABLE == null || DISHES_TABLE.trim().isEmpty()) {
//                logger.severe("DISHES_TABLE environment variable not set");
//                return Helper.createErrorResponse(500, "Server configuration error: DISHES_TABLE not set");
//            }
//
//            if (dishId == null || dishId.trim().isEmpty()) {
//                return Helper.createErrorResponse(400, "Bad Request: Missing or invalid dish ID");
//            }
//
//            Table table = dynamoDB.getTable(DISHES_TABLE);
//            Map<String, Object> expressionAttributeValues = new HashMap<>();
//            expressionAttributeValues.put(":id", dishId);
//            QuerySpec querySpec = new QuerySpec()
//                    .withKeyConditionExpression("dishId = :id")
//                    .withValueMap(expressionAttributeValues);
//
//            ItemCollection<QueryOutcome> items = table.query(querySpec);
//            DishIdDTO dish = StreamSupport.stream(items.spliterator(), false)
//                    .findFirst()
//                    .map(item -> mapItemToDishIdDTO(item))
//                    .orElse(null);
//
//            if (dish == null) {
//                return Helper.createErrorResponse(404, "Not Found: Dish with ID " + dishId + " not found");
//            }
//
//            logger.info("Retrieved dish with ID: " + dishId);
//            String responseBody = objectMapper.writeValueAsString(dish);
//            return Helper.createApiResponse(200, "{\"message\":\"Successful operation\", \"description\":\"Successful menu retrieval\", \"content\":" + responseBody);
//
//        } catch (Exception e) {
//            logger.severe("Error fetching dish by ID: " + e.getMessage());
//            return Helper.createErrorResponse(500, "Internal Server Error: " + e.getMessage());
//        }
//    }
//
//    private DishDTO mapItemToDishDTO(Item item) {
//        try {
//            return new DishDTO(
//                    item.getString("dishId"),
//                    item.getString("dishName"),
//                    item.getString("dishImageUrl"),
//                    item.getString("dishPrice"),
//                    item.getString("state"),
//                    item.getString("weight"),
//                    item.getString("dishType"), // Added to support filtering
//                    item.hasAttribute("dishFrequency") ? item.getInt("dishFrequency") : 0
//            );
//        } catch (Exception e) {
//            logger.warning("Error mapping dish " +
//                    (item.hasAttribute("dishId") ? item.getString("dishId") : "unknown") +
//                    ": " + e.getMessage());
//            return null;
//        }
//    }
//
//    private DishIdDTO mapItemToDishIdDTO(Item item) {
//        try {
//            return new DishIdDTO(
//                    item.getString("calories"),
//                    item.getString("carbohydrates"),
//                    item.getString("dishDescription"),
//                    item.getString("dishType"),
//                    item.getString("fats"),
//                    item.getString("dishId"),
//                    item.getString("dishImageUrl"),
//                    item.getString("dishName"),
//                    item.getString("dishPrice"),
//                    item.getString("proteins"),
//                    item.getString("state"),
//                    item.getString("vitamins"),
//                    item.getString("weight"),
//                    item.hasAttribute("dishFrequency") ? item.getInt("dishFrequency") : 0
//            );
//        } catch (Exception e) {
//            logger.warning("Error mapping detailed dish " +
//                    (item.hasAttribute("dishId") ? item.getString("dishId") : "unknown") +
//                    ": " + e.getMessage());
//            return null;
//        }
//    }
//
//    private PopularDishDTO mapItemToPopularDishDTO(Item item) {
//        try {
//            return new PopularDishDTO(
//                    item.getString("dishName"),
//                    item.getString("dishPrice"),
//                    item.getString("weight"),
//                    item.getString("dishImageUrl"),
//                    item.hasAttribute("dishFrequency") ? item.getInt("dishFrequency") : 0
//            );
//        } catch (Exception e) {
//            logger.warning("Error mapping popular dish " +
//                    (item.hasAttribute("dishId") ? item.getString("dishId") : "unknown") +
//                    ": " + e.getMessage());
//            return null;
//        }
//    }
//
//    private List<DishDTO> filterAndSortDishes(List<DishDTO> dishes, Map<String, String> queryParams) {
//        List<DishDTO> filteredDishes = new ArrayList<>(dishes);
//
//        // Sort by price or popularity
//        String sortBy = queryParams.getOrDefault("sortBy", DEFAULT_SORT_BY).toLowerCase();
//        String[] sortParts = sortBy.split("-");
//        if (sortParts.length != 2) {
//            logger.warning("Invalid sortBy format: " + sortBy + ". Using default: " + DEFAULT_SORT_BY);
//            sortBy = DEFAULT_SORT_BY;
//            sortParts = sortBy.split("-");
//        }
//
//        String sortField = sortParts[0];
//        String sortDirection = sortParts[1];
//
//        if (!VALID_SORT_FIELDS.contains(sortField)) {
//            logger.warning("Invalid sort field: " + sortField + ". Using default: popularity");
//            sortField = "popularity";
//        }
//        if (!VALID_SORT_DIRECTIONS.contains(sortDirection)) {
//            logger.warning("Invalid sort direction: " + sortDirection + ". Using default: asc");
//            sortDirection = "asc";
//        }
//
//        if ("price".equals(sortField)) {
//            String finalSortDirection = sortDirection;
//            filteredDishes.sort(Comparator.comparing(DishDTO::getPrice, (p1, p2) -> {
//                try {
//                    return "asc".equals(finalSortDirection) ?
//                            Double.compare(Double.parseDouble(p1.replace("$", "").trim()), Double.parseDouble(p2.replace("$", "").trim())) :
//                            Double.compare(Double.parseDouble(p2.replace("$", "").trim()), Double.parseDouble(p1.replace("$", "").trim()));
//                } catch (NumberFormatException e) {
//                    logger.warning("Failed to parse price for sorting: p1=" + p1 + ", p2=" + p2);
//                    return 0;
//                }
//            }));
//        } else if ("popularity".equals(sortField)) {
//            filteredDishes.sort(Comparator.comparingInt(DishDTO::getDishFrequency)
//                    .reversed()); // Default to descending for popularity
//            if ("asc".equals(sortDirection)) {
//                filteredDishes.sort(Comparator.comparingInt(DishDTO::getDishFrequency)); // Ascending if specified
//            }
//        }
//
//        return filteredDishes;
//    }
//}

package com.restaurant.services;

import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.dto.DishDTO;
import com.restaurant.dto.DishResponseDTO;
import com.restaurant.dto.DishIdDTO;
import com.restaurant.dto.PopularDishDTO;
import com.restaurant.utils.Helper;

import javax.inject.Inject;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class DishService {
    private static final Logger logger = Logger.getLogger(DishService.class.getName());
    private final DynamoDB dynamoDB;
    private final ObjectMapper objectMapper;
    private static final String DISHES_TABLE = System.getenv("DISHES_TABLE");
    private static final int POPULAR_DISH_LIMIT = 4;
    private static final Set<String> VALID_DISH_TYPES = Set.of("MAIN COURSES", "DESSERTS", "APPETIZERS");
    private static final Set<String> VALID_SORT_FIELDS = Set.of("price", "popularity");
    private static final Set<String> VALID_SORT_DIRECTIONS = Set.of("asc", "desc");
    private static final String DEFAULT_SORT_BY = "popularity-asc";

    @Inject
    public DishService(DynamoDB dynamoDB, ObjectMapper objectMapper) {
        this.dynamoDB = dynamoDB;
        this.objectMapper = objectMapper;
    }

    public APIGatewayProxyResponseEvent getPopularDishes(APIGatewayProxyRequestEvent request) {
        try {
            if (DISHES_TABLE == null || DISHES_TABLE.trim().isEmpty()) {
                logger.severe("DISHES_TABLE environment variable not set");
                return Helper.createErrorResponse(500, "Server configuration error: DISHES_TABLE not set");
            }

            Table table = dynamoDB.getTable(DISHES_TABLE);
            ItemCollection<ScanOutcome> items = table.scan();

            List<PopularDishDTO> popularDishes = StreamSupport.stream(items.spliterator(), false)
                    .sorted(Comparator.comparingInt(item -> {
                        try {
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
                    .map(item -> mapItemToPopularDishDTO(item))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            logger.info("Retrieved " + popularDishes.size() + " popular dishes");
            String responseBody = objectMapper.writeValueAsString(popularDishes); // Direct array output
            return Helper.createApiResponse(200, responseBody);

        } catch (Exception e) {
            logger.severe("Error fetching popular dishes: " + e.getMessage());
            return Helper.createErrorResponse(500, "Internal Server Error: " + e.getMessage());
        }
    }

    public APIGatewayProxyResponseEvent getAllDishes(APIGatewayProxyRequestEvent request) {
        try {
            // Extract user ID from JWT claims for authentication
            Map<String, Object> claims = Helper.extractClaims(request);
            logger.info("Extracted claims: " + claims);
            String userId = (String) claims.get("sub");
            String email = (String) claims.get("email");

            if (userId == null || userId.isEmpty()) {
                return Helper.createErrorResponse(401, "Unauthorized: Missing or invalid token.");
            }

            if (DISHES_TABLE == null || DISHES_TABLE.trim().isEmpty()) {
                logger.severe("DISHES_TABLE environment variable not set");
                return Helper.createErrorResponse(500, "Server configuration error: DISHES_TABLE not set");
            }

            Table table = dynamoDB.getTable(DISHES_TABLE);
            ItemCollection<ScanOutcome> items = table.scan();

            List<DishDTO> dishes = StreamSupport.stream(items.spliterator(), false)
                    .map(item -> mapItemToDishDTO(item))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            // Apply filtering and sorting based on query parameters
            Map<String, String> queryParams = request.getQueryStringParameters();
            List<DishDTO> filteredDishes = new ArrayList<>(dishes);

            if (queryParams != null) {
                String dishType = queryParams.get("dishType");
                if (dishType != null && !dishType.trim().isEmpty()) {
                    if (!VALID_DISH_TYPES.contains(dishType.toUpperCase())) {
                        return Helper.createErrorResponse(400, "Invalid dishType: " + dishType + ". Must be MAIN COURSES, DESSERTS, or APPETIZERS");
                    }
                    String finalDishType = dishType;
                    filteredDishes = filteredDishes.stream()
                            .filter(dish -> dish.getDishType() != null && dish.getDishType().toUpperCase().equals(finalDishType.toUpperCase()))
                            .collect(Collectors.toList());
                } else {
                    logger.info("dishType not provided, returning all dishes.");
                }

                String sortBy = queryParams.get("sortBy");
                if (sortBy == null || sortBy.trim().isEmpty()) {
                    logger.warning("sortBy not provided, using default: " + DEFAULT_SORT_BY);
                    sortBy = DEFAULT_SORT_BY;
                }

                String[] sortParts = sortBy.toLowerCase().split("-");
                if (sortParts.length != 2) {
                    return Helper.createErrorResponse(400, "Invalid sortBy format: " + sortBy + ". Use format field-direction (e.g., price-asc or popularity-desc)");
                }

                String sortField = sortParts[0];
                String sortDirection = sortParts[1];

                if (!VALID_SORT_FIELDS.contains(sortField)) {
                    return Helper.createErrorResponse(400, "Invalid sort field: " + sortField + ". Must be price or popularity");
                }
                if (!VALID_SORT_DIRECTIONS.contains(sortDirection)) {
                    return Helper.createErrorResponse(400, "Invalid sort direction: " + sortDirection + ". Must be asc or desc");
                }

                // Apply sorting
                filteredDishes = filterAndSortDishes(filteredDishes, queryParams);
            }

            logger.info("Retrieved " + filteredDishes.size() + " dishes");
            String content = objectMapper.writeValueAsString(filteredDishes);
            String responseBody = "{\"message\":\"Successful operation\", \"description\":\"Successful menu retrieval\", \"content\":" + content+" }";
            return Helper.createApiResponse(200, responseBody);

        } catch (Exception e) {
            logger.severe("Error fetching all dishes: " + e.getMessage());
            return Helper.createErrorResponse(500, "Internal Server Error: " + e.getMessage());
        }
    }

    public APIGatewayProxyResponseEvent getDishById(APIGatewayProxyRequestEvent request, String path) {
        try {
            String[] pathParts = path.split("/");
            if (pathParts.length < 3) {
                return Helper.createErrorResponse(400, "Invalid dish ID request format.");
            }
            String dishId = pathParts[pathParts.length - 1];
            Map<String, Object> claims = Helper.extractClaims(request);
            logger.info("Extracted claims: " + claims);
            String userId = (String) claims.get("sub");
            String email = (String) claims.get("email");

            if (userId == null || userId.isEmpty()) {
                return Helper.createErrorResponse(401, "Unauthorized: Missing or invalid token.");
            }

            if (DISHES_TABLE == null || DISHES_TABLE.trim().isEmpty()) {
                logger.severe("DISHES_TABLE environment variable not set");
                return Helper.createErrorResponse(500, "Server configuration error: DISHES_TABLE not set");
            }

            if (dishId == null || dishId.trim().isEmpty()) {
                return Helper.createErrorResponse(400, "Bad Request: Missing or invalid dish ID");
            }

            Table table = dynamoDB.getTable(DISHES_TABLE);
            Map<String, Object> expressionAttributeValues = new HashMap<>();
            expressionAttributeValues.put(":id", dishId);
            QuerySpec querySpec = new QuerySpec()
                    .withKeyConditionExpression("dishId = :id")
                    .withValueMap(expressionAttributeValues);

            ItemCollection<QueryOutcome> items = table.query(querySpec);
            DishIdDTO dish = StreamSupport.stream(items.spliterator(), false)
                    .findFirst()
                    .map(item -> mapItemToDishIdDTO(item))
                    .orElse(null);

            if (dish == null) {
                return Helper.createErrorResponse(404, "Not Found: Dish with ID " + dishId + " not found");
            }

            logger.info("Retrieved dish with ID: " + dishId);
            String responseBody = objectMapper.writeValueAsString(dish)+" }";
            return Helper.createApiResponse(200, "{\"message\":\"Successful operation\", \"description\":\"Successful dish retrieval\", \"content\":" + responseBody);

        } catch (Exception e) {
            logger.severe("Error fetching dish by ID: " + e.getMessage());
            return Helper.createErrorResponse(500, "Internal Server Error: " + e.getMessage());
        }
    }

    private DishDTO mapItemToDishDTO(Item item) {
        try {
            return new DishDTO(
                    item.getString("dishId"),
                    item.getString("dishName"),
                    item.getString("dishImageUrl"),
                    item.getString("dishPrice"),
                    item.getString("state"),
                    item.getString("weight"),
                    item.getString("dishType"), // Added to support filtering
                    item.hasAttribute("dishFrequency") ? item.getInt("dishFrequency") : 0
            );
        } catch (Exception e) {
            logger.warning("Error mapping dish " +
                    (item.hasAttribute("dishId") ? item.getString("dishId") : "unknown") +
                    ": " + e.getMessage());
            return null;
        }
    }

    private DishIdDTO mapItemToDishIdDTO(Item item) {
        try {
            return new DishIdDTO(
                    item.getString("calories"),
                    item.getString("carbohydrates"),
                    item.getString("dishDescription"),
                    item.getString("dishType"),
                    item.getString("fats"),
                    item.getString("dishId"),
                    item.getString("dishImageUrl"),
                    item.getString("dishName"),
                    item.getString("dishPrice"),
                    item.getString("proteins"),
                    item.getString("state"),
                    item.getString("vitamins"),
                    item.getString("weight"),
                    item.hasAttribute("dishFrequency") ? item.getInt("dishFrequency") : 0
            );
        } catch (Exception e) {
            logger.warning("Error mapping detailed dish " +
                    (item.hasAttribute("dishId") ? item.getString("dishId") : "unknown") +
                    ": " + e.getMessage());
            return null;
        }
    }

    private PopularDishDTO mapItemToPopularDishDTO(Item item) {
        try {
            return new PopularDishDTO(
                    item.getString("dishName"),
                    item.getString("dishPrice"),
                    item.getString("weight"),
                    item.getString("dishImageUrl"),
                    item.hasAttribute("dishFrequency") ? item.getInt("dishFrequency") : 0
            );
        } catch (Exception e) {
            logger.warning("Error mapping popular dish " +
                    (item.hasAttribute("dishId") ? item.getString("dishId") : "unknown") +
                    ": " + e.getMessage());
            return null;
        }
    }

    private List<DishDTO> filterAndSortDishes(List<DishDTO> dishes, Map<String, String> queryParams) {
        List<DishDTO> filteredDishes = new ArrayList<>(dishes);

        // Sort by price or popularity
        String sortBy = queryParams.getOrDefault("sortBy", DEFAULT_SORT_BY).toLowerCase();
        String[] sortParts = sortBy.split("-");
        if (sortParts.length != 2) {
            logger.warning("Invalid sortBy format: " + sortBy + ". Using default: " + DEFAULT_SORT_BY);
            sortBy = DEFAULT_SORT_BY;
            sortParts = sortBy.split("-");
        }

        String sortField = sortParts[0];
        String sortDirection = sortParts[1];

        if (!VALID_SORT_FIELDS.contains(sortField)) {
            logger.warning("Invalid sort field: " + sortField + ". Using default: popularity");
            sortField = "popularity";
        }
        if (!VALID_SORT_DIRECTIONS.contains(sortDirection)) {
            logger.warning("Invalid sort direction: " + sortDirection + ". Using default: asc");
            sortDirection = "asc";
        }

        if ("price".equals(sortField)) {
            String finalSortDirection = sortDirection;
            filteredDishes.sort(Comparator.comparing(DishDTO::getPrice, (p1, p2) -> {
                try {
                    return "asc".equals(finalSortDirection) ?
                            Double.compare(Double.parseDouble(p1.replace("$", "").trim()), Double.parseDouble(p2.replace("$", "").trim())) :
                            Double.compare(Double.parseDouble(p2.replace("$", "").trim()), Double.parseDouble(p1.replace("$", "").trim()));
                } catch (NumberFormatException e) {
                    logger.warning("Failed to parse price for sorting: p1=" + p1 + ", p2=" + p2);
                    return 0;
                }
            }));
        } else if ("popularity".equals(sortField)) {
            filteredDishes.sort(Comparator.comparingInt(DishDTO::getDishFrequency)
                    .reversed()); // Default to descending for popularity
            if ("asc".equals(sortDirection)) {
                filteredDishes.sort(Comparator.comparingInt(DishDTO::getDishFrequency)); // Ascending if specified
            }
        }

        return filteredDishes;
    }
}