////package com.restaurant;
////
////import com.amazonaws.services.lambda.runtime.Context;
////import com.amazonaws.services.lambda.runtime.RequestHandler;
////import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
////import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
////import com.restaurant.services.DishService;
////import com.restaurant.services.FeedbackService;
////import com.restaurant.services.LocationService;
////import com.restaurant.services.SignInService;
////import com.restaurant.services.SignUpService;
////import com.restaurant.config.*;
////import com.fasterxml.jackson.databind.ObjectMapper;
////import com.syndicate.deployment.annotations.resources.DependsOn;
////import com.syndicate.deployment.model.ResourceType;
////import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
////import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
////import com.syndicate.deployment.annotations.lambda.LambdaHandler;
////import com.syndicate.deployment.model.RetentionSetting;
////import com.syndicate.deployment.model.environment.ValueTransformer;
////
////import javax.inject.Inject;
////import java.util.HashMap;
////import java.util.Map;
////import java.util.logging.Logger;
////
////@DependsOn(resourceType = ResourceType.COGNITO_USER_POOL, name = "${user_pool}")
////@LambdaHandler(
////        lambdaName = "restaurant_handler",
////        roleName = "restaurant_handler-role",
////        isPublishVersion = true,
////        aliasName = "${lambdas_alias_name}",
////        logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
////)
////@EnvironmentVariables(value = {
////        @EnvironmentVariable(key = "USERS_TABLE", value = "${user_table}"),
////        @EnvironmentVariable(key = "WAITERS_TABLE", value = "${waiter_table}"),
////        @EnvironmentVariable(key = "LOCATIONS_TABLE", value = "tm7-Locations"),
////        @EnvironmentVariable(key = "COGNITO_USER_POOL_ID", value = "${user_pool}", valueTransformer = ValueTransformer.USER_POOL_NAME_TO_USER_POOL_ID),
////        @EnvironmentVariable(key = "REGION", value = "${region}"),
////        @EnvironmentVariable(key = "DISHES_TABLE", value = "tm7-Dishes"),
////        @EnvironmentVariable(key = "FEEDBACKS_TABLE", value = "tm7-Feedbacks"),
////        @EnvironmentVariable(key = "COGNITO_CLIENT_ID", value = "${user_pool}", valueTransformer = ValueTransformer.USER_POOL_NAME_TO_CLIENT_ID),
////        @EnvironmentVariable(key = "SPECIALITY_DISHES_API_URL", value = "${speciality_dishes_api_url}")
////})
////public class RestaurantHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
////
////    private static final Logger logger = Logger.getLogger(RestaurantHandler.class.getName());
////    private static final ObjectMapper objectMapper = new ObjectMapper();
////
////    @Inject
////    SignUpService signUpService;
////
////    @Inject
////    SignInService signInService;
////
////    @Inject
////    LocationService locationService;
////
////    @Inject
////    DishService dishService;
////
////    @Inject
////    FeedbackService feedbackService;
////
////    public RestaurantHandler() {
////        initDependencies();
////    }
////
////    private void initDependencies() {
////        AppComponent appComponent = DaggerAppComponent.builder()
////                .serviceModule(new ServiceModule())
////                .build();
////        appComponent.inject(this);
////    }
////
////    @Override
////    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
////        try {
////            String path = request.getPath();
////            String httpMethod = request.getHttpMethod();
////            Map<String, String> queryParams = request.getQueryStringParameters();
////
////            logger.info("Received request - Path: " + path + ", Method: " + httpMethod +
////                    ", Query: " + (queryParams != null ? queryParams.toString() : "none"));
////
////            // Handle sign-up
////            if ("/auth/sign-up".equals(path) && "POST".equals(httpMethod)) {
////                logger.info("Handling signup request");
////                return signUpService.handleSignUp(request);
////            }
////
////            // Handle sign-in
////            if ("/auth/sign-in".equals(path) && "POST".equals(httpMethod)) {
////                logger.info("Handling sign-in request");
////                return signInService.handleSignIn(request);
////            }
////
////            // Handle locations retrieval
////            if ("/locations".equals(path) && "GET".equals(httpMethod)) {
////                if (queryParams != null && queryParams.containsKey("locationId") && queryParams.containsKey("speciality-dishes")) {
////                    logger.info("Routing to getSpecialityDishes with query parameters: " + queryParams.toString());
////                    return locationService.getSpecialityDishes(request);
////                }
////                logger.info("Handling locations request");
////                return locationService.getLocations(request);
////            }
////
////            // Handle speciality dishes retrieval
////            if (path.startsWith("/locations/") && path.endsWith("/speciality-dishes") && "GET".equals(httpMethod)) {
////                logger.info("Handling speciality dishes request for path: " + path);
////                String[] pathParts = path.split("/");
////                if (pathParts.length >= 3) {
////                    String locationId = pathParts[pathParts.length - 2]; // e.g., LOC002
////                    if (queryParams == null) {
////                        queryParams = new HashMap<>();
////                    }
////                    queryParams.put("locationId", locationId);
////                    request.setQueryStringParameters(queryParams);
////                    logger.info("Extracted locationId: " + locationId + ", Updated query parameters: " + queryParams.toString());
////                    return locationService.getSpecialityDishes(request);
////                }
////                return new APIGatewayProxyResponseEvent()
////                        .withStatusCode(400)
////                        .withBody("{\"message\":\"Invalid speciality-dishes path format\"}")
////                        .withHeaders(Map.of("Content-Type", "application/json"));
////            }
////
////            // Handle popular dishes retrieval
////            if ("/dishes/popular".equals(path) && "GET".equals(httpMethod)) {
////                logger.info("Handling popular dishes request for path: " + path);
////                return dishService.getPopularDishes(request);
////            }
////
////            // Handle all dishes retrieval with dishType filtering
////            if ("/dishes".equals(path) && "GET".equals(httpMethod)) {
////                logger.info("Handling all dishes request for path: " + path + ", Query: " + (queryParams != null ? queryParams.toString() : "none"));
////                return dishService.getAllDishes(request);
////            }
////
////            // Handle dish by ID retrieval
////            if (path.startsWith("/dishes/") && path.split("/").length == 2 && "GET".equals(httpMethod)) {
////                logger.info("Handling dish by ID request for path: " + path);
////                String[] pathParts = path.split("/");
////                if (pathParts.length == 2 && !pathParts[1].isEmpty()) {
////                    String dishId = pathParts[1]; // e.g., 322846d5c951184d705b65d2
////                    Map<String, String> pathParams = new HashMap<>();
////                    pathParams.put("id", dishId);
////                    request.setPathParameters(pathParams);
////                    logger.info("Extracted dishId: " + dishId);
////                    return dishService.getDishById(request);
////                }
////                return new APIGatewayProxyResponseEvent()
////                        .withStatusCode(400)
////                        .withBody("{\"message\":\"Invalid dish ID path format\"}")
////                        .withHeaders(Map.of("Content-Type", "application/json"));
////            }
////
////            // Handle feedback retrieval for a location
////            if (path.startsWith("/locations/") && path.endsWith("/feedbacks") && "GET".equals(httpMethod)) {
////                logger.info("Handling feedback retrieval request for path: " + path);
////                String[] pathParts = path.split("/");
////                if (pathParts.length >= 3) {
////                    String locationId = pathParts[pathParts.length - 2]; // e.g., LOC002
////                    if (queryParams == null) {
////                        queryParams = new HashMap<>();
////                    }
////                    queryParams.put("locationId", locationId);
////                    request.setQueryStringParameters(queryParams);
////                    logger.info("Extracted locationId: " + locationId + ", Updated query parameters: " + queryParams.toString());
////                    return feedbackService.handleGetFeedbacks(request);
////                }
////                return new APIGatewayProxyResponseEvent()
////                        .withStatusCode(400)
////                        .withBody("{\"message\":\"Invalid feedbacks path format\"}")
////                        .withHeaders(Map.of("Content-Type", "application/json"));
////            }
////
////            logger.info("Not handling request for path: " + path + ", method: " + httpMethod);
////            return new APIGatewayProxyResponseEvent()
////                    .withStatusCode(405)
////                    .withBody("{\"message\":\"Method Not Allowed\", \"path\":\"" + path + "\", \"method\":\"" + httpMethod + "\"}")
////                    .withHeaders(Map.of("Content-Type", "application/json"));
////
////        } catch (Exception e) {
////            logger.severe("Error handling request: " + e.getMessage());
////            return new APIGatewayProxyResponseEvent()
////                    .withStatusCode(500)
////                    .withBody("{\"message\":\"Error: " + e.getMessage() + "\"}")
////                    .withHeaders(Map.of("Content-Type", "application/json"));
////        }
////    }
////}
//
//package com.restaurant;
//
//import com.amazonaws.services.lambda.runtime.Context;
//import com.amazonaws.services.lambda.runtime.RequestHandler;
//import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
//import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
//import com.restaurant.services.DishService;
//import com.restaurant.services.FeedbackService;
//import com.restaurant.services.LocationService;
//import com.restaurant.services.SignInService;
//import com.restaurant.services.SignUpService;
//import com.restaurant.config.*;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.syndicate.deployment.annotations.resources.DependsOn;
//import com.syndicate.deployment.model.ResourceType;
//import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
//import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
//import com.syndicate.deployment.annotations.lambda.LambdaHandler;
//import com.syndicate.deployment.model.RetentionSetting;
//import com.syndicate.deployment.model.environment.ValueTransformer;
//
//import javax.inject.Inject;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.logging.Logger;
//
//@DependsOn(resourceType = ResourceType.COGNITO_USER_POOL, name = "${user_pool}")
//@LambdaHandler(
//        lambdaName = "restaurant_handler",
//        roleName = "restaurant_handler-role",
//        isPublishVersion = true,
//        aliasName = "${lambdas_alias_name}",
//        logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
//)
//@EnvironmentVariables(value = {
//        @EnvironmentVariable(key = "USERS_TABLE", value = "${user_table}"),
//        @EnvironmentVariable(key = "WAITERS_TABLE", value = "${waiter_table}"),
//        @EnvironmentVariable(key = "LOCATIONS_TABLE", value = "tm7-Locations"),
//        @EnvironmentVariable(key = "COGNITO_USER_POOL_ID", value = "${user_pool}", valueTransformer = ValueTransformer.USER_POOL_NAME_TO_USER_POOL_ID),
//        @EnvironmentVariable(key = "REGION", value = "${region}"),
//        @EnvironmentVariable(key = "DISHES_TABLE", value = "tm7-Dishes"),
//        @EnvironmentVariable(key = "FEEDBACKS_TABLE", value = "tm7-Feedbacks"),
//        @EnvironmentVariable(key = "COGNITO_CLIENT_ID", value = "${user_pool}", valueTransformer = ValueTransformer.USER_POOL_NAME_TO_CLIENT_ID),
//        @EnvironmentVariable(key = "SPECIALITY_DISHES_API_URL", value = "${speciality_dishes_api_url}")
//})
//public class RestaurantHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
//
//    private static final Logger logger = Logger.getLogger(RestaurantHandler.class.getName());
//    private static final ObjectMapper objectMapper = new ObjectMapper();
//
//    @Inject
//    SignUpService signUpService;
//
//    @Inject
//    SignInService signInService;
//
//    @Inject
//    LocationService locationService;
//
//    @Inject
//    DishService dishService;
//
//    @Inject
//    FeedbackService feedbackService;
//
//    public RestaurantHandler() {
//        initDependencies();
//    }
//
//    private void initDependencies() {
//        AppComponent appComponent = DaggerAppComponent.builder()
//                .serviceModule(new ServiceModule())
//                .build();
//        appComponent.inject(this);
//    }
//
//    @Override
//    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
//        try {
//            String path = request.getPath();
//            String httpMethod = request.getHttpMethod();
//            Map<String, String> queryParams = request.getQueryStringParameters();
//
//            logger.info("Received request - Path: " + path + ", Method: " + httpMethod +
//                    ", Query: " + (queryParams != null ? queryParams.toString() : "none"));
//
//            // Handle sign-up
//            if ("/auth/sign-up".equals(path) && "POST".equals(httpMethod)) {
//                logger.info("Handling signup request");
//                return signUpService.handleSignUp(request);
//            }
//
//            // Handle sign-in
//            if ("/auth/sign-in".equals(path) && "POST".equals(httpMethod)) {
//                logger.info("Handling sign-in request");
//                return signInService.handleSignIn(request);
//            }
//
//            // Handle locations retrieval
//            if ("/locations".equals(path) && "GET".equals(httpMethod)) {
//                if (queryParams != null && queryParams.containsKey("locationId") && queryParams.containsKey("speciality-dishes")) {
//                    logger.info("Routing to getSpecialityDishes with query parameters: " + queryParams.toString());
//                    return locationService.getSpecialityDishes(request);
//                }
//                logger.info("Handling locations request");
//                return locationService.getLocations(request);
//            }
//
//            // Handle speciality dishes retrieval
//            if (path.startsWith("/locations/") && path.endsWith("/speciality-dishes") && "GET".equals(httpMethod)) {
//                logger.info("Handling speciality dishes request for path: " + path);
//                String[] pathParts = path.split("/");
//                if (pathParts.length >= 3) {
//                    String locationId = pathParts[pathParts.length - 2]; // e.g., LOC002
//                    if (queryParams == null) {
//                        queryParams = new HashMap<>();
//                    }
//                    queryParams.put("locationId", locationId);
//                    request.setQueryStringParameters(queryParams);
//                    logger.info("Extracted locationId: " + locationId + ", Updated query parameters: " + queryParams.toString());
//                    return locationService.getSpecialityDishes(request);
//                }
//                return new APIGatewayProxyResponseEvent()
//                        .withStatusCode(400)
//                        .withBody("{\"message\":\"Invalid speciality-dishes path format\"}")
//                        .withHeaders(Map.of("Content-Type", "application/json"));
//            }
//
//            // Handle popular dishes retrieval
//            if ("/dishes/popular".equals(path) && "GET".equals(httpMethod)) {
//                logger.info("Handling popular dishes request for path: " + path);
//                return dishService.getPopularDishes(request);
//            }
//
//            // Handle all dishes retrieval with dishType filtering
//            if ("/dishes".equals(path) && "GET".equals(httpMethod)) {
//                logger.info("Handling all dishes request for path: " + path + ", Query: " + (queryParams != null ? queryParams.toString() : "none"));
//                return dishService.getAllDishes(request);
//            }
//
//            // Handle dish by ID retrieval
//            if (path.startsWith("/dishes/") && path.split("/").length == 2 && "GET".equals(httpMethod)) {
//                logger.info("Handling dish by ID request for path: " + path);
//                String[] pathParts = path.split("/");
//                if (pathParts.length == 2 && !pathParts[1].isEmpty()) {
//                    String dishId = pathParts[1]; // e.g., 322846d5c951184d705b65d2
//                    Map<String, String> pathParams = new HashMap<>();
//                    pathParams.put("id", dishId);
//                    request.setPathParameters(pathParams);
//                    logger.info("Extracted dishId: " + dishId);
//                    return dishService.getDishById(request);
//                }
//                return new APIGatewayProxyResponseEvent()
//                        .withStatusCode(400)
//                        .withBody("{\"message\":\"Invalid dish ID path format\"}")
//                        .withHeaders(Map.of("Content-Type", "application/json"));
//            }
//
//            // Handle feedback retrieval for a location
//            if (path.startsWith("/locations/") && path.endsWith("/feedbacks") && "GET".equals(httpMethod)) {
//                logger.info("Handling feedback retrieval request for path: " + path);
//                String[] pathParts = path.split("/");
//                if (pathParts.length >= 3) {
//                    String locationId = pathParts[pathParts.length - 2]; // e.g., LOC002
//                    if (queryParams == null) {
//                        queryParams = new HashMap<>();
//                    }
//                    queryParams.put("locationId", locationId);
//                    request.setQueryStringParameters(queryParams);
//                    logger.info("Extracted locationId: " + locationId + ", Updated query parameters: " + queryParams.toString());
//                    return feedbackService.handleGetFeedbacks(request);
//                }
//                return new APIGatewayProxyResponseEvent()
//                        .withStatusCode(400)
//                        .withBody("{\"message\":\"Invalid feedbacks path format\"}")
//                        .withHeaders(Map.of("Content-Type", "application/json"));
//            }
//
//            logger.info("Not handling request for path: " + path + ", method: " + httpMethod);
//            return new APIGatewayProxyResponseEvent()
//                    .withStatusCode(405)
//                    .withBody("{\"message\":\"Method Not Allowed\", \"path\":\"" + path + "\", \"method\":\"" + httpMethod + "\"}")
//                    .withHeaders(Map.of("Content-Type", "application/json"));
//
//        } catch (Exception e) {
//            logger.severe("Error handling request: " + e.getMessage());
//            return new APIGatewayProxyResponseEvent()
//                    .withStatusCode(500)
//                    .withBody("{\"message\":\"Error: " + e.getMessage() + "\"}")
//                    .withHeaders(Map.of("Content-Type", "application/json"));
//        }
//    }
//}
package com.restaurant;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.restaurant.services.DishService;
import com.restaurant.services.FeedbackService;
import com.restaurant.services.LocationService;
import com.restaurant.services.SignInService;
import com.restaurant.services.SignUpService;
import com.restaurant.config.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
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
        @EnvironmentVariable(key = "COGNITO_USER_POOL_ID", value = "${user_pool}", valueTransformer = ValueTransformer.USER_POOL_NAME_TO_USER_POOL_ID),
        @EnvironmentVariable(key = "REGION", value = "${region}"),
        @EnvironmentVariable(key = "DISHES_TABLE", value = "tm7-Dishes"),
        @EnvironmentVariable(key = "FEEDBACKS_TABLE", value = "tm7-Feedbacks"),
        @EnvironmentVariable(key = "COGNITO_CLIENT_ID", value = "${user_pool}", valueTransformer = ValueTransformer.USER_POOL_NAME_TO_CLIENT_ID),
        @EnvironmentVariable(key = "SPECIALITY_DISHES_API_URL", value = "${speciality_dishes_api_url}")
})
public class RestaurantHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final Logger logger = Logger.getLogger(RestaurantHandler.class.getName());
    private static final ObjectMapper objectMapper = new ObjectMapper();

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

            // Handle sign-up
            if ("/auth/sign-up".equals(path) && "POST".equals(httpMethod)) {
                logger.info("Handling signup request");
                return signUpService.handleSignUp(request);
            }

            // Handle sign-in
            if ("/auth/sign-in".equals(path) && "POST".equals(httpMethod)) {
                logger.info("Handling sign-in request");
                return signInService.handleSignIn(request);
            }

            // Handle locations retrieval
            if ("/locations".equals(path) && "GET".equals(httpMethod)) {
                if (queryParams != null && queryParams.containsKey("locationId") && queryParams.containsKey("speciality-dishes")) {
                    logger.info("Routing to getSpecialityDishes with query parameters: " + queryParams.toString());
                    return locationService.getSpecialityDishes(request);
                }
                logger.info("Handling locations request");
                return locationService.getLocations(request);
            }

            // Handle speciality dishes retrieval
            if (path.startsWith("/locations/") && path.endsWith("/speciality-dishes") && "GET".equals(httpMethod)) {
                logger.info("Handling speciality dishes request for path: " + path);
                String[] pathParts = path.split("/");
                if (pathParts.length >= 3) {
                    String locationId = pathParts[pathParts.length - 2];
                    if (queryParams == null) {
                        queryParams = new HashMap<>();
                    }
                    queryParams.put("locationId", locationId);
                    request.setQueryStringParameters(queryParams);
                    logger.info("Extracted locationId: " + locationId + ", Updated query parameters: " + queryParams.toString());
                    return locationService.getSpecialityDishes(request);
                }
                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(400)
                        .withBody("{\"message\":\"Invalid speciality-dishes path format\"}")
                        .withHeaders(Map.of("Content-Type", "application/json"));
            }

            // Handle popular dishes retrieval
            if ("/dishes/popular".equals(path) && "GET".equals(httpMethod)) {
                logger.info("Handling popular dishes request for path: " + path);
                return dishService.getPopularDishes(request);
            }

            // Handle all dishes retrieval with dishType filtering
            if ("/dishes".equals(path) && "GET".equals(httpMethod)) {
                logger.info("Handling all dishes request for path: " + path + ", Query: " + (queryParams != null ? queryParams.toString() : "none"));
                return dishService.getAllDishes(request);
            }

            // Handle dish by ID retrieval (e.g., /dishes/D101)
            if (path.startsWith("/dishes/") && "GET".equalsIgnoreCase(httpMethod)) {
                return dishService.getDishById(request, path);
            }


            // Handle feedback retrieval for a location
            if (path.startsWith("/locations/") && path.endsWith("/feedbacks") && "GET".equals(httpMethod)) {
                logger.info("Handling feedback retrieval request for path: " + path);
                String[] pathParts = path.split("/");
                if (pathParts.length >= 3) {
                    String locationId = pathParts[pathParts.length - 2]; // e.g., LOC002
                    if (queryParams == null) {
                        queryParams = new HashMap<>();
                    }
                    queryParams.put("locationId", locationId);
                    request.setQueryStringParameters(queryParams);
                    logger.info("Extracted locationId: " + locationId + ", Updated query parameters: " + queryParams.toString());
                    return feedbackService.handleGetFeedbacks(request);
                }
                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(400)
                        .withBody("{\"message\":\"Invalid feedbacks path format\"}")
                        .withHeaders(Map.of("Content-Type", "application/json"));
            }

            logger.info("Not handling request for path: " + path + ", method: " + httpMethod);
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(405)
                    .withBody("{\"message\":\"Method Not Allowed\", \"path\":\"" + path + "\", \"method\":\"" + httpMethod + "\"}")
                    .withHeaders(Map.of("Content-Type", "application/json"));

        } catch (Exception e) {
            logger.severe("Error handling request: " + e.getMessage());
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withBody("{\"message\":\"Error: " + e.getMessage() + "\"}")
                    .withHeaders(Map.of("Content-Type", "application/json"));
        }
    }
}