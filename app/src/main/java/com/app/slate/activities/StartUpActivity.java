package com.app.slate.activities;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.app.slate.App;
import com.app.slate.R;
import com.app.slate.models.AreaLocationInfo;
import com.app.slate.util.AppUtil;
import com.app.slate.util.GeofenceManager;
import com.app.slate.util.StorageManager;
import com.app.slate.views.AreaOverlayView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

public class StartUpActivity extends AppCompatActivity implements OnMapReadyCallback {

    private boolean isMapAreaReady;

    private AreaOverlayView areaOverlayView;
    @Nullable
    private GoogleMap googleMap;
    @NonNull
    private final GeofenceManager geofenceManager = GeofenceManager.getGeofenceManager(App.getApp());

    private EditText wifiNetworkNameEditText;
    private CheckBox locationAreaCheckbox;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_up_layout);
        areaOverlayView = findViewById(R.id.area_overlay_view);
        locationAreaCheckbox = findViewById(R.id.location_area_checkbox);
        Toolbar toolbar = findViewById(R.id.toolbar);
        wifiNetworkNameEditText = findViewById(R.id.wifi_network_name_edit_text);
        setSupportActionBar(toolbar);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);
        updateSsidViewInfo();
        updateUseLocationViewInfo();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        updateMapBySettings();
        googleMap.setMinZoomPreference(googleMap.getMaxZoomLevel()/4);
        isMapAreaReady = true;
        invalidateOptionsMenu();
        areaOverlayView.setVisibility(View.VISIBLE);
    }

    private void updateSsidViewInfo(){
        if (wifiNetworkNameEditText != null) {
            String wifiAccessPointName = geofenceManager.getWifiAccessPointName();
            wifiNetworkNameEditText.setText(wifiAccessPointName);
            wifiNetworkNameEditText.setSelection(wifiNetworkNameEditText.getText().length());
        }
    }

    private void updateUseLocationViewInfo() {
        if (locationAreaCheckbox != null) {
            boolean isUse = StorageManager.getStorageManager().getUseLocationOption(this);
            locationAreaCheckbox.setChecked(isUse);
        }
    }

    private void updateMapBySettings(){
        AreaLocationInfo areaLocationInfo = geofenceManager.getAreaLocationInfo();
        if (googleMap != null && areaLocationInfo != null) {
            LatLng latLng = new LatLng(areaLocationInfo.getLatitude(), areaLocationInfo.getLongitude());
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(areaLocationInfo.getZoom()));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_done, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.item_done);
        if (item != null) {
            item.setEnabled(isMapAreaReady);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_done:
                saveUserAreaSettings();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveUserAreaSettings() {
        boolean isUseLocation = locationAreaCheckbox.isChecked();
        if (googleMap == null && isUseLocation) {
            App.getApp().showToast(R.string.area_info_location_save_error);
           return;
        }
        if (isUseLocation) {
            LatLng center = googleMap.getProjection().fromScreenLocation(
                    new Point(areaOverlayView.getCenterX(), areaOverlayView.getCenterY()));
            LatLng right = googleMap.getProjection().fromScreenLocation(
                    new Point(areaOverlayView.getCenterX() + Math.round(areaOverlayView.getRadius()),
                            areaOverlayView.getCenterY()));
            double radius = AppUtil.distanceBetweenPoints((float) center.latitude, (float) center.longitude, (float) right.latitude, (float) right.longitude);
            AreaLocationInfo areaLocationInfo = new AreaLocationInfo((float) radius, (float) center.latitude, (float) center.longitude);
            areaLocationInfo.setZoom(googleMap.getCameraPosition().zoom);
            geofenceManager.applyGeofenceLocation(this, areaLocationInfo);
        } else {
            geofenceManager.applyGeofenceLocation(this, null);
        }
        StorageManager.getStorageManager().saveUseLocationOption(this, isUseLocation);
        String wifiName = wifiNetworkNameEditText.getText().toString();
        geofenceManager.applyWifiNetworkName(this, wifiName);
        App.getApp().showToast(R.string.saved_settings_message);
        startActivity(new Intent(this, MainActivity.class));
    }
}
