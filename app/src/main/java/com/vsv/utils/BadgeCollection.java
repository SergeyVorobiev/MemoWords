package com.vsv.utils;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.view.Gravity;

import com.vsv.memorizer.R;

public final class BadgeCollection {

    private BadgeCollection() {

    }

    public static final int[] badges = new int[]{
            R.drawable.badge_ball,
            R.drawable.badge_bug,
            R.drawable.badge_bug2,
            R.drawable.badge_cannon,
            R.drawable.badge_castle,
            R.drawable.badge_cup,
            R.drawable.badge_diamond,
            R.drawable.badge_dice,
            R.drawable.badge_helmet,
            R.drawable.badge_icecream,
            R.drawable.badge_leaf,
            R.drawable.badge_pie,
            R.drawable.badge_pig,
            R.drawable.badge_pokemon,
            R.drawable.badge_puzzle,
            R.drawable.badge_rabbit,
            R.drawable.badge_snow,
            R.drawable.badge_star,
            R.drawable.badge_stars,
            R.drawable.badge_umbrella
    };

    public static LayerDrawable createRandomBadge(int badgeSize) {
        Drawable circle = StaticUtils.getDrawable(R.drawable.badge_circle);
        int iconId = StaticUtils.random.nextInt(BadgeCollection.badges.length);
        Drawable icon = StaticUtils.getDrawable(BadgeCollection.badges[iconId]);
        Drawable[] layers = {circle, icon};
        LayerDrawable badgeLayer = new LayerDrawable(layers);
        badgeLayer.setLayerSize(1, badgeSize, badgeSize);
        badgeLayer.setLayerGravity(1, Gravity.CENTER);
        return badgeLayer;
    }
}
