package com.vsv.dialogs;

import android.content.Context;
import android.util.Pair;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

import com.vsv.db.entities.Dictionary;
import com.vsv.db.entities.SpreadSheetInfo;
import com.vsv.dialogs.entities.SheetTab;
import com.vsv.memorizer.R;
import com.vsv.spreadsheet.SheetChooserLayout;
import com.vsv.statics.GlobalData;
import com.vsv.toasts.Toasts;

public class SendDictionaryDialog extends SingleCustomDialog {

    public interface OkListener {
        void sendOk(int mode, @Nullable String spreadsheetName, @Nullable String spreadsheetId,
                    @Nullable String sheetName, int sheetId, boolean saveProgress);
    }

    public static final int BIND_SPREADSHEET = 6;

    public static final int COPY = 0;

    public static final int SEND_TO_SHEET = 1;

    public static final int MOVE = 2;

    public static final int SHARE = 3;

    public static final int SHARE_SHEET = 4;

    public static final int MERGE = 5;

    private OkListener okListener;

    private RadioGroup chooserGroup;

    private RadioButton move;

    private RadioButton copy;

    private RadioButton copyToSheet;

    // copyToSheet.isChecked() does not work for the second time for unknown reason.
    private boolean copyToSheetChosen;

    // copyToSheet.isChecked() does not work for the second time for unknown reason.
    private boolean bindChosen;

    private RadioButton bind;

    private RadioButton share;

    private RadioButton merge;

    private RadioButton shareSheet;

    private SwitchCompat saveProgress;

    private View warningMessage;

    private final SheetChooserLayout sheetChooserLayout;

    // private TextView chooseLabel;

    public SendDictionaryDialog(@NonNull Context context, @NonNull Dictionary dictionary) {
        super(context, R.layout.dialog_send_dictionary, true, true);
        boolean canCopy = dictionary.canCopy;
        copyToSheet.setEnabled(canCopy && GlobalData.account != null);
        share.setEnabled(canCopy);
        merge.setEnabled(canCopy);
        bind.setEnabled(GlobalData.account != null);
        bind.setChecked(false);
        saveProgress.setChecked(false);
        shareSheet.setEnabled(dictionary.hasOwner());
        sheetChooserLayout = new SheetChooserLayout(context, dialogView, dictionary.spreadsheetId,
                new Pair<>(dictionary.sheetName, (int) dictionary.sheetId), SpreadSheetInfo.NOTEBOOK);
        sheetChooserLayout.setVisibility(View.GONE);
        okListener = (m, ssn, id, sn, sid, sp) -> {
        };
    }

    @Override
    public void setupViews(View dialogView) {
        move = dialogView.findViewById(R.id.move);
        copy = dialogView.findViewById(R.id.copy);
        copyToSheet = dialogView.findViewById(R.id.copyToSheet);
        share = dialogView.findViewById(R.id.share);
        bind = dialogView.findViewById(R.id.bind);
        saveProgress = dialogView.findViewById(R.id.saveProgress);
        warningMessage = dialogView.findViewById(R.id.replaceDataWarning);
        shareSheet = dialogView.findViewById(R.id.shareSpreadsheet);
        chooserGroup = dialogView.findViewById(R.id.chooserGroup);
        // chooseLabel = dialogView.findViewById(R.id.chooseSheetLabel);
        merge = dialogView.findViewById(R.id.merge);
    }

    @Override
    public void setupViewListeners(View dialogView) {
        dialogView.findViewById(R.id.sendDictionaryCancel).setOnClickListener((view) -> cancel());
        dialogView.findViewById(R.id.sendDictionaryOk).setOnClickListener(this::sendOk);
        dialogView.findViewById(R.id.closeSheetChooser).setOnClickListener((view) -> {
            chooserGroup.setVisibility(View.VISIBLE);
            sheetChooserLayout.setVisibility(View.GONE);
            copyToSheet.setChecked(false);
            bind.setChecked(false);
        });
        if (GlobalData.account == null) {
            copyToSheet.setText(R.string.send_dictionary_radio_copy_to_sheet_login);
            bind.setText(R.string.send_dictionary_radio_copy_to_bind_login);
        } else {
            copyToSheet.setOnCheckedChangeListener((view, isChecked) -> {
                copyToSheetChosen = isChecked;
                if (isChecked) {
                    // chooseLabel.setText(StaticUtils.getString(R.string.choose_sheet_label));
                    warningMessage.setVisibility(View.VISIBLE);
                    saveProgress.setVisibility(View.VISIBLE);
                    chooserGroup.setVisibility(View.GONE);
                    sheetChooserLayout.setVisibility(View.VISIBLE);
                    sheetChooserLayout.firstLoadSheet();
                } else {
                    chooserGroup.setVisibility(View.VISIBLE);
                    sheetChooserLayout.setVisibility(View.GONE);
                }
            });
        }
        bind.setOnCheckedChangeListener((view, isChecked) -> {
            bindChosen = isChecked;
            if (isChecked) {
                // chooseLabel.setText(StaticUtils.getString(R.string.choose_spreadsheet_label));
                warningMessage.setVisibility(View.GONE);
                saveProgress.setVisibility(View.GONE);
                chooserGroup.setVisibility(View.GONE);
                sheetChooserLayout.setVisibility(View.VISIBLE);
                sheetChooserLayout.firstLoadSheet();
            } else {
                chooserGroup.setVisibility(View.VISIBLE);
                sheetChooserLayout.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void setupViewAdjustments(View dialogView) {

    }

    private int getMode() {
        if (move.isChecked()) {
            return MOVE;
        } else if (copy.isChecked()) {
            return COPY;
        } else if (copyToSheetChosen) {
            return SEND_TO_SHEET;
        } else if (share.isChecked()) {
            return SHARE;
        } else if (shareSheet.isChecked()) {
            return SHARE_SHEET;
        } else if (merge.isChecked()) {
            return MERGE;
        } else if (bindChosen) {
            return BIND_SPREADSHEET;
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
            String spreadsheetId = info == null ? null : info.spreadSheetId;
            String sheetTitle = tab == null ? null : tab.getTitle();
            String spreadsheetName = info == null ? null : info.name;
            int sheetId = tab == null ? -1 : tab.getId();
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
            okListener.sendOk(mode, spreadsheetName, spreadsheetId, sheetTitle, sheetId, saveProgress.isChecked());
        }
    }
}
