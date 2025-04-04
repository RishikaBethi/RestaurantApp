package com.restaurant.config;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.restaurant.services.SignUpService;
import com.restaurant.services.SignInService;
import com.restaurant.services.ReservationService;
import com.restaurant.services.WaiterService;
import dagger.Module;
import dagger.Provides;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.regions.Region;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Singleton;


@Module
public class ServiceModule {

    private static final String SNS_TOPIC_ARN = System.getenv("SNS_TOPIC_ARN");  // Read from environment variable


    @Provides
    @Singleton
    public CognitoIdentityProviderClient provideCognitoClient() {
        return CognitoIdentityProviderClient.builder()
                .region(Region.of(System.getenv("REGION")))
                .build();
    }

    @Provides
    @Singleton
    public AmazonDynamoDB provideDynamoDBClient() {
        return AmazonDynamoDBClientBuilder.standard()
                .withRegion(System.getenv("REGION"))
                .build();
    }

    @Provides
    @Singleton
    public DynamoDB provideDynamoDB(AmazonDynamoDB amazonDynamoDB) {
        return new DynamoDB(amazonDynamoDB);
    }

    @Provides
    @Singleton
    public SignUpService provideSignUpService(
            CognitoIdentityProviderClient cognitoClient,
            ObjectMapper objectMapper,  // Missing
            DynamoDB dynamoDB,
            String clientId) {
        return new SignUpService(cognitoClient, objectMapper, dynamoDB, clientId);
    }

    @Provides
    @Singleton
    public SignInService provideSignInService(
            CognitoIdentityProviderClient cognitoClient,
            ObjectMapper objectMapper,
            String clientId,
            DynamoDB dynamoDB) {
        return new SignInService(cognitoClient, objectMapper, clientId, dynamoDB);
    }

    // Provide ReservationService Dependency
    @Provides
    @Singleton
    public ReservationService provideReservationService(DynamoDB dynamoDB, WaiterService waiterService) {
        return new ReservationService(dynamoDB, waiterService);
    }

    // Provide WaiterService Dependency
    @Provides
    @Singleton
    public WaiterService provideWaiterService(DynamoDB dynamoDB) {
        return new WaiterService(dynamoDB);
    }

    @Provides
    @Singleton
    public ObjectMapper provideObjectMapper() {
        return new ObjectMapper();
    }


    @Provides
    @Singleton
    public String provideClientId() {
        // Get Cognito client ID from environment variable or configuration
        return System.getenv("COGNITO_CLIENT_ID");
    }
}