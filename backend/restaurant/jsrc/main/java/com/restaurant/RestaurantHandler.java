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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.environment.ValueTransformer;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static com.restaurant.utils.Helper.*;

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
		@EnvironmentVariable(key = "LOCATIONS_TABLE", value = "${locations_table}"),
		@EnvironmentVariable(key = "DISHES_TABLE", value = "${dishes_table}"),
		@EnvironmentVariable(key = "FEEDBACKS_TABLE", value = "${feedbacks_table}"),
        @EnvironmentVariable(key = "RESERVATIONS_TABLE", value = "${reservations_table}"),
        @EnvironmentVariable(key = "TABLES_TABLE", value = "${tables_table}"),
        @EnvironmentVariable(key = "REPORTS_TABLE", value = "${reports_table}"),
        @EnvironmentVariable(key = "COGNITO_USER_POOL_ID", value = "${user_pool}", valueTransformer = ValueTransformer.USER_POOL_NAME_TO_USER_POOL_ID),
		@EnvironmentVariable(key = "COGNITO_CLIENT_ID", value = "${user_pool}", valueTransformer = ValueTransformer.USER_POOL_NAME_TO_CLIENT_ID),
		@EnvironmentVariable(key = "REGION", value = "${region}"),
        @EnvironmentVariable(key = "ORDERS_TABLE", value = "${orders_table}"),
})
public class RestaurantHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final ObjectMapper objectMapper = new ObjectMapper();
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
    GetAllLocationsService locationsService;

    @Inject
    GetReservationService getReservationService;

    @Inject
    CancelReservationService cancelReservationService;

    @Inject
    UpdateReservationService updateReservationService;

    @Inject
    BookingService bookingService;

    @Inject
    GetReportsService getReportsService;

    @Inject
    PostAFeedbackService postAFeedback;

    @Inject
    GetLatestFeedback latestFeedback;

    @Inject
    BookingsByWaiterService bookingsByWaiterService;

    @Inject
    UpdateReservationByWaiterService updateReservationByWaiterService;

    @Inject
    GetReservationByWaiterService getReservationByWaiterService;

    @Inject
    ProfileService profileService;

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
            if (path.equals("/auth/sign-up") && httpMethod.equalsIgnoreCase("POST")) {
                logger.info("Handling signup request");
                return signUpService.handleSignUp(request);
            }
            if (path.equals("/auth/sign-in") && httpMethod.equalsIgnoreCase("POST")) {
                logger.info("Handling sign-in request");
                return signInService.handleSignIn(request);
            }

            // Location routes
            if (path.equals("/locations") && httpMethod.equalsIgnoreCase("GET")) {
                if (queryParams != null && queryParams.containsKey("locationId") && queryParams.containsKey("speciality-dishes")) {
                    logger.info("Routing to getSpecialityDishes with query parameters: " + queryParams);
                    return locationService.getSpecialityDishes(request);
                }
                logger.info("Handling locations request");
                return locationService.getLocations(request);
            }

            if (path.startsWith("/locations/") && path.endsWith("/speciality-dishes") && httpMethod.equalsIgnoreCase("GET")) {
                return locationService.getSpecialityDishes(request);
            }

            if (path.equals("/locations/select-options") && httpMethod.equalsIgnoreCase("GET")) {
                logger.info("In locations handler");
                return locationsService.allAvailableLocations(
                        request, context);
            }

            // Dishes route
            if (path.equals("/dishes/popular") && httpMethod.equalsIgnoreCase("GET")) {
                logger.info("Handling popular dishes request");
                return dishService.getPopularDishes(request);
            }

            // Feedback route
            else if (path.matches("/locations/[^/]+/feedbacks") && "GET".equals(httpMethod)) {
                logger.info("Handling feedback retrieval request");
                return feedbackService.handleGetFeedbacks(request);
            }

            if (path.equals("/dishes") && httpMethod.equalsIgnoreCase("GET")) {
                logger.info("Handling all dishes request for path: " + path + ", Query: " + (queryParams != null ? queryParams.toString() : "none"));
                return dishService.getAllDishes(request);
            }


            if (path.startsWith("/dishes/") && httpMethod.equalsIgnoreCase("GET")) {
                return dishService.getDishById(request, path);
            }

            // Feedback route
            if (path.matches("/locations/[^/]+/feedbacks") && httpMethod.equalsIgnoreCase("GET")) {
                logger.info("Handling feedback retrieval request");
                return feedbackService.handleGetFeedbacks(request);
            }

            if (path.equals("/feedbacks") && httpMethod.equalsIgnoreCase("POST")) {
                return postAFeedback.handlePostAFeedback(request, context);
            }


            if (path.equals("/bookings/tables") && httpMethod.equalsIgnoreCase("GET")) {
                logger.info("In bookings handler");
                return tablesService.returnAvailableTablesFilteredByGivenCriteria(request, context);
            } else if (path.equals("/locations/select-options") && httpMethod.equalsIgnoreCase("GET")) {
                context.getLogger().log("In locations handler");
                return locationsService.allAvailableLocations(
                        request, context);
            }

            if (path.equals("/bookings/client") && httpMethod.equalsIgnoreCase("POST")) {
                return bookingService.handleCreateReservation(request);
            }

            if (path.equals("/bookings/waiter") && httpMethod.equalsIgnoreCase("POST")) {
                return bookingsByWaiterService.handleReservationByWaiter(request);
            }

            if (path.startsWith("/bookings/client/") && httpMethod.equalsIgnoreCase("PUT")) {
                return updateReservationService.handleUpdateReservation(request, path);
            }

            if (path.startsWith("/bookings/waiter/") && httpMethod.equalsIgnoreCase("PUT")) {
                return updateReservationByWaiterService.handleUpdateReservationByWaiter(request, path);
            }

            if (path.equals("/reservations") && httpMethod.equalsIgnoreCase("GET")) {
                return getReservationService.handleGetReservations(request);
            }

            if (path.startsWith("/reservations/") && httpMethod.equalsIgnoreCase("DELETE")) {
                return cancelReservationService.handleCancelReservation(request, path);
            }

            if (path.equals("/getPreviousFeedback") && httpMethod.equalsIgnoreCase("POST")) {
                return latestFeedback.returnLatestFeedback(request, context);
            }

            if (path.equals("/reservations/waiter") && httpMethod.equalsIgnoreCase("GET")) {
                return getReservationByWaiterService.handleGetReservationsByWaiter(request);
            }

            if ("/reports".equals(path) && "GET".equalsIgnoreCase(httpMethod)) {
                return getReportsService.handleGetReports(request);
            }

            if (path.equals("/users/profile") && httpMethod.equalsIgnoreCase("GET")) {
                logger.info("Handling get user profile request");
                return profileService.getUserProfile(request);
            }
            if (path.equals("/users/profile") && httpMethod.equalsIgnoreCase("PUT")) {
                logger.info("Handling update user profile request");
                return profileService.updateUserProfile(request);
            }
            if (path.equals("/users/profile/password") && httpMethod.equalsIgnoreCase("PUT")) {
                logger.info("Handling change password request");
                return profileService.changePassword(request);
            }
            return createErrorResponse(405, "Method Not Allowed: " + path + " with method " + httpMethod);

        } catch (Exception e) {
            logger.severe("Error handling request: " + e.getMessage());
            return createErrorResponse(500, "Error: " + e.getMessage());
        }
    }
}
