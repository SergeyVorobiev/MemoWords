package com.vsv.game.engine.objects;

import android.graphics.Paint;
import android.graphics.PorterDuffColorFilter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vsv.game.engine.GameObject;
import com.vsv.game.engine.ObjectManager;

public abstract class SpriteObject extends TextureObject {

    private final int startFrame;

    private boolean cycle;

    private float frameTime;

    private float currentFrameTime;

    private boolean stop;

    public SpriteObject(float width, float height, @NonNull TextureRegionProperties regionProperties,
                        int startFrame, float frameTime, boolean cycle) {
        super(width, height, regionProperties, startFrame);
        this.startFrame = startFrame;
        this.cycle = cycle;
        this.frameTime = frameTime;
    }

    public SpriteObject(float width, float height, @NonNull TextureRegionProperties[] regionsProperties,
                        int startFrame, float frameTime, boolean cycle) {
        super(width, height, regionsProperties, startFrame);
        this.startFrame = startFrame;
        this.cycle = cycle;
        this.frameTime = frameTime;
    }

    public void reset() {
        stop = false;
        currentFrameTime = 0;
        setFrame(startFrame);
    }

    public void setCycle(boolean cycle) {
        this.cycle = cycle;
    }

    public void setFrameTime(float frameTime) {
        this.frameTime = frameTime;
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
        super.act(dt, objectManager);
        if (frameTime > 0 && currentFrameTime >= frameTime) {
            currentFrameTime = 0;
            int frame = getFrame() + 1;
            if (frame == getFrameCount()) {
                if (cycle) {
                    setFrame(0);
                } else {
                    stop = true;
                }
            } else {
                setFrame(frame);
            }
        }
        currentFrameTime += dt;
    }

    @Override
    public boolean canRemoveFromRendering() {
        return stop;
    }

    @Override
    public void takeHit(float damage) {

    }

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
