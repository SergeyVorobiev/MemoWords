package com.vsv.dialogs;

import androidx.annotation.MainThread;

import com.vsv.toasts.Toasts;

public class SingleWindow {

    private static boolean SHOWN = false;

    @MainThread
    public static boolean isShown() {
        return SHOWN;
    }

    @MainThread
    public static void setShown(boolean shown) {
        SHOWN = shown;
    }

    @MainThread
    public static boolean isShownToast() {
        if (SHOWN) {
            // Toasts.tooFast();
        }
        return SHOWN;
    }
}
