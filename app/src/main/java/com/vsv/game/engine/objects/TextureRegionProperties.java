package com.vsv.game.engine.objects;

public class TextureRegionProperties {

    public final float width;

    public final float height;

    public final float startX;

    public final float startY;

    public final int frames;

    public final int textureId;

    public final float textureWidth;

    public final float textureHeight;

    public TextureRegionProperties(float width, float height, float startX, float startY, int frames,
                                   int textureId, float textureWidth, float textureHeight) {
        this.width = width;
        this.height = height;
        this.startX = startX;
        this.startY = startY;
        this.frames = frames;
        this.textureId = textureId;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
    }
}
