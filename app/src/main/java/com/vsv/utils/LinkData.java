package com.vsv.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class LinkData {

    public static final int FACEBOOK = 0;

    public static final int TWITTER = 1;

    public static final int INSTAGRAM = 2;

    public static final int YOUTUBE = 3;

    @NonNull
    public static final LinkData facebook;

    @NonNull
    public static final LinkData twitter;

    @NonNull
    public static final LinkData instagram;

    @NonNull
    public static final LinkData youtube;

    public final int type;

    @Nullable
    public final String appPackage;

    @Nullable
    public final String webLink;

    @Nullable
    public final String appLink;

    static {
        facebook = new LinkData("com.facebook.katana", "fb://page/", "https://www.facebook.com/", FACEBOOK);
        twitter = new LinkData("com.twitter.android", "twitter://user?screen_name=", "https://twitter.com/", TWITTER);
        instagram = new LinkData("com.instagram.android", "https://www.instagram.com/", "https://www.instagram.com/", INSTAGRAM);
        youtube = new LinkData("com.google.android.youtube", "http://www.youtube.com/channel/", "http://www.youtube.com/channel/", YOUTUBE);
    }

    private LinkData(@Nullable String appPackage, @Nullable String appLink, @Nullable String webLink, int type) {
        this.appPackage = appPackage;
        this.appLink = appLink;
        this.webLink = webLink;
        this.type = type;
    }
}
