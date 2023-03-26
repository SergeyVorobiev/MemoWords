package com.vsv.game.engine;

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.vsv.game.engine.objects.TextureDrawable;
import com.vsv.game.engine.screens.VertexData;
import com.vsv.game.engine.screens.VertexGL2;
import com.vsv.toasts.Toasts;

import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL10;

public class GL20DrawInstruments extends DrawInstruments<GL10> {

    private final VertexGL2 vertex2;

    private boolean loaded;

    public GL20DrawInstruments() {
        vertex2 = new VertexGL2(15, 500);
    }

    @Override
    public boolean initDrawer(@NonNull GL10 drawer, float width, float height) {
        super.initDrawer(drawer, width, height);
        Bitmap[] bitmaps = ShootAsset.getAsset().textures;
        int textureSize = bitmaps.length;
        if (textureSize > 32) {
            throw new RuntimeException("Too many textures: " + textureSize + ", 32 max");
        }
        IntBuffer buffer = IntBuffer.wrap(new int[1]);
        GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, buffer);
        int maxResolution = buffer.get(0);
        Log.d("GL", "MaxResolution: " + maxResolution);
        for (int i = 0; i < textureSize; i++) {
            Bitmap bitmap = bitmaps[i];
            if (bitmap.getWidth() > maxResolution) {
                bitmaps[i] = Bitmap.createScaledBitmap(bitmap, maxResolution, maxResolution, false);
            }
        }
        buffer.clear();
        GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_IMAGE_UNITS, buffer);
        int texturesCount = buffer.get(0);
        if (texturesCount < textureSize) {
            new Handler(Looper.getMainLooper()).post(() -> Toasts.notEnoughTextureSize(textureSize));
            return false;
        }
        Log.d("GL", "Max image units: " + texturesCount);
        int slot = GLES20.GL_TEXTURE0;
        int[] slots = new int[textureSize];
        for (int i = 0; i < textureSize; i++) {
            slots[i] = slot;
            slot += 1;
        }
        TextureSettings[] textureSettings = new TextureSettings[textureSize];
        for (int i = 0; i < textureSettings.length; i++) {
            // GLES20.GL_NEAREST = best
            // GLES20.GL_LINEAR = best
            // GL_NEAREST_MIPMAP_NEAREST ?
            // GL_LINEAR_MIPMAP_LINEAR no
            // GL_NEAREST_MIPMAP_LINEAR no
            // GLES20.GL_LINEAR_MIPMAP_NEAREST no
            textureSettings[i] = new TextureSettings(bitmaps[i], slots[i], GLES20.GL_NEAREST, GLES20.GL_NEAREST);
        }
        try {
            vertex2.setupGL(width, height, textureSettings);
            loaded = true;
        } catch (Throwable th) {
            Log.e("GL", "Can not init gl: " + th);
            new Handler(Looper.getMainLooper()).post(Toasts::cannotLoadGL);
            return false;
        }
        return true;
    }

    @Override
    public void fillScreen(float[] argb) {
        GLHelpers.clearScreenGL2(argb);
    }

    @Override
    public void drawBitmapInRect(Bitmap bitmap, Rect sourceRect, RectF destinationRect, Paint paint) {

    }

    @Override
    public void draw(TextureDrawable object) {
        if (loaded) {
            vertex2.put(object.getVertexData());
        }
    }

    @Override
    public void draw(VertexData data) {
        if (loaded) {
            vertex2.put(data);
        }
    }

    @Override
    public void commitDrawing() {
        if (loaded) {
            vertex2.draw();
        }
    }

    public void reloadTexture(Bitmap bitmap, int index, boolean recycle) {
        TextureSettings textureSettings = new TextureSettings(bitmap, GLES20.GL_TEXTURE0 + index, GLES20.GL_NEAREST, GLES20.GL_NEAREST);
        vertex2.reloadTexture(textureSettings, index, recycle);
    }

    @Override
    public void releaseResources() {
        try {
            vertex2.releaseResources();
        } catch (Throwable th) {
            Log.e("GL", "Can not release resources: " + th);
        }
    }
}
