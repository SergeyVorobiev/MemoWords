package com.vsv.game.engine.objects;

import android.graphics.RectF;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vsv.game.engine.DrawInstruments;
import com.vsv.game.engine.GameObject;
import com.vsv.game.engine.ShootAsset;
import com.vsv.game.engine.ObjectManager;
import com.vsv.utils.GameMath;

public class StationaryBigProjectileTurret extends ProjectileTurret {

    private float barrelLengthFromCenter;

    private float defaultBarrelAngle;

    private float barrelPositionX;

    private float barrelPositionY;

    private BigBarrel barrel;

    public StationaryBigProjectileTurret(float bulletSpeed, float shootFrequency, float queueSize,
                                         float coolTime, float damage, @Nullable SmokeParams smoke,
                                         Class<? extends Projectile> type) {
        super(bulletSpeed, shootFrequency, queueSize, coolTime, damage, smoke, type);
    }

    @Override
    protected void startProjectile(float x, float y, ObjectManager<GameObject> objectManager) {
        barrel.shoot();
        super.startProjectile(barrelPositionX, barrelPositionY, objectManager);
    }

    @Override
    public void install(RectF position) {
        super.install(position);
        calculateBarrelPoints();
        barrel = new BigBarrel(position.width(), position.height(), ShootAsset.getAsset().cannonBarrel, this);
        barrel.setPosition(position.centerX(), position.centerY());
    }

    private void calculateBarrelPoints() {
        float dxDefaultFromCenter = 0;
        float dyDefaultFromCenter = getPosition().height() / 2;
        defaultBarrelAngle = GameMath.getAngleByDiff(dyDefaultFromCenter, dxDefaultFromCenter);
        barrelLengthFromCenter = GameMath.getDistance(dxDefaultFromCenter, dyDefaultFromCenter);
    }

    @Override
    public void act(float dt, @NonNull ObjectManager<GameObject> objectManager) {
        super.act(dt, objectManager);
        this.barrel.act(dt, objectManager);
        float angle = getShootAngle() - defaultBarrelAngle;
        float dxFromCenter = GameMath.getXProjection(angle, barrelLengthFromCenter);
        float dyFromCenter = GameMath.getYProjection(angle, barrelLengthFromCenter);
        barrelPositionX = getPosition().centerX() + dxFromCenter;
        barrelPositionY = getPosition().centerY() - dyFromCenter;
    }

    @Override
    public void draw(@NonNull DrawInstruments<?> drawInstruments) {
        barrel.draw(drawInstruments);
    }
}
