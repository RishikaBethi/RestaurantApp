package com.restaurant.utils;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import java.time.LocalTime;


public class Helper {

    private static final Logger logger = Logger.getLogger(Helper.class.getName());
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Creates a successful API response.
     */
    public static APIGatewayProxyResponseEvent createApiResponse(int statusCode, Object body) {
        try {
            String responseBody;

            // Avoid serializing again if body is already a string or org.json.JSONArray
            if (body instanceof String) {
                responseBody = (String) body;
            } else if (body instanceof org.json.JSONArray || body instanceof org.json.JSONObject) {
                responseBody = body.toString();
            } else {
                responseBody = objectMapper.writeValueAsString(body);
            }

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(statusCode)
                    .withBody(responseBody)
                    .withHeaders(createCorsHeaders());

        } catch (Exception e) {
            logger.severe("Error serializing API response: " + e.getMessage());
            return createErrorResponse(500, "Error serializing response");
        }
    }

    /**
     * Creates an error response with a message.
     */
    public static APIGatewayProxyResponseEvent createErrorResponse(int statusCode, String message) {
        return createApiResponse(statusCode, Map.of("error", message));
    }

    /**
     * Extracts user claims from the JWT token in the request headers.
     */
    public static Map<String, Object> extractClaims(APIGatewayProxyRequestEvent request) {
        try {
            String authorizationHeader = request.getHeaders().get("Authorization");

            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                logger.warning("Authorization header is missing or invalid");
                return Map.of();
            }

            String token = authorizationHeader.substring(7);
            String[] tokenParts = token.split("\\.");

            if (tokenParts.length != 3) {
                logger.warning("Invalid JWT token format");
                return Map.of();
            }

            String payload = decodeBase64(tokenParts[1]);

            if (payload == null || payload.isEmpty()) {
                logger.warning("JWT payload is empty or invalid");
                return Map.of();
            }

            Map<String, Object> claims = objectMapper.readValue(payload, Map.class);

//            if (claims.containsKey("exp")) {
//                Object expObj = claims.get("exp");
//                if (expObj instanceof Number) {
//                    long expirationTimestamp = ((Number) expObj).longValue();
//                    long currentTimestamp = Instant.now().getEpochSecond(); // Current time in seconds
//
//                    if (currentTimestamp > expirationTimestamp) {
//                        logger.warning("JWT token has expired. Expiration: " + expirationTimestamp +
//                                ", Current time: " + currentTimestamp);
//                        return Map.of(); // Token expired, return empty map
//                    }
//                } else {
//                    logger.warning("JWT 'exp' claim is present but not a valid number");
//                    return Map.of();
//                }
//            } else {
//                logger.info("No 'exp' claim found in JWT; assuming no expiration check required");
//            }
            logger.info("Extracted Claims: " + claims);
            return claims;

        } catch (Exception e) {
            logger.severe("Error extracting claims: " + e.getMessage());
            return Map.of();
        }
    }

    /**
     * Decodes a Base64-encoded JWT payload safely.
     */
    private static String decodeBase64(String encoded) {
        try {
            return new String(Base64.getUrlDecoder().decode(encoded), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            logger.severe("Error decoding Base64 JWT payload: " + e.getMessage());
            return null;
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
}
