package com.vsv.game.engine.objects;

import androidx.annotation.NonNull;

import com.vsv.game.engine.DrawPriority;
import com.vsv.game.engine.ShootAsset;

public class SmokeObject extends SpriteObject {

    public static final DrawPriority drawPriority = new DrawPriority(DrawPriority.DYNAMIC_OBJECTS, DrawPriority.DYNAMIC_PRIORITY_EFFECTS);

    public SmokeObject() {
        super(24, 24, ShootAsset.getAsset().whiteSmoke, 0, 0.15f, false);
    }

    @NonNull
    @Override
    public DrawPriority getDrawPriority() {
        return drawPriority;
    }
}
