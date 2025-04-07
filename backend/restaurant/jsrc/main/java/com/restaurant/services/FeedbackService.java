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

public class FeedbackService {

    private static final Logger logger = Logger.getLogger(FeedbackService.class.getName());

    private final DynamoDB dynamoDB;
    private final ObjectMapper objectMapper;
    private static final String FEEDBACK_TABLE = "tm7-Feedback";
    private static final String LOCATION_ID_INDEX = "locationId-index";
    private static final String LOCATIONS_TABLE = "tm7-Locations"; // Correct table name

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
            return false; // Treat any error as invalid for safety
        }
    }

    public APIGatewayProxyResponseEvent handleGetFeedbacks(APIGatewayProxyRequestEvent request) {
        try {
            // Extract path parameters
            String path = request.getPath();
            String[] pathParts = path.split("/");
            if (pathParts.length < 3) {
                return createResponse(400, Map.of("message", "Invalid path format"));
            }
            String locationId = pathParts[2]; // Extract {id} from /locations/{id}/feedbacks

            // Validate location ID
            if (!isValidLocationId(locationId)) {
                return createResponse(400, Map.of("message", "Invalid location ID"));
            }

            // Extract query parameters
            Map<String, String> queryParams = request.getQueryStringParameters() != null
                    ? request.getQueryStringParameters()
                    : new HashMap<>();

            // Validate the 'type' parameter (required)
            String type = queryParams.get("type");
            if (type == null || type.isEmpty()) {
                return createResponse(400, Map.of("message", "Query parameter 'type' is required"));
            }

            // Normalize type to uppercase and validate allowed values
            type = type.toUpperCase();
            if (!type.equals("SERVICE") && !type.equals("CUISINE_EXPERIENCE")) {
                return createResponse(400, Map.of("message", "Query parameter 'type' must be either 'SERVICE' or 'CUISINE_EXPERIENCE'"));
            }

            // Pagination parameters
            int page;
            int size;
            try {
                page = Integer.parseInt(queryParams.getOrDefault("page", "0"));
                size = Integer.parseInt(queryParams.getOrDefault("size", "20"));
                if (page < 0 || size <= 0) {
                    return createResponse(400, Map.of("message", "Query parameters 'page' must be >= 0 and 'size' must be > 0"));
                }
            } catch (NumberFormatException e) {
                return createResponse(400, Map.of("message", "Query parameters 'page' and 'size' must be valid integers"));
            }

            // Sorting parameters
            String sort = queryParams.getOrDefault("sort", "date,asc");
            String[] sortParams = sort.split(",");
            if (sortParams.length != 2) {
                return createResponse(400, Map.of("message", "Query parameter 'sort' must be in the format 'property,direction' (e.g., 'date,asc')"));
            }

            String sortProperty = sortParams[0].toLowerCase();
            String sortDirection = sortParams[1].toLowerCase();

            // Validate sort property
            if (!sortProperty.equals("date") && !sortProperty.equals("rating")) {
                return createResponse(400, Map.of("message", "Query parameter 'sort' property must be either 'date' or 'rating'"));
            }

            // Validate sort direction
            if (!sortDirection.equals("asc") && !sortDirection.equals("desc")) {
                return createResponse(400, Map.of("message", "Query parameter 'sort' direction must be either 'asc' or 'desc'"));
            }

            boolean isAscending = sortDirection.equals("asc");

            // Query DynamoDB using the GSI
            Table table = dynamoDB.getTable(FEEDBACK_TABLE);
            Index index = table.getIndex(LOCATION_ID_INDEX);

            // Build the QuerySpec
            QuerySpec querySpec = new QuerySpec();
            querySpec.withKeyConditionExpression("locationId = :v_locationId");

            // Create a single ValueMap for all expression attributes
            ValueMap valueMap = new ValueMap().withString(":v_locationId", locationId);

            // Use an expression attribute name for 'type' since it's a reserved keyword
            querySpec.withFilterExpression("#feedbackType = :v_type");
            querySpec.withNameMap(new HashMap<String, String>() {{
                put("#feedbackType", "type");
            }});
            valueMap.withString(":v_type", type);

            // Apply the ValueMap to the QuerySpec
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
                feedbackList = targetPage.getLowLevelResult().getItems().stream()
                        .map(this::itemToFeedbackDTO)
                        .collect(Collectors.toList());
            }

            // Sort in memory based on sortProperty
            if (sortProperty.equals("date")) {
                feedbackList.sort((f1, f2) -> {
                    // Handle null dates
                    String date1 = f1.getDate() != null ? f1.getDate() : "";
                    String date2 = f2.getDate() != null ? f2.getDate() : "";
                    return isAscending ? date1.compareTo(date2) : date2.compareTo(date1);
                });
            } else if (sortProperty.equals("rating")) {
                feedbackList.sort((f1, f2) -> {
                    // Handle null ratings
                    String rate1 = f1.getRate() != null ? f1.getRate() : "0";
                    String rate2 = f2.getRate() != null ? f2.getRate() : "0";
                    // Convert ratings to float for proper numerical comparison
                    float r1 = Float.parseFloat(rate1);
                    float r2 = Float.parseFloat(rate2);
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
            responseBody.put("content", feedbackList); // Empty array if no items
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

            return createResponse(200, responseBody);

        } catch (Exception e) {
            logger.severe("Error retrieving feedbacks: " + e.getMessage());
            return createResponse(500, Map.of("message", "Internal Server Error"));
        }
    }

    private FeedbackDTO itemToFeedbackDTO(Item item) {
        FeedbackDTO dto = new FeedbackDTO();
        dto.setId(item.getString("feedbackId"));
        dto.setRate(item.getString("rating"));
        dto.setComment(item.getString("comment"));
        dto.setUserName(item.getString("userName"));
        dto.setUserAvatarUrl(item.getString("userAvatarUrl"));
        dto.setDate(item.getString("date"));
        dto.setType(item.getString("type"));
        dto.setLocationId(item.getString("locationId"));
        return dto;
    }
    private APIGatewayProxyResponseEvent createResponse(int statusCode, String message) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setHeaders(createCorsHeaders());
        return response.withStatusCode(statusCode).withBody("{\"message\":\"" + message + "\"}");
    }

    private Map<String, Object> createEmptyResponse() {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("totalPages", 0);
        responseBody.put("totalElements", 0);
        responseBody.put("size", 0);
        responseBody.put("content", Collections.emptyList());
        responseBody.put("number", 0);
        responseBody.put("sort", Collections.emptyList());
        responseBody.put("first", true);
        responseBody.put("last", true);
        responseBody.put("numberOfElements", 0);
        responseBody.put("pageable", Map.of(
                "offset", 0,
                "sort", Collections.emptyList(),
                "paged", true,
                "pageSize", 0,
                "pageNumber", 0,
                "unpaged", true
        ));
        responseBody.put("empty", true);
        return responseBody;
    }
}