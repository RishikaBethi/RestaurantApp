package com.restaurant;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.restaurant.config.AppComponent;
import com.restaurant.config.ServiceModule;
import com.restaurant.config.DaggerAppComponent;
import com.restaurant.services.*;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.resources.DependsOn;

import com.restaurant.config.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.environment.ValueTransformer;

import javax.inject.Inject;
import java.util.HashMap;
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
		@EnvironmentVariable(key = "WAITERS_TABLE", value = "${waiter_table}"),
		@EnvironmentVariable(key = "LOCATIONS_TABLE", value = "tm7-Locations"),
		@EnvironmentVariable(key = "DISHES_TABLE", value = "tm7-Dishes"),
		@EnvironmentVariable(key = "FEEDBACKS_TABLE", value = "tm7-Feedback"),
        @EnvironmentVariable(key = "RESERVATIONS_TABLE", value = "${reservations_table}"),
        @EnvironmentVariable(key = "TABLES_TABLE", value = "${tables_table}"),
        @EnvironmentVariable(key = "COGNITO_USER_POOL_ID", value = "${user_pool}", valueTransformer = ValueTransformer.USER_POOL_NAME_TO_USER_POOL_ID),
		@EnvironmentVariable(key = "COGNITO_CLIENT_ID", value = "${user_pool}", valueTransformer = ValueTransformer.USER_POOL_NAME_TO_CLIENT_ID),
		@EnvironmentVariable(key = "REGION", value = "${region}"),
        @EnvironmentVariable(key = "ORDERS_TABLE", value = "${orders_table}"),
})
public class RestaurantHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final ObjectMapper objectMapper = new ObjectMapper(); // Added objectMapper
	private static final Logger logger = Logger.getLogger(RestaurantHandler.class.getName());

	@Inject
    SignUpService signUpService;

	@Inject
    SignInService signInService;

	@Inject
    LocationService locationService;

	@Inject
    DishService dishService;

	@Inject
    FeedbackService feedbackService;

    @Inject
    TablesService tablesService;

    @Inject
    LocationsService locationsService;

    @Inject
    GetReservationService getReservationService;

    @Inject
    CancelReservationService cancelReservationService;

    @Inject
    UpdateReservationService updateReservationService;

    @Inject
    BookingService bookingService;

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
            Map<String, String> queryParams = request.getQueryStringParameters();

            logger.info("Received request - Path: " + path + ", Method: " + httpMethod +
                    ", Query: " + (queryParams != null ? queryParams.toString() : "none"));

            // Auth routes
            if ("/auth/sign-up".equals(path) && "POST".equals(httpMethod)) {
                logger.info("Handling signup request");
                return signUpService.handleSignUp(request);
            } else if ("/auth/sign-in".equals(path) && "POST".equals(httpMethod)) {
                logger.info("Handling sign-in request");
                return signInService.handleSignIn(request);
            }

            // Location routes
            else if ("/locations".equals(path) && "GET".equals(httpMethod)) {
                if (queryParams != null && queryParams.containsKey("locationId") && queryParams.containsKey("speciality-dishes")) {
                    logger.info("Routing to getSpecialityDishes with query parameters: " + queryParams);
                    return locationService.getSpecialityDishes(request);
                }
                logger.info("Handling locations request");
                return locationService.getLocations(request);
            } else if (path.startsWith("/locations/") && path.endsWith("/speciality-dishes") && "GET".equals(httpMethod)) {
                String[] pathParts = path.split("/");
                if (pathParts.length >= 3) {
                    String locationId = pathParts[pathParts.length - 2];
                    if (queryParams == null) {
                        queryParams = new HashMap<>();
                    }
                    queryParams.put("locationId", locationId);
                    request.setQueryStringParameters(queryParams);
                    logger.info("Extracted locationId: " + locationId);
                    return locationService.getSpecialityDishes(request);
                }
                return createErrorResponse(400, "Invalid speciality-dishes path format");
            }

            // Dishes route
            else if ("/dishes/popular".equals(path) && "GET".equals(httpMethod)) {
                logger.info("Handling popular dishes request");
                return dishService.getPopularDishes(request);
            }

            // Feedback route
            else if (path.matches("/locations/[^/]+/feedbacks") && "GET".equals(httpMethod)) {
                logger.info("Handling feedback retrieval request");
                return feedbackService.handleGetFeedbacks(request);
            }

            if (tablesService == null) {
                throw new IllegalStateException("Services not injected: tablesService=" + tablesService);
            }
            if (locationsService == null) {
                throw new IllegalStateException("Services not injected: locationsService=" + locationsService);
            }

            else if (path.equals("/bookings/tables") && httpMethod.equalsIgnoreCase("GET")) {
                context.getLogger().log("In bookings handler");
                return tablesService.returnAvailableTablesFilteredByGivenCriteria(request, context);
            }

            else if (path.equals("/locations/select-options") && httpMethod.equalsIgnoreCase("GET")) {
                context.getLogger().log("In locations handler");
                return locationsService.allAvailableLocations(
                        request, context);
            }

            if ("/bookings/client".equals(path) && "POST".equalsIgnoreCase(httpMethod)) {
                return bookingService.handleCreateReservation(request);
            }

            if ("/bookings/client/".equals(path) && "PUT".equalsIgnoreCase(httpMethod)) {
                return updateReservationService.handleUpdateReservation(request, path);
            }

            if ("/reservations".equals(path) && "GET".equalsIgnoreCase(httpMethod)) {
                return getReservationService.handleGetReservations(request);
            }

            if (path.startsWith("/reservations/") && "DELETE".equalsIgnoreCase(httpMethod)) {
                return cancelReservationService.handleCancelReservation(request, path);
            }

			return createErrorResponse(405, "Method Not Allowed: " + path + " with method " + httpMethod);
        }
        catch (Exception e) {
            logger.severe("Error handling request: " + e.getMessage());
            return createErrorResponse(500, "Error: " + e.getMessage());
        }
    }
    private APIGatewayProxyResponseEvent createErrorResponse(int statusCode, String message) {
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withBody("{\"message\":\"" + message + "\"}")
                .withHeaders(Map.of("Content-Type", "application/json"));
    }

//    private APIGatewayProxyResponseEvent createErrorResponseM(String message) {
//        String safeMessage = message != null ? message : "Unknown error occurred";
//        return new APIGatewayProxyResponseEvent()
//                .withStatusCode(400)
//                .withBody("{\"error\": \"" + safeMessage + "\"}")
//                .withHeaders(Map.of("Content-Type", "application/json"));
//    }

    private APIGatewayProxyResponseEvent methodNotAllowed() {
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(405)
                .withBody("{\"error\": \"Method Not Allowed\"}")
                .withHeaders(Map.of("Content-Type", "application/json"));
    }
}
