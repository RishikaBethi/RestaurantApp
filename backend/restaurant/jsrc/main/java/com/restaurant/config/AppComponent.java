package com.restaurant.config;

import com.restaurant.RestaurantHandler;
import com.restaurant.services.SignInService;
import com.restaurant.services.SignUpService;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = {ServiceModule.class})
public interface AppComponent {
    void inject(RestaurantHandler handler);
    SignUpService signUpService();
    SignInService signInService();
}