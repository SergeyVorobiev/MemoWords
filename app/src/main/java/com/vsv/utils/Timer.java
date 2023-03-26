package com.vsv.utils;

public class Timer {

    private long startTime;

    public static Timer newStart() {
        return new Timer().start();
    }

    public static float nanoTimeDiffFromNowInSeconds(long time) {
        return (System.nanoTime() - time) / 1000000000.0f;
    }

    public static long nanoTimeDiffFromNowInMilliseconds(long time) {
        return (long) ((System.nanoTime() - time) / 1000000.0f);
    }

    public static float nanoTimeDiffFromNowInMinutes(long time) {
        return nanoTimeDiffFromNowInSeconds(time) / 60.0f;
    }

    public Timer start() {
        startTime = System.nanoTime();
        return this;
    }

    public float stopInSeconds() {
        return (System.nanoTime() - startTime) / 1000000000.0f;
    }

    public String stopInSecondsString() {
        return String.valueOf((System.nanoTime() - startTime) / 1000000000.0f);
    }

    public float stopInMillis() {
        return (System.nanoTime() - startTime) / 1000000.0f;
    }

    public int stopInFPS() {
        return (int) (1 / stopInSeconds());
    }
}
