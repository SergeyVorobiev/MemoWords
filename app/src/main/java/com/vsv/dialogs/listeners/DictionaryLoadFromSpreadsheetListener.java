package com.vsv.dialogs.listeners;

import androidx.annotation.Nullable;

import com.vsv.dialogs.entities.SheetTab;

@FunctionalInterface
public interface DictionaryLoadFromSpreadsheetListener {

    boolean loadFromSpreadsheet(String dictName, String leftLanguageAbb, String rightLanguageAbb,
                                SheetTab sheetTab, String spreadsheetId, @Nullable String spreadsheetName,
                                boolean loadProgress, boolean bindSpreadsheet);
}
