package com.vsv.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.AnimationSet;

import androidx.annotation.NonNull;

public class MyAnimation extends AnimationSet {

    public MyAnimation(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @NonNull
    public AnimationSet copy() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
