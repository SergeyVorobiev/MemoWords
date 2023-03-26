package com.vsv.db.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "spreadSheetTabName", foreignKeys = {@ForeignKey(entity = SpreadSheetInfo.class,
        parentColumns = "id",
        childColumns = "spreadSheetInfoId",
        onDelete = ForeignKey.CASCADE)},
        indices = {@Index(value = {"spreadSheetInfoId"})})
@Deprecated
public class SpreadSheetTabName {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public long spreadSheetInfoId;

    public String name;

    public long sheetId;

    @Ignore
    transient
    public boolean isChecked;

    public SpreadSheetTabName(long id, long sheetId, long spreadSheetInfoId, String name) {
        this.id = id;
        this.spreadSheetInfoId = spreadSheetInfoId;
        this.name = name;
        this.sheetId = sheetId;
    }

    @Ignore
    public SpreadSheetTabName(long spreadsheetInfoId, long sheetId, String name) {
        this.spreadSheetInfoId = spreadsheetInfoId;
        this.name = name;
        this.sheetId = sheetId;
    }
}
