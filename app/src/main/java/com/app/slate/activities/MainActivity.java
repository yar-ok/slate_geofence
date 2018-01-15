package com.app.slate.activities;

import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.app.slate.App;
import com.app.slate.R;
import com.app.slate.util.AppUtil;
import com.app.slate.util.GeoFenceException;
import com.app.slate.util.GeofenceManager;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap map;
    private Circle circle;

    private EditText wifiNetworkNameEditText;
    private EditText radiusEditText;
    private Button saveButton;
    private View statusView;

    private final GeofenceManager geofenceManager = GeofenceManager.getGeofenceManager(App.getApp());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);
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
        updateAreaStatus();
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
