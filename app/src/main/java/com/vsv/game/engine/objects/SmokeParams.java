package com.vsv.game.engine.objects;

public class SmokeParams {

    public final TextureRegionProperties textureRegionProperties;

    public final float width;

    public final float height;

    public final float frameTime;

    public final int xSpeedMin;

    public final int xSpeedMax;

    public final int ySpeedMin;

    public final int ySpeedMax;

    public SmokeParams(TextureRegionProperties textureRegionProperties, float width, float height,
                       float frameTime, int xSpeedMin, int xSpeedMax, int ySpeedMin, int ySpeedMax) {
        this.textureRegionProperties = textureRegionProperties;
        this.width = width;
        this.height = height;
        this.frameTime = frameTime;
        this.xSpeedMin = xSpeedMin;
        this.xSpeedMax = xSpeedMax;
        this.ySpeedMin = ySpeedMin;
        this.ySpeedMax = ySpeedMax;
    }
}
