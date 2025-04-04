package com.restaurant;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.syndicate.deployment.annotations.resources.DependsOn;

import com.restaurant.config.*;
import com.restaurant.utils.Helper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.DeploymentRuntime;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.environment.ValueTransformer;
import com.restaurant.services.SignUpService;
import com.restaurant.services.SignInService;
import com.restaurant.services.ReservationService;
import com.restaurant.services.WaiterService;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

@DependsOn(resourceType = ResourceType.COGNITO_USER_POOL, name = "${user_pool}")
@LambdaHandler(
		lambdaName = "restaurant_handler",
		roleName = "restaurant_handler-role",
		isPublishVersion = true,
		aliasName = "${lambdas_alias_name}",
		logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@EnvironmentVariables(value = {
		@EnvironmentVariable(key = "USERS_TABLE", value = "${user_table}"),
		@EnvironmentVariable(key = "WAITERS_TABLE", value = "${waiters_table}"),
		@EnvironmentVariable(key = "RESERVATIONS_TABLE", value = "${reservations_table}"),
		@EnvironmentVariable(key = "LOCATIONS_TABLE", value = "${locations_table}"),
		@EnvironmentVariable(key = "ORDERS_TABLE", value = "${orders_table}"),
		@EnvironmentVariable(key = "COGNITO_USER_POOL_ID", value = "${user_pool}", valueTransformer = ValueTransformer.USER_POOL_NAME_TO_USER_POOL_ID),
		@EnvironmentVariable(key = "REGION", value = "${region}"),
		@EnvironmentVariable(key = "COGNITO_CLIENT_ID", value = "${user_pool}", valueTransformer = ValueTransformer.USER_POOL_NAME_TO_CLIENT_ID)
})
public class RestaurantHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	private static final Logger logger = Logger.getLogger(RestaurantHandler.class.getName());
	private static final ObjectMapper objectMapper = new ObjectMapper(); // Added objectMapper

	@Inject SignUpService signUpService;
	@Inject SignInService signInService;
	@Inject ReservationService reservationService;
	@Inject WaiterService waiterService;

	public RestaurantHandler() {
		initDependencies();
	}

	private void initDependencies() {
		AppComponent appComponent = DaggerAppComponent.builder()
				.serviceModule(new ServiceModule())
				.build();
		appComponent.inject(this);
	}

	@Override
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
		try {
			String path = request.getPath();
			String httpMethod = request.getHttpMethod();

			if ("/auth/sign-up".equals(path) && "POST".equals(httpMethod)) {
				logger.info("Handling signup request");
				return signUpService.handleSignUp(request);
			}

			if ("/auth/sign-in".equals(path) && "POST".equals(httpMethod)) {
				logger.info("Handling sign-in request");
				return signInService.handleSignIn(request);
			}

			if ("/bookings/client".equals(path) && "POST".equalsIgnoreCase(httpMethod)) {
				return handleCreateReservation(request);
			}

			if ("/reservations".equals(path) && "GET".equalsIgnoreCase(httpMethod)) {
				return handleGetReservations(request);
			}

			if (path.startsWith("/reservations/") && "DELETE".equalsIgnoreCase(httpMethod)) {
				return handleCancelReservation(request, path);
			}

			return Helper.createErrorResponse(400, "Invalid Request");
		} catch (java.lang.Exception e) {
			logger.severe("Error handling request: " + e.getMessage());
			return Helper.createErrorResponse(500, "Error: " + e.getMessage());
		}
	}

	private APIGatewayProxyResponseEvent handleCreateReservation(APIGatewayProxyRequestEvent request) {
		try {
			logger.info("Handling reservation booking request");

			// Parse request body
			Map<String, String> requestBody = parseJson(request.getBody());
			if (requestBody == null || requestBody.isEmpty()) {
				return Helper.createErrorResponse(400, "Invalid request data: Empty request body.");
			}

			// Extract user ID from JWT claims
			Map<String, Object> claims = Helper.extractClaims(request);
			logger.info("Extracted claims: " + claims); // Debugging purpose

			String userId = (String) claims.get("sub");
			String email = (String) claims.get("email");
			if (userId == null || userId.isEmpty() || email == null || email.isEmpty()) {
				return Helper.createErrorResponse(401, "Unauthorized: Missing or invalid token.");
			}

			// Validate required fields in request body
			List<String> requiredFields = List.of("locationId", "tableNumber", "date", "guestsNumber", "timeFrom", "timeTo");
			for (String field : requiredFields) {
				if (!requestBody.containsKey(field) || requestBody.get(field).trim().isEmpty()) {
					return Helper.createErrorResponse(400, "Missing required field: " + field);
				}
			}

			// Convert guestsNumber to Integer
			int guestsNumber;
			try {
				guestsNumber = Integer.parseInt(requestBody.get("guestsNumber"));
				if (guestsNumber <= 0) {
					return Helper.createErrorResponse(400, "Invalid guestsNumber: Must be a positive integer.");
				}
			} catch (NumberFormatException e) {
				return Helper.createErrorResponse(400, "Invalid guestsNumber: Must be an integer.");
			}

			// Assign a waiter
			String waiterId = waiterService.assignWaiter(requestBody.get("locationId"));

			// Create reservation
			String reservationId = reservationService.createReservation(requestBody, email, waiterId);

			Optional<Map<String, Object>> reservationDetailsOpt = reservationService.getReservationById(reservationId);

			if (reservationDetailsOpt.isEmpty()) {
				return Helper.createErrorResponse(404, "Reservation not found.");
			}

			Map<String, Object> reservationDetails = reservationDetailsOpt.get();

			// Construct response
			Map<String, Object> response = Map.of(
					"id", reservationDetails.get("id"),
					"status", reservationDetails.get("status"),
					"locationAddress", reservationDetails.get("locationAddress"),
					"date", reservationDetails.get("date"),
					"timeSlot", reservationDetails.get("timeSlot"),
					"preOrder", reservationDetails.get("preOrder"),
					"guestsNumber", reservationDetails.get("guestsNumber"),
					"feedbackId", reservationDetails.get("feedbackId")
			);

			return Helper.createApiResponse(200, response);


		} catch (Exception e) {
			logger.severe("Error creating reservation: " + e.getMessage());
			return Helper.createErrorResponse(500, "Error creating reservation: " + e.getMessage());
		}
	}


	private APIGatewayProxyResponseEvent handleGetReservations(APIGatewayProxyRequestEvent request) {
		try {
			Map<String, Object> claims = Helper.extractClaims(request);
			String userId = (String) claims.get("sub");

			if (userId == null || userId.isEmpty()) {
				return Helper.createErrorResponse(401, "Unauthorized: Missing or invalid token.");
			}

			List<Map<String, Object>> reservations = reservationService.getReservationsByUser(userId);
			return Helper.createApiResponse(200, reservations);
		} catch (Exception e) {
			return Helper.createErrorResponse(500, "Error fetching reservations: " + e.getMessage());
		}
	}


	private APIGatewayProxyResponseEvent handleCancelReservation(APIGatewayProxyRequestEvent request, String path) {
		try {
			String[] pathParts = path.split("/");
			if (pathParts.length < 3) {
				return Helper.createErrorResponse(400, "Invalid reservation cancellation request.");
			}
			String reservationId = pathParts[pathParts.length - 1];
			Map<String, String> requestBody = parseJson(request.getBody());
			// Extract user ID from JWT claims
			Map<String, Object> claims = Helper.extractClaims(request);
			String userId = (String) claims.get("sub");

			if (userId == null || userId.isEmpty()) {
				return Helper.createErrorResponse(400, "Missing userId.");
			}

			List<Map<String, Object>> reservations = reservationService.getReservations();
			boolean exists = reservations.stream()
					.anyMatch(res -> res.get("reservationId").equals(reservationId));

			if (!exists) {
				return Helper.createErrorResponse(404, "Reservation not found.");
			}
			reservationService.modifyReservation(reservationId, "Cancelled");
			return Helper.createApiResponse(200, Map.of("message", "Reservation Canceled"));
		} catch (Exception e) {
			logger.severe("Error canceling reservation: " + e.getMessage());
			return Helper.createErrorResponse(500, "Error canceling reservation: " + e.getMessage());
		}
	}

	private Map<String, String> parseJson(String json) {
		try {
			logger.info("Parsing JSON: " + json);  // Log incoming JSON
			return objectMapper.readValue(json, Map.class);
		} catch (Exception e) {
			logger.severe("Error parsing JSON: " + e.getMessage());
			return Map.of();
		}
	}
}
