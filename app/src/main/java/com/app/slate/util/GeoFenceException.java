package com.app.slate.util;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;

public class GeoFenceException extends Exception {
    private GeoFenceException() {
    }

    GeoFenceException(@NonNull String message) {
        super(message);
    }

    private GeoFenceException(String message, Throwable cause) {
        super(message, cause);
    }

    private GeoFenceException(Throwable cause) {
        super(cause);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private GeoFenceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
