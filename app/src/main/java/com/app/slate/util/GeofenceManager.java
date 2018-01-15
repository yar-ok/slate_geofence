package com.app.slate.util;

public class GeofenceManager {

    public static final double DEFAULT_GEOFENCE_RADIUS = 10000;

    private static volatile GeofenceManager instanceGeofenceManager;

    public static GeofenceManager getGeofenceManager() {
        GeofenceManager localGeofenceManager = instanceGeofenceManager;
        if (localGeofenceManager == null) {
            synchronized (GeofenceManager.class) {
                localGeofenceManager = instanceGeofenceManager;
                if (localGeofenceManager == null) {
                    localGeofenceManager = instanceGeofenceManager = new GeofenceManager();
                }
            }
        }
        return localGeofenceManager;
    }

    private GeofenceManager() {
    }



}
