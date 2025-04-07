package com.restaurant.config;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.services.*;

import dagger.Module;
import dagger.Provides;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.regions.Region;

import javax.inject.Singleton;

@Module
public class ServiceModule {

    private static final String SNS_TOPIC_ARN = System.getenv("SNS_TOPIC_ARN");

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
    public TablesService provideRestaurantService(DynamoDB dynamoDB) {
        return new TablesService(dynamoDB);
    }

    @Provides
    @Singleton
    public GetAllLocationsService provideLocationService(DynamoDB dynamoDB) {
        return new GetAllLocationsService(dynamoDB);
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
    public DishService provideDishService(DynamoDB dynamoDB, ObjectMapper objectMapper) {
        return new DishService(dynamoDB, objectMapper);
    }

//    @Provides
//    @Singleton
//    public DynamoDB provideDynamoDB(AmazonDynamoDB amazonDynamoDB) {
//        return new DynamoDB(amazonDynamoDB);
//    }

    @Provides
    @Singleton
    public FeedbackService provideFeedbackService(DynamoDB dynamoDB, ObjectMapper objectMapper) {
        return new FeedbackService(dynamoDB, objectMapper);
    }

    @Provides
    @Singleton
    public GetReservationService provideGetReservationService(DynamoDB dynamoDB) {
        return new GetReservationService(dynamoDB);
    }

    @Provides
    @Singleton
    public CancelReservationService provideCancelReservationService(DynamoDB dynamoDB) {
        return new CancelReservationService(dynamoDB);
    }

    @Provides
    @Singleton
    public UpdateReservationService provideUpdateReservationService(DynamoDB dynamoDB) {
        return new UpdateReservationService(dynamoDB);
    }

    @Provides
    @Singleton
    public BookingService provideBookingService(DynamoDB dynamoDB, WaiterService waiterService) {
        return new BookingService(dynamoDB, waiterService);
    }

    // Provide WaiterService Dependency
    @Provides
    @Singleton
    public WaiterService provideWaiterService(DynamoDB dynamoDB) {
        return new WaiterService(dynamoDB);
    }
}