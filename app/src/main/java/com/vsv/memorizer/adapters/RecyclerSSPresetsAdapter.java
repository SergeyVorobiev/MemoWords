package com.vsv.memorizer.adapters;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.vsv.db.entities.SpreadSheetInfo;
import com.vsv.memorizer.R;
import com.vsv.statics.GlobalData;
import com.vsv.utils.StaticUtils;

import java.util.ArrayList;

public class RecyclerSSPresetsAdapter extends RecyclerView.Adapter<RecyclerSSPresetsAdapter.SheetHolder> {

    public static class PresetItem {

        public String name;

        public String id;

        public int type;

        public boolean isChecked;

        public PresetItem(SpreadSheetInfo info) {
            id = info.spreadSheetId;
            name = info.name;
            type = info.type;
        }
    }

    public static class SheetHolder extends RecyclerView.ViewHolder {

        final TextView name;

        final TextView id;

        final View view;

        final TextView type;

        public SheetHolder(View view, View.OnClickListener itemClick) {
            super(view);
            this.view = view;
            this.view.setOnClickListener(itemClick);
            name = view.findViewById(R.id.spreadsheetName);
            id = view.findViewById(R.id.id);
            name.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, StaticUtils.getDrawable(R.drawable.ic_check), null);
            type = view.findViewById(R.id.type);
        }

        public void setData(PresetItem item, int position) {
            name.setText(item.name);
            id.setText(item.id);
            view.setTag(position);
            type.setText(GlobalData.TYPES[item.type]);
            Drawable drawable = item.isChecked ? StaticUtils.getDrawable(R.drawable.ic_check) : null;
            name.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, drawable, null);
        }
    }

    private ArrayList<PresetItem> items;

    private final View.OnClickListener itemClick = this::onItemClick;

    private void onItemClick(@NonNull View view) {
        PresetItem item = items.get((int) view.getTag());
        item.isChecked = !item.isChecked;
        Drawable drawable = item.isChecked ? StaticUtils.getDrawable(R.drawable.ic_check) : null;
        TextView name = view.findViewById(R.id.spreadsheetName);
        name.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, drawable, null);
    }

    @Nullable
    public ArrayList<PresetItem> getItems() {
        return items;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setItems(ArrayList<PresetItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerSSPresetsAdapter.SheetHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecyclerSSPresetsAdapter.SheetHolder(StaticUtils.inflate(R.layout.item_spreadsheet, parent), itemClick);
    }

    @Override
    public void onBindViewHolder(RecyclerSSPresetsAdapter.SheetHolder viewHolder, final int position) {
        viewHolder.setData(items.get(position), position);
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }
}

