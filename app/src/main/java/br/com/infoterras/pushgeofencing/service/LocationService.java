package br.com.infoterras.pushgeofencing.service;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;

import br.com.infoterras.pushgeofencing.manager.LocationManager;

/**
 * Created by Gustavo on 10/03/2017.
 */

public class LocationService extends Service implements LocationListener {

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 50; //50 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60; //60 seconds

    // Declaring a Location Manager
    protected android.location.LocationManager locationManager;

    @Override
    public void onCreate() {
        super.onCreate();

        locationManager = (android.location.LocationManager) getSystemService(LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            locationManager.requestLocationUpdates(
                    android.location.LocationManager.GPS_PROVIDER,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES,
                    this);

            locationManager.requestLocationUpdates(
                    android.location.LocationManager.NETWORK_PROVIDER,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES,
                    this);

            LocationManager.getInstance().setLocation(locationManager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER));
            if (LocationManager.getInstance().getLocation() == null)
                LocationManager.getInstance().setLocation(locationManager.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        LocationManager.getInstance().setLocation(location);
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("ACTION_LOCALE_CHANGED"));
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {}

    @Override
    public void onProviderEnabled(String s) {}

    @Override
    public void onProviderDisabled(String s) {}
}
