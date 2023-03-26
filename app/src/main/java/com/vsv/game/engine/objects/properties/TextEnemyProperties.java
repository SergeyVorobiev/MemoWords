package com.vsv.game.engine.objects.properties;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;

import com.vsv.game.engine.screens.ScreenParameters;
import com.vsv.game.engine.objects.TextEnemy;
import com.vsv.memorizer.R;
import com.vsv.utils.StaticUtils;

public final class TextEnemyProperties {

    public static final int blueColor = 0xFFE6FEFF;

    public static final int redColor = 0xFFFFC5C5;

    public static final float[] blueColorParts = new float[]{Color.alpha(blueColor) / 255.0f,
            Color.red(blueColor) / 255.0f, Color.green(blueColor) / 255.0f, Color.blue(blueColor) / 255.0f};

    public static final float[] redColorParts = new float[]{Color.alpha(redColor) / 255.0f, Color.red(redColor) / 255.0f,
            Color.green(redColor) / 255.0f, Color.blue(redColor) / 255.0f};

    public static final PorterDuffColorFilter redColorFilter = new PorterDuffColorFilter(0x1DFF0000, PorterDuff.Mode.SRC_ATOP);

    public static final PorterDuffColorFilter blueColorFilter = new PorterDuffColorFilter(0x33A2FFFF, PorterDuff.Mode.SRC_ATOP);

    public static final float speed = 130; // Default 65

    public static final float HEALTH_ALL = 4000;

    public static final float HEALTH_ONE = 1000;

    public static float correctDestroyX = 0;

    public static int minDestroyRotateSpeed = 1000;

    public static int maxDestroyRotateSpeed = 2000;

    public static float slowDownSpeedFactor = 8; // Default 2

    public static float destroySpeed = 1000;

    public static float yShiftBackWhenHit = 70;

    public static int minLiveRotateAngle = -3;

    public static int maxLiveRotateAngle = 3;

    public static final float width = 350;

    public static final float height = 350;

    private static final float textSize = StaticUtils.convertDpToPixels(StaticUtils.getDimension(R.dimen.word_game_size));

    public static void defaultSetup(TextEnemy enemy, int id, String text, int textureIndex) {
        int rotateSpeed = StaticUtils.getRandom(minDestroyRotateSpeed, maxDestroyRotateSpeed);
        int direction = StaticUtils.random.nextInt(2) - 1;
        if (direction < 0) {
            rotateSpeed *= direction;
        }
        float startRotateAngle = StaticUtils.random.nextInt(360);
        float liveRotateAngle = StaticUtils.getRandom(minLiveRotateAngle, maxLiveRotateAngle);
        enemy.setId(id);
        float scaledWidth = width * ScreenParameters.screenScaleFactor;
        float scaledHeight = height * ScreenParameters.screenScaleFactor;
        enemy.setupView(scaledWidth, scaledHeight);
        enemy.setupDestroyPoints(correctDestroyX, ScreenParameters.screenWidth, 0);
        int startBound = (int) (ScreenParameters.shiftWidth / 2 + scaledWidth / 2);
        int endBound = (int) (ScreenParameters.screenWidth - startBound);
        float x = StaticUtils.getRandom(startBound, endBound);
        float scaledTextSize = textSize * ScreenParameters.screenScaleFactor;
        enemy.setTextTextureId(textureIndex);
        enemy.setup(text, x, (int) ScreenParameters.shiftHeight, speed * ScreenParameters.screenScaleFactor,
                startRotateAngle, rotateSpeed, liveRotateAngle, scaledTextSize, slowDownSpeedFactor * ScreenParameters.screenScaleFactor,
                destroySpeed * ScreenParameters.screenScaleFactor, yShiftBackWhenHit * ScreenParameters.screenScaleFactor);
    }

    public static float[] generateArgbColor(int color) {
        return new float[]{Color.alpha(color) / 255.0f,
                Color.red(color) / 255.0f, Color.green(color) / 255.0f, Color.blue(color) / 255.0f};
    }
}
