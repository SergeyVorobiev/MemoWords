package com.vsv.game.engine;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

public class TextureSettings {

    public final int magFilter;

    public final int minFilter;

    public final Bitmap bitmap;

    public final int slot;

    public TextureSettings(@NonNull Bitmap bitmap, int slot, int magFilter, int minFilter) {
        this.bitmap = bitmap;
        this.slot = slot;
        this.magFilter = magFilter;
        this.minFilter = minFilter;
    }

}
