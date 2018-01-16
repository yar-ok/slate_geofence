package com.app.slate.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.app.slate.models.AreaLocationInfo;

public class GeofenceManager {

    public static final float DEFAULT_MAPS_ZOOM = 12.6f;

    private static volatile GeofenceManager instanceGeofenceManager;

    @Nullable
    private AreaLocationInfo areaLocationInfo;
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
        areaLocationInfo = getGeoFenceLocationFromStorage(context);
        wifiAccessPointName = getWifiNetworkNameFromStorage(context);
    }

    public void applyGeofenceLocation(@NonNull Context context, @Nullable AreaLocationInfo locationInfo) {
        areaLocationInfo = locationInfo;
        StorageManager.getStorageManager().saveLocation(context, locationInfo);
    }

    @Nullable
    private AreaLocationInfo getGeoFenceLocationFromStorage(@NonNull Context context){
        return StorageManager.getStorageManager().getLocationInfo(context);
    }

    public void applyWifiNetworkName(@NonNull Context context, String wifiPointName){
        wifiAccessPointName = wifiPointName;
        StorageManager.getStorageManager().saveWifiNetwork(context, wifiPointName);
    }

    @Nullable
    private String getWifiNetworkNameFromStorage(@NonNull Context context){
        return StorageManager.getStorageManager().getWifiNetwork(context);
    }

    public boolean isInArea(float currentLatitude, float currentLongitude, @Nullable String currentWifiNetworkName) {
        GeoFenceComparator geoFenceComparator = new GeoFenceComparator(areaLocationInfo,
                currentLatitude, currentLongitude, wifiAccessPointName, currentWifiNetworkName);
        return geoFenceComparator.isInArea();
    }

    @Nullable
    public AreaLocationInfo getAreaLocationInfo() {
        return areaLocationInfo;
    }

    @Nullable
    public String getWifiAccessPointName() {
        return wifiAccessPointName;
    }

    public boolean getUseLocationOption(@NonNull Context context){
        return StorageManager.getStorageManager().getUseLocationOption(context);
    }

    public void saveUseLocationOption(@NonNull Context context, boolean useLocationOption) {
        StorageManager.getStorageManager().saveUseLocationOption(context, useLocationOption);
    }
}
