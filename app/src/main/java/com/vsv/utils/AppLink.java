package com.vsv.utils;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AppLink {

    @NonNull
    public final LinkData linkData;

    @Nullable
    public final String appAcc;

    @Nullable
    public final String webAcc;

    @Nullable
    public final Drawable icon;

    public AppLink(@NonNull LinkData linkData, @Nullable String appAcc, @Nullable String webAcc,
                   @Nullable Drawable icon) {
        this.linkData = linkData;
        this.appAcc = appAcc;
        this.webAcc = webAcc;
        this.icon = icon;
    }
}
