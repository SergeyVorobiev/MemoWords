package com.vsv.game.engine.objects;

import androidx.annotation.NonNull;

import com.vsv.game.engine.DrawPriority;
import com.vsv.game.engine.ShootAsset;

public class FireBallExplosion extends SpriteObject {

    public static final DrawPriority drawPriority = new DrawPriority(DrawPriority.DYNAMIC_OBJECTS, DrawPriority.DYNAMIC_PRIORITY_PROJECTILES);

    public FireBallExplosion() {
        this(0, 0, ShootAsset.getAsset().fireBallExplosion, 0.025f);
    }

    public FireBallExplosion(float width, float height, TextureRegionProperties regionProperties, float frameTime) {
        super(width, height, regionProperties, 0, frameTime, false);
    }

    @NonNull
    @Override
    public DrawPriority getDrawPriority() {
        return drawPriority;
    }
}
