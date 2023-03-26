package com.vsv.dialogs.listeners;

import androidx.annotation.NonNull;

@FunctionalInterface
public interface NotebookLoadFromSpreadsheetListener {

    void loadFromSpreadsheet(@NonNull String notebookName, @NonNull String spreadsheetId,
                             @NonNull String spreadsheetName, @NonNull String sheetName,
                             long sheetId, boolean bindSpreadsheet);
}
