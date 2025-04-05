package com.restaurant.config;

import com.restaurant.RestaurantHandler;
import com.restaurant.services.LocationsService;
import com.restaurant.services.TablesService;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = {ServiceModule.class})
public interface AppComponent {
    void inject(RestaurantHandler app);
    TablesService getTablesService();
    LocationsService getLocationsService();
}
