package com.vsv.game.engine.objects;

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vsv.game.engine.DrawInstruments;
import com.vsv.game.engine.DrawPriority;
import com.vsv.game.engine.GameObject;
import com.vsv.game.engine.ShootAsset;
import com.vsv.game.engine.ObjectManager;
import com.vsv.game.engine.objects.properties.TextEnemyProperties;
import com.vsv.game.engine.screens.ScreenParameters;
import com.vsv.utils.GameMath;
import com.vsv.utils.StaticUtils;

public class TextEnemy extends TextureObject {

    public static final DrawPriority drawPriority = new DrawPriority(DrawPriority.DYNAMIC_OBJECTS, DrawPriority.DYNAMIC_PRIORITY_ENEMY);

    public static final int DIED = 0;

    public static final int LIVE = 1;

    public static final int DESTROYED = 2;

    private int id;

    public PorterDuffColorFilter colorFilter;

    private float hitForce;

    private int state;

    private float destroyDx;

    private float destroyDy;

    private boolean isDestroySpeedCalculated;

    private float destroyDistance;

    private float yShift;

    private float speed;

    private final ViewEnemy viewEnemy;

    private float currentHealth;

    private int destroyX;

    private int destroyY;

    private float rotateSpeed;

    private float slowDownSpeedFactor;

    private float destroySpeed;

    private float yShiftBackWhenHit;

    private float liveRotateAngle;

    private Rect textBoundRect;

    private boolean main;

    private final Object mainKey = new Object();

    private float width;

    private float height;

    private float correctDestroyPointX;

    private float wrongDestroyPointX;

    private Bitmap toDrawText;

    private int textTextureId;

    private int defaultColorIndex;

    private final float[] defaultColorRose = TextEnemyProperties.generateArgbColor(0xFFFFDDED);

    private final float[] defaultColorWhite = new float[]{1f, 1f, 1f, 1f};

    private final float[] defaultColorGreen = TextEnemyProperties.generateArgbColor(0xFFDDFFF1);

    private final float[] defaultColorMagenta = TextEnemyProperties.generateArgbColor(0xFFEFCBFF);

    private final float[] defaultColorGold = TextEnemyProperties.generateArgbColor(0xFFFFF5DD);

    private final float[][] defaultColors = new float[][]{defaultColorRose, defaultColorWhite, defaultColorMagenta, defaultColorGold, defaultColorGreen};

    private String text;

    private boolean rotateSignal;

    private float rotate360Angle;

    private final TextDrawer textDrawer;

    public TextEnemy() {
        super(0, 0, ShootAsset.getAsset().textCloud);
        viewEnemy = new ViewEnemy(this.getVertexData());
        textDrawer = new TextDrawer();
    }

    public Bitmap getTextBitmap() {
        return toDrawText;
    }

    public void setupView(float width, float height) {
        if (width != this.width || height != this.height || toDrawText == null) {
            toDrawText = Bitmap.createBitmap((int) width, (int) height, Bitmap.Config.ARGB_8888);
            this.width = width;
            this.height = height;
            resize(width, height);
            Rect textBitmapRect = new Rect(0, 0, (int) width, (int) height);
            textBoundRect = new Rect();
            int horizontalPadding = (int) (width * 0.2); // 20%
            int verticalPadding = (int) (height * 0.2); // 20%
            textBoundRect.set(textBitmapRect.left + horizontalPadding,
                    textBitmapRect.top + verticalPadding,
                    textBitmapRect.right - horizontalPadding,
                    textBitmapRect.bottom - verticalPadding);
        }
    }

    public void setupDestroyPoints(float correctDestroyPointX, float wrongDestroyPointX, float destroyPointY) {
        this.correctDestroyPointX = correctDestroyPointX;
        this.wrongDestroyPointX = wrongDestroyPointX;
        this.destroyY = (int) destroyPointY;
    }

    public void setHealth(float health, boolean isCorrect) {
        destroyX = isCorrect ? (int) correctDestroyPointX : (int) wrongDestroyPointX;
        this.currentHealth = health;
    }

    @Nullable
    public String getText() {
        return text;
    }

    public void rotate360() {
        rotateSignal = true;
    }

    public void setup(String text, float x, float y, float speed, float startRotateAngle,
                      float rotateSpeed, float liveRotateAngle, float textSize, float slowDownSpeedFactor,
                      float destroySpeed, float yShiftBackWhenHit) {
        this.text = text;
        rotateSignal = false;
        rotate360Angle = 0;
        this.slowDownSpeedFactor = slowDownSpeedFactor;
        this.destroySpeed = destroySpeed;
        this.yShiftBackWhenHit = yShiftBackWhenHit;
        this.liveRotateAngle = liveRotateAngle;
        defaultColorIndex = StaticUtils.random.nextInt(defaultColors.length);
        vertexData.setColor(defaultColors[defaultColorIndex]);
        this.speed = speed;
        synchronized (mainKey) {
            this.main = false;
        }
        state = LIVE;
        colorFilter = null;
        currentHealth = TextEnemyProperties.HEALTH_ONE;
        this.rotateSpeed = rotateSpeed;
        setRotateAngle(startRotateAngle);
        setPosition(x, y, 0.1f);
        isDestroySpeedCalculated = false;
        destroyDistance = 0;
        textDrawer.setupTextInCenter(toDrawText, true, textBoundRect, text, textSize);
    }

    public void setMain() {
        synchronized (mainKey) {
            this.main = true;
        }
    }

    @Override
    public int getState() {
        return 0;
    }

    @Override
    public boolean interact() {
        return state == LIVE;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    private void updateState() {
        if (state == LIVE && (currentHealth <= 0 || getY() >= ScreenParameters.screenHeight * 0.8f)) {
            state = DESTROYED;
        } else if (state == DESTROYED && getY() <= 0) {
            state = DIED;
        }
    }

    private void rotate360Process(float dt) {
        if (rotate360Angle < 360) {
            rotate360Angle += dt * 900;
            rotate360Angle = Math.min(360, rotate360Angle);
            setRotateAngle(rotate360Angle);
        } else {
            rotate360Angle = 0;
            rotateSignal = false;
        }
    }

    @Override
    public void act(float dt, @NonNull ObjectManager<GameObject> objectManager) {
        updateState();
        if (state == LIVE) {
            living(dt);
        } else if (state == DESTROYED) {
            destroying(dt);
        }
        hitForce = 0;
        yShift = 0;
    }

    private void living(float dt) {
        speed -= slowDownSpeedFactor * dt;
        if (speed < 0) {
            speed = 0;
        }
        float y = getY() + speed * dt;
        float scaleFactor = getScaleFactor();
        if (scaleFactor < 1) {
            scaleFactor += (5 * dt);
            scaleFactor = Math.min(1, scaleFactor);
            setPosition(getX(), y, scaleFactor);
            vertexData.setColor(defaultColors[defaultColorIndex]);
        } else {
            if (hitForce > 0) {
                colorFilter = TextEnemyProperties.redColorFilter;
                vertexData.setColor(TextEnemyProperties.redColorParts);
            } else if (main) {
                colorFilter = TextEnemyProperties.blueColorFilter;
                vertexData.setColor(TextEnemyProperties.blueColorParts);
            } else {
                colorFilter = null;
                vertexData.setColor(defaultColors[defaultColorIndex]);
            }
            float scaleShift = 1 + hitForce / 1000;
            if (scaleShift > 1.07) {
                scaleShift = 1.07f;
            }
            setPosition(getX(), y - yShift * dt, scaleShift);
            setRotateAngle(getRotateAngle() + liveRotateAngle * dt);
            if (rotateSignal) {
                rotate360Process(dt);
            }
        }
    }

    private void destroying(float dt) {
        synchronized (mainKey) {
            main = false;
        }
        if (!isDestroySpeedCalculated) {
            isDestroySpeedCalculated = true;
            float angle = GameMath.getAngleInDegrees(getX(), getY(), destroyX, destroyY);
            destroyDx = GameMath.getXProjection(angle, destroySpeed);
            destroyDy = GameMath.getYProjection(angle, destroySpeed);
            destroyDistance = GameMath.getDistance(getX() - destroyX, getY() - destroyY);
        }
        float currentDistance = GameMath.getDistance(getX() - destroyX, getY() - destroyY);
        float scaleFactor = destroyDistance == 0 ? 0 : currentDistance / destroyDistance;
        float x = getX() + destroyDx * dt;
        float y = getY() - destroyDy * dt;
        setPosition(x, y, scaleFactor);
        setRotateAngle(getRotateAngle() + rotateSpeed * dt);
    }

    public void setTextTextureId(int textTextureId) {
        this.textTextureId = textTextureId;
    }

    @Override
    public void takeHit(float damage) {
        currentHealth -= damage;
        yShift = yShiftBackWhenHit;
        hitForce = damage;
    }

    @NonNull
    @Override
    public DrawPriority getDrawPriority() {
        return drawPriority;
    }

    @Override
    public boolean canRemoveFromRendering() {
        return state == DIED;
    }

    @Override
    public void destroy() {
        toDrawText.recycle();
    }

    @Nullable
    @Override
    public PorterDuffColorFilter getColorFilter() {
        return colorFilter;
    }

    @Nullable
    @Override
    public Paint getPaint() {
        return textDrawer.getTextPaint();
    }

    public void draw(@NonNull DrawInstruments<?> drawInstruments) {
        super.draw(drawInstruments);
        viewEnemy.draw(drawInstruments, textTextureId);
        // drawInstruments.drawBitmapInRect(drawnText, textBitmapRect, getPosition(), null);
    }
}
