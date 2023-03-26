package com.vsv.game.engine.objects;

import android.graphics.Paint;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vsv.game.engine.DrawPriority;
import com.vsv.game.engine.GameObject;
import com.vsv.game.engine.ObjectManager;
import com.vsv.game.engine.ShootAsset;
import com.vsv.game.engine.screens.ScreenParameters;
import com.vsv.utils.GameMath;
import com.vsv.utils.StaticUtils;

public class Rocket extends TextureObject implements Projectile {

    public static final DrawPriority drawPriority = new DrawPriority(DrawPriority.DYNAMIC_OBJECTS, DrawPriority.DYNAMIC_PRIORITY_PROJECTILES);

    public static final int FLY = 0;

    public static final int DIED = 1;

    private static final float INIT_TIME = 0.2f;

    private GameObject target;

    private int state;

    private float speed;

    @SuppressWarnings("FieldCanBeLocal")
    private final float smokeTime = 0.03f;

    private float pastSmokeTime;

    private float pastTime;

    private float damage;

    @SuppressWarnings("FieldCanBeLocal")
    private final float acceleration = 800;

    @SuppressWarnings("FieldCanBeLocal")
    private final float boost = 1200;

    private float currentBoost;

    private float currentScale;

    public Rocket() {
        super(48 * ScreenParameters.screenScaleFactor, 20 * ScreenParameters.screenScaleFactor, ShootAsset.getAsset().rocket);
    }

    @Override
    public void explode(float x, float y, @NonNull ObjectManager<GameObject> objectManager) {
        RocketExplosion rocketExplosion = objectManager.getObjectFromPool(RocketExplosion.class);
        rocketExplosion.reset();
        float explodeObjectSize = 64;
        float size = explodeObjectSize * ScreenParameters.screenScaleFactor;
        rocketExplosion.resize(size, size);
        rocketExplosion.setFrameTime(0.1f);
        rocketExplosion.setPosition(x, y);
        rocketExplosion.setXYSpeed(0, -50 * ScreenParameters.screenScaleFactor);
        objectManager.addObject(rocketExplosion);
    }

    @NonNull
    public Rocket setup(float x, float y, @NonNull GameObject parent, @NonNull GameObject target, float speed, float angle, float damage) {
        setRotateAngle(angle);
        setPosition(x, y, 1);
        this.speed = speed;
        this.damage = damage;
        this.currentBoost = boost;
        this.setRotateAngle(angle + StaticUtils.getRandom(-15, 15));
        this.setMaxRotationSpeed(60);
        this.target = target;
        this.currentScale = 1;
        state = FLY;
        pastTime = 0;
        pastSmokeTime = 0;
        return this;
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

    private void setupSmoke(float x, float y, ObjectManager<GameObject> objectManager) {
        SmokeObject object = objectManager.getObjectFromPool(SmokeObject.class);
        object.reset();
        object.setXYSpeed(0, 50 * ScreenParameters.screenScaleFactor);
        object.resize(32 * ScreenParameters.screenScaleFactor, 32 * ScreenParameters.screenScaleFactor);
        object.setPosition(x, y, 1.0f);
        object.setFrameTime(0.25f);
        object.setView(ShootAsset.getAsset().whiteSmoke, 0);
        objectManager.addObject(object);
    }

    @Override
    public void act(float dt, @NonNull ObjectManager<GameObject> objectManager) {
        if (target != null && !target.interact()) {
            target = null;
        }
        if (getY() <= 0) {
            state = DIED;
        }
        if (state == FLY) {
            if (pastTime >= INIT_TIME) {
                speed += currentBoost;
                currentBoost = 0;
                speed += acceleration * ScreenParameters.screenScaleFactor;
                setMaxRotationSpeed(300);
            }
            float angle = target == null ? getRotateAngle() : GameMath.getAngleInDegrees(this, target);
            rotate(angle, dt);
            float speedDx = GameMath.getXProjection(getRotateAngle(), speed);
            float speedDy = GameMath.getYProjection(getRotateAngle(), speed);
            float x = getX();
            float y = getY();
            if (pastSmokeTime > smokeTime) {
                pastSmokeTime = 0;
                setupSmoke(x, y, objectManager);
            }
            x += speedDx * dt;
            y -= speedDy * dt;
            currentScale += dt / 2;
            setPosition(x, y, currentScale);
            checkCollision(objectManager);
        }
        pastTime += dt;
        pastSmokeTime += dt;
    }

    private void checkCollision(ObjectManager<GameObject> objectManager) {
        if (target != null && !target.canRemoveFromRendering()) {
            RectF bounds = target.getPosition();
            float x = getX();
            float y = getY();
            if (bounds.contains((int) x, (int) y)) {
                target.takeHit(damage);
                state = DIED;
                explode(getX(), getY() - 20, objectManager);
            } else if (y < bounds.top) {
                setPosition(getX(), bounds.top + 20);
                state = DIED;
                target.takeHit(damage);
                explode(getX(), bounds.top + 20, objectManager);
            }
        }
    }

    @Override
    public boolean canRemoveFromRendering() {
        return state == DIED;
    }

    @Override
    public void destroy() {

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
    public PorterDuffColorFilter getColorFilter() {
        return null;
    }

    @Nullable
    @Override
    public Paint getPaint() {
        return null;
    }
}
