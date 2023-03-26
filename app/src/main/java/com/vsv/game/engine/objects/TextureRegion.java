package com.vsv.game.engine.objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class TextureRegion {

    public float u1; // like x0

    public float u2; // like x1

    public float v1; // like y0 down - up

    public float v2; // like y1 down - up

    public float x1;

    public float x2;

    public float y1; // down - up

    public float y2; // down - up

    public float y1Inverse; // up - down

    public float y2Inverse; // up - down

    public int textureId;

    private void setupUV(float u1, float v1, float u2, float v2) {
        this.u1 = u1;
        this.u2 = u2;
        this.v1 = v1;
        this.v2 = v2;
    }

    private void setupXY(float x1, float y1, float x2, float y2, float y1Inverse, float y2Inverse) {
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
        this.y1Inverse = y1Inverse;
        this.y2Inverse = y2Inverse;
    }

    @NonNull
    public static TextureRegion[] buildRegions(TextureRegionProperties regionProperties, @Nullable TextureRegion[] regions) {
        float textureWidth = regionProperties.textureWidth;
        float textureHeight = regionProperties.textureHeight;
        if (regions == null || regions.length != regionProperties.frames) {
            regions = new TextureRegion[regionProperties.frames];
            for (int i = 0; i < regionProperties.frames; i++) {
                regions[i] = new TextureRegion();
            }
        }
        float partWidth = regionProperties.width / regionProperties.frames;
        float startY = textureHeight - regionProperties.startY; // inverse down - up to up - down
        for (int i = 0; i < regionProperties.frames; i++) {
            TextureRegion region = regions[i];
            region.textureId = regionProperties.textureId;
            float startX = regionProperties.startX + i * partWidth;
            float endX = startX + partWidth;
            region.setupXY(startX, regionProperties.startY, (int) endX,
                    (int) (regionProperties.startY + regionProperties.height),
                    (int) startY, (int) (startY - regionProperties.height));
            float u1 = startX / textureWidth;
            float u2 = endX / textureWidth;
            float v1 = regionProperties.startY / textureHeight;
            float v2 = (regionProperties.startY + regionProperties.height) / textureHeight;
            region.setupUV(u1, v1, u2, v2);
        }
        return regions;
    }
}
