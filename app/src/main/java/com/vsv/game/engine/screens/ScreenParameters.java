package com.vsv.game.engine.screens;

public class ScreenParameters {

    public static final float worldWidth = 1080; //864; //1080;

    public static final float worldHeight = 1523; //1218; //1523;

    public static float screenScaleFactor;

    public static float screenWidth;

    public static float screenHeight;

    public static float shiftWidth;

    public static float shiftHeight;

    public static void setup(float screenWidth, float screenHeight) {
        ScreenParameters.screenWidth = screenWidth;
        ScreenParameters.screenHeight = screenHeight;
        if (screenWidth == worldWidth && screenHeight == worldHeight) {
            screenScaleFactor = 1;
        } else {
            float worldRate = worldWidth / worldHeight;
            float screenRate = screenWidth / screenHeight;
            if (screenRate > worldRate) { // Width of screen in proportion more than width of world.
                screenScaleFactor = screenHeight / worldHeight;
                float scaledWorldWidth = worldWidth * screenScaleFactor;
                shiftWidth = Math.abs(scaledWorldWidth - screenWidth) / 2;
            } else if (screenRate < worldRate) { // Height of screen in proportion more than height of world.
                screenScaleFactor = screenWidth / worldWidth;
                float scaledWorldHeight = worldHeight * screenScaleFactor;
                shiftHeight = Math.abs(scaledWorldHeight - screenHeight) / 2;
            } else {
                screenScaleFactor = screenWidth / worldWidth;
            }
        }
    }
}
