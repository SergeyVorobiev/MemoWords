package com.vsv.dialogs;

import android.view.View;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.api.services.sheets.v4.model.BatchUpdateValuesResponse;
import com.vsv.db.entities.SpreadSheetInfo;
import com.vsv.dialogs.entities.BackgroundTask;
import com.vsv.dialogs.entities.SheetTab;
import com.vsv.memorizer.R;
import com.vsv.spreadsheet.SheetChooserLayout;
import com.vsv.spreadsheet.SheetLoader;
import com.vsv.toasts.Toasts;
import com.vsv.utils.GoogleTasksExceptionHandler;
import com.vsv.utils.StaticUtils;
import com.vsv.utils.TableConverter;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class SaveLoadSpreadsheetsDialog extends SingleCustomDialog {

    private static final int SAVE = 0;

    private static final int LOAD = 1;

    private static final int NO_ACTION = -1;

    private final SheetChooserLayout sheetChooserLayout;

    private final RadioButton saveRadioButton;

    private final RadioButton loadRadioButton;

    public SaveLoadSpreadsheetsDialog() {
        super(R.layout.dialog_save_load_spreadsheets, false, true);
        sheetChooserLayout = new SheetChooserLayout(context, dialogView, null, null, -1);
        dialogView.findViewById(R.id.btnOk).setOnClickListener(this::onClickOk);
        dialogView.findViewById(R.id.btnCancel).setOnClickListener(this::onClickCancel);
        saveRadioButton = dialogView.findViewById(R.id.save);
        loadRadioButton = dialogView.findViewById(R.id.load);
        this.setOnDismissListener((dialogInterface -> sheetChooserLayout.removeObservers()));
        sheetChooserLayout.setVisibility(View.VISIBLE);

    }

    @Override
    public void setupViews(View dialogView) {

    }

    @Override
    public void setupViewListeners(View dialogView) {

    }

    @Override
    public void setupViewAdjustments(View dialogView) {

    }

    private void onClickOk(View view) {
        SpreadSheetInfo spreadsheet = sheetChooserLayout.getChosenSpreadsheet();
        SheetTab sheet = sheetChooserLayout.getChosenSheet();
        ArrayList<SpreadSheetInfo> spreadsheets = sheetChooserLayout.getAllSpreadsheets();
        int action = NO_ACTION;
        if (saveRadioButton.isChecked()) {
            action = SAVE;
        } else if (loadRadioButton.isChecked()) {
            action = LOAD;
        }
        if (action == NO_ACTION) {
            Toasts.chooseAnAction();
            return;
        }
        if (spreadsheet == null) {
            Toasts.chooseSpreadsheet();
            return;
        }
        if (sheet == null) {
            Toasts.chooseSheet();
            return;
        }
        if (action == LOAD) {
            load(spreadsheet.spreadSheetId, sheet.getTitle(), spreadsheets);
        } else {
            if (spreadsheets != null && !spreadsheets.isEmpty()) {
                spreadsheets.removeIf(spreadSheetInfo -> spreadSheetInfo.spreadSheetId.equals(spreadsheet.spreadSheetId));
            }
            if (spreadsheets == null || spreadsheets.isEmpty()) {
                Toasts.nothingToSave();
                return;
            }
            save(spreadsheet.spreadSheetId, sheet.getTitle(), spreadsheets);
        }
    }

    private void load(@NonNull String spreadsheetId, @NonNull String sheetName, @Nullable ArrayList<SpreadSheetInfo> spreadsheets) {
        BackgroundTask<ArrayList<SpreadSheetInfo>> spreadsheetsFromSpreadsheet = SheetLoader.loadSpreadsheets(spreadsheetId, sheetName);
        spreadsheetsFromSpreadsheet.setRunMainThreadOnSuccess((result) -> {
            if (result == null || result.isEmpty()) {
                Toasts.nothingToLoad();
            } else {
                if (spreadsheets != null && !spreadsheets.isEmpty()) {

                    // Filter existed ones.
                    result = (ArrayList<SpreadSheetInfo>) result.stream().filter(spreadsheet -> {
                        for (int i = 0; i < spreadsheets.size(); i++) {
                            SpreadSheetInfo existedSpreadsheet = spreadsheets.get(i);
                            if (existedSpreadsheet.spreadSheetId.equals(spreadsheet.spreadSheetId)) {
                                return false;
                            }
                        }
                        return true;
                    }).collect(Collectors.toList());
                }
                StaticUtils.getModel().getSpreadsheetsRepository().insertSeveral(result);
            }
        });
        spreadsheetsFromSpreadsheet.setRunMainThreadOnFail((e) -> Toasts.longShowRaw(GoogleTasksExceptionHandler.handle(e)));
        spreadsheetsFromSpreadsheet.buildWaitDialog().showOver();
        cancel();
    }

    private void save(@NonNull String spreadsheetId, @NonNull String sheetName, @NonNull ArrayList<SpreadSheetInfo> spreadsheets) {
        BackgroundTask<BatchUpdateValuesResponse> task = SheetLoader.buildWriteTableIntoSheetTask(spreadsheetId, sheetName,
                TableConverter.convertSpreadsheetsToTable(spreadsheets), "A1", 15);
        task.setRunMainThreadOnFail((e) -> Toasts.longShowRaw(GoogleTasksExceptionHandler.handle(e)));
        task.setRunMainThreadOnSuccess(response -> Toasts.success());
        task.buildWaitDialog().showOver();
        cancel();
    }

    private void onClickCancel(View view) {
        dialog.cancel();
    }
}
