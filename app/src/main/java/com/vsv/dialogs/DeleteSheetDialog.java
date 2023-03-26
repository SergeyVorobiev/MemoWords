package com.vsv.dialogs;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.vsv.memorizer.R;

public class DeleteSheetDialog extends SingleCustomDialog {

    private CheckBox confirm;

    private View delete;

    private Runnable deleteListener;

    private Runnable cancelListener;

    public DeleteSheetDialog(Context context, String sheetName) {
        super(context, R.layout.dialog_delete_sheet, false, true);
        String header = context.getResources().getString(R.string.dialog_delete_tab_header);
        ((TextView) dialogView.findViewById(R.id.deleteTabDialogHeader)).setText(String.format(header, sheetName));
        deleteListener = () -> {
        };
        cancelListener = () -> {
        };
    }

    private void onClickCancel(View view) {
        cancelListener.run();
        dialog.cancel();
    }

    @Override
    public void setupViews(View dialogView) {
        confirm = dialogView.findViewById(R.id.deleteTabConfirm);
        delete = dialogView.findViewById(R.id.sheetTabDelete);
    }

    @Override
    public void setupViewListeners(View dialogView) {
        delete.setOnClickListener(this::onClickOk);
        dialogView.findViewById(R.id.sheetTabCancel).setOnClickListener(this::onClickCancel);
        confirm.setOnCheckedChangeListener((buttonView, isChecked) -> {
            delete.setClickable(isChecked);
            delete.setActivated(isChecked);
        });
    }

    @Override
    public void setupViewAdjustments(View dialogView) {
        delete.setClickable(false);
        delete.setActivated(false);
    }

    public void setOnDeleteListener(Runnable deleteListener) {
        this.deleteListener = deleteListener;
    }

    public void setOnCancelListener(Runnable cancelListener) {
        this.cancelListener = cancelListener;
    }

    private void onClickOk(View view) {
        deleteListener.run();
        dialog.cancel();
    }
}
