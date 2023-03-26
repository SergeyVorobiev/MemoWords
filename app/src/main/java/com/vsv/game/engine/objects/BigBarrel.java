package com.vsv.game.engine.objects;

import android.graphics.Paint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vsv.game.engine.DrawPriority;
import com.vsv.game.engine.GameObject;
import com.vsv.game.engine.ObjectManager;
import com.vsv.game.engine.screens.ScreenParameters;
import com.vsv.utils.GameMath;

public class BigBarrel extends TextureObject {

    private static final DrawPriority drawPriority = new DrawPriority(DrawPriority.DYNAMIC_OBJECTS, DrawPriority.DYNAMIC_PRIORITY_CANNONS);

    private final ProjectileTurret turret;

    private boolean shoot;

    private float shiftX;

    private float shiftY;

    private final float shift;

    private float pastTime = 0;

    public BigBarrel(float width, float height, @NonNull TextureRegionProperties regionProperties, ProjectileTurret turret) {
        super(width, height, regionProperties);
        this.turret = turret;
        this.shift = 10 * ScreenParameters.screenScaleFactor;
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

    public void shoot() {
        this.shoot = true;
        this.pastTime = 0;
        float angle = turret.getShootAngle();
        setRotateAngle(angle);
        shiftX = GameMath.getXProjection(angle + 180, shift);
        shiftY = GameMath.getYProjection(angle + 180, shift);
    }

    @Override
    public void act(float dt, @NonNull ObjectManager<GameObject> manager) {
        if (shoot) {
            pastTime += dt;
            float sinFactor = (float) Math.sin(pastTime * 6 * Math.PI);
            float x;
            float y;
            if (sinFactor > 0 && sinFactor <= GameMath.HALF_PI) {
                x = turret.getX() + shiftX * sinFactor;
                y = turret.getY() - shiftY * sinFactor;
            } else if (sinFactor > GameMath.HALF_PI && sinFactor < Math.PI) {
                x = turret.getX() - shiftX * sinFactor;
                y = turret.getY() + shiftY * sinFactor;
            } else {
                shoot = false;
                pastTime = 0;
                x = turret.getX();
                y = turret.getY();
            }
            setPosition(x, y);
        } else {
            setRotateAngle(turret.getShootAngle());
        }
    }

    @Override
    public boolean canRemoveFromRendering() {
        return false;
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
