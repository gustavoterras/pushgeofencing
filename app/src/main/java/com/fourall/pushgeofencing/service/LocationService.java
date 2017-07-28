package com.fourall.pushgeofencing.service;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.fourall.pushgeofencing.manager.LocationManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by Gustavo on 10/03/2017.
 */

public class LocationService extends Service implements LocationListener, ConnectionCallbacks, OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private static final String TAG = LocationService.class.getSimpleName();

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 50; // 50 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 5; // 5 minutes
    private static final long MIN_TIME_BW_UPDATES_FAST = 1000 * 10; // 60 seconds

    // Declaring a Location Manager
    private android.location.LocationManager locationManager;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;

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

            buildGoogleApiClient();
            createLocationRequest();

            LocationManager.getInstance().setLocation(locationManager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER));
            if (LocationManager.getInstance().getLocation() == null)
                LocationManager.getInstance().setLocation(locationManager.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER));
        }
    }

    /**
     * Creating google api client object
     * */
    private synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        googleApiClient.connect();
    }

    /**
     * Creating location request object
     * */
    private void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(MIN_TIME_BW_UPDATES);
        locationRequest.setFastestInterval(MIN_TIME_BW_UPDATES_FAST);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setSmallestDisplacement(MIN_DISTANCE_CHANGE_FOR_UPDATES);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            locationManager.removeUpdates(this);
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
        }catch (Exception e){
            Log.e(TAG, "onDestroy: ", e);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i("OkHttp", "onLocationChanged");
        LocationManager.getInstance().setLocation(location);
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("ACTION_LOCALE_CHANGED"));
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {}

    @Override
    public void onProviderEnabled(String s) {}

    @Override
    public void onProviderDisabled(String s) {}

    @SuppressWarnings("MissingPermission")
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        LocationManager.getInstance().setLocation(LocationServices.FusedLocationApi.getLastLocation(googleApiClient));
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {}
}
