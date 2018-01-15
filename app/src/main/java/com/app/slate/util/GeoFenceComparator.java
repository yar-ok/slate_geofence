package com.app.slate.util;

import android.support.annotation.Nullable;
import android.text.TextUtils;

public class GeoFenceComparator {

    public final float distanceToPoint;
    public final float settingsPointAreaRadius;
    @Nullable
    public final String settingsPointWifiName;
    @Nullable
    public final String currentActiveWifiName;

    public GeoFenceComparator(float settingsPointAreaRadius, @Nullable String settingsPointWifiName, float distanceToPoint, @Nullable String currentActiveWifiName) {
        this.distanceToPoint = distanceToPoint;
        this.settingsPointAreaRadius = settingsPointAreaRadius;
        this.settingsPointWifiName = settingsPointWifiName;
        this.currentActiveWifiName = currentActiveWifiName;
    }

    public boolean isInArea() {
        if (isInAreaByWifiAccessPoint()) {
            return true;
        }
        return isInAreaByDistance();
    }

    public boolean isInAreaByWifiAccessPoint() {
        if (TextUtils.isEmpty(settingsPointWifiName)) {
            return false;
        }
        if (TextUtils.isEmpty(currentActiveWifiName)) {
            return false;
        }
        return settingsPointWifiName.equals(currentActiveWifiName);
    }

    public boolean isInAreaByDistance() {
        return distanceToPoint <= settingsPointAreaRadius;
    }


}
