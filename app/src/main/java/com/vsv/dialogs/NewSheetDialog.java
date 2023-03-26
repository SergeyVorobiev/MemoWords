package com.vsv.dialogs;

import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.vsv.dialogs.listeners.SheetCreateTabListener;
import com.vsv.memorizer.R;
import com.vsv.toasts.Toasts;

public class NewSheetDialog extends SingleCustomDialog {

    private TextView inputName;

    private SheetCreateTabListener listener;

    public NewSheetDialog() {
        super(R.layout.dialog_new_sheet, true, true);
    }

    @Override
    public void setupViews(View dialogView) {
        inputName = dialogView.findViewById(R.id.newSheetNameInput);
    }

    @Override
    public void setupViewListeners(View dialogView) {
        dialogView.findViewById(R.id.newSheetOk).setOnClickListener(this::onClickOk);
        dialogView.findViewById(R.id.newSheetCancel).setOnClickListener(this::onClickCancel);
    }

    @Override
    public void setupViewAdjustments(View dialogView) {
        inputName.requestFocus();
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
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
