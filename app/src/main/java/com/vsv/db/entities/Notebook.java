package com.vsv.db.entities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.vsv.utils.DateUtils;

import java.util.Date;

@Entity(tableName = "notebook")
@TypeConverters({Converter.class})
public class Notebook implements UpdateDate {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "sorted")
    public boolean sorted;

    @ColumnInfo(name = "can_copy")
    public boolean canCopy;

    @ColumnInfo(name = "author")
    public String author;

    @ColumnInfo(name = "spreadsheet_name")
    public String spreadsheetName;

    @ColumnInfo(name = "spreadsheet_id")
    public String spreadsheetId;

    @Nullable
    @ColumnInfo(name = "update_check")
    public Date updateCheck;

    @Nullable
    @ColumnInfo(name = "data_date")
    public Date dataDate;

    @ColumnInfo(name = "need_update", defaultValue = "0")
    public boolean needUpdate;

    @ColumnInfo(name = "sheet_id")
    public long sheetId;

    @ColumnInfo(name = "sheet_name")
    public String sheetName;

    // Not used
    @ColumnInfo(name = "notes_count")
    public int notesCount;

    public int fontNotesTitleIndex;

    @Ignore
    transient
    public static final int MINUTES_TO_UPDATE = 0;

    public Notebook(long id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public long getSheetId() {
        return sheetId;
    }

    @Override
    public void setSheetId(long sheetId) {
        this.sheetId = sheetId;
    }

    @Override
    public boolean needUpdate() {
        return needUpdate;
    }

    @Override
    public boolean hasOwner() {
        return spreadsheetId != null && !spreadsheetId.isEmpty() && sheetName != null && !sheetName.isEmpty();
    }

    public boolean hasOwner(@Nullable String spreadsheetId, @Nullable String sheetName) {
        if (spreadsheetId == null || spreadsheetId.isEmpty() || sheetName == null || sheetName.isEmpty()) {
            return false;
        }
        return spreadsheetId.equals(this.spreadsheetId) && sheetName.equals(this.sheetName);
    }

    @Override
    public boolean needCheckUpdate(Date date) {
        return this.updateCheck == null || DateUtils.getMinutesBetweenDates(this.updateCheck, date) >= MINUTES_TO_UPDATE;
    }

    @Nullable
    @Override
    public String getSpreadsheetId() {
        return spreadsheetId;
    }

    @Nullable
    @Override
    public Date getLastUpdatedDate() {
        return dataDate;
    }

    public Notebook copy() {
        Notebook newNotebook = new Notebook(this.name);
        newNotebook.sheetName = this.sheetName;
        newNotebook.spreadsheetId = this.spreadsheetId;
        newNotebook.canCopy = this.canCopy;
        newNotebook.sorted = this.sorted;
        newNotebook.notesCount = this.notesCount;
        newNotebook.fontNotesTitleIndex = this.fontNotesTitleIndex;
        if (this.dataDate != null) {
            newNotebook.dataDate = (Date) this.dataDate.clone();
        }
        if (this.updateCheck != null) {
            newNotebook.updateCheck = (Date) this.updateCheck.clone();
        }
        newNotebook.needUpdate = this.needUpdate;
        newNotebook.sheetId = this.sheetId;
        newNotebook.spreadsheetName = this.spreadsheetName;
        newNotebook.author = this.author;
        return newNotebook;
    }

    @Ignore
    public Notebook(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public long getId() {
        return this.id;
    }

    @NonNull
    @Override
    public String getSheetName() {
        return sheetName;
    }

    @Override
    public int getType() {
        return NOTEBOOK;
    }

    public void setId(long id) {
        this.id = id;
    }
}
