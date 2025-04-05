package com.restaurant.services.reservations;

import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.restaurant.utils.Helper;
import java.util.logging.Logger;
import java.util.*;

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
                return Helper.createErrorResponse(400, "Invalid reservation cancellation request.");
            }
            String reservationId = pathParts[pathParts.length - 1];

            // Extract user email from JWT claims
            Map<String, Object> claims = Helper.extractClaims(request);
            if (claims == null || !claims.containsKey("email")) {
                return Helper.createErrorResponse(404, "Unauthorized access: User not logged in.");
            }

            String email = (String) claims.get("email");
            if (email == null || email.isEmpty()) {
                return Helper.createErrorResponse(404, "Unauthorized access: Email missing.");
            }

            // Fetch the specific reservation
            Item reservationItem = reservationTable.getItem("reservationId", reservationId);
            if (reservationItem == null) {
                return Helper.createErrorResponse(404, "Reservation not found.");
            }

            String reservationEmail = reservationItem.getString("email");
            String status = reservationItem.getString("status");

            // Check if the reservation belongs to the user
            if (!email.equals(reservationEmail)) {
                return Helper.createErrorResponse(403, "Forbidden: This reservation does not belong to you.");
            }

            // Check if the reservation is already cancelled or completed
            if (!"Reserved".equalsIgnoreCase(status)) {
                return Helper.createErrorResponse(204, "No active reservation to cancel.");
            }

            // Cancel the reservation
            cancelReservationStatus(reservationId, "Cancelled");
            return Helper.createApiResponse(200, Map.of("message", "Reservation Canceled"));
        } catch (Exception e) {
            logger.severe("Error canceling reservation: " + e.getMessage());
            return Helper.createErrorResponse(500, "Error canceling reservation: " + e.getMessage());
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
