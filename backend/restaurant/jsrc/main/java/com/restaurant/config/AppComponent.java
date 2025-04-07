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
    TablesService getTablesService();
    LocationsService getLocationsService();
    GetReservationService getReservationService();
    CancelReservationService cancelReservationService();
    UpdateReservationService updateReservationService();
    WaiterService waiterService();
    BookingService bookingService();

    @Component.Builder
    interface Builder {
        Builder serviceModule(ServiceModule module); // Allow passing ServiceModule
        AppComponent build();
    }
}