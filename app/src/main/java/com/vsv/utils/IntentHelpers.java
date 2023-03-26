package com.vsv.utils;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vsv.memorizer.R;
import com.vsv.statics.WeakContext;
import com.vsv.toasts.Toasts;

public final class IntentHelpers {

    private IntentHelpers() {

    }

    public static void openLink(@NonNull LinkData linkData, @Nullable String appAcc,
                                 @Nullable String webAcc) {
        String fullWebLink = null;
        String fullAppLink = null;
        if (appAcc != null && !appAcc.isEmpty()) {
            fullAppLink = linkData.appLink + appAcc;
        }
        if (webAcc != null && !webAcc.isEmpty()) {
            fullWebLink = linkData.webLink + webAcc;
        }
        openAppOrWebByLink(linkData.appPackage, fullAppLink, fullWebLink);
    }

    public static void openLink(@Nullable AppLink appLink) {
        if (appLink == null) {
            return;
        }
        openLink(appLink.linkData, appLink.appAcc, appLink.webAcc);
    }

    public static void openAppOrWebByLink(@Nullable String appPackage, @Nullable String appLink,
                                        @Nullable String webLink) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setPackage(appPackage);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            intent.setData(Uri.parse(appLink));
            WeakContext.getMainActivity().startActivity(intent);
        } catch (Exception e) {
            Log.e("OpenLink", e.toString());
            try {
                intent.setData(Uri.parse(webLink));
                intent.setPackage(null);
                WeakContext.getMainActivity().startActivity(intent);
            } catch(Exception e1) {
                Log.e("OpenLink", e1.toString());
                Toasts.shortShow(R.string.toast_cannot_open_link);
            }
        }
    }
}
