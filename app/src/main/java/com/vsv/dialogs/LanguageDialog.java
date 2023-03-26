package com.vsv.dialogs;

import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.vsv.db.entities.Settings;
import com.vsv.memorizer.R;
import com.vsv.memorizer.adapters.RecyclerLanguageAdapter;
import com.vsv.statics.GlobalData;
import com.vsv.statics.WeakContext;
import com.vsv.utils.StaticUtils;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@SuppressWarnings("FieldCanBeLocal")
public class LanguageDialog extends BottomDialog {

    private final int[] stringsId = new int[] {R.string.default_language, R.string.russian_language, R.string.english_language};

    private final String[] locales = new String[] {"", "ru", "us"};

    private final RecyclerLanguageAdapter adapter;

    public LanguageDialog() {
        super(R.layout.dialog_language);
        dialogView.findViewById(R.id.changeLanguage).setOnClickListener(this::changeLanguage);
        RecyclerView content = dialogView.findViewById(R.id.languageContent);
        adapter = new RecyclerLanguageAdapter();
        ArrayList<RecyclerLanguageAdapter.Item> items = new ArrayList<>();
        String currentLocale = GlobalData.getSettings().appLocale;
        if (currentLocale == null) {
            currentLocale = "";
        }
        for (int i = 0; i < stringsId.length; i++) {
            RecyclerLanguageAdapter.Item item = new RecyclerLanguageAdapter.Item(StaticUtils.getString(stringsId[i]), locales[i]);
            if (currentLocale.equals(locales[i])) {
                item.isChecked = true;
            }
            items.add(item);
        }
        adapter.setItems(items);
        content.setAdapter(adapter);
    }

    private void changeLanguage(@Nullable View view) {
        RecyclerLanguageAdapter.Item item = adapter.getChosenItem();
        Settings settings = GlobalData.getSettings();
        String settingsLocale = settings.appLocale;
        if (settingsLocale == null) {
            settingsLocale = "";
        }
        if (!item.locale.equals(settingsLocale)) {
            settings.appLocale = item.locale;
            try {
                StaticUtils.getModel().updateAndGetFuture(settings).get(10, TimeUnit.SECONDS);
            } catch (ExecutionException | InterruptedException | TimeoutException e) {
                // Ok we nevertheless try to restart the app
            }
            WeakContext.getMainActivity().recreate();
        }
        dialog.cancel();
    }
}
