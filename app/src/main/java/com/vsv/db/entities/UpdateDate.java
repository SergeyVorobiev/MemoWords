package com.vsv.db.entities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Date;

public interface UpdateDate {

    int DICTIONARY = 0;

    int NOTEBOOK = 1;

    long getSheetId();

    void setSheetId(long sheetId);

    boolean needUpdate();

    boolean hasOwner();

    boolean needCheckUpdate(Date date);

    @Nullable
    String getSpreadsheetId();

    @Nullable
    Date getLastUpdatedDate();

    long getId();

    @NonNull
    String getSheetName();

    int getType();
}
