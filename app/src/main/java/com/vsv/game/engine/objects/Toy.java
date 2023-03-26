package com.vsv.game.engine.objects;

import android.graphics.Paint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vsv.game.engine.DrawPriority;
import com.vsv.game.engine.GameObject;
import com.vsv.game.engine.ShootAsset;
import com.vsv.game.engine.ObjectManager;
import com.vsv.game.engine.screens.ScreenParameters;

public class Toy extends SpriteObject {

    public static final DrawPriority drawPriority = new DrawPriority(DrawPriority.BACKGROUND, DrawPriority.BACKGROUND_PRIORITY_TOYS);

    private static final Paint paint = new Paint();

    private float speedX;

    private float speedY;

    private float rotationSpeed;

    public Toy() {
        super(64 * ScreenParameters.screenScaleFactor, 64 * ScreenParameters.screenScaleFactor,
                new TextureRegionProperties[]{ShootAsset.getAsset().toyRow1, ShootAsset.getAsset().toyRow2,
                        ShootAsset.getAsset().toyRow3}, 0, 0, true);
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

    public void setRotationSpeed(float rotationSpeed) {
        this.rotationSpeed = rotationSpeed;
    }

    public void setSpeedXY(float speedX, float speedY) {
        this.speedX = speedX;
        this.speedY = speedY;
    }

    @Override
    public void act(float dt, @NonNull ObjectManager<GameObject> manager) {
        if (speedX != 0 || speedY != 0) {
            float dx = speedX * dt;
            float dy = speedY * dt;
            setPosition(getX() + dx, getY() + dy);
        }
        setRotateAngle(getRotateAngle() + rotationSpeed * dt);
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
        return paint;
    }
}
