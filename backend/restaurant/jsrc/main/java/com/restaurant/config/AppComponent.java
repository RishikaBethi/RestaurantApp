package com.restaurant.config;

import com.restaurant.RestaurantHandler;
import com.restaurant.services.auth.SignUpService;
import com.restaurant.services.auth.SignInService;
import com.restaurant.services.reservations.GetReservationService;
import com.restaurant.services.reservations.CancelReservationService;
import com.restaurant.services.reservations.UpdateReservationService;
import com.restaurant.services.bookings.BookingService;
import com.restaurant.services.waiters.WaiterService;

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