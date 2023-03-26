package com.vsv.speech;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collection;
import java.util.Locale;

public class TwoSpeakers {

    private SingleSpeaker firstSpeaker;

    private SingleSpeaker secondSpeaker;

    private final Object sync = new Object();

    public TwoSpeakers() {

    }

    public void initSpeakers() {
        firstSpeaker = new SingleSpeaker();
        secondSpeaker = new SingleSpeaker();
    }

    public void setLanguages(@Nullable Locale firstLanguage, @Nullable Locale secondLanguage) {
        if (firstSpeaker == null || firstSpeaker.isError() || firstSpeaker.isShutdown()) {
            firstSpeaker = new SingleSpeaker();
        }
        firstSpeaker.setLanguageTag(firstLanguage);
        if (secondSpeaker == null || secondSpeaker.isError() || secondSpeaker.isShutdown()) {
            secondSpeaker = new SingleSpeaker();
        }
        secondSpeaker.setLanguageTag(secondLanguage);
    }

    public void speak(@Nullable String message, @Nullable Locale locale) {
        synchronized (sync) {
            if (firstSpeaker == null || secondSpeaker == null || locale == null) {
                return;
            }
            String tag = locale.toLanguageTag();
            if (firstSpeaker.getLanguageTag().equals(tag)) {
                secondSpeaker.stop();
                firstSpeaker.speak(message);
            } else if (secondSpeaker.getLanguageTag().equals(tag)) {
                firstSpeaker.stop();
                secondSpeaker.speak(message);
            } else {
                firstSpeaker.stop();
                secondSpeaker.stop();
            }
        }
    }

    public void shutdown() {
        stopSpeaking();
        synchronized (sync) {
            if (firstSpeaker != null) {
                firstSpeaker.shutdown();
                firstSpeaker = null;
            }
            if (secondSpeaker != null) {
                secondSpeaker.shutdown();
                secondSpeaker = null;
            }
        }
    }

    public int getStatus(@NonNull Locale locale) {
        synchronized (sync) {
            if (firstSpeaker == null || secondSpeaker == null) {
                return SingleSpeaker.FINISHED_SPEAKING;
            }
            if (firstSpeaker.getLanguageTag().equals(locale.toLanguageTag())) {
                return firstSpeaker.getStatus();
            } else if (secondSpeaker.getLanguageTag().equals(locale.toLanguageTag())) {
                return secondSpeaker.getStatus();
            }
        }
        return SingleSpeaker.FINISHED_SPEAKING;
    }

    public boolean waitFinishSpeaking(Locale locale, int surveyDelay) {
        String tag = locale.toLanguageTag();
        SingleSpeaker waitSpeaker;
        synchronized (sync) {
            if (firstSpeaker == null || secondSpeaker == null) {
                return false;
            }
            if (firstSpeaker.getLanguageTag().equals(tag)) {
                waitSpeaker = firstSpeaker;
            } else if (secondSpeaker.getLanguageTag().equals(tag)) {
                waitSpeaker = secondSpeaker;
            } else {
                return true;
            }
        }
        if (waitSpeaker != null) {
            return waitSpeaker.waitFinishSpeaking(surveyDelay);
        }
        return false;
    }

    public void stopSpeaking() {
        synchronized (sync) {
            if (firstSpeaker != null) {
                firstSpeaker.stop();
            }
            if (secondSpeaker != null) {
                secondSpeaker.stop();
            }
        }
    }
}
