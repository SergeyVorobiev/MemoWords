package com.vsv.speech;

import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vsv.statics.GlobalExecutors;
import com.vsv.statics.WeakContext;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class SingleSpeaker extends UtteranceProgressListener {

    public static final int SHOULD_START_SPEAK = 0;

    public static final int SPEAKING = 1;

    public static final int FINISHED_SPEAKING = 2;

    private TextToSpeech speaker;

    private boolean isError = false;

    private final AtomicInteger status = new AtomicInteger(FINISHED_SPEAKING);

    private final AtomicBoolean stopSignal = new AtomicBoolean(false);

    private final AtomicBoolean shutdownSignal = new AtomicBoolean(false);

    private Locale locale;

    private String languageTag = "";

    private final Object setup = new Object();

    private volatile boolean isLoaded;

    public SingleSpeaker() {
        GlobalExecutors.modelsExecutor.execute(() -> speaker = new TextToSpeech(WeakContext.getContext(), status -> {
            if (status == TextToSpeech.ERROR) {
                isError = true;
            } else {
                synchronized (setup) {
                    /*
                    for (Locale locale : speaker.getAvailableLanguages()) {
                        Log.d("Locale", locale.toString());
                    }*/
                    speaker.setOnUtteranceProgressListener(this);
                    speaker.setSpeechRate(0.9f);
                    speaker.setPitch(0.8f);
                    if (locale != null) {
                        speaker.setLanguage(locale);
                        languageTag = locale.toLanguageTag();
                    } else {
                        languageTag = "";
                    }
                    isLoaded = true;
                }
            }
            if (shutdownSignal.get()) {
                speaker.shutdown();
            }
        }));
    }

    public void setLanguageTag(@Nullable Locale locale) {
        GlobalExecutors.modelsExecutor.execute(() -> {
            synchronized (setup) {
                if (locale != null) {
                    this.locale = locale;
                    if (speaker != null && isLoaded && !isShutdown()) {
                        speaker.setLanguage(locale);
                        speaker.speak("", TextToSpeech.QUEUE_FLUSH, null, "id");
                    }
                    languageTag = locale.toLanguageTag();
                } else {
                    this.locale = null;
                    languageTag = "";
                }
            }
        });
    }

    public boolean isError() {
        return isError;
    }

    public @NonNull
    String getLanguageTag() {
        synchronized (setup) {
            return languageTag;
        }
    }

    public void speak(String string) {
        if (string == null || isError || speaker == null || getLanguageTag().isEmpty() || !isLoaded) {
            return;
        }
        status.set(SHOULD_START_SPEAK);
        stop();
        synchronized (setup) {
            speaker.speak(string, TextToSpeech.QUEUE_FLUSH, null, "id");
        }
    }

    public void shutdown() {
        shutdownSignal.set(true);
        if (speaker != null) {
            speaker.stop();
            speaker.shutdown();
            speaker = null;
        }
    }

    public boolean isShutdown() {
        return shutdownSignal.get();
    }

    public void stop() {
        stopSignal.set(true);
        if (speaker != null) {
            speaker.stop();
        }
    }

    private void changeStatus(int status) {
        this.status.set(status);
    }

    @Override
    public void onStart(String utteranceId) {
        changeStatus(SPEAKING);
    }

    @Override
    public void onDone(String utteranceId) {
        changeStatus(FINISHED_SPEAKING);
    }

    @Override
    public void onError(String utteranceId) {
        changeStatus(FINISHED_SPEAKING);
    }

    public int getStatus() {
        return status.get();
    }

    /**
     * Should be invoked after invoking {@link #speak(String)} method.
     */
    public boolean waitFinishSpeaking(int surveyDelay) {
        if (!isLoaded) {
            return false;
        }
        stopSignal.set(false);
        while (!Thread.currentThread().isInterrupted() && !stopSignal.get()) {
            try {
                if (status.get() == FINISHED_SPEAKING) {
                    return true;
                }
                if (shutdownSignal.get()) {
                    return false;
                }

                // noinspection BusyWait
                Thread.sleep(surveyDelay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }
}
