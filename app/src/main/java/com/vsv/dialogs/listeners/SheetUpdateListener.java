package com.vsv.dialogs.listeners;

import com.vsv.db.entities.SpreadSheetInfo;

@FunctionalInterface
public interface SheetUpdateListener {

    void updateSheet(SpreadSheetInfo sheet);
}
