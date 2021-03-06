package com.app.slate.util;

import android.content.Context;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.app.slate.models.AreaLocationInfo;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

public class AppUtil {

    private static final double EARTH_RADIUS = 6366000;

    @Nullable
    public static String getWifiActiveSsid(@NonNull Context context) {
        final WifiManager mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (mWifiManager != null && mWifiManager.isWifiEnabled()) {
            WifiInfo info = mWifiManager.getConnectionInfo();

            if (info.getSupplicantState() == SupplicantState.COMPLETED) {
                return info.getSSID();
            }
        }
        return null;
    }

    public static double distanceBetweenPoints(float latA, float lngA, float latB, float lngB) {
        float rad = (float) (180.f / Math.PI);

        float a1 = latA / rad;
        float a2 = lngA / rad;
        float b1 = latB / rad;
        float b2 = lngB / rad;

        double t1 = Math.cos(a1) * Math.cos(a2) * Math.cos(b1) * Math.cos(b2);
        double t2 = Math.cos(a1) * Math.sin(a2) * Math.cos(b1) * Math.sin(b2);
        double t3 = Math.sin(a1) * Math.sin(b1);
        double tt = Math.acos(t1 + t2 + t3);
        return EARTH_RADIUS * tt;
    }

    public static void updateMapMinZoom(@NonNull GoogleMap googleMap) {
        googleMap.setMinZoomPreference(googleMap.getMaxZoomLevel() / 4);
    }

    public static void moveCameraByAreaLocationInfo(@NonNull GoogleMap googleMap, @NonNull LatLng latLng, float zoom){
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

}
