package com.vsv.game.engine.objects;

import android.graphics.Paint;
import android.graphics.PorterDuffColorFilter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vsv.game.engine.DrawInstruments;
import com.vsv.game.engine.DrawPriority;
import com.vsv.game.engine.GameObject;
import com.vsv.game.engine.ObjectManager;
import com.vsv.game.engine.ShootAsset;
import com.vsv.game.engine.objects.properties.TextEnemyProperties;
import com.vsv.game.engine.screens.ScreenParameters;
import com.vsv.utils.GameMath;
import com.vsv.utils.StaticUtils;

public class Laser extends TextureObject implements Projectile {

    public static final DrawPriority drawPriority = new DrawPriority(DrawPriority.DYNAMIC_OBJECTS, DrawPriority.DYNAMIC_PRIORITY_LASER);

    private float pastTime = 0;

    private final float width;

    private boolean shoot;

    private float damage;

    private GameObject target;

    private float startPositionX;

    private float startPositionY;

    private float endPositionX;

    private float endPositionY;

    private GameObject parent;

    private float lastRatio;

    private float emptyPositionY; // if we do not have a target but laser is still work.

    private float emptyPositionX;

    @SuppressWarnings("FieldCanBeLocal")
    private final float lightWidth = 64;

    @SuppressWarnings("FieldCanBeLocal")
    private final float lightHeight = 64;

    private final TextureObject light;

    @SuppressWarnings("FieldCanBeLocal")
    private final int speedLaserFactor = 5;

    private int lastTargetId;

    private int smokeCounter = 0;

    private final float[] colorRed = TextEnemyProperties.generateArgbColor(0xFFFF4343);

    private final float[] colorGreen = TextEnemyProperties.generateArgbColor(0xFF43FF46);

    private final float[] colorBlue = TextEnemyProperties.generateArgbColor(0xFF43FFEF);

    private final float[][] colors = new float[][]{colorRed, colorGreen, colorBlue};

    public Laser() {
        super(32 * ScreenParameters.screenScaleFactor, 32 * ScreenParameters.screenScaleFactor, ShootAsset.getAsset().laser);
        this.width = 32 * ScreenParameters.screenScaleFactor;
        light = new TextureObject(0, 0, ShootAsset.getAsset().gaussExplosion) {

            @Override
            public boolean canRemoveFromRendering() {
                return false;
            }

            @NonNull
            @Override
            public DrawPriority getDrawPriority() {
                return drawPriority;
            }
        };
    }

    public void setRandomColor() {
        setColor(colors[StaticUtils.random.nextInt(colors.length)]);
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
    public void act(float dt, @NonNull ObjectManager<GameObject> manager) {
        if (target != null && !target.interact()) {
            target = null;
        }
        if (shoot && target != null && target.getId() != lastTargetId) {
            target = null;
        }
        if (target == null && pastTime == 0) {
            return;
        }
        if (!shoot) {
            pastTime = 0;
            return;
        }
        pastTime += dt * speedLaserFactor;
        if (pastTime > Math.PI) {
            pastTime = 0;
            shoot = false;
        }
        if (target != null) {
            target.takeHit(damage * dt);
            endPositionX = target.getX();
            endPositionY = target.getY();
            light.setPosition(endPositionX, endPositionY);
            light.act(dt, manager);
            int noise = (int) (StaticUtils.random.nextInt(30) * ScreenParameters.screenScaleFactor);
            light.resize(lightWidth * ScreenParameters.screenScaleFactor + noise,
                    lightHeight * ScreenParameters.screenScaleFactor + noise, endPositionX, endPositionY, light.getScaleFactor());
            if (smokeCounter++ > 4) {
                smokeCounter = 0;
                SmokeObject smokeObject = manager.getObjectFromPool(SmokeObject.class);
                smokeObject.reset();
                smokeObject.resize(48 * ScreenParameters.screenScaleFactor, 48 * ScreenParameters.screenScaleFactor);
                smokeObject.setView(ShootAsset.getAsset().blackSmoke, 0);
                smokeObject.setFrameTime(0.2f);
                smokeObject.setXYSpeed(ScreenParameters.screenScaleFactor * (StaticUtils.random.nextInt(40) - 20),
                        -100 * ScreenParameters.screenScaleFactor);
                smokeObject.setPosition(endPositionX, endPositionY, 1);
                manager.addObject(smokeObject);
            }
            setRotateAngle(((ProjectileTurret) parent).getShootAngle() + 90);
        }
        if (target == null) {
            if (emptyPositionX == -1) {
                if (lastRatio < 0) {
                    emptyPositionX = endPositionX - ScreenParameters.screenWidth;
                    emptyPositionY = endPositionY - ScreenParameters.screenWidth * Math.abs(lastRatio);
                } else if (lastRatio == 0) {
                    emptyPositionX = startPositionX;
                    emptyPositionY = 0;
                } else {
                    emptyPositionX = endPositionX + ScreenParameters.screenWidth;
                    emptyPositionY = endPositionY - ScreenParameters.screenWidth * Math.abs(lastRatio);
                }
            }
            endPositionX = emptyPositionX;
            endPositionY = emptyPositionY;
        }
        float dx = endPositionX - startPositionX;
        float dy = startPositionY - endPositionY;
        if (target != null) {
            if (dx == 0) {
                lastRatio = 0;
            } else {
                lastRatio = dy / dx;
            }

        }
        float distance = GameMath.getDistance(dx, dy);
        float centerX = dx / 2 + startPositionX;
        float centerY = dy / 2 + endPositionY;
        float result = (float) Math.sin(pastTime);
        resize(width * result, distance, centerX, centerY, getScaleFactor());
        if (target != null && !target.interact()) {
            target = null;
        }
    }

    @Override
    public boolean canRemoveFromRendering() {
        return !shoot;
    }

    public boolean canRotate() {
        return !shoot || (target != null && target.interact());
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

    @Override
    public void draw(@NonNull DrawInstruments<?> drawInstruments) {
        super.draw(drawInstruments);
        if (target != null) {
            light.draw(drawInstruments);
        }
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
    public void explode(float x, float y, @NonNull ObjectManager<GameObject> objectManager) {

    }

    @NonNull
    @Override
    public Projectile setup(float x, float y, @NonNull GameObject parent, @NonNull GameObject target,
                            float projectileSpeed, float shootAngle, float damage) {
        if (shoot || !target.interact()) {
            return this;
        }
        this.damage = damage;
        this.lastTargetId = target.getId();
        this.target = target;
        startPositionX = x;
        startPositionY = y;
        emptyPositionY = -1;
        emptyPositionX = -1;
        endPositionX = target.getX();
        endPositionY = target.getY();
        light.setPosition(endPositionX, endPositionY);
        pastTime = 0;
        shoot = true;
        this.parent = parent;
        return this;
    }
}
