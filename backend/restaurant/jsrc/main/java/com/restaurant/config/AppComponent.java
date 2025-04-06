package com.restaurant.config;

import com.restaurant.RestaurantHandler;
import com.restaurant.services.*;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = {ServiceModule.class})
public interface AppComponent {
    void inject(RestaurantHandler handler);
    SignUpService signUpService();
    SignInService signInService();
    LocationService locationService();
    DishService dishService();
    FeedbackService feedbackService();
}