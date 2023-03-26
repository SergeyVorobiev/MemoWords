package com.vsv.db.entities;

import androidx.room.ColumnInfo;

public class DictNoteSpreadsheetNamesUpdater {

    public long id;

    @ColumnInfo(name = "spreadsheet_name")
    public String spreadsheetName;

    public DictNoteSpreadsheetNamesUpdater(long id, String spreadsheetName) {
        this.id = id;
        this.spreadsheetName = spreadsheetName;
    }
}
