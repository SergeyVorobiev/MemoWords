package com.vsv.dialogs;

import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.vsv.memorizer.R;
import com.vsv.statics.WeakContext;

public class DeleteNoteDialog extends SingleCustomDialog {

    private CheckBox confirm;

    private View delete;

    private Runnable deleteListener;

    private Runnable cancelListener;

    public DeleteNoteDialog(String noteName) {
        super(WeakContext.getContext(), R.layout.dialog_delete_notebook, false, true);
        dialogView.findViewById(R.id.deleteNotebookDialogWarning).setVisibility(View.GONE);
        String header = context.getResources().getString(R.string.dialog_delete_note_header);
        ((TextView) dialogView.findViewById(R.id.deleteNotebookDialogHeader)).setText(String.format(header, noteName));
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
        confirm = dialogView.findViewById(R.id.deleteNotebookConfirm);
        delete = dialogView.findViewById(R.id.notebookDelete);
    }

    @Override
    public void setupViewListeners(View dialogView) {
        delete.setOnClickListener(this::onClickOk);
        dialogView.findViewById(R.id.notebookCancel).setOnClickListener(this::onClickCancel);
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

