package com.vsv.game.engine;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;

import androidx.annotation.NonNull;

import com.vsv.game.engine.objects.TextureDrawable;
import com.vsv.game.engine.objects.TextureRegion;
import com.vsv.game.engine.screens.VertexData;

public class CanvasDrawInstruments extends DrawInstruments<Canvas> {

    public final Paint strokePaint;

    public final Paint fillPaint;

    public final Paint colorFilter;

    private final Rect regionRect = new Rect();

    public CanvasDrawInstruments() {
        strokePaint = new Paint();
        strokePaint.setAntiAlias(true);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(8);
        fillPaint = new Paint();
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setAntiAlias(true);
        colorFilter = new Paint();
    }

    public void fillScreen(float[] argb) {
        Paint paint = this.fillPaint;
        paint.setColor(Color.argb(argb[0], argb[1], argb[2], argb[3]));
        drawer.drawRect(0, 0, drawer.getWidth(), drawer.getHeight(), paint);
    }

    public void setCurrentDrawer(@NonNull Canvas drawer) {
        this.drawer = drawer;
    }

    public void drawBitmapInRect(Bitmap bitmap, Rect sourceRect, RectF destinationRect, Paint paint) {
        drawer.drawBitmap(bitmap, sourceRect, destinationRect, paint);
    }

    public void draw(TextureDrawable object) {
        Paint paint = object.getPaint();
        if (paint != null && object.getColorFilter() != null) {
            paint = colorFilter;
            colorFilter.setColorFilter((PorterDuffColorFilter) object.getColorFilter());
        }
        float angle = object.getRotateAngle();
        RectF position = object.getPosition();
        Bitmap bitmap = ShootAsset.getAsset().getTexture(object.getTextureId());
        if (angle != 0) {
            drawer.save();
            drawer.rotate(-angle, position.centerX(), position.centerY());
            TextureRegion region = object.getTextureRegion();
            regionRect.set((int) region.x1, (int) region.y2Inverse, (int) region.x2, (int) region.y1Inverse);
            drawer.drawBitmap(bitmap, regionRect, position, paint);
            drawer.restore();
        } else {
            drawer.drawBitmap(bitmap, regionRect, position, paint);
        }
    }

    @Override
    public void draw(VertexData data) {

    }

    @Override
    public void commitDrawing() {

    }

    @Override
    public void releaseResources() {

    }
}
