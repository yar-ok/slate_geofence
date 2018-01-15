package com.app.slate.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class GeofenceManager {

    public static final float DEFAULT_GEOFENCE_RADIUS = 10000;
    public static final float MINIMAL_GEOFENCE_RADIUS = 5000;

    private static volatile GeofenceManager instanceGeofenceManager;

    private float radius;
    @Nullable
    private String wifiAccessPointName;

    public static GeofenceManager getGeofenceManager(@NonNull Context context) {
        GeofenceManager localGeofenceManager = instanceGeofenceManager;
        if (localGeofenceManager == null) {
            synchronized (GeofenceManager.class) {
                localGeofenceManager = instanceGeofenceManager;
                if (localGeofenceManager == null) {
                    localGeofenceManager = instanceGeofenceManager = new GeofenceManager(context);
                }
            }
        }
        return localGeofenceManager;
    }

    private GeofenceManager(@NonNull Context context) {
        radius = getGeoFenceRadiusFromStorage(context);
        wifiAccessPointName = getWifiNetworkNameFromStorage(context);
    }

    public void applyGeofenceRadius(@NonNull Context context, float radius) throws GeoFenceException{
        if (radius > MINIMAL_GEOFENCE_RADIUS) {
            StorageManager.getStorageManager().saveRadius(context, radius);
            this.radius = radius;
        } else {
            throw new GeoFenceException("Radius must be greater or equal "+MINIMAL_GEOFENCE_RADIUS);
        }
    }

    private float getGeoFenceRadiusFromStorage(@NonNull Context context){
        return StorageManager.getStorageManager().getRadius(context);
    }

    public void applyWifiNetworkName(@NonNull Context context, String wifiPointName){
        wifiAccessPointName = wifiPointName;
        StorageManager.getStorageManager().saveWifiNetwork(context, wifiPointName);
    }

    @Nullable
    private String getWifiNetworkNameFromStorage(@NonNull Context context){
        return StorageManager.getStorageManager().getWifiNetwork(context);
    }

    public boolean isInArea(float distanceCenterPointArea, @Nullable String currentWifiNetworkName) {
        GeoFenceComparator geoFenceComparator = new GeoFenceComparator(radius, wifiAccessPointName, distanceCenterPointArea, currentWifiNetworkName);
        return geoFenceComparator.isInArea();
    }

}
