package com.vsv.game.engine.objects;

import android.graphics.RectF;

import androidx.annotation.NonNull;

import com.vsv.game.engine.DrawPriority;
import com.vsv.game.engine.GameObject;
import com.vsv.game.engine.DrawInstruments;
import com.vsv.game.engine.ObjectManager;
import com.vsv.game.engine.screens.ScreenParameters;
import com.vsv.utils.StaticUtils;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nullable;

public class ProjectileTurret implements GameObject {

    private static final DrawPriority drawPriority = new DrawPriority(0, 0);

    public static final int COOL = 1;

    public static final int SHOOT = 2;

    private final float bulletSpeed;

    private final float shootFrequency;

    private final float queueSize;

    private final float coolTime;

    private int state = COOL;

    private float pastTimeFromShoot = 0;

    private float coolPastTime;

    private int countShootBullets = 0;

    private GameObject target;

    private float shootAngle = 0;

    private boolean canShoot;

    protected RectF position;

    private final AtomicBoolean shootFlag = new AtomicBoolean(false);

    private final float damage;

    private final SmokeParams smoke;

    private final boolean isLaser;

    protected final Class<? extends Projectile> type;

    public ProjectileTurret(float bulletSpeed, float shootFrequency, float queueSize,
                            float coolTime, float damage, @Nullable SmokeParams smoke, Class<? extends Projectile> type) {
        isLaser = type.toString().equals(Laser.class.toString());
        this.type = type;
        this.smoke = smoke;
        this.bulletSpeed = bulletSpeed;
        this.shootFrequency = shootFrequency;
        this.queueSize = queueSize;
        this.coolTime = coolTime;
        this.damage = damage;
        this.coolPastTime = this.coolTime;
    }

    public void install(RectF position) {
        this.position = position;
    }

    @Override
    public int getState() {
        return 0;
    }

    @Override
    public int getTextureId() {
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
        pastTimeFromShoot += dt;
        if (state == SHOOT) {
            shoot(objectManager);
        } else if (state == COOL) {
            coolPastTime += dt;
            canShoot = coolPastTime >= coolTime;
            if (target != null && shootFlag.get()) {
                shootFlag.set(false);
                shoot(objectManager);
            }
        }
    }

    public void commandShoot() {
        shootFlag.set(true);
    }

    private void shoot(ObjectManager<GameObject> objectManager) {
        if (!canShoot) {
            return;
        }
        coolPastTime = 0;
        state = SHOOT;
        if (countShootBullets >= queueSize) {
            countShootBullets = 0;
            state = COOL;
        } else if (pastTimeFromShoot >= shootFrequency) {
            startProjectile(position.centerX(), position.centerY(), objectManager);
        }
    }

    protected void startProjectile(float x, float y, ObjectManager<GameObject> objectManager) {
        Projectile projectile = objectManager.getObjectFromPool(type);
        if (isLaser) {
            ((Laser) projectile).setRandomColor();
        }
        projectile.setup(x, y, this, target, bulletSpeed, shootAngle, damage);
        objectManager.addObject(projectile);
        countShootBullets += 1;
        pastTimeFromShoot = 0;
        if (smoke != null) {
            setupSmoke(x, y, objectManager);
        }
    }

    protected void setupSmoke(float x, float y, ObjectManager<GameObject> manager) {
        SmokeObject object = manager.getObjectFromPool(SmokeObject.class);
        object.reset();
        float xSpeed = StaticUtils.getRandom(smoke.xSpeedMin, smoke.xSpeedMax) * ScreenParameters.screenScaleFactor;
        float ySpeed = StaticUtils.getRandom(smoke.ySpeedMin, smoke.ySpeedMax) * ScreenParameters.screenScaleFactor;
        object.setXYSpeed(xSpeed, ySpeed);
        object.resize(smoke.width * ScreenParameters.screenScaleFactor, smoke.height * ScreenParameters.screenScaleFactor);
        object.setPosition(x, y);
        object.setFrameTime(smoke.frameTime);
        object.setView(smoke.textureRegionProperties, 0);
        manager.addObject(object);
    }

    public boolean canRotate(ObjectManager<GameObject> objectManager) {
        if (isLaser) {
            ArrayList<GameObject> objects = objectManager.getPriorityGroup(DrawPriority.DYNAMIC_OBJECTS, DrawPriority.DYNAMIC_PRIORITY_LASER);
            if (!objects.isEmpty()) {
                Laser laser = (Laser) objects.get(0);
                return laser.canRotate();
            }
        }
        return true;
    }

    public void setTarget(float angle, @Nullable GameObject target) {
        this.shootAngle = angle;
        this.target = target;
    }

    public float getShootAngle() {
        return shootAngle;
    }

    @Override
    public boolean canRemoveFromRendering() {
        return false;
    }

    @Override
    public void draw(@NonNull DrawInstruments<?> drawInstruments) {

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

    @Override
    public float getX() {
        return position.centerX();
    }

    @Override
    public float getY() {
        return position.centerY();
    }

    @NonNull
    @Override
    public RectF getPosition() {
        return position;
    }
}
