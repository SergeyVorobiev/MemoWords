package com.vsv.utils;

import android.graphics.Typeface;

public class StaticFonts {

    public static final Typeface[] fonts = new Typeface[]{Typeface.SERIF, Typeface.DEFAULT, Typeface.MONOSPACE, null, null, null, null};

    static {
        fonts[3] = Typeface.create("cursive", Typeface.BOLD);
        fonts[4] = Typeface.create("casual", Typeface.BOLD);
        fonts[5] = Typeface.create("sans-serif-smallcaps", Typeface.BOLD);
        fonts[6] = Typeface.create("serif-monospace", Typeface.BOLD);
    }
}
