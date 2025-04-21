package com.restaurant.services;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.dto.SignInDTO;
import static com.restaurant.utils.Helper.*;
import com.restaurant.validators.EmailValidator;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.NotAuthorizedException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserNotFoundException;

import javax.inject.Inject;
import java.time.Instant;
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
    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final long LOCKOUT_DURATION_SECONDS = 30 * 60;

    @Inject
    public SignInService(CognitoIdentityProviderClient cognitoClient, ObjectMapper objectMapper, String clientId, DynamoDB dynamoDB) {
        this.cognitoClient = cognitoClient;
        this.objectMapper = objectMapper;
        this.clientId = clientId;
        this.dynamoDB = dynamoDB;
        this.usersTable = dynamoDB.getTable(System.getenv("USERS_TABLE"));
    }

    public APIGatewayProxyResponseEvent handleSignIn(APIGatewayProxyRequestEvent request) {
        try {
            SignInDTO signInDTO = SignInDTO.fromJson(request.getBody());

            // Validate input
            if (!EmailValidator.validateEmail(signInDTO.getEmail())) {
                return createErrorResponse(400, "Invalid Email");
            }
            if (signInDTO.getEmail() == null || signInDTO.getEmail().isEmpty()) {
                return createErrorResponse(400, "Email is required");
            }
            if (signInDTO.getPassword() == null || signInDTO.getPassword().isEmpty()) {
                return createErrorResponse(400, "Password is required");
            }

            // Check if the user is locked out
            Item userItem = usersTable.getItem("email", signInDTO.getEmail());
            if (userItem != null) {
                if (isAccountLocked(userItem)) {
                    return createApiResponse(403, Map.of("message","Your account is temporarily locked due to multiple failed login attempts. Please try again later."));
                }
            }

            // Cognito sign-in
            Map<String, String> authParams = new HashMap<>();
            authParams.put("USERNAME", signInDTO.getEmail());
            authParams.put("PASSWORD", signInDTO.getPassword());

            InitiateAuthRequest authRequest = InitiateAuthRequest.builder()
                    .authFlow(AuthFlowType.USER_PASSWORD_AUTH)
                    .clientId(clientId)
                    .authParameters(authParams)
                    .build();

            InitiateAuthResponse authResponse;
            try {
                authResponse = cognitoClient.initiateAuth(authRequest);

                // Successful login - reset failed attempts
                if (userItem != null) {
                    resetFailedAttempts(signInDTO.getEmail());
                }

            } catch (UserNotFoundException | NotAuthorizedException e) {
                // Increment failed login attempts
                incrementFailedAttempts(signInDTO.getEmail(), userItem);
                return createErrorResponse(401, "Incorrect email or password. Try again or create an account.");
            }

            String accessToken = authResponse.authenticationResult().idToken();

            // Fetch user data
            userItem = usersTable.getItem("email", signInDTO.getEmail());

            String username = (userItem != null) ? userItem.getString("firstName") + " " + userItem.getString("lastName")
                    : signInDTO.getEmail();
            String role = (userItem != null) ? userItem.getString("role") : "CLIENT";

            Map<String, String> responseData = new HashMap<>();
            responseData.put("accessToken", accessToken);
            responseData.put("username", username);
            responseData.put("role", role);

            return createApiResponse(201,responseData);

        } catch (Exception e) {
            logger.severe("Sign-in failed: " + e.getMessage());
            return createErrorResponse(500, "Sign-in failed: " + e.getMessage());
        }
    }

    private boolean isAccountLocked(Item userItem) {
        if (userItem.hasAttribute("lockoutUntil")) {
            long lockoutUntil = userItem.getLong("lockoutUntil");
            long currentTime = Instant.now().getEpochSecond();
            return currentTime < lockoutUntil;
        }
        return false;
    }

    private void incrementFailedAttempts(String email, Item userItem) {
        if (userItem == null) {
            // If user doesn't exist in DynamoDB, we can't track failed attempts
            return;
        }

        int failedAttempts = userItem.hasAttribute("failedAttempts") ? userItem.getInt("failedAttempts") : 0;
        failedAttempts++;

        if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
            // Lock the account for 30 minutes
            long lockoutUntil = Instant.now().getEpochSecond() + LOCKOUT_DURATION_SECONDS;
            usersTable.updateItem(new UpdateItemSpec()
                    .withPrimaryKey("email", email)
                    .withUpdateExpression("SET failedAttempts = :attempts, lockoutUntil = :lockout")
                    .withValueMap(new ValueMap()
                            .withInt(":attempts", failedAttempts)
                            .withLong(":lockout", lockoutUntil)));
        } else {
            // Increment failed attempts
            usersTable.updateItem(new UpdateItemSpec()
                    .withPrimaryKey("email", email)
                    .withUpdateExpression("SET failedAttempts = :attempts")
                    .withValueMap(new ValueMap()
                            .withInt(":attempts", failedAttempts)));
        }
    }

    private void resetFailedAttempts(String email) {
        usersTable.updateItem(new UpdateItemSpec()
                .withPrimaryKey("email", email)
                .withUpdateExpression("REMOVE failedAttempts, lockoutUntil"));
    }
}