package br.com.infoterras.pushgeofencing.manager;

import android.content.Context;
import android.content.Intent;
import android.location.Location;

import br.com.infoterras.pushgeofencing.service.LocationService;

/**
 * Created by Gustavo on 10/03/2017.
 */

public class LocationManager {

    private static final String TAG = LocationManager.class.getSimpleName();

    private static LocationManager instance = null;

    private Location location;

    public boolean hasLocation(){
        return location != null;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public synchronized static LocationManager getInstance() {
        if (instance == null)
            instance = new LocationManager();
        return instance;
    }

    public void startLocationService(Context context){
        context.startService(new Intent(context, LocationService.class));
    }

    public void stopLocationService(Context context){
        context.stopService(new Intent(context, LocationService.class));
    }
}
