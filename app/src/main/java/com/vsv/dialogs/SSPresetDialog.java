package com.vsv.dialogs;

import android.content.DialogInterface;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.vsv.db.entities.SpreadSheetInfo;
import com.vsv.memorizer.R;
import com.vsv.memorizer.adapters.RecyclerSSPresetsAdapter;
import com.vsv.spreadsheet.SheetLoader;
import com.vsv.statics.GlobalData;
import com.vsv.statics.GlobalExecutors;
import com.vsv.statics.WeakContext;
import com.vsv.toasts.Toasts;
import com.vsv.utils.StaticUtils;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class SSPresetDialog extends BottomDialog {

    private final RecyclerSSPresetsAdapter adapter;

    private final @Nullable
    ArrayList<SpreadSheetInfo> existedItems;

    private final AtomicBoolean closed = new AtomicBoolean(false);

    private final ProgressBar progress;

    private final TextView header;

    private final ViewGroup addLayout;

    public SSPresetDialog(@Nullable ArrayList<SpreadSheetInfo> existedItems) {
        super(R.layout.dialog_spreadsheet_presets);
        this.existedItems = existedItems;
        dialogView.findViewById(R.id.addPreset).setOnClickListener(this::addPreset);
        dialog.setOnShowListener(this::loadData);
        setOnDismissListener((dialogInterface) -> closed.set(true));
        RecyclerView content = dialogView.findViewById(R.id.ssPresetsContent);
        progress = dialogView.findViewById(R.id.progress);
        header = dialogView.findViewById(R.id.ssPresetsHeader);
        addLayout = dialogView.findViewById(R.id.addLayout);
        addLayout.setVisibility(View.GONE);
        adapter = new RecyclerSSPresetsAdapter();
        content.setAdapter(adapter);
    }

    private void loadData(@NonNull DialogInterface dialogInterface) {
        GlobalExecutors.modelsExecutor.execute(() -> {
            Pair<ArrayList<SpreadSheetInfo>, String> loaded = SheetLoader.loadDictPresets(GlobalData.account,
                    GlobalData.presetsSpreadsheetId, GlobalData.presetsSheetName);
            ArrayList<SpreadSheetInfo> presets = loaded.first;
            String error = loaded.second;
            if (error != null) {
                WeakContext.getMainActivity().runOnUiThread(() -> {
                    if (!closed.get()) {
                        progress.setVisibility(View.GONE);
                        header.setText(StaticUtils.getString(R.string.preset_error_header));
                        Toasts.shortShowRaw(error);
                    }
                });
            } else {
                WeakContext.getMainActivity().runOnUiThread(() -> {
                    if (!closed.get()) {
                        progress.setVisibility(View.GONE);
                        if (presets == null) {
                            Toasts.somethingWentWrongTryLatter();
                            header.setText(StaticUtils.getString(R.string.preset_error_header));
                        } else if (presets.isEmpty()) {
                            Toasts.noPresets();
                            header.setText(StaticUtils.getString(R.string.preset_error_header));
                        } else {
                            header.setText(StaticUtils.getString(R.string.preset_header));
                            addLayout.setVisibility(View.VISIBLE);
                            ArrayList<RecyclerSSPresetsAdapter.PresetItem> list = new ArrayList<>();
                            presets.stream().map(RecyclerSSPresetsAdapter.PresetItem::new).forEach(list::add);
                            adapter.setItems(list);
                        }
                    }
                });
            }
        });
    }

    private void addPreset(@Nullable View view) {
        ArrayList<SpreadSheetInfo> toAdd = new ArrayList<>();
        ArrayList<RecyclerSSPresetsAdapter.PresetItem> items = adapter.getItems();
        if (items != null) {
            for (RecyclerSSPresetsAdapter.PresetItem item : items) {
                if (item.isChecked) {
                    if (existedItems != null) {
                        if (existedItems.stream().noneMatch((sheetInfo) -> sheetInfo.spreadSheetId.equals(item.id))) {
                            toAdd.add(new SpreadSheetInfo(item.id, item.name, item.type));
                        }
                    } else {
                        toAdd.add(new SpreadSheetInfo(item.id, item.name));
                    }
                }
            }
        }
        if (!toAdd.isEmpty()) {
            StaticUtils.getModel().getSpreadsheetsRepository().insertSeveral(toAdd);
        }
        dialog.cancel();
    }
}
