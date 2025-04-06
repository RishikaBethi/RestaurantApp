package com.restaurant.config;

//import com.google.gson.Gson;
import com.restaurant.RestaurantHandler;
import com.restaurant.services.*;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = {ServiceModule.class})
public interface AppComponent {
    void inject(RestaurantHandler handler);
     LocationService locationService();
     DishService dishService();
     FeedbackService feedbackService();
}