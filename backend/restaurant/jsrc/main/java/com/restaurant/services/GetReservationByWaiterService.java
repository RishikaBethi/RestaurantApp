package com.restaurant.services;

import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.inject.Inject;
import static com.restaurant.utils.Helper.*;

public class GetReservationByWaiterService {
    private static final Logger logger = Logger.getLogger(GetReservationByWaiterService.class.getName());
    private final Table reservationTable;
    private final Table waitersTable;
    private final Table ordersTable;
    private final Table usersTable;
    private final DynamoDB dynamoDB;
    private final GetAllLocationsService locationsService;
    private static long visitorCount = 1;

    @Inject
    public GetReservationByWaiterService(DynamoDB dynamoDB, GetAllLocationsService locationsService) {
        this.dynamoDB = dynamoDB;
        this.reservationTable = dynamoDB.getTable(System.getenv("RESERVATIONS_TABLE"));
        this.waitersTable = dynamoDB.getTable(System.getenv("WAITERS_TABLE"));
        this.ordersTable = dynamoDB.getTable(System.getenv("ORDERS_TABLE"));
        this.usersTable = dynamoDB.getTable(System.getenv("USERS_TABLE"));
        this.locationsService = locationsService;
    }

    public APIGatewayProxyResponseEvent handleGetReservationsByWaiter(APIGatewayProxyRequestEvent request) {
        try {
            Map<String, Object> claims = extractClaims(request);
            String email = (String) claims.get("email");

            logger.info("Extracted email: " + email);
            if (email == null || email.isEmpty()) {
                return createErrorResponse(401, "Unauthorized: Email not found in token.");
            }

            // Get waiterId from waiters table using email
            Index emailIndexWaiter = waitersTable.getIndex("email-index");
            if (emailIndexWaiter == null) {
                logger.severe("DynamoDB index 'email-index' does not exist in waiters table.");
                return createErrorResponse(500, "Server error: Waiter email index not found.");
            }

            ItemCollection<QueryOutcome> waiterItems = emailIndexWaiter.query(
                    new QuerySpec().withKeyConditionExpression("email = :email")
                            .withValueMap(new ValueMap().withString(":email", email))
            );

            String waiterId = null;
            for (Item item : waiterItems) {
                waiterId = item.getString("waiterId");
                break; // Assuming email is unique, take the first match
            }

            if (waiterId == null) {
                return createErrorResponse(404, "Waiter not found for email: " + email);
            }

            // Get optional filter parameters
            Map<String, String> queryParams = request.getQueryStringParameters() != null ? request.getQueryStringParameters() : new HashMap<>();
            String filterDate = queryParams.get("date");
            String filterTimeFrom = queryParams.get("timeFrom");
            String filterTableNumber = queryParams.get("tableNumber");

            // Build ScanSpec with filters using ValueMap
            ValueMap valueMap = new ValueMap().withString(":waiterId", waiterId);
            Map<String, String> nameMap = new HashMap<>();
            nameMap.put("#waiterId", "waiterId");
            ScanSpec scanSpec = new ScanSpec()
                    .withFilterExpression("#waiterId = :waiterId");

            List<String> filterExpressions = new ArrayList<>();
            filterExpressions.add("#waiterId = :waiterId");

            if (filterDate != null && !filterDate.isEmpty()) {
                filterExpressions.add("#date = :date");
                valueMap.withString(":date", filterDate);
                nameMap.put("#date", "date");
            }
            if (filterTimeFrom != null && !filterTimeFrom.isEmpty()) {
                filterExpressions.add("#timeFrom = :timeFrom");
                valueMap.withString(":timeFrom", filterTimeFrom);
                nameMap.put("#timeFrom", "timeFrom");
            }
            if (filterTableNumber != null && !filterTableNumber.isEmpty()) {
                filterExpressions.add("#tableNumber = :tableNumber");
                try {
                    int tableNum = Integer.parseInt(filterTableNumber); // Convert string to integer
                    valueMap.withNumber(":tableNumber", tableNum); // Use withNumber for numeric match
                    logger.info("Filtering tableNumber with numeric value: " + tableNum);
                } catch (NumberFormatException e) {
                    logger.warning("Invalid tableNumber format: " + filterTableNumber + ", treating as string");
                    valueMap.withString(":tableNumber", filterTableNumber); // Fallback to string if invalid
                }
                nameMap.put("#tableNumber", "tableNumber");
            }

            // Combine filter expressions if multiple filters are present
            if (filterExpressions.size() > 1) {
                scanSpec.withFilterExpression(String.join(" AND ", filterExpressions));
            }
            scanSpec.withValueMap(valueMap);
            scanSpec.withNameMap(nameMap);

            ItemCollection<ScanOutcome> reservationItems = reservationTable.scan(scanSpec);
            logger.info("Scan returned " + reservationItems.getAccumulatedItemCount() + " items");

            // Fetch all locations to map locationId to address
            APIGatewayProxyResponseEvent locationsResponse = locationsService.allAvailableLocations(request, null);
            logger.info("Locations response: " + locationsResponse.toString());
            if (locationsResponse.getStatusCode() != 200) {
                return createErrorResponse(500, "Error fetching locations: " + locationsResponse.getBody());
            }

            JSONArray locationsArray = new JSONArray(locationsResponse.getBody());
            Map<String, String> locationIdToAddress = new HashMap<>();
            for (int i = 0; i < locationsArray.length(); i++) {
                JSONObject locationObj = locationsArray.getJSONObject(i);
                String locationId = locationObj.getString("id");
                String address = locationObj.getString("address");
                locationIdToAddress.put(locationId, address != null ? address : "Unknown");
                logger.info("Mapped locationId: " + locationId + " to address: " + address);
            }

            JSONArray reservationsArray = new JSONArray();
            for (Item reservation : reservationItems) {
                Map<String, Object> res = reservation.asMap();
                String reservationEmail = reservation.getString("email");
                String orderId = reservation.getString("orderId");
                String locationId = reservation.get("locationId") != null ? reservation.getString("locationId") : "Unknown";
                logger.info("Reservation data: " + res + ", locationId: " + locationId);
                String address = locationIdToAddress.getOrDefault(locationId, "Unknown");

                // Get pre-order details
                int dishCount = 0;
                boolean isPreorder = false;
                if (orderId != null && !orderId.equals("N/A")) {
                    Item orderItem = ordersTable.getItem(new GetItemSpec().withPrimaryKey("orderId", orderId, "email", reservationEmail));
                    if (orderItem != null && orderItem.isPresent("dishItems")) {
                        List<String> dishes = orderItem.getList("dishItems");
                        dishCount = dishes != null ? dishes.size() : 0;
                        isPreorder = dishCount > 0;
                    }
                }

                // Determine name
                String name;
                if (email.equals(reservationEmail)) {
                    Item waiter = waitersTable.getItem(new GetItemSpec().withPrimaryKey("waiterId", waiterId));
                    String waiterName = waiter != null ? waiter.getString("waiterName") : "Unknown";
                    name = "waiter " + waiterName + " (Visitor " + visitorCount + ")";
                    visitorCount++;
                } else {
                    Item user = usersTable.getItem(new GetItemSpec().withPrimaryKey("email", reservationEmail));
                    String firstName = user != null ? user.getString("firstName") : "Unknown";
                    String lastName = user != null ? user.getString("lastName") : "Unknown";
                    name = "Customer " + firstName + " " + lastName;
                }

                // Build response object with address instead of locationId
                JSONObject reservationObj = new JSONObject()
                        .put("address", address)
                        .put("tableNumber", reservation.getString("tableNumber"))
                        .put("date", reservation.getString("date"))
                        .put("timeFrom", reservation.getString("timeFrom"))
                        .put("timeTo", reservation.getString("timeTo"))
                        .put("guestsNumber", reservation.getString("guestsNumber"))
                        .put("preOrderDishesCount", dishCount)
                        .put("preorder", isPreorder)
                        .put("name", name);

                reservationsArray.put(reservationObj);
            }

            return createApiResponse(200, reservationsArray);
        } catch (Exception e) {
            logger.severe("Error fetching waiter reservations: " + e.getMessage());
            return createErrorResponse(500, "Error fetching waiter reservations: " + e.getMessage());
        }
    }


}