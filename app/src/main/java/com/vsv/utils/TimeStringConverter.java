package com.vsv.utils;

public final class TimeStringConverter {

    private TimeStringConverter() {

    }

    public static String fromSeconds(long time) {
        long allHours = time / 3600;
        long restMinutes = time - allHours * 3600;
        long allMinutes = restMinutes / 60;
        long allSeconds = restMinutes - allMinutes * 60;
        return toStringNumber(allHours) + ":" + toStringNumber(allMinutes) + ":" +
                toStringNumber(allSeconds);
    }

    private static String toStringNumber(long time) {
        return time < 10 ? "0" + time : String.valueOf(time);
    }
}
