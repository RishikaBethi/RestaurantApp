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

			if ("/auth/sign-up".equals(path) && "POST".equalsIgnoreCase(httpMethod)) {
				logger.info("Handling signup request");
				return signUpService.handleSignUp(request);
			}

			if ("/auth/sign-in".equals(path) && "POST".equalsIgnoreCase(httpMethod)) {
				logger.info("Handling sign-in request");
				return signInService.handleSignIn(request);
			}

			if ("/bookings/client".equals(path) && "POST".equalsIgnoreCase(httpMethod)) {
				return reservationService.handleCreateReservation(request);
			}

			if ("/bookings/client/".equals(path) && "PUT".equalsIgnoreCase(httpMethod)) {
				// Extract reservationId from the path
				String[] pathParts = path.split("/");
				if (pathParts.length == 4) {
					String reservationId = pathParts[3];
					return reservationService.handleUpdateReservation(request, reservationId);
				} else {
					return Helper.createErrorResponse(400, "Invalid reservationId in path");
				}
			}

			if ("/reservations".equals(path) && "GET".equalsIgnoreCase(httpMethod)) {
				return reservationService.handleGetReservations(request);
			}

			if (path.startsWith("/reservations/") && "DELETE".equalsIgnoreCase(httpMethod)) {
				return reservationService.handleCancelReservation(request, path);
			}

			return Helper.createErrorResponse(400, "Invalid Request");
		} catch (java.lang.Exception e) {
			logger.severe("Error handling request: " + e.getMessage());
			return Helper.createErrorResponse(500, "Error: " + e.getMessage());
		}
	}
}
