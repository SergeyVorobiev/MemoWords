package com.vsv.game.engine;

import android.graphics.RectF;

import androidx.annotation.NonNull;

public interface PositionInfo {

    float getX();

    float getY();

    @NonNull
    RectF getPosition();
}
