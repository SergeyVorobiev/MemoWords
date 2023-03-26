package com.vsv.dialogs;

import android.view.View;
import android.widget.TextView;

import com.vsv.dialogs.listeners.SheetCreateTabListener;
import com.vsv.memorizer.R;
import com.vsv.toasts.Toasts;

public class UpdateSheetDialog extends SingleCustomDialog {

    private TextView inputName;

    private SheetCreateTabListener listener;

    public UpdateSheetDialog(String sheetTabName) {
        super(R.layout.dialog_update_sheet, true, true);
        inputName.setText(sheetTabName);
        showKeyboardFor(inputName);
    }

    @Override
    public void setupViews(View dialogView) {
        inputName = dialogView.findViewById(R.id.updateSheetNameInput);
    }

    @Override
    public void setupViewListeners(View dialogView) {
        dialogView.findViewById(R.id.updateSheetOk).setOnClickListener(this::onClickOk);
        dialogView.findViewById(R.id.updateSheetCancel).setOnClickListener(this::onClickCancel);
    }

    @Override
    public void setupViewAdjustments(View dialogView) {

    }

    private void onClickOk(View view) {
        String name = inputName.getText().toString().trim();
        if (name.isEmpty()) {
            Toasts.sheetNameEmpty();
        } else {
            listener.createSheetTab(name);
            dialog.cancel();
        }
    }

    public void setCreateSheetTabListener(SheetCreateTabListener listener) {
        this.listener = listener;
    }

    private void onClickCancel(View view) {
        dialog.cancel();
    }
}
