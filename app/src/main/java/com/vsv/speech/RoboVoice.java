package com.vsv.speech;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Locale;

public class RoboVoice {

    private static final TwoSpeakers twoSpeakers = new TwoSpeakers();

    private static Locale lastUsedFirstLocale;

    private static Locale lastUsedSecondLocale;

    private RoboVoice() {

    }

    public static void init() {
        twoSpeakers.initSpeakers();
        restoreLast();
    }

    public static void restoreLast() {
        if (lastUsedFirstLocale != null && lastUsedSecondLocale != null) {
            twoSpeakers.setLanguages(lastUsedFirstLocale, lastUsedSecondLocale);
        }
    }

    public static void setLanguages(@Nullable Locale firstLocale, @Nullable Locale secondLocale) {
        lastUsedFirstLocale = firstLocale;
        lastUsedSecondLocale = secondLocale;
        twoSpeakers.setLanguages(firstLocale, secondLocale);
    }

    @NonNull
    public static TwoSpeakers getInstance() {
        return twoSpeakers;
    }

    public static void stopSpeaking() {
        twoSpeakers.stopSpeaking();
    }

    public static void shutdown() {
        twoSpeakers.shutdown();
    }
}
