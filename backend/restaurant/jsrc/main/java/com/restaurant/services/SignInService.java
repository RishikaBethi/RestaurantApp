package com.restaurant.services;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.model.SignInEntity;
import com.restaurant.validators.EmailValidator;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.NotAuthorizedException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserNotFoundException;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;

import javax.inject.Inject;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class SignInService {
    private final CognitoIdentityProviderClient cognitoClient;
    private final ObjectMapper objectMapper;
    private final String clientId;
    private static final Logger logger = Logger.getLogger(SignInService.class.getName());
    private final DynamoDB dynamoDB;
    private final Table usersTable;

    @Inject
    public SignInService(CognitoIdentityProviderClient cognitoClient, ObjectMapper objectMapper, String clientId, DynamoDB dynamoDB) {
        this.cognitoClient = cognitoClient;
        this.objectMapper = objectMapper;
        this.clientId = clientId;
        this.dynamoDB = dynamoDB;
        this.usersTable = dynamoDB.getTable(System.getenv("USERS_TABLE"));
    }

    public String extractUserIdFromToken(String idToken) {
        DecodedJWT jwt = JWT.decode(idToken);
        return jwt.getClaim("sub").asString();
    }

    public APIGatewayProxyResponseEvent handleSignIn(APIGatewayProxyRequestEvent request) {
        try {
            SignInEntity signInEntity = SignInEntity.fromJson(request.getBody());

            // Validate input
            if (!EmailValidator.validateEmail(signInEntity.getEmail())) {
                return createResponse(400, "Invalid Email");
            }
            if (signInEntity.getEmail() == null || signInEntity.getEmail().isEmpty()) {
                return createResponse(400, "Email is required");
            }
            if (signInEntity.getPassword() == null || signInEntity.getPassword().isEmpty()) {
                return createResponse(400, "Password is required");
            }

            // Cognito sign-in
            Map<String, String> authParams = new HashMap<>();
            authParams.put("USERNAME", signInEntity.getEmail());
            authParams.put("PASSWORD", signInEntity.getPassword());

            InitiateAuthRequest authRequest = InitiateAuthRequest.builder()
                    .authFlow(AuthFlowType.USER_PASSWORD_AUTH)
                    .clientId(clientId)
                    .authParameters(authParams)
                    .build();

            InitiateAuthResponse authResponse;
            try {
                authResponse = cognitoClient.initiateAuth(authRequest);
            } catch (UserNotFoundException | NotAuthorizedException e) {
                return createResponse(401, "Unauthorized access");
            }

            String accessToken = authResponse.authenticationResult().idToken();
            String userId = extractUserIdFromToken(accessToken);
            Item userItem = usersTable.getItem("userId", userId);

            String username = (userItem != null) ? userItem.getString("firstName") + " " + userItem.getString("lastName")
                    : signInEntity.getEmail();
            String role = (userItem != null) ? userItem.getString("role") : "CLIENT";

            Map<String, String> responseData = new HashMap<>();
            responseData.put("accessToken", accessToken);
            responseData.put("username", username);
            responseData.put("role", role);

            return createSuccessResponse(responseData);

        } catch (Exception e) {
            logger.severe("Sign-in failed: " + e.getMessage());
            return createResponse(500, "Sign-in failed: " + e.getMessage());
        }
    }

    private APIGatewayProxyResponseEvent createSuccessResponse(Map<String, String> data) {
        try {
            String body = objectMapper.writeValueAsString(data);
            APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
            response.setHeaders(createCorsHeaders());
            return response.withStatusCode(200).withBody(body);
        } catch (Exception e) {
            return createResponse(500, "Response Error");
        }
    }

    private static Map<String, String> createCorsHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Access-Control-Allow-Origin", "*");
        headers.put("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        headers.put("Access-Control-Allow-Headers", "Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token");
        return Collections.unmodifiableMap(headers);
    }

    private APIGatewayProxyResponseEvent createResponse(int statusCode, String message) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setHeaders(createCorsHeaders());
        return response.withStatusCode(statusCode).withBody("{\"message\":\"" + message + "\"}");
    }
}