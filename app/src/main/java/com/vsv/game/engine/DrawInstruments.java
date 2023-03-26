package com.vsv.game.engine;

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import androidx.annotation.NonNull;

import com.vsv.game.engine.objects.TextureDrawable;
import com.vsv.game.engine.screens.VertexData;

public abstract class DrawInstruments<T> {

    protected T drawer;

    public abstract void fillScreen(float[] argb);

    public boolean initDrawer(@NonNull T drawer, float width, float height) {
        this.drawer = drawer;
        return true;
    }

    // Override this method if you want to setup drawer on each frame.
    public void setCurrentDrawer(@NonNull T drawer) {

    }

    public abstract void drawBitmapInRect(Bitmap bitmap, Rect sourceRect, RectF destinationRect, Paint paint);

    public abstract void draw(TextureDrawable object);

    public abstract void draw(VertexData data);

    public abstract void commitDrawing();

    public abstract void releaseResources();
}
