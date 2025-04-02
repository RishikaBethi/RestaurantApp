package com.restaurant;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.restaurant.services.SignInService;
import com.syndicate.deployment.annotations.resources.DependsOn;

import com.restaurant.config.*;
import com.syndicate.deployment.model.ResourceType;
import com.restaurant.services.SignUpService;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.DeploymentRuntime;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.environment.ValueTransformer;

import javax.inject.Inject;
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
		@EnvironmentVariable(key = "COGNITO_USER_POOL_ID", value = "${user_pool}", valueTransformer = ValueTransformer.USER_POOL_NAME_TO_USER_POOL_ID),
		@EnvironmentVariable(key = "REGION", value = "${region}"),
		@EnvironmentVariable(key = "COGNITO_CLIENT_ID", value = "${user_pool}", valueTransformer = ValueTransformer.USER_POOL_NAME_TO_CLIENT_ID)
})
public class RestaurantHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	private static final Logger logger = Logger.getLogger(RestaurantHandler.class.getName());

	@Inject
	SignUpService signUpService;

	@Inject
	SignInService signInService;

	public RestaurantHandler() {
		AppComponent appComponent = DaggerAppComponent.create();

		appComponent.inject(this);
	}

	@Override
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
		String path = request.getPath();
		String httpMethod = request.getHttpMethod();

		if ("/auth/sign-up".equals(path) && "POST".equals(httpMethod)) {
			logger.info("Handling signup request");
			return signUpService.handleSignUp(request);
		}
		else if ("/auth/sign-in".equals(path) && "POST".equals(httpMethod)) {
			logger.info("Handling sign-in request");
			return signInService.handleSignIn(request);
		}
		logger.info("not Handling signup request");
		return new APIGatewayProxyResponseEvent()
				.withStatusCode(405)
				.withBody("{\"message\":\"Method Not Allowed\", \"path\":\"" + path + "\", \"method\":\"" + httpMethod + "\"}")
				.withHeaders(Map.of("Content-Type", "application/json"));
	}
}
