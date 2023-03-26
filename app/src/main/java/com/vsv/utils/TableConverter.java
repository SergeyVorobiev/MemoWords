package com.vsv.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vsv.db.entities.SpreadSheetInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class TableConverter {

    private TableConverter() {

    }

    @NonNull
    public static List<List<Object>> convertSpreadsheetsToTable(@Nullable Collection<SpreadSheetInfo> spreadsheets) {
        List<List<Object>> rows = new ArrayList<>();
        if (spreadsheets != null) {
            for (SpreadSheetInfo spreadsheet : spreadsheets) {
                List<Object> row = new ArrayList<>();
                row.add(spreadsheet.getStringType());
                row.add(spreadsheet.spreadSheetId);
                row.add(spreadsheet.name);
                rows.add(row);
            }
        }
        return rows;
    }

    public static void convertTableToSpreadsheets(@Nullable List<List<Object>> table, @Nullable Collection<SpreadSheetInfo> result) {
        if (table == null || result == null) {
            return;
        }
        for (List<Object> row : table) {
            if (row.size() < 2) {
                continue;
            }
            String type = SheetDataBuilder.getCellValueOrEmpty(row, 0, 100);
            int intType = 0;
            if (type.equals(CodeNames.DICT_SHEET)) {
                intType = 1;
            } else if (type.equals(CodeNames.NOTE_SHEET)) {
                intType = 2;
            }
            String sheetId = SheetDataBuilder.getCellValueOrEmpty(row, 1, 100);
            String name = SheetDataBuilder.getCellValueOrEmpty(row, 2, 100);
            if (sheetId.isEmpty() || name.isEmpty()) {
                continue;
            }
            result.add(new SpreadSheetInfo(sheetId, name, intType));
        }
    }
}
