package com.vsv.db.entities;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.TypeConverters;

import java.util.Date;

@TypeConverters({Converter.class})
public class SheetDatesUpdate {

    public long id;

    @ColumnInfo(name = "sheet_name")
    public String sheetName;

    @ColumnInfo(name = "sheet_id")
    public long sheetId;

    @Nullable
    @ColumnInfo(name = "update_check")
    public Date successfulUpdateCheck;

    @Nullable
    @ColumnInfo(name = "data_date")
    public Date dataDate;

    @ColumnInfo(name = "need_update", defaultValue = "0")
    public boolean needUpdate;
}
