package com.restaurant.config;

import com.restaurant.RestaurantHandler;
import com.restaurant.services.SignUpService;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = {ServiceModule.class})
public interface AppComponent {
    // Inject dependencies into RestaurantHandler
    void inject(RestaurantHandler handler);
    // For constructor injection, we need to expose the SignUpService
    SignUpService signUpService();
}