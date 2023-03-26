package com.vsv.game.engine.objects;

import androidx.annotation.NonNull;

import com.vsv.game.engine.DrawPriority;
import com.vsv.game.engine.ShootAsset;

public class RocketExplosion extends SpriteObject {

    public static final DrawPriority drawPriority = new DrawPriority(DrawPriority.DYNAMIC_OBJECTS, DrawPriority.DYNAMIC_PRIORITY_EFFECTS);

    public RocketExplosion() {
        this(0, 0, ShootAsset.getAsset().rocketExplosion, 0, 0.25f, false);
    }

    public RocketExplosion(float width, float height, TextureRegionProperties regionProperties, int startFrame, float frameTime, boolean cycle) {
        super(width, height, regionProperties, startFrame, frameTime, cycle);
    }

    @NonNull
    @Override
    public DrawPriority getDrawPriority() {
        return drawPriority;
    }
}
