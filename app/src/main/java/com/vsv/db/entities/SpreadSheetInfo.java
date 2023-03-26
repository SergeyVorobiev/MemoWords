package com.vsv.db.entities;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "spreadSheetInfo")
public class SpreadSheetInfo {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public String spreadSheetId;

    public String name;

    public static final int ANY = 0;

    public static final int DICT = 1;

    public static final int NOTEBOOK = 2;

    public static final String S_ANY = "any";

    public static final String S_DICT = "dict";

    public static final String S_NOTEBOOK = "note";

    // Any, Dictionaries, Notebooks
    public int type;

    @Ignore
    transient
    public boolean isChecked;

    public SpreadSheetInfo(long id, String spreadSheetId, String name) {
        this.id = id;
        this.spreadSheetId = spreadSheetId;
        this.name = name;
    }

    @Ignore
    public SpreadSheetInfo(String spreadSheetId, String name) {
        this.spreadSheetId = spreadSheetId;
        this.name = name;
    }

    @Ignore
    public SpreadSheetInfo(String spreadSheetId, String name, int type) {
        this.spreadSheetId = spreadSheetId;
        this.name = name;
        this.type = type;
    }

    public String getStringType() {
        String sType = S_ANY;
        if (type == 1) {
            sType = S_DICT;
        } else if (type == 2) {
            sType = S_NOTEBOOK;
        }
        return sType;
    }

    public String getName() {
        return name;
    }
}
