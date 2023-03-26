package com.vsv.dialogs;

import android.content.DialogInterface;
import android.view.View;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.vsv.memorizer.R;
import com.vsv.statics.WeakContext;
import com.vsv.utils.StaticUtils;

public class BottomDialog extends SingleWindow {

    protected final View dialogView;

    protected final BottomSheetDialog dialog;

    public BottomDialog(int layout) {
        dialog = new BottomSheetDialog(WeakContext.getContext(), R.style.SheetDialog);
        dialogView = StaticUtils.inflate(layout);
        dialog.setContentView(dialogView);
        dialog.setOnDismissListener((dialogInterface) -> setShown(false));
    }

    public void setOnDismissListener(DialogInterface.OnDismissListener listener) {
        dialog.setOnDismissListener((dialogInterface -> {
            setShown(false);
            listener.onDismiss(dialogInterface);
        }));
    }

    public void show() {
        setShown(true);
        dialog.show();
    }
}
