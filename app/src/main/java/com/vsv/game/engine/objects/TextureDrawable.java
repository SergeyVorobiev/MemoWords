package com.vsv.game.engine.objects;

import android.graphics.Paint;
import android.graphics.RectF;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vsv.game.engine.screens.VertexData;

public interface TextureDrawable {

    int getTextureId();

    @NonNull
    TextureRegion getTextureRegion();

    @NonNull
    VertexData getVertexData();

    @NonNull
    RectF getPosition();

    float getRotateAngle();

    float getScaleFactor();

    Object getColorFilter();

    @Nullable
    Paint getPaint();
}
