package com.restaurant.services;

import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.utils.Helper;
import com.restaurant.validators.NameValidator;
import com.restaurant.validators.PasswordValidator;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ChangePasswordRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InvalidParameterException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.NotAuthorizedException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserNotFoundException;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class ProfileService {
    private static final Logger logger = Logger.getLogger(ProfileService.class.getName());
    private final CognitoIdentityProviderClient cognitoClient;
    private final DynamoDB dynamoDB;
    private final Table usersTable;
    private final Table feedbacksTable;
    private final Table reservationsTable;

    private final ObjectMapper objectMapper;
    private final String clientId;

    @Inject
    public ProfileService(CognitoIdentityProviderClient cognitoClient, DynamoDB dynamoDB,
                          ObjectMapper objectMapper, String clientId) {
        this.cognitoClient = cognitoClient;
        this.dynamoDB = dynamoDB;
        this.usersTable = dynamoDB.getTable(System.getenv("USERS_TABLE"));
        this.feedbacksTable = dynamoDB.getTable(System.getenv("FEEDBACKS_TABLE"));
        this.reservationsTable = dynamoDB.getTable(System.getenv("RESERVATIONS_TABLE"));
        this.objectMapper = objectMapper;
        this.clientId = clientId;
        logger.info("Initialized with clientId: " + clientId);
    }

    public APIGatewayProxyResponseEvent getUserProfile(APIGatewayProxyRequestEvent request) {
        try {
            Map<String, Object> claims = Helper.extractClaims(request);
            String email = (String) claims.get("email");

            if (email == null || email.isEmpty()) {
                return Helper.createErrorResponse(401, "Unauthorized: Email not found in token.");
            }

            Item userItem = usersTable.getItem("email", email);
            if (userItem == null) {
                return Helper.createErrorResponse(404, "User not found.");
            }

            Map<String, String> response = new HashMap<>();
            response.put("firstName", userItem.getString("firstName"));
            response.put("lastName", userItem.getString("lastName"));
            response.put("imageUrl", userItem.getString("imageUrl") != null ? userItem.getString("imageUrl") : "");

            return Helper.createApiResponse(200, response);
        } catch (Exception e) {
            logger.severe("Error fetching user profile: " + e.getMessage());
            return Helper.createErrorResponse(500, "Error fetching user profile: " + e.getMessage());
        }
    }

    public APIGatewayProxyResponseEvent updateUserProfile(APIGatewayProxyRequestEvent request) {
        try {
            Map<String, Object> claims = Helper.extractClaims(request);
            String email = (String) claims.get("email");

            if (email == null || email.isEmpty()) {
                return Helper.createErrorResponse(401, "Unauthorized: Email not found in token.");
            }

            Map<String, String> requestBody = objectMapper.readValue(request.getBody(), Map.class);
            String firstName = requestBody.get("firstName");
            String lastName = requestBody.get("lastName");
            String base64encodedImage = requestBody.get("base64encodedImage");

            if (firstName == null || lastName == null) {
                return Helper.createErrorResponse(400, "First name and last name are required.");
            }
            if (!NameValidator.validateFirstName(firstName)) {
                return Helper.createErrorResponse(400, "Invalid or missing first name");
            }
            if (!NameValidator.validateLastName(lastName)) {
                return Helper.createErrorResponse(400, "Invalid or missing last name");
            }

            // Validate Base64 string if provided
            if (base64encodedImage != null && !base64encodedImage.isEmpty()) {
                try {
                    byte[] decodedBytes = Base64.getDecoder().decode(base64encodedImage);
                    if (decodedBytes.length < 100) {
                        return Helper.createErrorResponse(400, "Invalid Base64 encoded image data.");
                    }
                    BufferedImage image = ImageIO.read(new ByteArrayInputStream(decodedBytes));
                    if (image == null) {
                        return Helper.createErrorResponse(400, "Invalid Base64 encoded image data.");
                    }
                } catch (IllegalArgumentException e) {
                    return Helper.createErrorResponse(400, "Invalid Base64 encoded image data.");
                } catch (IOException e) {
                    return Helper.createErrorResponse(400, "Failed to process image data.");
                }
            }

            Item existingItem = usersTable.getItem("email", email);
            if (existingItem == null) {
                return Helper.createErrorResponse(404, "User not found.");
            }

            StringBuilder updateExpression = new StringBuilder("SET ");
            ValueMap valueMap = new ValueMap();
            updateExpression.append("firstName = :fn, lastName = :ln");
            valueMap.withString(":fn", firstName)
                    .withString(":ln", lastName);

            if (base64encodedImage != null) {
                updateExpression.append(", imageUrl = :img");
                valueMap.withString(":img", base64encodedImage);
            } else {
                updateExpression.append(", imageUrl = :img");
                valueMap.withString(":img", "");
            }
            usersTable.updateItem(new UpdateItemSpec()
                    .withPrimaryKey("email", email)
                    .withUpdateExpression(updateExpression.toString())
                    .withValueMap(valueMap));

            Index emailIndex = reservationsTable.getIndex("email-index");
            QuerySpec querySpec = new QuerySpec()
                    .withKeyConditionExpression("email = :e")
                    .withValueMap(new ValueMap().withString(":e", email));
            ItemCollection<QueryOutcome> items = emailIndex.query(querySpec);
            List<Map<String, String>> feedbackEntries = new ArrayList<>();
            for (Item item : items) {
                String feedbackId = item.getString("feedbackId");
                String locationId = item.getString("locationId");
                if (feedbackId != null && !feedbackId.isEmpty() && locationId != null && !locationId.isEmpty()) {
                    Map<String, String> entry = new HashMap<>();
                    entry.put("feedbackId", feedbackId);
                    entry.put("locationId", locationId);
                    feedbackEntries.add(entry);
                }
            }

            String userName = firstName + " " + lastName;
            String userAvatarUrl = (base64encodedImage != null && !base64encodedImage.isEmpty()) ? base64encodedImage : "";
            for (Map<String, String> entry : feedbackEntries) {
                String feedbackId = entry.get("feedbackId");
                String locationId = entry.get("locationId");
                ValueMap feedbackValueMap = new ValueMap()
                        .withString(":un", userName)
                        .withString(":ua", userAvatarUrl);
                feedbacksTable.updateItem(new UpdateItemSpec()
                        .withPrimaryKey("feedbackId", feedbackId, "locationId", locationId)
                        .withUpdateExpression("SET userName = :un, userAvatarUrl = :ua")
                        .withValueMap(feedbackValueMap));
            }

            return Helper.createApiResponse(200, Map.of("message", "Profile has been successfully updated"));
        } catch (Exception e) {
            logger.severe("Error updating user profile: " + e.getMessage());
            return Helper.createErrorResponse(500, "Error updating user profile: " + e.getMessage());
        }
    }

    public APIGatewayProxyResponseEvent changePassword(APIGatewayProxyRequestEvent request) {
        try {
            Map<String, Object> claims = Helper.extractClaims(request);
            String email = (String) claims.get("email");

            if (email == null || email.isEmpty()) {
                return Helper.createErrorResponse(401, "Unauthorized: Email not found in token.");
            }

            Map<String, String> requestBody = objectMapper.readValue(request.getBody(), Map.class);
            String oldPassword = requestBody.get("oldPassword");
            String newPassword = requestBody.get("newPassword");

            if (oldPassword == null || newPassword == null) {
                return Helper.createErrorResponse(400, "Old password and new password are required.");
            }

            String accessToken = getAccessToken(email, oldPassword);
            if (accessToken == null) {
                return Helper.createErrorResponse(400, "Incorrect old password");
            }

            if (oldPassword.equals(newPassword)) {
                return Helper.createErrorResponse(400, "New password must be different from the old password");
            }

            if (!PasswordValidator.validatePassword(newPassword)) {
                return Helper.createErrorResponse(400, "New password must be 8-16 characters, include uppercase, lowercase, number, and special character");
            }

            ChangePasswordRequest changePasswordRequest = ChangePasswordRequest.builder()
                    .previousPassword(oldPassword)
                    .proposedPassword(newPassword)
                    .accessToken(accessToken)
                    .build();

            cognitoClient.changePassword(changePasswordRequest);

            Item userItem = usersTable.getItem("email", email);
            if (userItem != null && (userItem.hasAttribute("failedAttempts") || userItem.hasAttribute("lockoutUntil"))) {
                usersTable.updateItem(new UpdateItemSpec()
                        .withPrimaryKey("email", email)
                        .withUpdateExpression("REMOVE failedAttempts, lockoutUntil"));
            }

            return Helper.createApiResponse(200, Map.of("message", "Password has been successfully updated"));
        } catch (InvalidParameterException e) {
            logger.severe("Cognito validation error: " + e.getMessage());
            if (e.getMessage().contains("Invalid Access Token")) {
                return Helper.createErrorResponse(401, "Unauthorized: Invalid access token. Check issuer and audience.");
            }
            return Helper.createErrorResponse(400, e.getMessage());
        } catch (NotAuthorizedException e) {
            logger.severe("Authentication error: " + e.getMessage());
            return Helper.createErrorResponse(400, "Incorrect old password");
        } catch (UserNotFoundException e) {
            logger.severe("User not found error: " + e.getMessage());
            return Helper.createErrorResponse(404, "User not found");
        } catch (Exception e) {
            logger.severe("Error changing password: " + e.getMessage());
            return Helper.createErrorResponse(500, "Error changing password: " + e.getMessage());
        }
    }

    private String getAccessToken(String username, String password) {
        try {
            Map<String, String> authParams = new HashMap<>();
            authParams.put("USERNAME", username);
            authParams.put("PASSWORD", password);

            InitiateAuthRequest authRequest = InitiateAuthRequest.builder()
                    .authFlow(AuthFlowType.USER_PASSWORD_AUTH)
                    .clientId(clientId)
                    .authParameters(authParams)
                    .build();

            InitiateAuthResponse response = cognitoClient.initiateAuth(authRequest);
            return response.authenticationResult().accessToken();
        } catch (Exception e) {
            logger.severe("Error obtaining access token: " + e.getMessage());
            return null;
        }
    }
}