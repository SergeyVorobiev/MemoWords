package com.vsv.dialogs;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.view.View;

import androidx.annotation.Nullable;

import com.vsv.db.entities.SpreadSheetInfo;
import com.vsv.io.Storage;
import com.vsv.io.StorageCSV;
import com.vsv.memorizer.R;
import com.vsv.statics.WeakContext;
import com.vsv.toasts.Toasts;
import com.vsv.utils.StaticUtils;

import java.io.File;

public class SSSendDialog extends BottomDialog {

    private final SpreadSheetInfo spreadsheet;

    public SSSendDialog(SpreadSheetInfo spreadsheet) {
        super(R.layout.dialog_spreadsheet_send);
        dialogView.findViewById(R.id.ss_copy).setOnClickListener(this::onCopyClick);
        dialogView.findViewById(R.id.ss_share).setOnClickListener(this::onSendClick);
        this.spreadsheet = spreadsheet;
    }

    private void onCopyClick(@Nullable View view) {
        ClipboardManager clipboardManager = WeakContext.getContext().getSystemService(ClipboardManager.class);
        clipboardManager.setPrimaryClip(ClipData.newPlainText("spreadsheetId", spreadsheet.spreadSheetId));
        dialog.cancel();
        Toasts.spreadsheetIdCopied();
    }

    private void onSendClick(@Nullable View view) {
        dialog.cancel();
        File sendFile = StorageCSV.createTemporaryFile(R.string.toast_cannot_share_spreadsheet, StaticUtils.getString(R.string.share_file_name_sheet));
        boolean result = Storage.getDocumentsStorage().loadSpreadsheetIntoFile(spreadsheet, sendFile);
        if (!result) {
            Toasts.cannotShareSpreadsheet();
            return;
        }
        StorageCSV.sendFile(sendFile);
    }
}
