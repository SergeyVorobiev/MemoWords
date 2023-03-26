package com.vsv.dialogs;


import android.app.AlertDialog;

import androidx.annotation.NonNull;

import com.vsv.memorizer.R;
import com.vsv.statics.WeakContext;
import com.vsv.utils.AppLink;
import com.vsv.utils.IntentHelpers;
import com.vsv.utils.StaticUtils;

public class AuthorPageDialog extends SingleWindow {

    private AlertDialog.Builder builder;

    public AuthorPageDialog(@NonNull AppLink appLink) {
        builder = new AlertDialog.Builder(WeakContext.getContext());
        builder.setMessage(StaticUtils.getString(R.string.author_dialog_header));
        builder.setPositiveButton(StaticUtils.getString(R.string.author_dialog_ok), (dialog, which) -> IntentHelpers.openLink(appLink))
                .setNegativeButton(StaticUtils.getString(R.string.author_dialog_cancel), (dialog, which) -> {});
        builder.setOnDismissListener((dialog -> setShown(false)));
    }

    public void show() {
        if (isShownToast()) {
            return;
        }
        setShown(true);
        builder.show();
    }
}
