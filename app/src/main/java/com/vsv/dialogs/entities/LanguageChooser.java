package com.vsv.dialogs.entities;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;

import com.vsv.memorizer.R;
import com.vsv.speech.SupportedLanguages;

public class LanguageChooser implements AdapterView.OnItemSelectedListener {

    private final ArrayAdapter<String> leftLanguageAdapter;

    private String leftLanguageAbb;

    private String rightLanguageAbb;

    public LanguageChooser(@NonNull Context context, @NonNull View dialogView, @NonNull String leftLanguageAbb, @NonNull String rightLanguageAbb) {
        this.leftLanguageAbb = leftLanguageAbb;
        this.rightLanguageAbb = rightLanguageAbb;
        Spinner leftLanguages = dialogView.findViewById(R.id.chooseLeftLanguage);
        leftLanguageAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, SupportedLanguages.getLanguages());
        leftLanguages.setAdapter(leftLanguageAdapter);
        leftLanguages.setSelection(SupportedLanguages.getIndex(this.leftLanguageAbb));
        leftLanguages.setOnItemSelectedListener(this);

        Spinner rightLanguages = dialogView.findViewById(R.id.chooseRightLanguage);
        ArrayAdapter<String> rightLanguageAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, SupportedLanguages.getLanguages());
        rightLanguages.setAdapter(rightLanguageAdapter);
        rightLanguages.setSelection(SupportedLanguages.getIndex(this.rightLanguageAbb));
        rightLanguages.setOnItemSelectedListener(this);
    }

    public String getLeftLanguageAbb() {
        return leftLanguageAbb;
    }

    public String getRightLanguageAbb() {
        return rightLanguageAbb;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getAdapter() == leftLanguageAdapter) {
            leftLanguageAbb = SupportedLanguages.getLanguageAbb(position);
        } else {
            rightLanguageAbb = SupportedLanguages.getLanguageAbb(position);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
