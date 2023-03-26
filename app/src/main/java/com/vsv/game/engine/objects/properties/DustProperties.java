package com.vsv.game.engine.objects.properties;

import com.vsv.game.engine.objects.Dust;
import com.vsv.game.engine.screens.ScreenParameters;
import com.vsv.utils.StaticUtils;

public final class DustProperties {

    public static final float speedX = 0;

    public static final float speedY = -200; // Default -200

    public static final float width = 32;

    public static final float height = 32;

    public static final int minFrameCount = 0;

    public static final int maxFrameCount = 16;

    private DustProperties() {

    }

    public static void defaultSetup(Dust dust) {
        float scaledWidth = width * ScreenParameters.screenScaleFactor;
        float scaledHeight = height * ScreenParameters.screenScaleFactor;
        dust.resize(scaledWidth, scaledHeight);
        float x = StaticUtils.random.nextInt((int) ScreenParameters.screenWidth);
        float y = ScreenParameters.screenHeight;
        int frameIndex = StaticUtils.getRandom(minFrameCount, maxFrameCount);
        dust.setup(frameIndex, x, y, 1, speedX * ScreenParameters.screenScaleFactor,
                speedY * ScreenParameters.screenScaleFactor, StaticUtils.random.nextInt(360));
    }
}
