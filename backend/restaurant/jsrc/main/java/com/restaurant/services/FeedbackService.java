package com.restaurant.services;

import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.dto.FeedbackDTO;

import javax.inject.Inject;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.restaurant.utils.Helper.*;

public class FeedbackService {

    private static final Logger logger = Logger.getLogger(FeedbackService.class.getName());

    private final DynamoDB dynamoDB;
    private final ObjectMapper objectMapper;
    private static final String FEEDBACK_TABLE = "tm7-Feedback";
    private static final String LOCATION_ID_INDEX = "locationId-index";
    private static final String LOCATIONS_TABLE = "tm7-Locations";

    @Inject
    public FeedbackService(DynamoDB dynamoDB, ObjectMapper objectMapper) {
        this.dynamoDB = dynamoDB;
        this.objectMapper = objectMapper;
    }

    private boolean isValidLocationId(String locationId) {
        try {
            Table locationsTable = dynamoDB.getTable(LOCATIONS_TABLE);
            GetItemSpec spec = new GetItemSpec()
                    .withPrimaryKey("locationId", locationId);
            Item item = locationsTable.getItem(spec);
            return item != null;
        } catch (Exception e) {
            logger.severe("Error validating location ID: " + e.getMessage());
            return false;
        }
    }

    public APIGatewayProxyResponseEvent handleGetFeedbacks(APIGatewayProxyRequestEvent request) {
        try {
            // Extract path parameters
            String path = request.getPath();
            String[] pathParts = path.split("/");
            if (pathParts.length < 3) {
                return createErrorResponse(400, "Invalid path format");
            }
            String locationId = pathParts[2];

            // Validate location ID
            if (!isValidLocationId(locationId)) {
                return createErrorResponse(400, "Invalid location ID");
            }

            // Extract query parameters
            Map<String, String> queryParams = request.getQueryStringParameters() != null
                    ? request.getQueryStringParameters()
                    : new HashMap<>();

            // Validate the 'type' parameter (required)
            String type = queryParams.get("type");
            if (type == null || type.isEmpty()) {
                return createErrorResponse(400, "Query parameter 'type' is required");
            }

            // Normalize type to uppercase and validate allowed values
            type = type.toUpperCase();
            if (!type.equals("SERVICE") && !type.equals("CUISINE_EXPERIENCE")) {
                return createErrorResponse(400, "Query parameter 'type' must be either 'SERVICE' or 'CUISINE_EXPERIENCE'");
            }

            // Log the type for debugging
            logger.info("Processing feedback type: " + type);

            // Pagination parameters
            int page;
            int size;
            try {
                page = Integer.parseInt(queryParams.getOrDefault("page", "0"));
                size = Integer.parseInt(queryParams.getOrDefault("size", "4"));
                if (page < 0 || size <= 0) {
                    return createErrorResponse(400, "Query parameters 'page' must be >= 0 and 'size' must be > 0");
                }
            } catch (NumberFormatException e) {
                return createErrorResponse(400, "Query parameters 'page' and 'size' must be valid integers");
            }

            // Sorting parameters
            String sort = queryParams.getOrDefault("sort", "date,asc");
            String[] sortParams = sort.split(",");
            if (sortParams.length != 2) {
                return createErrorResponse(400, "Query parameter 'sort' must be in the format 'property,direction'");
            }

            String sortProperty = sortParams[0].toLowerCase();
            String sortDirection = sortParams[1].toLowerCase();

            // Validate sort property
            if (!sortProperty.equals("date") && !sortProperty.equals("rating")) {
                return createErrorResponse(400, "Query parameter 'sort' property must be either 'date' or 'rating'");
            }

            // Validate sort direction
            if (!sortDirection.equals("asc") && !sortDirection.equals("desc")) {
                return createErrorResponse(400, "Query parameter 'sort' direction must be either 'asc' or 'desc'");
            }

            boolean isAscending = sortDirection.equals("asc");

            // Query DynamoDB using the GSI
            Table table = dynamoDB.getTable(FEEDBACK_TABLE);
            Index index = table.getIndex(LOCATION_ID_INDEX);

            // Build the QuerySpec
            QuerySpec querySpec = new QuerySpec();
            querySpec.withKeyConditionExpression("locationId = :v_locationId");

            // Create a ValueMap for expression attributes
            ValueMap valueMap = new ValueMap().withString(":v_locationId", locationId);

            // Filter by non-empty comment based on type
            String filterExpression;
            if (type.equals("CUISINE_EXPERIENCE")) {
                filterExpression = "cuisineComment <> :empty AND attribute_exists(cuisineComment)";
            } else {
                filterExpression = "serviceComment <> :empty AND attribute_exists(serviceComment)";
            }
            valueMap.withString(":empty", "");
            querySpec.withFilterExpression(filterExpression);
            querySpec.withValueMap(valueMap);

            // Apply pagination
            querySpec.withMaxPageSize(size);
            ItemCollection<QueryOutcome> items = index.query(querySpec);

            // Paginate to the requested page
            Iterator<Page<Item, QueryOutcome>> pages = items.pages().iterator();
            Page<Item, QueryOutcome> targetPage = null;
            int currentPage = 0;
            while (pages.hasNext() && currentPage <= page) {
                targetPage = pages.next();
                currentPage++;
            }

            // Convert items to FeedbackDTO
            List<FeedbackDTO> feedbackList = new ArrayList<>();
            if (targetPage != null) {
                String finalType1 = type;
                feedbackList = targetPage.getLowLevelResult().getItems().stream()
                        .map(item -> itemToFeedbackDTO(item, finalType1))
                        .collect(Collectors.toList());
            }

            // Sort in memory based on sortProperty
            if (sortProperty.equals("date")) {
                feedbackList.sort((f1, f2) -> {
                    String date1 = f1.getDate() != null ? f1.getDate() : "";
                    String date2 = f2.getDate() != null ? f2.getDate() : "";
                    return isAscending ? date1.compareTo(date2) : date2.compareTo(date1);
                });
            } else if (sortProperty.equals("rating")) {
                String finalType2 = type;
                feedbackList.sort((f1, f2) -> {
                    String rating1 = finalType2.equals("CUISINE_EXPERIENCE") ? f1.getCuisineRating() : f1.getServiceRating();
                    String rating2 = finalType2.equals("CUISINE_EXPERIENCE") ? f2.getCuisineRating() : f2.getServiceRating();
                    float r1 = parseRating(rating1, f1.getId());
                    float r2 = parseRating(rating2, f2.getId());
                    return isAscending ? Float.compare(r1, r2) : Float.compare(r2, r1);
                });
            }

            // Calculate pagination metadata
            long totalElements = items.getAccumulatedItemCount();
            int totalPages = (int) Math.ceil((double) totalElements / size);

            // Build the response
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("totalPages", totalPages);
            responseBody.put("totalElements", totalElements);
            responseBody.put("size", size);
            String finalType = type;
            responseBody.put("content", feedbackList.stream().map(dto -> {
                Map<String, String> feedbackMap = new HashMap<>();
                feedbackMap.put("id", dto.getId());
                feedbackMap.put("rate", finalType.equals("CUISINE_EXPERIENCE") ? dto.getCuisineRating() : dto.getServiceRating());
                feedbackMap.put("comment", finalType.equals("CUISINE_EXPERIENCE") ? dto.getCuisineComment() : dto.getServiceComment());
                feedbackMap.put("userName", dto.getUserName());
                feedbackMap.put("userAvatarUrl", dto.getUserAvatarUrl());
                feedbackMap.put("date", dto.getDate());
                feedbackMap.put("type", dto.getType());
                feedbackMap.put("locationId", dto.getLocationId());
                return feedbackMap;
            }).collect(Collectors.toList()));
            responseBody.put("number", page);
            responseBody.put("sort", Collections.singletonList(Map.of(
                    "direction", isAscending ? "ASC" : "DESC",
                    "nullHandling", "NATIVE",
                    "ascending", isAscending,
                    "property", sortProperty,
                    "ignoreCase", false
            )));
            responseBody.put("first", page == 0);
            responseBody.put("last", page == totalPages - 1 || totalPages == 0);
            responseBody.put("numberOfElements", feedbackList.size());
            responseBody.put("pageable", Map.of(
                    "offset", page * size,
                    "sort", Collections.singletonList(Map.of(
                            "direction", isAscending ? "ASC" : "DESC",
                            "nullHandling", "NATIVE",
                            "ascending", isAscending,
                            "property", sortProperty,
                            "ignoreCase", false
                    )),
                    "paged", true,
                    "pageSize", size,
                    "pageNumber", page,
                    "unpaged", false
            ));
            responseBody.put("empty", feedbackList.isEmpty());

            return createApiResponse(200, responseBody);

        } catch (Exception e) {
            logger.severe("Error retrieving feedbacks: " + e.getMessage());
            return createErrorResponse(500, "Internal Server Error");
        }
    }

    private float parseRating(String rating, String feedbackId) {
        if (rating == null || rating.trim().isEmpty()) {
            return 0.0f;
        }
        try {
            return Float.parseFloat(rating);
        } catch (NumberFormatException e) {
            logger.warning("Invalid rating format for feedback ID " + feedbackId + ": " + rating);
            return 0.0f;
        }
    }

    private FeedbackDTO itemToFeedbackDTO(Item item, String type) {
        FeedbackDTO dto = new FeedbackDTO();
        dto.setId(item.getString("feedbackId"));
        dto.setUserName(item.getString("userName"));
        dto.setUserAvatarUrl(item.getString("userAvatarUrl"));
        dto.setDate(item.getString("date"));
        dto.setType(type);
        dto.setLocationId(item.getString("locationId"));
        if (type.equals("CUISINE_EXPERIENCE")) {
            dto.setCuisineComment(item.getString("cuisineComment"));
            dto.setCuisineRating(item.getString("cuisineRating"));
        } else {
            dto.setServiceComment(item.getString("serviceComment"));
            dto.setServiceRating(item.getString("serviceRating"));
        }
        return dto;
    }
}