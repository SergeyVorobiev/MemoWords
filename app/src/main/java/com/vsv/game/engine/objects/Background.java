package com.vsv.game.engine.objects;

import android.graphics.Color;
import android.graphics.RectF;

import androidx.annotation.NonNull;

import com.vsv.game.engine.DrawPriority;
import com.vsv.game.engine.GameObject;
import com.vsv.game.engine.DrawInstruments;
import com.vsv.game.engine.ObjectManager;
import com.vsv.game.engine.objects.properties.DustProperties;
import com.vsv.game.engine.objects.properties.StarProperties;
import com.vsv.game.engine.objects.properties.ToyProperties;

public class Background implements GameObject {

    private static final DrawPriority drawPriority = new DrawPriority(DrawPriority.BACKGROUND, 0);

    @SuppressWarnings("FieldCanBeLocal")
    private final float spawnDust = 0.3f;

    @SuppressWarnings("FieldCanBeLocal")
    private final float spawnToy = 0.8f;

    @SuppressWarnings("FieldCanBeLocal")
    private final float spawnStar = 0.03f;

    private float passedDustTime = 0;

    private float passedToyTime = 0;

    private float passedStarTime = 0;

    private int backgroundColor = 0xFF003A6C; // 0xff008ECA;// 0xff83daff; FF00396A

    private final float[] backgroundColorParts = new float[4];

    private final RectF position = new RectF();

    public Background() {
    }

    public float greenRatio = 136f / 255f;

    public float currentBlue = 108;

    public float currentGreen;

    public float one = -1;

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

    private void changeBackground(float dt) {
        backgroundColorParts[0] = 1;
        backgroundColorParts[1] = 0;
        backgroundColorParts[2] = currentGreen / 255;
        backgroundColorParts[3] = currentBlue / 255;
        backgroundColor = Color.argb(1, 0, backgroundColorParts[2], backgroundColorParts[3]);
        currentBlue += dt * one;
        if (currentBlue > 170) {
            currentBlue = 170;
            one = -one;
        } else if (currentBlue < 0 && one < 0) {
            currentBlue = 0;
            one = -one;
        }
        currentGreen = currentBlue * greenRatio;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    private void spawnDust(float dt, ObjectManager<GameObject> manager) {
        passedDustTime += dt;
        if (passedDustTime >= spawnDust) {
            passedDustTime = 0;
            Dust dust = manager.getObjectFromPool(Dust.class);
            DustProperties.defaultSetup(dust);
            manager.addObject(dust);
        }
    }

    private void spawnToy(float dt, ObjectManager<GameObject> manager) {
        passedToyTime += dt;
        if (passedToyTime >= spawnToy) {
            passedToyTime = 0;
            Toy toy = manager.getObjectFromPool(Toy.class);
            ToyProperties.defaultSetup(toy);
            manager.addObject(toy);
        }
    }

    private void spawnStar(float dt, ObjectManager<GameObject> manager) {
        passedStarTime += dt;
        if (passedStarTime >= spawnStar) {
            passedStarTime = 0;
            Star star = manager.getObjectFromPool(Star.class);
            StarProperties.defaultSetup(star);
            manager.addObject(star);
        }
    }

    @Override
    public void act(float dt, @NonNull ObjectManager<GameObject> objectManager) {
        changeBackground(dt);
        spawnStar(dt, objectManager);
        spawnDust(dt, objectManager);
        spawnToy(dt, objectManager);
    }

    @Override
    public boolean canRemoveFromRendering() {
        return false;
    }

    @Override
    public void draw(DrawInstruments<?> drawInstruments) {
        drawInstruments.fillScreen(backgroundColorParts);
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
        return 0;
    }

    @Override
    public float getY() {
        return 0;
    }

    @NonNull
    @Override
    public RectF getPosition() {
        return position;
    }
}
