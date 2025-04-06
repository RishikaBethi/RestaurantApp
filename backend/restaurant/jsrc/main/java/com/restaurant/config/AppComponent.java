package com.restaurant.config;

import com.restaurant.RestaurantHandler;
import com.restaurant.services.SignUpService;
import com.restaurant.services.SignInService;
import com.restaurant.services.GetReservationService;
import com.restaurant.services.CancelReservationService;
import com.restaurant.services.UpdateReservationService;
import com.restaurant.services.BookingService;
import com.restaurant.services.WaiterService;

import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = {ServiceModule.class})
public interface AppComponent {
    void inject(RestaurantHandler handler);

    SignUpService signUpService();
    SignInService signInService();
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