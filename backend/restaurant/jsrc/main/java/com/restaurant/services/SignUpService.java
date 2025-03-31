package com.restaurant.services;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.Index;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.dto.SignUpDTO;
import com.restaurant.validators.*;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;


public class SignUpService {

    // Declare all dependencies as final fields
    private final CognitoIdentityProviderClient cognitoClient;
    private final ObjectMapper objectMapper;
    private final DynamoDB dynamoDB;
    private final Table usersTable;
    private final Table waitersTable;
    private final String clientId;

    private static final Logger logger = Logger.getLogger(SignUpService.class.getName());

    // Constructor injection with @Inject annotation
    @Inject
    public SignUpService(
            CognitoIdentityProviderClient cognitoClient,
            ObjectMapper objectMapper,
            DynamoDB dynamoDB,
            String clientId) {
        this.cognitoClient = cognitoClient;
        this.objectMapper = objectMapper;
        this.dynamoDB = dynamoDB;
        this.usersTable = dynamoDB.getTable(System.getenv("USERS_TABLE"));
        this.waitersTable = dynamoDB.getTable(System.getenv("WAITERS_TABLE"));
        this.clientId = clientId;
    }

    public APIGatewayProxyResponseEvent handleSignUp(APIGatewayProxyRequestEvent request) {
        try {
            SignUpDTO signUpDto = SignUpDTO.fromJson(request.getBody());

            // Validate email and password using custom validators
            if (!EmailValidator.validateEmail(signUpDto.getEmail())) {
                return createResponse(400, "Invalid email format", null);
            }

            if (!PasswordValidator.validatePassword(signUpDto.getPassword())) {
                return createResponse(400, "Password must be 8-16 characters long, include an uppercase letter, a number, and a special character", null);
            }

            // Cognito sign-up
            SignUpRequest signUpRequest = SignUpRequest.builder()
                    .clientId(clientId)
                    .username(signUpDto.getEmail())
                    .password(signUpDto.getPassword())
                    .userAttributes(
                            AttributeType.builder().name("email").value(signUpDto.getEmail()).build()
                    )
                    .build();

            SignUpResponse signUpResponse = cognitoClient.signUp(signUpRequest);
            String userId = signUpResponse.userSub();

            // Check if user already exists in the Users table
            if (isUserAlreadyRegistered(signUpDto.getEmail())) {
                return createResponse(400, "A user with this userId already exists.", null);
            }

            // Check if the email exists in the Waiters table
            String role = isEmailInWaitersTable(signUpDto.getEmail()) ? "Waiter" : "Customer";

            // Store in DynamoDB
            usersTable.putItem(new PutItemSpec().withItem(new Item()
                    .withPrimaryKey("userId", userId)
                    .withString("email", signUpDto.getEmail())
                    .withString("firstName", signUpDto.getFirstName())
                    .withString("lastName", signUpDto.getLastName())
                    .withString("role", role)
            ));

            return createResponse(200, "User registered successfully", Map.of("userId", userId, "role", role));

        } catch (Exception e) {
            logger.severe("Error in signup: " + e.getMessage());
            return createResponse(500, "Signup failed: " + e.getMessage(), null);
        }
    }

    // Helper method to check if user already exists in the Users table
    private boolean isUserAlreadyRegistered(String email) {
        Index emailIndex = usersTable.getIndex("email-index");
        boolean item = emailIndex.query("email", email).iterator().hasNext(); // Query the GSI
        return item;
    }

    // Helper method to check if the email exists in the Waiters table
    private boolean isEmailInWaitersTable(String email) {
        Index emailIndex = waitersTable.getIndex("email-index");
        boolean item = emailIndex.query("email", email).iterator().hasNext(); // Query the GSI
        return item;
    }

    private APIGatewayProxyResponseEvent createResponse(int statusCode, String message, Map<String, String> data) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        if (data != null) response.put("data", data);

        try {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(statusCode)
                    .withBody(objectMapper.writeValueAsString(response));

        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent().withStatusCode(500).withBody("{\"message\":\"Response Error\"}");
        }
    }
}