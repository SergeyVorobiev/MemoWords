package com.vsv.game.engine.objects;

import android.graphics.Paint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vsv.game.engine.DrawPriority;
import com.vsv.game.engine.GameObject;
import com.vsv.game.engine.ShootAsset;
import com.vsv.game.engine.ObjectManager;
import com.vsv.utils.GameMath;
import com.vsv.utils.StaticUtils;

public class Star extends TextureObject {

    public static final DrawPriority drawPriority = new DrawPriority(DrawPriority.BACKGROUND, DrawPriority.BACKGROUND_PRIORITY_STARS);

    private float pastTime;

    private float scaleStarSpeed;

    private float scaleCometSpeed;

    private float scaleGalaxySpeed;

    private float scaleSpeed;

    private float rotationSpeed;

    private float speedX;

    private float speedY;

    public Star() {
        super(0, 0, ShootAsset.getAsset().star);
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

    public void setupKind(int cometChance, int galaxyChance, float cometSpeed, float starSpeed,
                          TextureRegionProperties galaxyProperties, TextureRegionProperties startProperties) {
        boolean comet = StaticUtils.random.nextInt(cometChance) == 0;
        boolean isGalaxy = StaticUtils.random.nextInt(galaxyChance) == 0;
        setView(isGalaxy && !comet ? galaxyProperties : startProperties, 0);
        if (comet) {
            float angle = (float) StaticUtils.random.nextInt(360);
            speedX = GameMath.getXProjection(angle, cometSpeed);
            speedY = GameMath.getYProjection(angle, cometSpeed);
            scaleSpeed = scaleCometSpeed;
        } else {
            scaleSpeed = isGalaxy ? scaleGalaxySpeed : scaleStarSpeed;
            speedX = 0;
            speedY = starSpeed;
        }
    }

    public void setScaleSpeed(float scaleStarSpeed, float scaleCometSpeed, float scaleGalaxySpeed) {
        this.scaleStarSpeed = scaleStarSpeed;
        this.scaleCometSpeed = scaleCometSpeed;
        this.scaleGalaxySpeed = scaleGalaxySpeed;
    }

    public void reset() {
        pastTime = 0;
    }

    public void setRotationSpeed(float rotationSpeed) {
        this.rotationSpeed = rotationSpeed;
    }

    @Override
    public void act(float dt, @NonNull ObjectManager<GameObject> manager) {
        pastTime += dt * scaleSpeed;
        float scale = (float) Math.sin(pastTime);
        if (speedX != 0 || speedY != 0) {
            float x = getX() + speedX * dt;
            float y = getY() + speedY * dt;
            setPosition(x, y, scale);
        } else {
            setPosition(getX(), getY(), scale);
        }
        setRotateAngle(getRotateAngle() + rotationSpeed * dt);
    }

    @Override
    public boolean canRemoveFromRendering() {
        return getScaleFactor() <= 0;
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
