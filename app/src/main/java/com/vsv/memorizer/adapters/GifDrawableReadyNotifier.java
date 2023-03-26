package com.vsv.memorizer.adapters;

import com.bumptech.glide.load.resource.gif.GifDrawable;

@FunctionalInterface
public interface GifDrawableReadyNotifier {

    void resourceReady(GifDrawable drawable, int itemIndex);
}
