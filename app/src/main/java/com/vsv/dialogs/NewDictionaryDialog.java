package com.vsv.dialogs;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.vsv.db.entities.SpreadSheetInfo;
import com.vsv.dialogs.entities.LanguageChooser;
import com.vsv.dialogs.entities.SheetTab;
import com.vsv.dialogs.listeners.DictionaryCreateListener;
import com.vsv.dialogs.listeners.DictionaryLoadFromSpreadsheetListener;
import com.vsv.memorizer.R;
import com.vsv.speech.SupportedLanguages;
import com.vsv.spreadsheet.SheetChooserLayout;

import com.vsv.statics.GlobalData;
import com.vsv.toasts.Toasts;
import com.vsv.utils.StaticUtils;
import com.vsv.utils.Symbols;

public class NewDictionaryDialog extends SingleCustomDialog {

    private final TextView inputDictName;

    private DictionaryCreateListener listener;

    private DictionaryLoadFromSpreadsheetListener spreadsheetListener;

    private boolean unfoldSheets = false;

    private final ImageButton unfoldButton;

    private final View chooseLanguagesLayout;

    private final ImageView unfoldLanguagesButton;

    private boolean unfoldLanguages;

    private final LanguageChooser languageChooser;

    private final SheetChooserLayout sheetChooserLayout;

    private final CheckBox bindSpreadsheet;

    private final CheckBox loadProgress;

    private final TextView pleaseLogin;

    private final Drawable down;

    private final Drawable up;

    public NewDictionaryDialog() {
        super(R.layout.dialog_new_dictionary, false, true);
        unfoldButton = dialogView.findViewById(R.id.unfoldSheets);
        chooseLanguagesLayout = dialogView.findViewById(R.id.chooseLanguagesLayout);
        unfoldLanguagesButton = dialogView.findViewById(R.id.unfoldLanguages);
        inputDictName = dialogView.findViewById(R.id.newDictionaryInput);
        inputDictName.setText(StaticUtils.getString(R.string.def_dict_name, Symbols.getRandomNameSymbol()));
        pleaseLogin = dialogView.findViewById(R.id.pleaseLoginText);
        bindSpreadsheet = dialogView.findViewById(R.id.bindSpreadsheet);
        bindSpreadsheet.setChecked(true);
        loadProgress = dialogView.findViewById(R.id.loadProgress);
        loadProgress.setChecked(true);
        unfoldLanguagesButton.setOnClickListener(this::unfoldLanguages);
        unfoldButton.setOnClickListener(this::unfoldSheets);
        dialogView.findViewById(R.id.newDictionaryOk).setOnClickListener(this::onClickOk);
        dialogView.findViewById(R.id.newDictionaryCancel).setOnClickListener(this::onClickCancel);
        String noneAbb = SupportedLanguages.noneAbbreviation;
        languageChooser = new LanguageChooser(context, dialogView, noneAbb, noneAbb);
        sheetChooserLayout = new SheetChooserLayout(context, dialogView, null, null, SpreadSheetInfo.NOTEBOOK);
        chooseLanguagesLayout.setVisibility(View.GONE);
        pleaseLogin.setVisibility(View.GONE);
        down = ResourcesCompat.getDrawable(context.getResources(), R.drawable.btn_down_switch, context.getTheme());
        up = ResourcesCompat.getDrawable(context.getResources(), R.drawable.btn_up_switch, context.getTheme());
    }

    private void unfoldLanguages(View view) {
        closeKeyboardFor(inputDictName);
        unfoldSheets = false;
        sheetChooserLayout.hideLayout();
        unfoldButton.setBackground(down);
        unfoldLanguages = !unfoldLanguages;
        if (unfoldLanguages) {
            chooseLanguagesLayout.setVisibility(View.VISIBLE);
            unfoldLanguagesButton.setBackground(up);
        } else {
            chooseLanguagesLayout.setVisibility(View.GONE);
            unfoldLanguagesButton.setBackground(down);
        }
    }

    private void unfoldSheets(View view) {
        closeKeyboardFor(inputDictName);
        chooseLanguagesLayout.setVisibility(View.GONE);
        unfoldLanguagesButton.setBackground(down);
        unfoldLanguages = false;
        unfoldSheets = !unfoldSheets;
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

    private void onClickOk(View view) {
        String name = inputDictName.getText().toString().trim();
        SpreadSheetInfo chosenSheet = sheetChooserLayout.getChosenSpreadsheet();
        SheetTab chosenTab = sheetChooserLayout.getChosenSheet();
        String spreadsheetId = chosenSheet == null ? "" : chosenSheet.spreadSheetId;
        String sheetName = chosenTab == null ? "" : chosenTab.getTitle();
        int sheetId = chosenTab == null ? -1 : chosenTab.getId();
        if (!spreadsheetId.isEmpty() && !sheetName.isEmpty()) {
            SheetTab sheetTab = new SheetTab(sheetName, sheetId);
            boolean result = this.spreadsheetListener.loadFromSpreadsheet(name, languageChooser.getLeftLanguageAbb(),
                    languageChooser.getRightLanguageAbb(), sheetTab, spreadsheetId, chosenSheet.name,
                    loadProgress.isChecked(), bindSpreadsheet.isChecked());
            if (result) {
                sheetChooserLayout.removeObservers();
                dialog.cancel();
            }
        } else if (name.isEmpty()) {
            Toasts.dictionaryNameEmpty();
        } else {
            // boolean creationResult = Storage.getStorageData().createDictionaryFile(name);
            listener.createDictionary(name, languageChooser.getLeftLanguageAbb(), languageChooser.getRightLanguageAbb());
            sheetChooserLayout.removeObservers();
            dialog.cancel();
        }
    }

    public void setDictionaryCreateListener(DictionaryCreateListener listener) {
        this.listener = listener;
    }

    public void setDictionaryLoadFromSpreadsheetListener(DictionaryLoadFromSpreadsheetListener listener) {
        this.spreadsheetListener = listener;
    }

    private void onClickCancel(View view) {
        sheetChooserLayout.removeObservers();
        dialog.cancel();
    }

    @Override
    public void setupViews(View dialogView) {

    }

    @Override
    public void setupViewListeners(View dialogView) {

    }

    @Override
    public void setupViewAdjustments(View dialogView) {

    }
}
