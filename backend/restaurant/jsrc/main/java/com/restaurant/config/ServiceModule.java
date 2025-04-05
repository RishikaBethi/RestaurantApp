package com.restaurant.config;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.services.LocationsService;
import com.restaurant.services.TablesService;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module
public class ServiceModule {

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
}
