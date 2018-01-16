package com.app.slate.util;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.app.slate.models.AreaLocationInfo;

public class GeoFenceComparator {

    public final float currentLatitude;
    public final float currentLongitude;
    public final AreaLocationInfo areaLocationInfo;
    @Nullable
    public final String settingsPointWifiName;
    @Nullable
    public final String currentActiveWifiName;

    public GeoFenceComparator(@Nullable AreaLocationInfo areaLocationInfo, float currentLatitude, float currentLongitude, @Nullable String settingsPointWifiName, @Nullable String currentActiveWifiName) {
        this.currentLatitude = currentLatitude;
        this.currentLongitude = currentLongitude;
        this.areaLocationInfo = areaLocationInfo;
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
        return settingsPointWifiName.equals(currentActiveWifiName) || ("\"" + settingsPointWifiName + "\"").equals(currentActiveWifiName);
    }

    public boolean isInAreaByDistance() {
        if (areaLocationInfo == null) {
            return false;
        }
        double radius = AppUtil.distanceBetweenPoints(areaLocationInfo.getLatitude(), areaLocationInfo.getLongitude(), currentLatitude, currentLongitude);
        return radius <= areaLocationInfo.getRadius();
    }


}
