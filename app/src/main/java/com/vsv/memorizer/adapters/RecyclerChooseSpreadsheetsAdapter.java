package com.vsv.memorizer.adapters;

import android.annotation.SuppressLint;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.vsv.db.entities.SpreadSheetInfo;
import com.vsv.memorizer.R;
import com.vsv.statics.GlobalData;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class RecyclerChooseSpreadsheetsAdapter extends RecyclerView.Adapter<RecyclerChooseSpreadsheetsAdapter.CardHolder> {

    private ArrayList<SpreadSheetInfo> items;

    private ArrayList<SpreadSheetInfo> filteredItems;

    private final View.OnClickListener clickCardListener;

    private String filterQuery;

    private final int excType;

    public static class CardHolder extends RecyclerView.ViewHolder {

        private final View card;

        private final TextView name;

        private final TextView id;

        public final TextView type;

        private static float spreadsheetIdTextSize = -1;

        // private static final int badgeSize = (int) WeakContext.getContext().getResources().getDimension(R.dimen.badge_size_mini);

        public CardHolder(View view) {
            super(view);
            name = view.findViewById(R.id.spreadsheetName);
            id = view.findViewById(R.id.id);
            spreadsheetIdTextSize = spreadsheetIdTextSize == -1 ?
                    view.getContext().getResources().getDimension(R.dimen.item_chosen_spreadsheet_id_size) :
                    spreadsheetIdTextSize;
            id.setTextSize(TypedValue.COMPLEX_UNIT_PX, spreadsheetIdTextSize);
            // id.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, BadgeCollection.createRandomBadge(badgeSize), null);
            type = view.findViewById(R.id.type);
            card = view;
        }

        public View getCard() {
            return card;
        }

        public TextView getId() {
            return id;
        }

        public TextView getName() {
            return name;
        }
    }

    public RecyclerChooseSpreadsheetsAdapter(View.OnClickListener clickCardListener, int excType) {
        this.excType = excType;
        this.clickCardListener = clickCardListener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void applyFilter(String query) {
        filterQuery = query;
        if (filter()) {
            notifyDataSetChanged();
        }
    }

    public SpreadSheetInfo getItem(int position) {
        return filteredItems.get(position);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setItems(ArrayList<SpreadSheetInfo> items) {
        this.items = items;
        if (filter()) {
            notifyDataSetChanged();
        }
    }

    private boolean filter() {
        if (items == null) {
            filteredItems = null;
        } else {
            filteredItems = new ArrayList<>(items);
        }
        if (items != null && filterQuery != null && !filterQuery.isEmpty()) {
            filteredItems = (ArrayList<SpreadSheetInfo>) items.stream()
                    .filter((item) -> item.spreadSheetId.toLowerCase()
                            .contains(filterQuery.toLowerCase()) || item.name.toLowerCase().contains(filterQuery.toLowerCase())).collect(Collectors.toList());
        }
        if (filteredItems != null && !filteredItems.isEmpty() && excType != 0) {
            filteredItems = (ArrayList<SpreadSheetInfo>) filteredItems.stream().filter((item) -> item.type != excType).collect(Collectors.toList());
        }
        return true;
    }

    @NonNull
    @Override
    public RecyclerChooseSpreadsheetsAdapter.CardHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_spreadsheet_choose, parent, false);
        v.setOnClickListener(clickCardListener);
        return new RecyclerChooseSpreadsheetsAdapter.CardHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerChooseSpreadsheetsAdapter.CardHolder viewHolder, final int position) {
        SpreadSheetInfo item = filteredItems.get(position);
        viewHolder.getName().setText(item.name);
        viewHolder.getId().setText(item.spreadSheetId);
        viewHolder.getCard().setTag(position);
        if (item.type > 0) {
            viewHolder.type.setText(GlobalData.TYPES[item.type]);
        } else {
            viewHolder.type.setText("");
        }
    }

    @Nullable
    public ArrayList<SpreadSheetInfo> getAllItems() {
        return items;
    }

    @Override
    public int getItemCount() {
        return filteredItems == null ? 0 : filteredItems.size();
    }
}
