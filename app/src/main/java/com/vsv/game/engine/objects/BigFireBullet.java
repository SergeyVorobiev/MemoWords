package com.vsv.game.engine.objects;

import androidx.annotation.NonNull;

import com.vsv.game.engine.GameObject;
import com.vsv.game.engine.ObjectManager;
import com.vsv.game.engine.ShootAsset;
import com.vsv.game.engine.screens.ScreenParameters;
import com.vsv.utils.GameMath;

public class BigFireBullet extends FireBullet {

    private float xProjection;

    private float yProjection;

    public BigFireBullet() {
        super(40 * ScreenParameters.screenScaleFactor, 40 * ScreenParameters.screenScaleFactor, ShootAsset.getAsset().bigFireBullet);
    }

    @Override
    public void explode(float x, float y, ObjectManager<GameObject> objectManager) {
        FireBallExplosion explodeObject = objectManager.getObjectFromPool(FireBallExplosion.class);
        explodeObject.reset();
        float size = 96 * ScreenParameters.screenScaleFactor;
        explodeObject.resize(size, size);
        explodeObject.setFrameTime(0.05f);
        float speed = 50 * ScreenParameters.screenScaleFactor;
        explodeObject.setPosition(x + xProjection * speed, y - yProjection * speed);
        objectManager.addObject(explodeObject);
    }

    @NonNull
    @Override
    public FireBullet setup(float x, float y, @NonNull GameObject parent, @NonNull GameObject target,
                            float speed, float angle, float damage) {
        this.setPosition(x, y, 1);
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
            setPosition(x, y, getScaleFactor() + dt * 2);
            checkCollision(x, y, manager);
        }
    }
}
