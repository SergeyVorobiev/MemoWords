package com.vsv.game.engine.objects.properties;

import com.vsv.game.engine.screens.ScreenParameters;
import com.vsv.game.engine.objects.Toy;
import com.vsv.utils.StaticUtils;

public final class ToyProperties {

    public static final float width = 32;

    public static final float height = 32;

    public static final float speedX = 0;

    public static final float speedY = -200; // Default -200

    public static final int minRotationSpeed = -360;

    public static final int maxRotationSpeed = 360;

    private ToyProperties() {

    }

    public static float getRotationSpeed() {
        return StaticUtils.getRandom(minRotationSpeed, maxRotationSpeed);
    }

    private static void setColor(Toy toy) {
        float r = StaticUtils.random.nextInt(256) / 255.0f;
        float g = StaticUtils.random.nextInt(256) / 255.0f;
        float b = StaticUtils.random.nextInt(256) / 255.0f;
        float a = StaticUtils.random.nextInt(256) / 255.0f;
        a = Math.min(1, a + 0.5f);
        toy.setColor(a, r, g, b);
    }

    public static void defaultSetup(Toy toy) {
        float scaledWidth = width * ScreenParameters.screenScaleFactor;
        float scaledHeight = height * ScreenParameters.screenScaleFactor;
        toy.resize(scaledWidth, scaledHeight);
        float halfScaledWidth = scaledWidth / 2;
        float x = StaticUtils.getRandom((int) halfScaledWidth, (int) (ScreenParameters.screenWidth - halfScaledWidth));
        float y = ScreenParameters.screenHeight;
        toy.setPosition(x, y);
        setColor(toy);
        float speedYn = speedY + StaticUtils.getRandom(-100, 100);
        toy.setFrame(StaticUtils.random.nextInt(toy.getFrameCount()));
        toy.setSpeedXY(speedX * ScreenParameters.screenScaleFactor, speedYn * ScreenParameters.screenScaleFactor);
        toy.setRotationSpeed(getRotationSpeed());
        toy.setColorFilterATOP(StaticUtils.random.nextInt());
    }
}
