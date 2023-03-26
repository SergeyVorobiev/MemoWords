package com.vsv.game.engine.objects;

import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vsv.game.engine.DrawInstruments;
import com.vsv.game.engine.GameObject;
import com.vsv.game.engine.ObjectManager;
import com.vsv.game.engine.screens.ScreenParameters;
import com.vsv.game.engine.screens.VertexData;

public abstract class TextureObject implements GameObject, TextureDrawable {

    private float halfWidth;

    private float halfHeight;

    private float rotateSpeed;

    private final RectF positionRect;

    private int textureId;

    private TextureRegion[] textureRegions;

    protected final VertexData vertexData;

    private int frame;

    protected float speedX;

    protected float speedY;

    @Nullable
    protected PorterDuffColorFilter colorFilter;

    public TextureObject(float width, float height) {
        this(width, height, (TextureRegionProperties) null, 0);
    }

    public TextureObject(float width, float height, @Nullable TextureRegionProperties regionProperties) {
        this(width, height, regionProperties, 0);
    }

    public TextureObject(float width, float height, @Nullable TextureRegionProperties regionProperties, int frame) {
        this.halfWidth = width / 2.0f;
        this.halfHeight = height / 2.0f;
        this.positionRect = new RectF();
        vertexData = new VertexData(width, height, regionProperties == null ? -1 : regionProperties.textureId);
        if (regionProperties != null) {
            setView(regionProperties, frame);
        }
    }

    public TextureObject(float width, float height, @NonNull TextureRegionProperties[] regionsProperties, int frame) {
        this.halfWidth = width / 2.0f;
        this.halfHeight = height / 2.0f;
        this.positionRect = new RectF();
        vertexData = new VertexData(width, height, regionsProperties[0].textureId);
        setView(regionsProperties, frame);
    }

    public void setXYSpeed(float speedX, float speedY) {
        this.speedX = speedX;
        this.speedY = speedY;
    }

    public int getFrameCount() {
        return this.textureRegions.length;
    }

    public void setFrame(int frame) {
        this.frame = frame;
        vertexData.setupTexturePosition(textureRegions[frame], textureRegions[frame].textureId);
    }

    @Override
    public void act(float dt, @NonNull ObjectManager<GameObject> objectManager) {
        if (speedX != 0 || speedY != 0) {
            float dx = speedX * dt;
            float dy = speedY * dt;
            setPosition(getX() + dx, getY() + dy);
        }
    }

    public int getFrame() {
        return frame;
    }

    public void setView(@NonNull TextureRegionProperties regionProperties, int frame) {
        this.frame = frame;
        if (textureRegions != null && textureRegions.length != regionProperties.frames) {
            textureRegions = null;
        }
        this.textureRegions = TextureRegion.buildRegions(regionProperties, textureRegions);
        this.textureId = regionProperties.textureId;
        vertexData.setupTexturePosition(textureRegions[frame], textureRegions[frame].textureId);
    }

    // Be careful this method will create temporary arrays.
    public void setView(@NonNull TextureRegionProperties[] regionProperties, int frame) {
        this.frame = frame;
        int count = 0;
        for (TextureRegionProperties properties : regionProperties) {
            count += properties.frames;
        }
        textureRegions = new TextureRegion[count];
        int i = 0;
        for (TextureRegionProperties properties : regionProperties) {
            TextureRegion[] temp = TextureRegion.buildRegions(properties, null);
            for (TextureRegion region : temp) {
                textureRegions[i++] = region;
            }
        }
    }

    public TextureObject setRotateAngle(float rotateAngle) {
        this.vertexData.setAngle(rotateAngle);
        return this;
    }

    private void setPositionRect(float x, float y, float scaleFactor) {
        float w = this.halfWidth * scaleFactor;
        float h = this.halfHeight * scaleFactor;
        positionRect.set((x - w), (y - h), (x + w), (y + h));
        vertexData.setXY(positionRect.centerX(), ScreenParameters.screenHeight - positionRect.centerY());
        vertexData.scale(scaleFactor);
    }

    public void resize(float width, float height) {
        resize(width, height, getX(), getY(), getScaleFactor());
    }

    public void resize(float width, float height, float x, float y, float scaleFactor) {
        this.halfWidth = width / 2.0f;
        this.halfHeight = height / 2.0f;
        vertexData.setSize(width, height, scaleFactor);
        setPosition(x, y);
    }

    public TextureObject setPosition(float x, float y) {
        setPosition(x, y, vertexData.getScaleFactor());
        return this;
    }

    public void setPosition(float x, float y, float scaleFactor) {
        setPositionRect(x, y, scaleFactor);
    }

    public boolean canRotate() {
        return rotateSpeed > 0;
    }

    public TextureObject setMaxRotationSpeed(float rotateSpeed) {
        this.rotateSpeed = rotateSpeed;
        return this;
    }

    public void rotate(float angle, float dt) {
        float da = angle - vertexData.getAngle();
        da = adjustAngle(da, rotateSpeed * dt);
        vertexData.setAngle(vertexData.getAngle() + da);
    }

    public float adjustAngle(float da, float maxRotate) {
        if (Math.abs(da) > maxRotate) {
            da = da > 0 ? maxRotate : -maxRotate;
        }
        return da;
    }

    @Override
    public void destroy() {

    }

    @Override
    public void takeHit(float damage) {

    }

    public void setColor(float a, float r, float g, float b) {
        this.vertexData.setColor(a, r, g, b);
    }

    public void setColor(float[] argb) {
        this.vertexData.setColor(argb);
    }

    public void setColorFilterATOP(int color) {
        colorFilter = new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    }

    @Nullable
    @Override
    public PorterDuffColorFilter getColorFilter() {
        return colorFilter;
    }

    @Nullable
    @Override
    public Paint getPaint() {
        return null;
    }

    @Override
    public int getState() {
        return 0;
    }

    @Override
    public int getTextureId() {
        return textureId;
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

    @NonNull
    public TextureRegion getTextureRegion() {
        return textureRegions[frame];
    }

    @NonNull
    public VertexData getVertexData() {
        return vertexData;
    }

    @NonNull
    @Override
    public RectF getPosition() {
        return positionRect;
    }

    @Override
    public float getRotateAngle() {
        return vertexData.getAngle();
    }

    @Override
    public float getScaleFactor() {
        return vertexData.getScaleFactor();
    }

    public void draw(DrawInstruments<?> drawInstruments) {
        drawInstruments.draw(this);
    }

    public float getX() {
        return positionRect.centerX();
    }

    public float getY() {
        return positionRect.centerY();
    }
}
