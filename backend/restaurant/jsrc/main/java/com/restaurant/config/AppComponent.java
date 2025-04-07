package com.restaurant.config;

import com.restaurant.RestaurantHandler;
import com.restaurant.services.*;
import com.restaurant.services.SignUpService;
import com.restaurant.services.SignInService;
import com.restaurant.services.ReservationService;
import com.restaurant.services.WaiterService;

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
    TablesService getTablesService();
    LocationsService getLocationsService();
    ReservationService reservationService();
    WaiterService waiterService();

    @Component.Builder
    interface Builder {
        Builder serviceModule(ServiceModule module); // Allow passing ServiceModule
        AppComponent build();
    }
}