package com.vsv.dialogs.listeners;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vsv.db.entities.SpreadSheetInfo;
import com.vsv.dialogs.entities.SheetTab;

import java.util.ArrayList;

@FunctionalInterface
public interface ShelfCreateListener {

    void createShelf(@NonNull String name, @Nullable SpreadSheetInfo spreadsheet,
                     @Nullable ArrayList<SheetTab> sheets, boolean bindSpreadsheet,
                     boolean loadProgress);
}
