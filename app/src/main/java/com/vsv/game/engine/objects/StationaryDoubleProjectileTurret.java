package com.vsv.game.engine.objects;

import android.graphics.RectF;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vsv.game.engine.DrawInstruments;
import com.vsv.game.engine.GameObject;
import com.vsv.game.engine.ObjectManager;
import com.vsv.utils.GameMath;

public class StationaryDoubleProjectileTurret extends ProjectileTurret {

    private boolean isLeft;

    private float barrelLengthFromCenter;

    private float defaultLeftBarrelAngle;

    private float defaultRightBarrelAngle;

    private final float defaultBarrelDistanceFromCenter;

    private float leftBarrelPositionX;

    private float leftBarrelPositionY;

    private float rightBarrelPositionX;

    private float rightBarrelPositionY;

    public StationaryDoubleProjectileTurret(float bulletSpeed, float shootFrequency, float queueSize,
                                            float coolTime, float damage, @Nullable SmokeParams smoke, float defaultBarrelDistanceFromCenter,
                                            Class<? extends Projectile> type) {
        super(bulletSpeed, shootFrequency, queueSize, coolTime, damage, smoke, type);
        this.defaultBarrelDistanceFromCenter = defaultBarrelDistanceFromCenter;
    }

    @Override
    protected void startProjectile(float x, float y, ObjectManager<GameObject> objectManager) {
        if (isLeft) {
            x = leftBarrelPositionX;
            y = leftBarrelPositionY;
        } else {
            x = rightBarrelPositionX;
            y = rightBarrelPositionY;
        }
        isLeft = !isLeft;
        super.startProjectile(x, y, objectManager);
    }

    @Override
    public void install(RectF position) {
        super.install(position);
        calculateBarrelPoints();
    }

    private void calculateBarrelPoints() {
        float dxDefaultLeftFromCenter = -defaultBarrelDistanceFromCenter;
        float dyDefaultFromCenter = getPosition().height() / 2;
        defaultLeftBarrelAngle = GameMath.getAngleByDiff(dyDefaultFromCenter, dxDefaultLeftFromCenter);
        defaultRightBarrelAngle = GameMath.getAngleByDiff(dyDefaultFromCenter, defaultBarrelDistanceFromCenter);
        barrelLengthFromCenter = GameMath.getDistance(dxDefaultLeftFromCenter, dyDefaultFromCenter);
    }

    @Override
    public void act(float dt, @NonNull ObjectManager<GameObject> objectManager) {
        super.act(dt, objectManager);
        float leftAngle = getShootAngle() - defaultLeftBarrelAngle;
        float rightAngle = getShootAngle() - defaultRightBarrelAngle;
        float dxLeftFromCenter = GameMath.getXProjection(leftAngle, barrelLengthFromCenter);
        float dyLeftFromCenter = GameMath.getYProjection(leftAngle, barrelLengthFromCenter);
        float dxRightFromCenter = GameMath.getXProjection(rightAngle, barrelLengthFromCenter);
        float dyRightFromCenter = GameMath.getYProjection(rightAngle, barrelLengthFromCenter);
        leftBarrelPositionX = getPosition().centerX() + dxLeftFromCenter;
        leftBarrelPositionY = getPosition().centerY() - dyLeftFromCenter;
        rightBarrelPositionX = getPosition().centerX() + dxRightFromCenter;
        rightBarrelPositionY = getPosition().centerY() - dyRightFromCenter;
    }

    @Override
    public void draw(@NonNull DrawInstruments<?> drawInstruments) {
        // drawInstruments.drawPoint(leftBarrelPositionX, leftBarrelPositionY, 5, drawInstruments.strokePaint);
        // drawInstruments.drawPoint(rightBarrelPositionX, rightBarrelPositionY, 5, drawInstruments.strokePaint);
    }
}
