package com.vsv.dialogs;

import androidx.recyclerview.widget.RecyclerView;

import com.vsv.memorizer.R;
import com.vsv.memorizer.adapters.RecyclerHelpAdapter;

import java.util.ArrayList;

public class HelpDialog extends BottomDialog {

    public HelpDialog() {
        super(R.layout.dialog_help);
        RecyclerView content = dialogView.findViewById(R.id.helpContent);
        RecyclerHelpAdapter adapter = new RecyclerHelpAdapter();
        ArrayList<RecyclerHelpAdapter.HelpEntry> entries = new ArrayList<>();
        entries.add(RecyclerHelpAdapter.HelpEntry.createEmptyString());
        entries.add(RecyclerHelpAdapter.HelpEntry.createTopTextWithEmptyStringAtTheBottom(R.string.help1));
        entries.add(RecyclerHelpAdapter.HelpEntry.createTopTextWithEmptyStringAtTheBottom(R.string.help2));
        buildEmptyString(entries);
        entries.add(RecyclerHelpAdapter.HelpEntry.createTopTextWithEmptyStringAtTheBottom(R.string.help3));
        buildEmptyString(entries);
        entries.add(RecyclerHelpAdapter.HelpEntry.createTopTextWithEmptyStringAtTheBottom(R.string.help4));
        entries.add(RecyclerHelpAdapter.HelpEntry.createImage(R.raw.sample_example));
        entries.add(RecyclerHelpAdapter.HelpEntry.createEmptyString());
        entries.add(RecyclerHelpAdapter.HelpEntry.createTopTextWithEmptyStringAtTheBottom(R.string.help5));
        entries.add(RecyclerHelpAdapter.HelpEntry.createImage(R.raw.game_gym_example));
        entries.add(RecyclerHelpAdapter.HelpEntry.createEmptyString());
        entries.add(RecyclerHelpAdapter.HelpEntry.createTopTextWithEmptyStringAtTheBottom(R.string.help6));
        buildEmptyString(entries);
        entries.add(RecyclerHelpAdapter.HelpEntry.createTopTextWithEmptyStringAtTheBottom(R.string.help7));
        buildEmptyString(entries);
        entries.add(RecyclerHelpAdapter.HelpEntry.createTopTextWithEmptyStringAtTheBottom(R.string.help8));
        buildEmptyString(entries);
        entries.add(RecyclerHelpAdapter.HelpEntry.createTopTextWithEmptyStringAtTheBottom(R.string.help9));
        entries.add(RecyclerHelpAdapter.HelpEntry.createEmptyString());
        entries.add(RecyclerHelpAdapter.HelpEntry.createTopTextWithEmptyStringAtTheBottom(R.string.help10));
        entries.add(RecyclerHelpAdapter.HelpEntry.createAnimatedDrawable(R.raw.guide1, entries.size(), adapter));
        entries.add(RecyclerHelpAdapter.HelpEntry.createEmptyString());
        entries.add(RecyclerHelpAdapter.HelpEntry.createTopTextWithEmptyStringAtTheBottom(R.string.help11));
        buildEmptyString(entries);
        entries.add(RecyclerHelpAdapter.HelpEntry.createTopTextWithEmptyStringAtTheBottom(R.string.help12));
        buildEmptyString(entries);
        entries.add(RecyclerHelpAdapter.HelpEntry.createTopTextWithEmptyStringAtTheBottom(R.string.help13));
        buildEmptyString(entries);
        entries.add(RecyclerHelpAdapter.HelpEntry.createTopTextWithEmptyStringAtTheBottom(R.string.help14));
        buildEmptyString(entries);
        entries.add(RecyclerHelpAdapter.HelpEntry.createTopTextWithEmptyStringAtTheBottom(R.string.help15));
        entries.add(RecyclerHelpAdapter.HelpEntry.createEmptyString());
        buildEmptyString(entries);
        entries.add(RecyclerHelpAdapter.HelpEntry.createTopTextWithEmptyStringAtTheBottom(R.string.help16));
        buildEmptyString(entries);
        buildEmptyString(entries);
        entries.add(RecyclerHelpAdapter.HelpEntry.createTopTextWithEmptyStringAtTheBottom(R.string.help17));
        entries.add(RecyclerHelpAdapter.HelpEntry.createEmptyString());
        buildEmptyString(entries);
        adapter.setItems(entries);
        content.setAdapter(adapter);
    }

    private void buildEmptyString(ArrayList<RecyclerHelpAdapter.HelpEntry> entries) {
        entries.add(RecyclerHelpAdapter.HelpEntry.createEmptyString());
    }
}
