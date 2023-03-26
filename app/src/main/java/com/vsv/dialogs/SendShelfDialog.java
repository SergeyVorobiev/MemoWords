package com.vsv.dialogs;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.vsv.db.entities.Dictionary;
import com.vsv.db.entities.SpreadSheetInfo;
import com.vsv.dialogs.listeners.ShelfCreateListener;
import com.vsv.memorizer.R;
import com.vsv.spreadsheet.SheetChooserLayout3;
import com.vsv.spreadsheet.SheetLoader;
import com.vsv.statics.GlobalData;
import com.vsv.toasts.Toasts;

import java.util.ArrayList;

public class SendShelfDialog extends SingleCustomDialog {

    private ShelfCreateListener listener;

    private final SheetChooserLayout3 sheetChooserLayout;

    private final ArrayList<Dictionary> dictionaries;

    public SendShelfDialog(@NonNull ArrayList<Dictionary> dictionaries) {
        super(R.layout.dialog_send_shelf, false, true);
        this.dictionaries = dictionaries;
        sheetChooserLayout = new SheetChooserLayout3(context, dialogView, SpreadSheetInfo.NOTEBOOK);
        TextView pleaseLogin = dialogView.findViewById(R.id.pleaseLoginText);
        View sheetChooserContent = dialogView.findViewById(R.id.sheetChooserContent);
        TextView warningMessage = dialogView.findViewById(R.id.warningMessage);
        if (GlobalData.account == null) {
            warningMessage.setVisibility(View.GONE);
            sheetChooserContent.setVisibility(View.GONE);
            pleaseLogin.setVisibility(View.VISIBLE);
        } else {
            warningMessage.setVisibility(View.VISIBLE);
            sheetChooserContent.setVisibility(View.VISIBLE);
            pleaseLogin.setVisibility(View.GONE);
        }
    }

    private void onClickOk(View view) {
        SpreadSheetInfo sheetInfo = sheetChooserLayout.getChosenSheet();
        if (sheetInfo == null) {
            Toasts.chooseSpreadsheet();
        } else {
            SheetLoader.sendShelf(dictionaries, sheetInfo.spreadSheetId);
            dialog.cancel();
        }
    }

    private void onClickCancel(View view) {
        dialog.cancel();
    }

    @Override
    public void setupViews(View dialogView) {

    }

    @Override
    public void setupViewListeners(View dialogView) {
        dialogView.findViewById(R.id.newShelfOk).setOnClickListener(this::onClickOk);
        dialogView.findViewById(R.id.newShelfCancel).setOnClickListener(this::onClickCancel);
    }

    @Override
    public void setupViewAdjustments(View dialogView) {
        TextView header = dialogView.findViewById(R.id.newShelfDialogHeader);
        header.setText(R.string.send_shelf);
    }
}
