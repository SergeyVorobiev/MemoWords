package com.vsv.game.engine.objects;

import android.graphics.Paint;
import android.graphics.PorterDuffColorFilter;

import androidx.annotation.NonNull;

import com.vsv.game.engine.DrawPriority;
import com.vsv.game.engine.GameObject;
import com.vsv.game.engine.DrawInstruments;
import com.vsv.game.engine.ObjectManager;
import com.vsv.game.engine.ShootAsset;
import com.vsv.utils.GameMath;

import java.util.ArrayList;

import javax.annotation.Nullable;

public class Cannon extends TextureObject {

    private static final DrawPriority drawPriority = new DrawPriority(DrawPriority.DYNAMIC_OBJECTS, DrawPriority.DYNAMIC_PRIORITY_CANNONS);

    private final ProjectileTurret projectileTurret;

    private boolean isLaser;

    private final TextureObject cannonPlatform;

    public Cannon(float width, float height, TextureRegionProperties textureRegionProperties, ProjectileTurret projectileTurret) {
        super(width, height, textureRegionProperties);
        this.cannonPlatform = new TextureObject(width, height, ShootAsset.getAsset().cannonCloud) {

            final DrawPriority drawPriority = new DrawPriority(DrawPriority.STATIC_OBJECTS, DrawPriority.STATIC_PRIORITY_PLATFORM);

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
                setRotateAngle(getRotateAngle() + dt * 10);
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
        };
        this.projectileTurret = projectileTurret;
    }

    public Cannon isLaser() {
        isLaser = true;
        return this;
    }

    public TextureObject install(float x, float y, ObjectManager<GameObject> objectManager) {
        super.setPosition(x, y);
        objectManager.addObject(this);
        this.cannonPlatform.setPosition(x, y);
        objectManager.addObject(this.cannonPlatform);
        this.projectileTurret.install(getPosition());
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

    @Override
    public void act(float dt, @NonNull ObjectManager<GameObject> objectManager) {
        GameObject target = chooseTarget(objectManager);
        float setAngle = computeAngle(target);
        if (!isLaser || projectileTurret.canRotate(objectManager)) {
            rotate(setAngle, dt);
        }
        boolean readyToShoot = (getRotateAngle() == setAngle || !canRotate());
        projectileTurret.setTarget(getRotateAngle(), readyToShoot ? target : null);
        projectileTurret.act(dt, objectManager);
    }

    public float computeAngle(@Nullable GameObject target) {
        return target == null ? 0 : (GameMath.getAngleInDegrees(this, target));
    }

    public @Nullable
    GameObject chooseTarget(ObjectManager<GameObject> objectManager) {
        ArrayList<GameObject> objects = objectManager.getPriorityGroup(DrawPriority.DYNAMIC_OBJECTS, DrawPriority.DYNAMIC_PRIORITY_ENEMY);
        for (int i = 0; i < objects.size(); i++) {
            GameObject gameObject = objects.get(i);
            if (gameObject.interact()) {
                return gameObject;
            }
        }
        return null;
    }

    @Override
    public boolean canRemoveFromRendering() {
        return false;
    }

    @Override
    public void draw(@NonNull DrawInstruments<?> drawInstruments) {
        super.draw(drawInstruments);
        projectileTurret.draw(drawInstruments);
        // drawInstruments.drawBitmapInRect(cannonBasement, getSourceRegion(), getPosition(), null);
    }

    @Override
    public void takeHit(float damage) {

    }

    @NonNull
    @Override
    public DrawPriority getDrawPriority() {
        return drawPriority;
    }

    public void commandShoot() {
        projectileTurret.commandShoot();
    }

    @Override
    @Nullable
    public PorterDuffColorFilter getColorFilter() {
        return null;
    }

    @Nullable
    @Override
    public Paint getPaint() {
        return null;
    }
}
