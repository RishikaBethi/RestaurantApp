package com.restaurant.config;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.services.*;
import com.restaurant.services.LocationService;
import com.restaurant.services.DishService;

import com.restaurant.services.LocationsService;
import com.restaurant.services.TablesService;
import dagger.Module;
import dagger.Provides;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.regions.Region;

import javax.inject.Singleton;

@Module
public class ServiceModule {

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
    public DynamoDB provideDynamoDB() {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
        return new DynamoDB(client);
    }

    @Provides
    @Singleton
    public ObjectMapper provideObjectMapper() {
        return new ObjectMapper();
    }

    @Provides
    @Singleton
    public TablesService provideRestaurantService(DynamoDB dynamoDB, ObjectMapper objectMapper) {
        return new TablesService(dynamoDB, objectMapper);
    }

    @Provides
    @Singleton
    public LocationsService provideLocationService(DynamoDB dynamoDB, ObjectMapper objectMapper) {
        return new LocationsService(dynamoDB, objectMapper);
    }

    @Provides
    @Singleton
    public String provideClientId() {
        return System.getenv("COGNITO_CLIENT_ID");
    }

    @Provides
    @Singleton
    public SignUpService provideSignUpService(CognitoIdentityProviderClient cognitoClient,
                                              ObjectMapper objectMapper,
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

    @Provides
    @Singleton
    public  DishService provideDishService(DynamoDB dynamoDB, ObjectMapper objectMapper){
        return new DishService(dynamoDB,objectMapper);
    }

    @Provides
    @Singleton
    public  FeedbackService provideFeedbackService(DynamoDB dynamoDB, ObjectMapper objectMapper){
        return new FeedbackService(dynamoDB,objectMapper);
    }

}
