package com.vsv.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateUtils {

    private static final long DAY_IN_MILLISECONDS = 1000 * 60 * 60 * 24;

    private static final long MINUTE_IN_MILLISECONDS = 1000 * 60;

    private static final Calendar calendar = Calendar.getInstance();

    private static final Date minDate = new Date(1640984400L * 1000);

    private DateUtils() {

    }

    public static long getTimestampInDays() {
        return getCurrentDate().getTime() / DAY_IN_MILLISECONDS;
    }

    // Returns false if the first or both dates is null
    public static boolean firstDateNewerTheSecond(@Nullable Date firstDate, @Nullable Date secondDate) {
        if (firstDate == null) { // No matter what the second date is.
            return false;
        } else {
            if (secondDate == null) {
                return true;
            }
            return (firstDate.getTime() - secondDate.getTime()) > 0;
        }
    }

    public static long getMinutesBetweenDates(@NonNull Date firstDate, @NonNull Date lastDate) {
        long first = firstDate.getTime();
        long last = lastDate.getTime();
        long result = last - first;
        return result / MINUTE_IN_MILLISECONDS;
    }

    public static long getMinutesFromNow(@NonNull Date date) {
        return getMinutesBetweenDates(date, getCurrentDate());
    }

    public static int getDaysBetweenDatesOrZero(@NonNull Date firstDate, @NonNull Date lastDate) {
        int pastDays = getDaysBetweenDates(firstDate, lastDate);
        if (pastDays == 0) {

            // Past less than 24 hours but the day has been changed.
            int firstDayNumber = getDayNumber(firstDate);
            int lastDayNumber = getDayNumber(lastDate);
            if (firstDayNumber != lastDayNumber) {
                pastDays = 1;
            }
        }
        return Math.max(0, pastDays);
    }

    public static int getDaysBetweenDates(@NonNull Date firstDate, @NonNull Date lastDate) {
        long first = firstDate.getTime();
        long last = lastDate.getTime();
        long result = last - first;
        return (int) (result / DAY_IN_MILLISECONDS);
    }

    @Nullable
    public static Date convertLotus123(long number) {
        try {
            LocalDate localeDate = LocalDate.of(1899, Month.DECEMBER, 30).plusDays(number);
            return new Date(localeDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond() * 1000);
        } catch (Throwable th) {
            return null;
        }
    }

    @Nullable
    public static Date checkCorrectedOrNull(@Nullable Date date) {
        if (date == null) {
            return null;
        }
        Date currentDate = getCurrentDate();
        int year = date.getYear() + 1900;
        if (year > (currentDate.getYear() + 1900) || year < 2022) {
            return null;
        }
        long minutes = getMinutesBetweenDates(date, getCurrentDate());
        if (minutes < 0) {
            return null;
        }
        return date;
    }

    @NonNull
    public static Date getCurrentDate() {
        return Calendar.getInstance(TimeZone.getDefault()).getTime();
    }

    public static int getDayNumber(@NonNull Date date) {
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_MONTH);
    }
}
