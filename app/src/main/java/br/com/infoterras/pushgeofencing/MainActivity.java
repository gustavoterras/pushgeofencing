package br.com.infoterras.pushgeofencing;

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
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import br.com.infoterras.pushgeofencing.manager.LocationManager;
import br.com.infoterras.pushgeofencing.util.PermissionUtil;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int REQUEST_CODE_PERMISSION = 515;
    private List<Location> locations;
    private BroadcastReceiver locationReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initFences();

        if (PermissionUtil.checkLocationPermission(this)) {
            LocationManager.getInstance().startLocationService(this);
            initMaps();
        } else
            requestPermissions();

        locationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().compareTo("ACTION_LOCALE_CHANGED") == 0) {
                    Location location = LocationManager.getInstance().getLocation();
                    float[] distance = new float[2];
                    for (Location l : locations) {
                        Location.distanceBetween(location.getLatitude(), location.getLongitude(), l.getLatitude(), l.getLongitude(), distance);
                        if (distance[0] < 25) {
                            Toast.makeText(context, "Você está próximo ao - " + l.getProvider(), Toast.LENGTH_SHORT).show();
                        } else
                            Log.e("TAG", "distante " + distance[0] + " de " + l.getProvider());
                    }
                }
            }
        };
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
                    .radius(25)
                    .strokeWidth(0f)
                    .fillColor(0x550090FF));
        }
    }

    private void initFences(){

        locations = new ArrayList<>();

        //-30.0271609,-51.1816301
        Location locationValkiria = new Location("VALKIRIA CAFE");
        locationValkiria.setLatitude(-30.0271609);
        locationValkiria.setLongitude(-51.1816301);

        locations.add(locationValkiria);

        //-30.0273448,-51.1825776
        Location locationDCafe = new Location("DCAFE");
        locationDCafe.setLatitude(-30.0273448);
        locationDCafe.setLongitude(-51.1825776);

        locations.add(locationDCafe);
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
}
