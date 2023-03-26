package com.vsv.dialogs.listeners;

import com.vsv.db.entities.SpreadSheetTabName;

@FunctionalInterface
public interface SheetUpdateTabListener {

    void updateSheetTab(SpreadSheetTabName tab);
}
