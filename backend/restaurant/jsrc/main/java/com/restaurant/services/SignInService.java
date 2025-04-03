package com.restaurant.services;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.dto.SignInDTO;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.NotAuthorizedException;
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
        this.dynamoDB=dynamoDB;
        this.usersTable = dynamoDB.getTable(System.getenv("USERS_TABLE"));
    }

    public String extractUserIdFromToken(String idToken) {
        DecodedJWT jwt = JWT.decode(idToken);
        return jwt.getClaim("sub").asString(); // Extract "sub" claim (user ID)
    }

    public APIGatewayProxyResponseEvent handleSignIn(APIGatewayProxyRequestEvent request) {
        try {
            SignInDTO signInDto = SignInDTO.fromJson(request.getBody());

            // Validate input
            if (signInDto.getEmail() == null || signInDto.getEmail().isEmpty()) {
                return createResponse(400, "Email is required");
            }
            if (signInDto.getPassword() == null || signInDto.getPassword().isEmpty()) {
                return createResponse(400, "Password is required");
            }

            // Cognito sign-in
            Map<String, String> authParams = new HashMap<>();
            authParams.put("USERNAME", signInDto.getEmail());
            authParams.put("PASSWORD", signInDto.getPassword());


            InitiateAuthRequest authRequest = InitiateAuthRequest.builder()
                    .authFlow(AuthFlowType.USER_PASSWORD_AUTH)
                    .clientId(clientId)
                    .authParameters(authParams)
                    .build();

            InitiateAuthResponse authResponse = cognitoClient.initiateAuth(authRequest);
            String accessToken = authResponse.authenticationResult().idToken(); // Using idToken as accessToken

            // Query DynamoDB for the username using userId
            String userId = extractUserIdFromToken(accessToken);
            Item userItem = usersTable.getItem("userId", userId); // Get user details by ID

            // Extract username; fallback to email if not found
            String username = (userItem != null) ? userItem.getString("firstName") + " " + userItem.getString("lastName")
                    : signInDto.getEmail(); // Default to email if missing
            String role = (userItem != null) ? userItem.getString("role") : "CUSTOMER"; // Default to CLIENT if user not found

            Map<String, String> responseData = new HashMap<>();
            responseData.put("accessToken", accessToken);
            responseData.put("username", username);
            responseData.put("role", role);

            return createSuccessResponse(responseData);

        } catch (NotAuthorizedException e) {
            logger.severe("Invalid credentials: " + e.getMessage());
            return createResponse(401, "Invalid email or password");
        } catch (Exception e) {
            logger.severe("Sign-in failed: " + e.getMessage());
            return createResponse(500, "Sign-in failed: " + e.getMessage());
        }
    }

    private APIGatewayProxyResponseEvent createSuccessResponse(Map<String, String> data) {
        try {
            String body = objectMapper.writeValueAsString(data);
            APIGatewayProxyResponseEvent apiGatewayProxyResponseEvent = new APIGatewayProxyResponseEvent();
            apiGatewayProxyResponseEvent.setHeaders(createCorsHeaders());
            return apiGatewayProxyResponseEvent
                    .withStatusCode(200)
                    .withBody(body);
        } catch (Exception e) {
            return createResponse(500, "{\"message\":\"Response Error\"}");
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
        APIGatewayProxyResponseEvent apiGatewayProxyResponseEvent = new APIGatewayProxyResponseEvent();
        apiGatewayProxyResponseEvent.setHeaders(createCorsHeaders());

        return apiGatewayProxyResponseEvent
                .withStatusCode(statusCode)
                .withBody("{\"message\":\"" + message + "\"}");

    }
}

