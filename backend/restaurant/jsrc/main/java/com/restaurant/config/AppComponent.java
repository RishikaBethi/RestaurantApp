package com.restaurant.config;

import com.restaurant.ReportHandler;
import com.restaurant.ReportsSenderHandler;
import com.restaurant.RestaurantHandler;
import com.restaurant.services.*;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = {ServiceModule.class})
public interface AppComponent {
    void inject(RestaurantHandler handler);
    void inject(ReportHandler reportHandler);
    void inject(ReportsSenderHandler reportsSenderHandler);
    SignUpService signUpService();
    SignInService signInService();
    LocationService locationService();
    DishService dishService();
    FeedbackService feedbackService();
    TablesService getTablesService();
    GetAllLocationsService getLocationsService();
    GetReservationService getReservationService();
    CancelReservationService cancelReservationService();
    UpdateReservationService updateReservationService();
    WaiterService waiterService();
    BookingService bookingService();
    GetReportsService getReportsService();

    @Component.Builder
    interface Builder {
        Builder serviceModule(ServiceModule module); // Allow passing ServiceModule
        AppComponent build();
    }
}