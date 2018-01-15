package com.app.slate.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

class StorageManager {

    private static final String PREF_FILE_NAME = "pref_settings";
    private static final String GEO_FENCE_RADIUS_KEY = "radius";
    private static final String WIFI_KEY = "wifi";
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

    public float getRadius(@NonNull Context context){
        return context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE).getFloat(GEO_FENCE_RADIUS_KEY, GeofenceManager.DEFAULT_GEOFENCE_RADIUS);
    }

    @Nullable
    public String getWifiNetwork(@NonNull Context context){
        return context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE).getString(WIFI_KEY, null);
    }

    public void saveRadius(@NonNull Context context, float radius){
        context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE).edit().putFloat(GEO_FENCE_RADIUS_KEY,
                radius).apply();
    }

    @Nullable
    public void saveWifiNetwork(@NonNull Context context, @Nullable String wifiNetworkName){
        context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE).edit().putString(WIFI_KEY, wifiNetworkName).apply();
    }

}
