package com.app.slate.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.app.slate.App;
import com.app.slate.R;
import com.app.slate.models.AreaLocationInfo;
import com.app.slate.util.AppDialogBuilder;
import com.app.slate.util.AppUtil;
import com.app.slate.util.GeofenceManager;
import com.app.slate.views.AreaOverlayView;
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

    private GoogleMap map;

    private View statusView;

    private final GeofenceManager geofenceManager = GeofenceManager.getGeofenceManager(App.getApp());
    private FusedLocationProviderClient fusedLocationProviderClient;
    private SettingsClient settingsClient;
    private LocationRequest locationRequest;
    private LocationSettingsRequest locationSettingsRequest;
    private LocationCallback locationCallback;
    private Location currentLocation;
    private AlertDialog permissionAlertDialog;
    private boolean isNeedEnableLocation = true;
    private boolean isNeedMoveCamera = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        settingsClient = LocationServices.getSettingsClient(this);
        statusView = findViewById(R.id.status_view);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                currentLocation = locationResult.getLastLocation();
                updateAreaStatus();
                checkMoveCamera();
            }
        };
        updateAreaStatus();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(networkStateReceiver, filter);
    }

    private void checkMoveCamera(){
        if(currentLocation == null || map == null || !isNeedMoveCamera){
            return;
        }
        isNeedMoveCamera = false;
        AreaLocationInfo areaLocationInfo = GeofenceManager.getGeofenceManager(MainActivity.this).getAreaLocationInfo();
        if (areaLocationInfo == null) {
            AppUtil.moveCameraByAreaLocationInfo(map,
                    new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                    GeofenceManager.DEFAULT_MAPS_ZOOM);
        } else {
            AppUtil.moveCameraByAreaLocationInfo(map,
                    new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                    areaLocationInfo.getRadius());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isNeedEnableLocation) {
            if (isNeedCheckPermissions()) {
                requestLocationPermissions();
            } else {
                startLocationUpdates();
            }
        } else {
            if (permissionAlertDialog == null || !permissionAlertDialog.isShowing()) {
                finish();
            }
        }
    }

    private BroadcastReceiver networkStateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
           updateAreaStatus();
        }
    };

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
        if (locationSettingsRequest == null) {
            checkLocationRequest();
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
            builder.addLocationRequest(locationRequest);
            locationSettingsRequest = builder.build();
        }
    }

    private void startLocationUpdates() {
        buildLocationSettingsRequest();
        settingsClient.checkLocationSettings(locationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        try {
                            fusedLocationProviderClient.requestLocationUpdates(locationRequest,
                                    locationCallback, Looper.myLooper());
                            enableUserLocation();
                        } catch (SecurityException e){
                            App.getApp().showToast(R.string.location_security_excetion_toast);
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof ApiException) {
                            int statusCode = ((ApiException) e).getStatusCode();
                            switch (statusCode) {
                                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                    try {
                                        ResolvableApiException rae = (ResolvableApiException) e;
                                        rae.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                                    } catch (IntentSender.SendIntentException sie) {
                                        App.getApp().showToast(R.string.location_settings_available_error);
                                    }
                                    break;
                                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                    App.getApp().showToast(R.string.fix_location_settings_manually_message);
                                    break;
                            }
                        }  else {
                            App.getApp().showToast(R.string.location_settings_available_error);
                        }
                    }
                });
    }

    private boolean isNeedCheckPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        return permissionState != PackageManager.PERMISSION_GRANTED;
    }

    private void updateAreaStatus() {
        String activeWifiNetwork = AppUtil.getWifiActiveSsid(App.getApp());
        boolean isAInArea;
        if (currentLocation != null) {
            isAInArea = geofenceManager.isInArea((float) currentLocation.getLatitude(), (float) currentLocation.getLongitude(), activeWifiNetwork);
        } else {
            isAInArea = geofenceManager.isInArea(Float.MAX_VALUE, Float.MAX_VALUE, activeWifiNetwork);
        }
        drawViewByAreaStatus(isAInArea);
    }

    public void drawViewByAreaStatus(boolean isInArea) {
        if (isInArea) {
            statusView.setBackgroundResource(R.drawable.inside_area_shape);
        } else {
            statusView.setBackgroundResource(R.drawable.outside_area_shape);
        }
    }

    private void enableUserLocation() {
        if (map != null) {
            try {
                map.setMyLocationEnabled(true);
            } catch (SecurityException ignore) {

            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.clear();
        enableUserLocation();
        AppUtil.updateMapMinZoom(map);
        if (geofenceManager.getUseLocationOption(MainActivity.this)) {
            AreaLocationInfo areaLocationInfo = GeofenceManager.getGeofenceManager(MainActivity.this).getAreaLocationInfo();
            if (areaLocationInfo != null) {
                LatLng latLng = new LatLng(areaLocationInfo.getLatitude(), areaLocationInfo.getLongitude());
                map.addCircle(new CircleOptions()
                        .center(latLng)
                        .radius(areaLocationInfo.getRadius())
                        .strokeWidth(AreaOverlayView.OVERLAY_STROKE_SIZE)
                        .strokeColor(AreaOverlayView.OVERLAY_STROKE_COLOR)
                        .fillColor(AreaOverlayView.OVERLAY_AREA_COLOR));
            }
        }
        checkMoveCamera();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                processOnDeniedPermission();
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                processOnDeniedPermission();
            }
        }
    }

    private void processOnDeniedPermission() {
        isNeedEnableLocation = false;
        if (permissionAlertDialog != null && permissionAlertDialog.isShowing()) {
            permissionAlertDialog.dismiss();
        }
        permissionAlertDialog = new AppDialogBuilder().showInfoDialog(this, getString(R.string.attention),
                getString(R.string.denided_permission_message), getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkStateReceiver);
    }

}
