package com.vsv.dialogs;

import android.util.Pair;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vsv.db.entities.Notebook;
import com.vsv.db.entities.SpreadSheetInfo;
import com.vsv.dialogs.entities.SheetTab;
import com.vsv.memorizer.R;
import com.vsv.spreadsheet.SheetChooserLayout;
import com.vsv.statics.GlobalData;
import com.vsv.toasts.Toasts;
import com.vsv.utils.Spec;

public class SendNotebookDialog extends SingleCustomDialog {

    public interface OkListener {
        void sendOk(int mode, @Nullable String sheetId, @Nullable String tabName);
    }

    public static final int COPY = 0;

    public static final int SEND_TO_SHEET = 1;

    public static final int SHARE = 2;

    public static final int SHARE_SHEET = 3;

    private OkListener okListener;

    private RadioGroup chooserGroup;

    private RadioButton copy;

    // copyToSheet.isChecked() does not work for the second time for unknown reason.
    private boolean copyToSheetChosen;

    private RadioButton copyToSheet;

    private RadioButton share;

    private RadioButton shareSpreadsheet;

    private final SheetChooserLayout sheetChooserLayout;

    public SendNotebookDialog(@NonNull Notebook notebook, int count) {
        super(R.layout.dialog_send_notebook, true, true);
        boolean canCopy = notebook.canCopy;
        copyToSheet.setEnabled(canCopy && GlobalData.account != null);
        copy.setEnabled(canCopy && count > -1 && count < Spec.MAX_NOTEBOOKS);
        share.setEnabled(canCopy);
        copy.setEnabled((canCopy));
        sheetChooserLayout = new SheetChooserLayout(context, dialogView, notebook.spreadsheetId,
                new Pair<>(notebook.sheetName, (int) notebook.sheetId), SpreadSheetInfo.DICT);
        sheetChooserLayout.setVisibility(View.GONE);
        okListener = (m, id, n) -> {
        };
    }

    @Override
    public void setupViews(View dialogView) {
        copy = dialogView.findViewById(R.id.copy);
        copyToSheet = dialogView.findViewById(R.id.copyToSheet);
        share = dialogView.findViewById(R.id.share);
        chooserGroup = dialogView.findViewById(R.id.chooserGroup);
        shareSpreadsheet = dialogView.findViewById(R.id.shareSpreadsheet);
    }

    @Override
    public void setupViewListeners(View dialogView) {
        dialogView.findViewById(R.id.cancel).setOnClickListener((view) -> cancel());
        dialogView.findViewById(R.id.ok).setOnClickListener(this::sendOk);
        dialogView.findViewById(R.id.closeSheetChooser).setOnClickListener((view) -> {
            chooserGroup.setVisibility(View.VISIBLE);
            sheetChooserLayout.setVisibility(View.GONE);
            copyToSheet.setChecked(false);
        });
        if (GlobalData.account == null) {
            copyToSheet.setText(R.string.send_notebook_radio_copy_to_sheet_login);
        } else {
            copyToSheet.setOnCheckedChangeListener((view, isChecked) -> {
                copyToSheetChosen = isChecked;
                if (isChecked) {
                    chooserGroup.setVisibility(View.GONE);
                    sheetChooserLayout.setVisibility(View.VISIBLE);
                    sheetChooserLayout.firstLoadSheet();
                } else {
                    chooserGroup.setVisibility(View.VISIBLE);
                    sheetChooserLayout.setVisibility(View.GONE);
                }
            });
        }
    }

    @Override
    public void setupViewAdjustments(View dialogView) {

    }

    private int getMode() {
        if (copy.isChecked()) {
            return COPY;
        } else if (copyToSheetChosen) {
            return SEND_TO_SHEET;
        } else if (share.isChecked()) {
            return SHARE;
        } else if (shareSpreadsheet.isChecked()) {
            return SHARE_SHEET;
        } else {
            return -1;
        }
    }

    public void setOkListener(OkListener listener) {
        this.okListener = listener;
    }

    private void sendOk(@Nullable View view) {
        int mode = getMode();
        if (mode == -1) {
            Toasts.chooseAnAction();
        } else {
            SpreadSheetInfo info = sheetChooserLayout.getChosenSpreadsheet();
            SheetTab tab = sheetChooserLayout.getChosenSheet();
            String spreadsheetId = null;
            String sheetTitle = null;
            if (mode == SEND_TO_SHEET) {
                if (info == null) {
                    Toasts.chooseSpreadsheet();
                    return;
                } else if (tab == null) {
                    Toasts.chooseSheet();
                    return;
                }
                spreadsheetId = info.spreadSheetId;
                sheetTitle = tab.getTitle();
            }
            cancel();
            okListener.sendOk(mode, spreadsheetId, sheetTitle);
        }
    }
}
