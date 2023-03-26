package com.vsv.utils;

import android.graphics.drawable.Drawable;
import android.util.Pair;

import androidx.annotation.Nullable;

import com.vsv.memorizer.R;

import java.util.TreeMap;

public final class LinkBuilder {

    private final static TreeMap<String, Pair<LinkData, Drawable>> map = new TreeMap<>();

    static {
        map.put("i", new Pair<>(LinkData.instagram, StaticUtils.getDrawable(R.drawable.btn_instagram)));
        map.put("y", new Pair<>(LinkData.youtube, StaticUtils.getDrawable(R.drawable.btn_youtube)));
        map.put("t", new Pair<>(LinkData.twitter, StaticUtils.getDrawable(R.drawable.btn_twitter)));
        map.put("f", new Pair<>(LinkData.facebook, StaticUtils.getDrawable(R.drawable.btn_facebook)));
    }

    private LinkBuilder() {

    }

    @Nullable
    public static AppLink buildLinkFromString(@Nullable String string) {
        if (string == null || string.isEmpty()) {
            return null;
        }
        int dividerIndex = string.indexOf(":");
        if (dividerIndex < 0 || dividerIndex == string.length() - 1) {
            return null;
        }
        String type = string.substring(0, dividerIndex);
        String account = string.substring(dividerIndex + 1);
        if (type.isEmpty() || account.isEmpty()) {
            return null;
        }
        Pair<LinkData, Drawable> data = map.get(type);
        if (data == null) {
            return null;
        }
        LinkData linkData = data.first;
        if (linkData == null) {
            return null;
        }
        assert LinkData.facebook.appPackage != null;
        if (LinkData.facebook.appPackage.equals(linkData.appPackage)) {
            return new AppLink(linkData, null, account, data.second);
        }
        return new AppLink(linkData, account, account, data.second);
    }
}
