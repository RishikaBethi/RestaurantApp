package com.restaurant;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.restaurant.config.AppComponent;
import com.restaurant.config.DaggerAppComponent;
import com.restaurant.services.LocationsService;
import com.restaurant.services.TablesService;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;

import javax.inject.Inject;
import java.util.Map;

@LambdaHandler(
        lambdaName = "restaurant_handler",
        roleName = "restaurant_handler-role",
        isPublishVersion = true,
        aliasName = "${lambdas_alias_name}",
        logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)

@EnvironmentVariables(value = {
		@EnvironmentVariable(key = "LOCATIONS_TABLE", value = "${locations_table}"),
		@EnvironmentVariable(key = "RESERVATIONS_TABLE", value = "${reservations_table}"),
		@EnvironmentVariable(key = "TABLES_TABLE", value = "${tables_table}"),
		@EnvironmentVariable(key = "REGION", value = "${region}")
})

public class RestaurantHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    @Inject
    TablesService tablesService;

    @Inject
    LocationsService locationsService;

    private static final AppComponent appComponent;

    static {
        try {
            appComponent = DaggerAppComponent.create();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize AppComponent: " + e.getMessage(), e);
        }
    }

    public RestaurantHandler() {
        try {
            appComponent.inject(this);
        } catch (Exception e) {
            throw new RuntimeException("Injection failed in constructor: " + e.getMessage(), e);
        }
    }

    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        context.getLogger().log("Starting request handling, RequestId: " + context.getAwsRequestId());
        context.getLogger().log("tablesService: " + (tablesService != null ? "injected" : "null"));
        //context.getLogger().log("locationService: " + (locationService != null ? "injected" : "null"));

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        try {
            if (tablesService == null) {
                throw new IllegalStateException("Services not injected: tablesService=" + tablesService);
            }
            if (locationsService == null) {
                throw new IllegalStateException("Services not injected: locationsService=" + locationsService);
            }

            String path = event.getPath();
            String method = event.getHttpMethod();
            context.getLogger().log("Path: " + path + ", Method: " + method);

            if (method.equalsIgnoreCase("GET")) {
                if (path.equals("/bookings/tables")) {
                    context.getLogger().log("In bookings handler");
                    return tablesService.returnAvailableTablesFilteredByGivenCriteria(event, context);
                } else if (path.equals("/locations/select-options")) {
                    context.getLogger().log("In locations handler");
                    return locationsService.allAvailableLocations(event, context);
                } else {
                    context.getLogger().log("In else of paths");
                    return methodNotAllowed();
                }
            } else {
                context.getLogger().log("In else of GET");
                return methodNotAllowed();
            }
        } catch (Exception e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : e.toString();
            context.getLogger().log("Error processing request: " + errorMessage);
            return createErrorResponse(errorMessage);
        }
        //return response;
    }

    private APIGatewayProxyResponseEvent createErrorResponse(String message) {
        String safeMessage = message != null ? message : "Unknown error occurred";
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(400)
                .withBody("{\"error\": \"" + safeMessage + "\"}")
                .withHeaders(Map.of("Content-Type", "application/json"));
    }

    private APIGatewayProxyResponseEvent methodNotAllowed() {
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(405)
                .withBody("{\"error\": \"Method Not Allowed\"}")
                .withHeaders(Map.of("Content-Type", "application/json"));
    }
}


