package com.vsv.game.engine.objects;

import android.graphics.Paint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vsv.game.engine.DrawPriority;
import com.vsv.game.engine.GameObject;
import com.vsv.game.engine.ShootAsset;
import com.vsv.game.engine.ObjectManager;

public class Dust extends TextureObject {

    private static final DrawPriority drawPriority = new DrawPriority(DrawPriority.BACKGROUND, DrawPriority.BACKGROUND_PRIORITY_DUST);

    public Dust() {
        super(0, 0, ShootAsset.getAsset().dust);
    }

    public void setup(int frameIndex, float x, float y, float scaleFactor, float xSpeed, float ySpeed, float angle) {
        setFrame(frameIndex);
        setPosition(x, y, scaleFactor);
        setRotateAngle(angle);
        setXYSpeed(xSpeed, ySpeed);
    }

    @Override
    public int getState() {
        return 0;
    }

    @Override
    public boolean interact() {
        return false;
    }

    @Override
    public int getId() {
        return 0;
    }

    @Override
    public void setId(int id) {

    }

    @Override
    public void act(float dt, @NonNull ObjectManager<GameObject> objectManager) {
        super.act(dt, objectManager);
        setRotateAngle(getRotateAngle() + 10 * dt);
    }

    @Override
    public boolean canRemoveFromRendering() {
        return getY() < -getPosition().height();
    }

    @Override
    public void takeHit(float damage) {

    }

    @NonNull
    @Override
    public DrawPriority getDrawPriority() {
        return drawPriority;
    }

    @Nullable
    @Override
    public Paint getPaint() {
        return null;
    }
}
