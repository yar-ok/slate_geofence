package com.app.slate;

import android.app.Application;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.widget.Toast;

public class App extends Application {

    private static App app;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
    }

    public static App getApp() {
        return app;
    }

    public void showToast(@NonNull CharSequence text) {
        Toast.makeText(app, text, Toast.LENGTH_LONG).show();
    }

    public void showToast(@StringRes int textId) {
        Toast.makeText(app, textId, Toast.LENGTH_LONG).show();
    }
}
