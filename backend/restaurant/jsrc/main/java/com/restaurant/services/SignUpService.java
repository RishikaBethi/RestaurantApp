package com.restaurant.services;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Index;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.dto.SignUpDTO;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;
import com.amazonaws.services.dynamodbv2.document.Table;
import javax.inject.Inject;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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

            // Cognito sign-up
            SignUpRequest signUpRequest = SignUpRequest.builder()
                    .clientId(clientId)
                    .username(signUpDto.getEmail())
                    .password(signUpDto.getPassword())
                    .userAttributes(
                            AttributeType.builder().name("email").value(signUpDto.getEmail()).build()
                    )
                    .build();


            try {

                SignUpResponse signUpResponse = cognitoClient.signUp(signUpRequest);
                String userId = signUpResponse.userSub();

                AdminConfirmSignUpRequest confirmRequest = AdminConfirmSignUpRequest.builder()
                        .userPoolId(System.getenv("COGNITO_USER_POOL_ID"))
                        .username(signUpDto.getEmail())
                        .build();
                logger.info("Confirm Signup called successfully");
                cognitoClient.adminConfirmSignUp(confirmRequest);
                String role = isEmailInWaitersTable(signUpDto.getEmail()) ? "Waiter" : "Customer";

                try{
                    // Store in DynamoDB
                    usersTable.putItem(new PutItemSpec().withItem(new Item()
                            .withPrimaryKey("userId", userId)
                            .withString("email", signUpDto.getEmail())
                            .withString("firstName", signUpDto.getFirstName())
                            .withString("lastName", signUpDto.getLastName())
                            .withString("role", role)
                    ));
                } catch (Exception e) {
                    logger.severe("Error storing user data in DynamoDB: " + e.getMessage());
                    return createResponse(500, "Error saving user data.");
                }

            }
            catch (UsernameExistsException e) {
                return createResponse(400,"{\"message\":\"A user with this email address already exists.\"}");

            } catch (Exception e) {
                return createResponse(500,"{\"message\":\"An error occurred.\"}"+e.getMessage());
            }

            return createResponse(200, "User registered successfully");

        } catch (Exception e) {
            logger.severe("Error in signup: " + e.getMessage());
            return createResponse(500, "Signup failed: " + e.getMessage());
        }
    }

    private boolean isEmailInWaitersTable(String email) {
        Index emailIndex = waitersTable.getIndex("email-index");
        boolean item = emailIndex.query("email", email).iterator().hasNext(); // Query the GSI
        return item;
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
                .withStatusCode(statusCode).withBody("{\"message\":"+"\""+message+"\""+"}");


    }


}