package com.vsv.entities;

import androidx.annotation.NonNull;

public class SheetUpdateData {

    @NonNull
    public final String sheetName;

    public final long sheetId;

    public final long timestamp;

    public SheetUpdateData(@NonNull String sheetName, long sheetId, long timestamp) {
        this.sheetName = sheetName;
        this.sheetId = sheetId;
        this.timestamp = timestamp;
    }
}
