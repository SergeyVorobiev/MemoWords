package com.vsv.db.entities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.TypeConverter;

import java.util.Date;

public class Converter {

    @TypeConverter
    @Nullable
    public static Date fromTimestamp(@Nullable Long value) {
        try {
            return value == null || value < 0 ? null : new Date(value);
        } catch (Throwable th) {
            return null;
        }
    }

    @TypeConverter
    @NonNull
    public static Long dateToTimestamp(@Nullable Date date) {
        return date == null ? -1L : date.getTime();
    }
}
