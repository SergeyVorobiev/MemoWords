package com.vsv.game.engine.objects;

import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vsv.game.engine.DrawPriority;
import com.vsv.game.engine.GameObject;
import com.vsv.game.engine.ShootAsset;
import com.vsv.game.engine.ObjectManager;
import com.vsv.game.engine.screens.ScreenParameters;
import com.vsv.utils.GameMath;

public class FireBullet extends TextureObject implements Projectile {

    public static final DrawPriority drawPriority = new DrawPriority(DrawPriority.DYNAMIC_OBJECTS, DrawPriority.DYNAMIC_PRIORITY_PROJECTILES);

    public static final int FLY = 0;

    public static final int DIED = 1;

    protected float damage;

    protected int state;

    protected GameObject target;

    private int id;

    public static int idc;

    private float xProjection;

    private float yProjection;

    public FireBullet() {
        this(10 * ScreenParameters.screenScaleFactor, 10 * ScreenParameters.screenScaleFactor, ShootAsset.getAsset().fireBullet);
    }

    public FireBullet(float width, float height, @NonNull TextureRegionProperties textureRegionProperties) {
        super(width, height, textureRegionProperties);
    }

    @NonNull
    @Override
    public FireBullet setup(float x, float y, @NonNull GameObject parent, @NonNull GameObject target,
                            float speed, float angle, float damage) {
        this.setPosition(x, y);
        id = idc++;
        this.damage = damage;
        xProjection = GameMath.getXProjection(angle, 1);
        yProjection = GameMath.getYProjection(angle, 1);
        speedX = xProjection * speed;
        speedY = yProjection * speed;
        this.target = target;
        state = FLY;
        return this;
    }

    @Override
    public int getState() {
        return state;
    }

    @Override
    public boolean interact() {
        return false;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public void act(float dt, @NonNull ObjectManager<GameObject> manager) {
        if (target != null && !target.interact()) {
            target = null;
        }
        if (getY() <= 0) {
            state = DIED;
        }
        if (state == FLY) {
            float x = getX() + speedX * dt;
            float y = getY() - speedY * dt;
            setPosition(x, y);
            checkCollision(x, y, manager);
        }
    }

    protected void checkCollision(float x, float y, ObjectManager<GameObject> manager) {
        if (target != null && !target.canRemoveFromRendering()) {
            RectF bounds = target.getPosition();
            if (bounds.contains((int) x, (int) y)) {
                target.takeHit(damage);
                state = DIED;
                explode(getX(), getY() - 20, manager);
            } else if (y < bounds.top) {
                y = bounds.top;
                explode(getX(), y + 20, manager);
                state = DIED;
                target.takeHit(damage);
            }
        }
    }

    public void explode(float x, float y, ObjectManager<GameObject> objectManager) {
        RocketExplosion rocketExplosion = objectManager.getObjectFromPool(RocketExplosion.class);
        rocketExplosion.reset();
        float explodeObjectSize = 16;
        float size = explodeObjectSize * ScreenParameters.screenScaleFactor;
        rocketExplosion.resize(size, size);
        rocketExplosion.setFrameTime(0.05f);
        float speed = 20 * ScreenParameters.screenScaleFactor;
        rocketExplosion.setPosition(x + xProjection * speed, y - yProjection * speed);
        rocketExplosion.setXYSpeed(0, -10 * ScreenParameters.screenScaleFactor);
        objectManager.addObject(rocketExplosion);
    }

    @Override
    public boolean canRemoveFromRendering() {
        return state == DIED;
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
