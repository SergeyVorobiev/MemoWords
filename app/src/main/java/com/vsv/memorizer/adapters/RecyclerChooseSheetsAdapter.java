package com.vsv.memorizer.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.vsv.dialogs.entities.SheetTab;
import com.vsv.memorizer.R;
import com.vsv.utils.StaticUtils;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class RecyclerChooseSheetsAdapter extends RecyclerView.Adapter<RecyclerSheetsAdapter.CardHolder> {

    private ArrayList<SheetTab> items;

    private ArrayList<SheetTab> filteredItems;

    private final View.OnClickListener clickCardListener;

    private String filterQuery;

    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    public static class CardHolder extends RecyclerView.ViewHolder {

        private final View card;

        private final TextView name;

        public CardHolder(View view) {
            super(view);
            name = view.findViewById(R.id.sheetName);
            card = view;
        }

        public View getCard() {
            return card;
        }

        public TextView getName() {
            return name;
        }
    }

    public RecyclerChooseSheetsAdapter(View.OnClickListener clickCardListener) {
        this.clickCardListener = clickCardListener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void applyFilter(String query) {
        filterQuery = query.toLowerCase();
        if (filter()) {
            notifyDataSetChanged();
        }
    }

    public SheetTab getItem(int position) {
        return filteredItems.get(position);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setItems(ArrayList<SheetTab> items) {
        this.items = items;
        if (filter()) {
            notifyDataSetChanged();
        }
    }

    @Nullable
    public ArrayList<SheetTab> getItems() {
        return this.items;
    }

    private boolean filter() {
        if (items == null) {
            filteredItems = null;
        } else {
            filteredItems = new ArrayList<>(items);
        }
        if (items != null && filterQuery != null && !filterQuery.isEmpty()) {
            filteredItems = (ArrayList<SheetTab>) items.stream()
                    .filter((item) -> item.getTitle().toLowerCase()
                            .contains(filterQuery)).collect(Collectors.toList());
        }
        return true;
    }

    @NonNull
    @Override
    public RecyclerSheetsAdapter.CardHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        // Create a new view.
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sheet, parent, false);
        v.setOnClickListener(clickCardListener);
        return new RecyclerSheetsAdapter.CardHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerSheetsAdapter.CardHolder viewHolder, final int position) {
        SheetTab item = filteredItems.get(position);
        TextView name = viewHolder.getName();
        name.setText(item.getTitle());
        if (item.isChecked()) {
            name.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, StaticUtils.getDrawable(R.drawable.ic_check), null);
        } else {
            name.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null);
        }
        viewHolder.card.setTag(position);
    }

    @Override
    public int getItemCount() {
        return filteredItems == null ? 0 : filteredItems.size();
    }
}
