package com.vsv.entities;

import android.graphics.drawable.TransitionDrawable;

import com.vsv.memorizer.R;
import com.vsv.utils.StaticUtils;

public class SampleView {

    public final TransitionDrawable sampleDrawable;

    public final TransitionDrawable percentageDrawable;

    public float elapsedTransitionSampleTime;

    public float elapsedTransitionPercentageTime;

    public SampleView() {
        sampleDrawable = (TransitionDrawable) StaticUtils.getDrawable(R.drawable.bg_item_sample_transition);
        percentageDrawable = (TransitionDrawable) StaticUtils.getDrawable(R.drawable.bg_item_perc_transition);
    }
}
