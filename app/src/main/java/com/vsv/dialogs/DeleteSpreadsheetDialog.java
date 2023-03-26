package com.vsv.dialogs;

import android.view.View;
import android.widget.TextView;

import com.vsv.memorizer.R;

public class DeleteSpreadsheetDialog extends SingleCustomDialog {

    public final View deleteButton;

    public final View cancelButton;

    public DeleteSpreadsheetDialog(String spreadsheetName) {
        super(R.layout.dialog_delete_spreadsheet, false, true);
        TextView headerView = dialogView.findViewById(R.id.deleteSheetDialogHeader);
        String header = context.getString(R.string.dialog_delete_spreadsheet_header);
        headerView.setText(String.format(header, spreadsheetName));
        deleteButton = dialogView.findViewById(R.id.deleteSpreadsheetOk);
        deleteButton.setActivated(true);
        cancelButton = dialogView.findViewById(R.id.deleteSpreadsheetCancel);
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
