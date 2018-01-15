package com.app.slate.activities;

import android.Manifest;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.app.slate.App;
import com.app.slate.R;
import com.app.slate.util.AppUtil;
import com.app.slate.util.GeoFenceException;
import com.app.slate.util.GeofenceManager;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSIONS_REQUEST_CODE = 80;
    private static final int REQUEST_CHECK_SETTINGS = 90;
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    private static final String TAG = MainActivity.class.getSimpleName();

    private GoogleMap map;
    private Circle circle;

    private EditText wifiNetworkNameEditText;
    private EditText radiusEditText;
    private Button saveButton;
    private View statusView;

    private final GeofenceManager geofenceManager = GeofenceManager.getGeofenceManager(App.getApp());
    private FusedLocationProviderClient fusedLocationProviderClient;
    private SettingsClient settingsClient;
    private LocationRequest locationRequest;
    private LocationSettingsRequest locationSettingsRequest;
    private LocationCallback locationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        settingsClient = LocationServices.getSettingsClient(this);
        wifiNetworkNameEditText = findViewById(R.id.wifi_network_name_edit_text);
        radiusEditText = findViewById(R.id.radius_edit_text);
        statusView = findViewById(R.id.status_view);
        saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String radiusText = radiusEditText.getText().toString();
                if (TextUtils.isEmpty(radiusText)) {
                    App.getApp().showToast(R.string.empty_radius_field_error);
                    return;
                }
                try {
                    float radius = Float.parseFloat(radiusText);
                    geofenceManager.applyGeofenceRadius(MainActivity.this, radius);
                    String wifiName = wifiNetworkNameEditText.getText().toString();
                    geofenceManager.applyWifiNetworkName(MainActivity.this, wifiName);
                    App.getApp().showToast(R.string.saved_settings_message);
                    updateAreaStatus();
                } catch (NumberFormatException e) {
                    App.getApp().showToast(R.string.proces_radius_error);
                } catch (GeoFenceException e) {
                    App.getApp().showToast(e.getMessage());
                }
            }
        });

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
            }
        };

       // updateAreaStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isNeedCheckPermissions()) {
            requestLocationPermissions();
        } else  {
            startLocationUpdates();
        }
    }

    private void requestLocationPermissions() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSIONS_REQUEST_CODE);
    }

    private void checkLocationRequest() {
        if (locationRequest == null) {
            locationRequest = new LocationRequest();
            locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
            locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }
    }

    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        locationSettingsRequest = builder.build();
    }

    private void startLocationUpdates() {
        // Begin by checking if the device has the necessary location settings.
        checkLocationRequest();
        settingsClient.checkLocationSettings(locationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {

                        //noinspection MissingPermission
                        try {
                            fusedLocationProviderClient.requestLocationUpdates(locationRequest,
                                    locationCallback, Looper.myLooper());
                        }catch (SecurityException e){

                        }

                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                App.getApp().showToast("Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.");
                               break;
                        }
                    }
                });
    }

    private boolean isNeedCheckPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void updateAreaStatus() {
        String activeWifiNetwork = AppUtil.getWifiActiveSsid(App.getApp());
        boolean isAInArea = geofenceManager.isInArea(1000, activeWifiNetwork);
        drawViewByAreaStatus(isAInArea);
    }

    public void drawViewByAreaStatus(boolean isInArea) {
        if (isInArea) {
            statusView.setBackgroundResource(R.drawable.inside_area_shape);
        } else {
            statusView.setBackgroundResource(R.drawable.outside_area_shape);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if (circle != null) {
                    circle.remove();
                }
                circle = map.addCircle(new CircleOptions()
                        .center(latLng)
                        .radius(GeofenceManager.DEFAULT_GEOFENCE_RADIUS)
                        .fillColor(Color.BLUE));
            }
        });
    }
}
