package com.vsv.db.entities;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "settings")
public class Settings {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public boolean sortShelf;

    public boolean sortNotebooks;

    public long trainTimeInSeconds;

    public boolean sortSheets;

    public boolean autoLogout;

    public boolean mobileInternet;

    public boolean sortTabs;

    public int fontDictTitleIndex;

    public int fontSpreadsheetTitleIndex;

    public int fontNotebookTitleIndex;

    public int startIndex;

    public long todayScore;

    public long score;

    public long timestampForTodayScore;

    public String lastCrashException;

    public String appLocale;

    public Settings(long id) {
        this.id = id;
    }

    @Ignore
    public Settings() {
    }
}
