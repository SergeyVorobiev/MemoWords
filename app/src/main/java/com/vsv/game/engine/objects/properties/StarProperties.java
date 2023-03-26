package com.vsv.game.engine.objects.properties;

import com.vsv.game.engine.ShootAsset;
import com.vsv.game.engine.screens.ScreenParameters;
import com.vsv.game.engine.objects.Star;
import com.vsv.utils.StaticUtils;

public class StarProperties {

    public static final float width = 24;

    public static final float height = 24;

    public static final float scaleStarSpeed = 1;

    public static final float scaleCometSpeed = 3;

    public static final float scaleGalaxySpeed = 1;

    public static final float cometSpeed = 500;

    public static final int cometChance = 50; // 1 from 50.

    public static final int galaxyChance = 50; // 1 from 50 if not comet.

    public static final int minRotationSpeed = 10;

    public static final int maxRotationSpeed = 50;

    public static void defaultSetup(Star star) {
        star.reset();
        float scaledWidth = width * ScreenParameters.screenScaleFactor;
        float scaledHeight = height * ScreenParameters.screenScaleFactor;
        star.resize(scaledWidth, scaledHeight);
        star.setScaleSpeed(scaleStarSpeed, scaleCometSpeed, scaleGalaxySpeed);
        star.setRotateAngle(StaticUtils.random.nextInt(360));
        star.setPosition(StaticUtils.random.nextInt((int) ScreenParameters.screenWidth), StaticUtils.random.nextInt((int) ScreenParameters.screenHeight));
        float speed = cometSpeed * ScreenParameters.screenScaleFactor;
        float starSpeed = 5 * ScreenParameters.screenScaleFactor;
        star.setupKind(cometChance, galaxyChance, speed, starSpeed, ShootAsset.getAsset().galaxy, ShootAsset.getAsset().star);
        star.setRotationSpeed(StaticUtils.getRandom(minRotationSpeed, maxRotationSpeed));
    }
}
