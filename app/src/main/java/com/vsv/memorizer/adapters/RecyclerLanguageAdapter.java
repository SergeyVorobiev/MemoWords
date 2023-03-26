package com.vsv.memorizer.adapters;

import android.annotation.SuppressLint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.vsv.memorizer.R;
import com.vsv.utils.StaticUtils;

import java.util.ArrayList;

public class RecyclerLanguageAdapter extends RecyclerView.Adapter<RecyclerLanguageAdapter.LanguageHolder> {

    private Item chosenItem = null;

    public static class Item {

        public String language;

        public String locale;

        public boolean isChecked;

        public Item(String language, String locale) {
            this.language = language;
            this.locale = locale;
        }
    }

    public static class LanguageHolder extends RecyclerView.ViewHolder {

        public final TextView name;

        public final CheckBox checkBox;

        public LanguageHolder(View view, View.OnClickListener itemClick) {
            super(view);
            view.setOnClickListener(itemClick);
            name = view.findViewById(R.id.languageName);
            checkBox = view.findViewById(R.id.checkBox);
        }
    }

    private ArrayList<Item> items;

    private final View.OnClickListener itemClick = this::onItemClick;

    @SuppressLint("NotifyDataSetChanged")
    private void onItemClick(@NonNull View view) {
        for (int i = 0; i < items.size(); i++) {
            items.get(i).isChecked = false;
        }
        Item item = items.get((int) view.getTag());
        chosenItem = item;
        item.isChecked = true;
        notifyDataSetChanged();
    }

    @Nullable
    public ArrayList<Item> getItems() {
        return items;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setItems(ArrayList<Item> items) {
        chosenItem = null;
        this.items = items;
        if (items != null) {
            for (Item item : items) {
                if (item.isChecked) {
                    chosenItem = item;
                    break;
                }
            }
        }
        notifyDataSetChanged();
    }

    public Item getChosenItem() {
        return chosenItem;
    }

    @NonNull
    @Override
    public RecyclerLanguageAdapter.LanguageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecyclerLanguageAdapter.LanguageHolder(StaticUtils.inflate(R.layout.item_language, parent), itemClick);
    }

    @Override
    public void onBindViewHolder(RecyclerLanguageAdapter.LanguageHolder viewHolder, final int position) {
        Item item = items.get(position);
        viewHolder.itemView.setTag(position);
        if (item.isChecked) {
            chosenItem = item;
        }
        viewHolder.itemView.setBackground(item.isChecked ? StaticUtils.getDrawable(R.drawable.bg_language_item) : null);
        viewHolder.name.setText(item.language);
        viewHolder.checkBox.setEnabled(false);
        viewHolder.checkBox.setChecked(item.isChecked);
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }
}

