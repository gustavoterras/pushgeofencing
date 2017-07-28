package com.fourall.pushgeofencing;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.fourall.pushgeofencing.manager.LocationManager;
import com.fourall.pushgeofencing.network.PushNotificationService;
import com.fourall.pushgeofencing.util.PermissionUtil;
import com.fourall.pushgeofencing.util.PreferencesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import fourall.com.pay_lib.FourAllPayment;
import fourall.com.pay_lib.FourAll_LoginCallback;
import fourall.com.pay_lib.FourAll_SessionToken;

public class MainActivity extends AppCompatActivity implements PushNotificationService.OnTaskCompleted, OnMapReadyCallback, View.OnClickListener{

    private PushNotificationService pushNotificationService;
    private static final int REQUEST_CODE_PERMISSION = 515;
    private List<Location> locations = new ArrayList<>();
    private BroadcastReceiver locationReceiver;
    private Timer timer = new Timer();
    private GoogleMap googleMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (PermissionUtil.checkLocationPermission(this)) {
            LocationManager.getInstance().startLocationService(this);
            initMaps();
        } else
            requestPermissions();

        locationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, Intent intent) {
                if(intent.getAction().compareTo("ACTION_LOCALE_CHANGED") == 0) {
                    Location location = LocationManager.getInstance().getLocation();
                    float[] distance = new float[2];
                    for (final Location l : locations) {
                        Location.distanceBetween(location.getLatitude(), location.getLongitude(), l.getLatitude(), l.getLongitude(), distance);
                        if (distance[0] < l.getExtras().getInt("radius") && !l.getExtras().getBoolean("pushed")) {

                            final Map<String, Object> body = new HashMap<>();

                            try {
                                body.put("venue", l.getExtras().getInt("id"));
                                body.put("customers", Collections.singletonList(FourAllPayment.getPersistedUser().getInt("customerId")));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            if (timer != null) {
                                timer.cancel();
                                timer = new Timer();
                                timer.schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        pushNotificationService.sendPushNotifications(body, 2);
                                        l.getExtras().putBoolean("pushed", true);
                                    }
                                }, 1000 * l.getExtras().getInt("stay"));

                                Toast.makeText(context, "entrou na fence "+ l.getProvider() + ", tempo permanencia " + l.getExtras().getInt("stay") / 60 + "min", Toast.LENGTH_SHORT).show();
                                l.getExtras().putBoolean("isFenced", true);
                            }

                        } else {
                            if(l.getExtras().getBoolean("isFenced")) {
                                Toast.makeText(context, "saiu da fence " + l.getProvider(), Toast.LENGTH_SHORT).show();
                                l.getExtras().putBoolean("isFenced", false);
                                timer.cancel();
                            }
                            Log.e("OkHttp", "distante " + distance[0] + " de " + l.getProvider());
                        }
                    }
                }
            }
        };

        pushNotificationService = new PushNotificationService();
        pushNotificationService.setOnTaskCompleted(this);
        pushNotificationService.getLocations(-1);
    }

    private void initMaps(){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void requestPermissions() {
        int fineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int coarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        List<String> listPermissionsNeeded = new ArrayList<>();

        if (fineLocationPermission != PackageManager.PERMISSION_GRANTED)
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);

        if (coarseLocationPermission != PackageManager.PERMISSION_GRANTED)
            listPermissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION);

        if (!listPermissionsNeeded.isEmpty())
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_CODE_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSION && PermissionUtil.checkLocationPermission(this)) {
            LocationManager.getInstance().startLocationService(this);

            initMaps();
        }
    }

    @Override
    public void onMapReady(final GoogleMap map) {

        googleMap = map;

        CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(-30.0273404, -51.1820533)).zoom(18).build();
        CameraUpdate update = CameraUpdateFactory.newCameraPosition(cameraPosition);
        map.animateCamera(update);

        if(PermissionUtil.checkLocationPermission(this))
            map.setMyLocationEnabled(true);

        for (Location location : locations) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            map.addMarker(new MarkerOptions().position(latLng).title(location.getProvider()));

            map.addCircle(new CircleOptions()
                    .center(latLng)
                    .radius(location.getExtras().getInt("radius"))
                    .strokeWidth(0f)
                    .fillColor(0x550090FF));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(locationReceiver, new IntentFilter("ACTION_LOCALE_CHANGED"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(locationReceiver);
    }

    @Override
    public void onClick(View v) {
        if(FourAllPayment.isLogged())
            sendPushIdentities(MainActivity.this);
        else {
            try {
                FourAllPayment.login(this, new FourAll_LoginCallback() {
                    @Override
                    public void onLogin(String s, String s1, String s2) {
                        sendPushIdentities(MainActivity.this);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Envia o identificador do push
     * @param context context
     */
    public void sendPushIdentities(Context context){
        HashMap<String, Object> payload = new HashMap<>();

        payload.put("pushToken", PreferencesUtil.getPreferenceValue(context, "tokenFireBase", ""));
        payload.put("operatingSystem", 2); //2 = Android

        if(FourAllPayment.isLogged())
            payload.put("sessionToken", FourAll_SessionToken.getSessionToken());

        Location location = LocationManager.getInstance().getLocation();

        payload.put("latitude", String.valueOf(location != null ? location.getLatitude() : 0));
        payload.put("longitude", String.valueOf(location != null ? location.getLongitude() : 0));

        pushNotificationService.sendPushIdentities(payload);
    }

    @Override
    public void onSuccess(Object response, int code, int requestCode) {
        Toast.makeText(this, "onSuccess", Toast.LENGTH_SHORT).show();

        if(code == 200){

            JsonArray jsonArray = ((JsonObject) response).get("data").getAsJsonArray();

            for (int i = 0; i < jsonArray.size(); i++) {

                JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();

                Location location = new Location(jsonObject.get("name").getAsString());
                location.setLatitude(jsonObject.get("latitude").getAsDouble());
                location.setLongitude(jsonObject.get("longitude").getAsDouble());

                Bundle bundle = new Bundle();
                bundle.putString("description", jsonObject.get("description").getAsString());
                bundle.putInt("radius", jsonObject.get("radius").getAsInt());
                bundle.putInt("stay", jsonObject.get("stay").getAsInt());
                bundle.putInt("id", jsonObject.get("id").getAsInt());
                bundle.putBoolean("isFenced", false);
                bundle.putBoolean("pushed", false);

                location.setExtras(bundle);

                locations.add(location);
            }

            onMapReady(googleMap);
        }
    }

    @Override
    public void onFailure(Throwable error, int requestCode) {
        Toast.makeText(this, "onFailure", Toast.LENGTH_SHORT).show();
    }
}
