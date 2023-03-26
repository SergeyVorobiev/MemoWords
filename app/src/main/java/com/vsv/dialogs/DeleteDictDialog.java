package com.vsv.dialogs;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.vsv.memorizer.R;

public class DeleteDictDialog extends SingleCustomDialog {

    private CheckBox confirm;

    private View delete;

    private Runnable deleteListener;

    private Runnable cancelListener;

    public DeleteDictDialog(@NonNull Context context, String sheetName) {
        super(context, R.layout.dialog_delete_dictionary, false, true);
        String header = context.getResources().getString(R.string.dialog_delete_dict_header);
        ((TextView) dialogView.findViewById(R.id.deleteDictDialogHeader)).setText(String.format(header, sheetName));
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
        confirm = dialogView.findViewById(R.id.deleteDictConfirm);
        delete = dialogView.findViewById(R.id.dictDelete);
    }

    @Override
    public void setupViewListeners(View dialogView) {
        delete.setOnClickListener(this::onClickOk);
        dialogView.findViewById(R.id.dictCancel).setOnClickListener(this::onClickCancel);
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

