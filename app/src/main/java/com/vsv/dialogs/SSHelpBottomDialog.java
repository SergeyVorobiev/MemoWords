package com.vsv.dialogs;

import androidx.recyclerview.widget.RecyclerView;

import com.vsv.memorizer.R;
import com.vsv.memorizer.adapters.RecyclerHelpAdapter;
import com.vsv.utils.Spec;
import com.vsv.utils.StaticUtils;

import java.util.ArrayList;

public class SSHelpBottomDialog extends BottomDialog {

    public SSHelpBottomDialog() {
        super(R.layout.dialog_spreadsheet_help);
        RecyclerView content = dialogView.findViewById(R.id.ssHelpContent);
        RecyclerHelpAdapter adapter = new RecyclerHelpAdapter();
        ArrayList<RecyclerHelpAdapter.HelpEntry> entries = new ArrayList<>();
        entries.add(RecyclerHelpAdapter.HelpEntry.createEmptyString());
        entries.add(RecyclerHelpAdapter.HelpEntry.createTopTextWithEmptyStringAtTheBottom(R.string.spreadsheet_help));
        entries.add(RecyclerHelpAdapter.HelpEntry.createEmptyString());
        entries.add(RecyclerHelpAdapter.HelpEntry.createTopTextWithEmptyStringAtTheBottom(R.string.spreadsheet_help1_0));
        entries.add(RecyclerHelpAdapter.HelpEntry.createEmptyString());
        entries.add(RecyclerHelpAdapter.HelpEntry.createTopTextWithEmptyStringAtTheBottom(R.string.spreadsheet_help1_1));
        entries.add(RecyclerHelpAdapter.HelpEntry.createEmptyString());
        entries.add(RecyclerHelpAdapter.HelpEntry.createTopTextWithEmptyStringAtTheBottom(R.string.spreadsheet_help1_2));
        entries.add(RecyclerHelpAdapter.HelpEntry.createEmptyString());
        entries.add(RecyclerHelpAdapter.HelpEntry.createTopTextWithEmptyStringAtTheBottom(R.string.spreadsheet_help2));
        entries.add(RecyclerHelpAdapter.HelpEntry.createImage(R.raw.google_id));
        entries.add(RecyclerHelpAdapter.HelpEntry.createEmptyString());
        entries.add(RecyclerHelpAdapter.HelpEntry.createImage(R.raw.spreadsheet_example));
        entries.add(RecyclerHelpAdapter.HelpEntry.createEmptyString());
        entries.add(RecyclerHelpAdapter.HelpEntry.createTopTextWithEmptyStringAtTheBottom(R.string.spreadsheet_help3));
        entries.add(RecyclerHelpAdapter.HelpEntry.createImage(R.raw.google_example));
        entries.add(RecyclerHelpAdapter.HelpEntry.createEmptyString());
        entries.add(RecyclerHelpAdapter.HelpEntry.createTopTextWithEmptyStringAtTheBottom(R.string.spreadsheet_help4));
        entries.add(RecyclerHelpAdapter.HelpEntry.createEmptyString());
        entries.add(RecyclerHelpAdapter.HelpEntry.createTopText(StaticUtils.getString(R.string.spreadsheet_help5, Spec.MAX_SAMPLES)));
        entries.add(RecyclerHelpAdapter.HelpEntry.createEmptyString());
        entries.add(RecyclerHelpAdapter.HelpEntry.createTopTextWithEmptyStringAtTheBottom(R.string.spreadsheet_help6));
        entries.add(RecyclerHelpAdapter.HelpEntry.createEmptyString());
        entries.add(RecyclerHelpAdapter.HelpEntry.createTopTextWithEmptyStringAtTheBottom(R.string.spreadsheet_help7));
        entries.add(RecyclerHelpAdapter.HelpEntry.createEmptyString());
        entries.add(RecyclerHelpAdapter.HelpEntry.createTopTextWithEmptyStringAtTheBottom(R.string.spreadsheet_help8));
        entries.add(RecyclerHelpAdapter.HelpEntry.createEmptyString());
        entries.add(RecyclerHelpAdapter.HelpEntry.createTopTextWithEmptyStringAtTheBottom(R.string.spreadsheet_help9));
        entries.add(RecyclerHelpAdapter.HelpEntry.createEmptyString());
        entries.add(RecyclerHelpAdapter.HelpEntry.createImage(R.raw.clean_samples_example));
        entries.add(RecyclerHelpAdapter.HelpEntry.createEmptyString());
        adapter.setItems(entries);
        content.setAdapter(adapter);
    }
}
