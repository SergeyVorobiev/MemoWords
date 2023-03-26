package com.vsv.statics;

import android.content.Context;
import android.util.DisplayMetrics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vsv.memorizer.MainActivity;

import java.lang.ref.WeakReference;

public final class WeakContext {

    private WeakContext() {

    }

    private static WeakReference<Context> contextRef;

    public static void setupContext(Context context) {
        contextRef = new WeakReference<>(context);
    }

    public static @NonNull
    Context getContext() {
        if (contextRef == null) {
            throw new RuntimeException("Context ref is null");
        }
        Context context = contextRef.get();
        if (context == null) {
            throw new RuntimeException("Context is null");
        }
        return context;
    }

    public static @NonNull
    MainActivity getMainActivity() {
        return (MainActivity) getContext();
    }

    public static @Nullable
    MainActivity getMainActivityOrNull() {
        return (MainActivity) getContextOrNull();
    }

    public static @NonNull
    DisplayMetrics buildDisplayMetrics() {
        DisplayMetrics result = new DisplayMetrics();
        getMainActivity().getWindowManager().getDefaultDisplay().getMetrics(result);
        return result;
    }

    public static @Nullable
    Context getContextOrNull() {
        if (contextRef == null) {
            throw new RuntimeException("Context ref is null");
        }
        return contextRef.get();
    }
}
