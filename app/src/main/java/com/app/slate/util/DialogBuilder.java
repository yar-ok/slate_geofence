package com.app.slate.util;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;

public abstract class DialogBuilder {

    public AlertDialog showInfoDialog(@NonNull Activity activity, CharSequence title, CharSequence message,
                                      CharSequence positiveButtonText, final DialogInterface.OnClickListener acceptListener) {
        if (!isAliveActivity(activity)) {
            return null;
        }
        int theme = getDialogTheme();
        AlertDialog.Builder alertBuilder;
        if(theme > 0){
            alertBuilder = new AlertDialog.Builder(activity, theme);
        } else {
            alertBuilder = new AlertDialog.Builder(activity);
        }
        alertBuilder.setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if (acceptListener != null) {
                    acceptListener.onClick(dialog, whichButton);
                }
            }
        });
        alertBuilder.setMessage(message);
        alertBuilder.setTitle(title);
        return alertBuilder.show();
    }

    public abstract int getDialogTheme();

    private static boolean isAliveActivity(@Nullable Activity activity) {
        if (activity == null || activity.isFinishing()) {
            return false;
        }
        return !activity.isDestroyed();
    }
}