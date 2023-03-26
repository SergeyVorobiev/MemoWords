package com.vsv.dialogs;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.vsv.db.entities.SpreadSheetInfo;
import com.vsv.dialogs.entities.SheetTab;
import com.vsv.dialogs.listeners.NotebookCreateListener;
import com.vsv.dialogs.listeners.NotebookLoadFromSpreadsheetListener;
import com.vsv.memorizer.R;
import com.vsv.spreadsheet.SheetChooserLayout;
import com.vsv.statics.GlobalData;
import com.vsv.toasts.Toasts;

public class NewNotebookDialog extends SingleCustomDialog {

    private final TextView inputNotebookName;

    private NotebookCreateListener listener;

    private NotebookLoadFromSpreadsheetListener spreadsheetListener;

    private boolean unfoldSheets = false;

    private final ImageButton unfoldButton;

    private final SheetChooserLayout sheetChooserLayout;

    private final TextView pleaseLogin;

    private final CheckBox bindSpreadsheet;

    private final Drawable down;

    private final Drawable up;

    public NewNotebookDialog() {
        super(R.layout.dialog_new_notebook, false, true);
        unfoldButton = dialogView.findViewById(R.id.unfoldSheets);
        inputNotebookName = dialogView.findViewById(R.id.nameInput);
        pleaseLogin = dialogView.findViewById(R.id.pleaseLoginText);
        bindSpreadsheet = dialogView.findViewById(R.id.bindSpreadsheet);
        bindSpreadsheet.setChecked(true);
        unfoldButton.setOnClickListener(this::unfoldSheets);
        dialogView.findViewById(R.id.newOk).setOnClickListener(this::onClickOk);
        dialogView.findViewById(R.id.newCancel).setOnClickListener(this::onClickCancel);
        sheetChooserLayout = new SheetChooserLayout(context, dialogView, null, null, SpreadSheetInfo.DICT);
        pleaseLogin.setVisibility(View.GONE);
        down = ResourcesCompat.getDrawable(context.getResources(), R.drawable.btn_down_switch, context.getTheme());
        up = ResourcesCompat.getDrawable(context.getResources(), R.drawable.btn_up_switch, context.getTheme());
    }

    private void unfoldSheets(View view) {
        closeKeyboardFor(inputNotebookName);
        unfoldSheets = !unfoldSheets;
        if (unfoldSheets) {
            if (GlobalData.account == null) {
                pleaseLogin.setVisibility(View.VISIBLE);
            } else {
                sheetChooserLayout.setVisibility(View.VISIBLE);
            }
            unfoldButton.setBackground(up);
        } else {
            if (GlobalData.account == null) {
                pleaseLogin.setVisibility(View.GONE);
            } else {
                sheetChooserLayout.setVisibility(View.GONE);
            }
            unfoldButton.setBackground(down);
        }
    }

    private void onClickOk(View view) {
        String notebookName = inputNotebookName.getText().toString().trim();
        SpreadSheetInfo chosenSheet = sheetChooserLayout.getChosenSpreadsheet();
        SheetTab chosenTab = sheetChooserLayout.getChosenSheet();
        String spreadsheetId = chosenSheet == null ? "" : chosenSheet.spreadSheetId;
        String sheetName = chosenTab == null ? "" : chosenTab.getTitle();
        long sheetId = chosenTab == null ? -1 : chosenTab.getId();
        if (!spreadsheetId.isEmpty() && !sheetName.isEmpty()) {
            this.spreadsheetListener.loadFromSpreadsheet(notebookName, spreadsheetId, chosenSheet.name, sheetName, sheetId, bindSpreadsheet.isChecked());
            this.cancel();
        } else if (notebookName.isEmpty()) {
            Toasts.notebookNameEmpty();
        } else {
            listener.createNotebook(notebookName);
            sheetChooserLayout.removeObservers();
            dialog.cancel();
        }
    }

    public void cancel() {
        sheetChooserLayout.removeObservers();
        dialog.cancel();
    }

    public void setNotebookCreateListener(NotebookCreateListener listener) {
        this.listener = listener;
    }

    public void setNotebookLoadFromSpreadsheetListener(NotebookLoadFromSpreadsheetListener listener) {
        this.spreadsheetListener = listener;
    }

    private void onClickCancel(View view) {
        sheetChooserLayout.removeObservers();
        dialog.cancel();
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
}
