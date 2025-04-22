package com.restaurant.services;

import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import static com.restaurant.utils.Helper.*;
import java.util.logging.Logger;
import java.util.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

public class CancelReservationService {
    private static final Logger logger = Logger.getLogger(CancelReservationService.class.getName());
    private final Table reservationTable;
    private final DynamoDB dynamoDB;

    public CancelReservationService(DynamoDB dynamoDB) {
        this.dynamoDB = dynamoDB;
        this.reservationTable = dynamoDB.getTable(System.getenv("RESERVATIONS_TABLE"));
    }

    public APIGatewayProxyResponseEvent handleCancelReservation(APIGatewayProxyRequestEvent request, String path) {
        try {
            String[] pathParts = path.split("/");
            if (pathParts.length < 3) {
                return createErrorResponse(400, "Invalid reservation cancellation request.");
            }
            String reservationId = pathParts[pathParts.length - 1];

            // Extract user email from JWT claims
            Map<String, Object> claims = extractClaims(request);
            if (claims == null || !claims.containsKey("email")) {
                return createErrorResponse(404, "Unauthorized access: User not logged in.");
            }

            String email = (String) claims.get("email");
            if (email == null || email.isEmpty()) {
                return createErrorResponse(404, "Unauthorized access: Email missing.");
            }

            // Fetch the specific reservation
            Item reservationItem = reservationTable.getItem("reservationId", reservationId);
            if (reservationItem == null) {
                return createErrorResponse(404, "Reservation not found.");
            }

            String reservationEmail = reservationItem.getString("email");
            String waiterId = reservationItem.getString("waiterId");
            String status = reservationItem.getString("status");

            boolean isCustomer = email.equalsIgnoreCase(reservationEmail);
            boolean isAssignedWaiter = false;

            if (waiterId != null && !waiterId.isEmpty()) {
                Table waitersTable = dynamoDB.getTable(System.getenv("WAITERS_TABLE"));
                Item waiterItem = waitersTable.getItem("waiterId", waiterId);

                if (waiterItem != null) {
                    String assignedWaiterEmail = waiterItem.getString("email");
                    isAssignedWaiter = email.equalsIgnoreCase(assignedWaiterEmail);
                }
            }

            if (!isCustomer && !isAssignedWaiter) {
                return createErrorResponse(403, "Forbidden: You are not authorized to cancel this reservation.");
            }
            // Check if the reservation is already cancelled or completed
            if (!"Reserved".equalsIgnoreCase(status)) {
                return createErrorResponse(204, "No active reservation to cancel.");
            }

            // Check if current IST time is at least 30 minutes before the reservation time
            String dateStr = reservationItem.getString("date"); // e.g., "2025-04-02"
            String startTimeStr = reservationItem.getString("timeFrom"); // Get "12:15"

            // Combine date and time into LocalDateTime
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime reservationDateTime = LocalDateTime.parse(dateStr + " " + startTimeStr, formatter);

            // Convert to IST ZonedDateTime
            ZonedDateTime reservationIST = reservationDateTime.atZone(ZoneId.of("Asia/Kolkata"));
            ZonedDateTime nowIST = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));

            Duration duration = Duration.between(nowIST, reservationIST);
            if (!duration.isNegative() && duration.toMinutes() <= 30) {
                return createErrorResponse(409, "Reservation cannot be canceled within 30 minutes of the reservation time.");
            }
            String orderId = reservationItem.getString("orderId");

            // If canceled by waiter — delete the reservation
            if (isAssignedWaiter) {
                reservationTable.deleteItem("reservationId", reservationId);
                logger.info("Reservation " + reservationId + " deleted by waiter: " + email);

                if (orderId != null && reservationEmail != null) {
                    cancelOrderState(orderId, reservationEmail);
                }

                return createApiResponse(200, Map.of("message", "Reservation deleted successfully by waiter."));
            }
            // Cancel the reservation
            cancelReservationStatus(reservationId, "Cancelled");

            if (orderId != null && reservationEmail != null) {
                cancelOrderState(orderId, reservationEmail);
            }

            return createApiResponse(200, Map.of("message", "Reservation and associated order Canceled successfully"));
        } catch (Exception e) {
            logger.severe("Error canceling reservation: " + e.getMessage());
            return createErrorResponse(500, "Error canceling reservation: " + e.getMessage());
        }
    }

    // Modify reservation status
    public String cancelReservationStatus(String reservationId, String status) {
        try {
            UpdateItemSpec updateItemSpec = new UpdateItemSpec()
                    .withPrimaryKey("reservationId", reservationId)
                    .withUpdateExpression("set #s = :status")
                    .withNameMap(Collections.singletonMap("#s", "status"))
                    .withValueMap(new ValueMap().withString(":status", status));

            reservationTable.updateItem(updateItemSpec);
            return "Reservation updated to: " + status;
        } catch (Exception e) {
            System.err.println("Error updating reservation: " + e.getMessage());
            return "Failed to update reservation.";
        }
    }

    private void cancelOrderState(String orderId, String email) {
        try {
            Table ordersTable = dynamoDB.getTable(System.getenv("ORDERS_TABLE"));

            UpdateItemSpec updateItemSpec = new UpdateItemSpec()
                    .withPrimaryKey("orderId", orderId, "email", email)
                    .withUpdateExpression("set #s = :cancelled")
                    .withNameMap(Collections.singletonMap("#s", "status"))
                    .withValueMap(new ValueMap().withString(":cancelled", "Cancelled"));

            ordersTable.updateItem(updateItemSpec);
            logger.info("Order " + orderId + " state updated to Cancelled.");
        } catch (Exception e) {
            logger.warning("Failed to update order state: " + e.getMessage());
        }
    }


    // Get all reservations (admin functionality)
    public List<Map<String, Object>> getReservations() {
        ItemCollection<ScanOutcome> items = reservationTable.scan();

        List<Map<String, Object>> reservations = new ArrayList<>();
        for (Item item : items) {
            reservations.add(item.asMap());
        }
        return reservations;
    }
}
