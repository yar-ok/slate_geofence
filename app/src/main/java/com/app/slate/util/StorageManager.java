package com.app.slate.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.app.slate.models.AreaLocationInfo;

class StorageManager {

    private static final String PREF_FILE_NAME = "pref_settings";
    private static final String GEO_FENCE_RADIUS_KEY = "radius";
    private static final String GEO_FENCE_LATITUDE_KEY = "latitude";
    private static final String GEO_FENCE_LONGITUDE_KEY = "longitude";
    private static final String GEO_FENCE_ZOOM_KEY = "zoom";
    private static final String WIFI_KEY = "wifi";
    private static final String USE_LOCATION_OPTION_KEY = "use_location";
    private static volatile StorageManager instanceStorageManager;

    public static StorageManager getStorageManager() {
        StorageManager localStorageManager = instanceStorageManager;
        if (localStorageManager == null) {
            synchronized (StorageManager.class) {
                localStorageManager = instanceStorageManager;
                if (localStorageManager == null) {
                    localStorageManager = instanceStorageManager = new StorageManager();
                }
            }
        }
        return localStorageManager;
    }

    private StorageManager() {
    }

    @Nullable
    public AreaLocationInfo getLocationInfo(@NonNull Context context) {
        float radius = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE).getFloat(GEO_FENCE_RADIUS_KEY, Float.MIN_VALUE);
        float latitude = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE).getFloat(GEO_FENCE_LATITUDE_KEY, Float.MIN_VALUE);
        float longitude = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE).getFloat(GEO_FENCE_LONGITUDE_KEY, Float.MIN_VALUE);
        float zoom = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE).getFloat(GEO_FENCE_ZOOM_KEY, Float.MIN_VALUE);
        if (radius == Float.MIN_VALUE || latitude == Float.MIN_VALUE || longitude == Float.MIN_VALUE || zoom == Float.MIN_VALUE) {
            return null;
        }
        AreaLocationInfo locationInfo = new AreaLocationInfo(radius, latitude, longitude);
        locationInfo.setZoom(zoom);
        return locationInfo;
    }

    @Nullable
    public String getWifiNetwork(@NonNull Context context){
        return context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE).getString(WIFI_KEY, null);
    }

    public boolean getUseLocationOption(@NonNull Context context){
        return context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE).getBoolean(USE_LOCATION_OPTION_KEY, true);
    }

    public void saveLocation(@NonNull Context context, @Nullable AreaLocationInfo locationInfo) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE).edit();
        if (locationInfo == null) {
            editor.putFloat(GEO_FENCE_RADIUS_KEY, Float.MIN_VALUE).apply();
            editor.putFloat(GEO_FENCE_LATITUDE_KEY, Float.MIN_VALUE).apply();
            editor.putFloat(GEO_FENCE_LONGITUDE_KEY, Float.MIN_VALUE).apply();
            editor.putFloat(GEO_FENCE_ZOOM_KEY, Float.MIN_VALUE).apply();
        } else {
            editor.putFloat(GEO_FENCE_RADIUS_KEY, locationInfo.getRadius()).apply();
            editor.putFloat(GEO_FENCE_LATITUDE_KEY, locationInfo.getLatitude()).apply();
            editor.putFloat(GEO_FENCE_LONGITUDE_KEY, locationInfo.getLongitude()).apply();
            editor.putFloat(GEO_FENCE_ZOOM_KEY, locationInfo.getZoom()).apply();
        }
        editor.apply();
    }

    public void saveWifiNetwork(@NonNull Context context, @Nullable String wifiNetworkName){
        context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE).edit().putString(WIFI_KEY, wifiNetworkName).apply();
    }

    public void saveUseLocationOption(@NonNull Context context, boolean useLocationOption){
        context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE).edit().putBoolean(USE_LOCATION_OPTION_KEY, useLocationOption).apply();
    }

}
