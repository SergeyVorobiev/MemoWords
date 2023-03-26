package com.vsv.dialogs;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.vsv.db.entities.SpreadSheetInfo;
import com.vsv.dialogs.listeners.ShelfCreateListener;
import com.vsv.memorizer.R;
import com.vsv.spreadsheet.SheetChooserLayout2;
import com.vsv.statics.GlobalData;
import com.vsv.toasts.Toasts;
import com.vsv.utils.StaticUtils;
import com.vsv.utils.Symbols;

public class NewShelfDialog extends SingleCustomDialog {

    private EditText input;

    private ShelfCreateListener listener;

    private final SheetChooserLayout2 sheetChooserLayout;

    private final TextView pleaseLogin;

    private final Drawable down;

    private final Drawable up;

    private boolean unfoldSheets = false;

    private final ImageButton unfoldButton;

    private final CheckBox bindSpreadsheet;

    private final CheckBox loadProgress;

    public NewShelfDialog(boolean showLoadFromSpreadsheetOption) {
        super(R.layout.dialog_new_shelf, false, true);
        // showKeyboardFor(input);
        sheetChooserLayout = new SheetChooserLayout2(context, dialogView, SpreadSheetInfo.NOTEBOOK);
        pleaseLogin = dialogView.findViewById(R.id.pleaseLoginText);
        unfoldButton = dialogView.findViewById(R.id.unfoldSheets);
        int ssLayoutVisibility = showLoadFromSpreadsheetOption ? View.VISIBLE : View.GONE;
        dialogView.findViewById(R.id.loadFromSSLayout).setVisibility(ssLayoutVisibility);
        unfoldButton.setOnClickListener(this::unfoldSheets);
        pleaseLogin.setVisibility(View.GONE);
        bindSpreadsheet = dialogView.findViewById(R.id.bindSpreadsheet);
        bindSpreadsheet.setChecked(true);
        loadProgress = dialogView.findViewById(R.id.loadProgress);
        loadProgress.setChecked(true);
        down = ResourcesCompat.getDrawable(context.getResources(), R.drawable.btn_down_switch, context.getTheme());
        up = ResourcesCompat.getDrawable(context.getResources(), R.drawable.btn_up_switch, context.getTheme());
    }

    private void onClickOk(View view) {
        String name = input.getText().toString().trim();
        if (name.isEmpty()) {
            Toasts.shelfNameEmpty();
        } else {
            listener.createShelf(name, sheetChooserLayout.getChosenSheet(),
                    sheetChooserLayout.getChosenTabs(), bindSpreadsheet.isChecked(), loadProgress.isChecked());
            dialog.cancel();
        }
    }

    private void unfoldSheets(View view) {
        unfoldSheets = !unfoldSheets;
        closeKeyboardFor(input);
        if (unfoldSheets) {
            if (GlobalData.account == null) {
                pleaseLogin.setVisibility(View.VISIBLE);
            } else {
                sheetChooserLayout.setVisibility(View.VISIBLE);
            }
            unfoldButton.setBackground(up);
        } else {
            if (GlobalData.account == null) {
                pleaseLogin.setVisibility(View.GONE);
            } else {
                sheetChooserLayout.setVisibility(View.GONE);
            }
            unfoldButton.setBackground(down);
        }
    }

    public void createShelfListener(ShelfCreateListener listener) {
        this.listener = listener;
    }

    private void onClickCancel(View view) {
        dialog.cancel();
    }

    @Override
    public void setupViews(View dialogView) {
        input = dialogView.findViewById(R.id.newShelfInput);
    }

    @Override
    public void setupViewListeners(View dialogView) {
        dialogView.findViewById(R.id.newShelfOk).setOnClickListener(this::onClickOk);
        dialogView.findViewById(R.id.newShelfCancel).setOnClickListener(this::onClickCancel);
    }

    @Override
    public void setupViewAdjustments(View dialogView) {
        TextView header = dialogView.findViewById(R.id.newShelfDialogHeader);
        header.setText(R.string.new_shelf_dialog_header);
        input.setText(StaticUtils.getString(R.string.new_shelf_name_default, Symbols.getRandomNameSymbol()));
    }
}
