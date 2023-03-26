package com.vsv.memorizer.adapters;

import android.annotation.SuppressLint;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vsv.dialogs.entities.SheetTab;
import com.vsv.memorizer.R;
import com.vsv.statics.GlobalData;
import com.vsv.utils.StaticUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

public class RecyclerSheetsAdapter extends RecyclerView.Adapter<RecyclerSheetsAdapter.CardHolder> {

    private ArrayList<SheetTab> items;

    private ArrayList<SheetTab> filteredItems;

    private final View.OnLongClickListener longClickListener;

    private int lastPosition = -1;

    public static class CardHolder extends RecyclerView.ViewHolder {

        public final View card;

        private final TextView name;

        private final View paddingView;

        public final View mainView;

        public CardHolder(View view) {
            super(view);
            name = view.findViewById(R.id.sheetName);
            mainView = view.findViewById(R.id.sheetItem);
            card = view;
            paddingView = view.findViewById(R.id.paddingView);
        }

        public TextView getName() {
            return name;
        }
    }

    public RecyclerSheetsAdapter(View.OnLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    public SheetTab getItem(int position) {
        return filteredItems.get(position);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setItems(ArrayList<SheetTab> items) {
        this.items = items;
        filter();
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void applyFilter(String query) {
        GlobalData.spreadsheetTabQuery = query.toLowerCase();
        filter();
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void update() {
        filter();
        notifyDataSetChanged();
    }

    private void filter() {
        String filterQuery = GlobalData.spreadsheetTabQuery;
        if (items == null) {
            filteredItems = null;
        } else {
            filteredItems = new ArrayList<>(items);
        }
        if (items != null && filterQuery != null && !filterQuery.isEmpty()) {
            filteredItems = (ArrayList<SheetTab>) items.stream()
                    .filter((item) -> item.getTitle().toLowerCase()
                            .contains(filterQuery.toLowerCase())).collect(Collectors.toList());
        }
        if (GlobalData.getSettings().sortTabs) {
            sort();
        }
    }

    private void sort() {
        if (filteredItems != null && filteredItems.size() > 0) {
            Comparator<SheetTab> comparator = Comparator.comparing(SheetTab::getTitle);
            filteredItems.sort(comparator);
        }
    }

    @NonNull
    @Override
    public RecyclerSheetsAdapter.CardHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecyclerSheetsAdapter.CardHolder(StaticUtils.inflate(R.layout.item_sheet, parent));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerSheetsAdapter.CardHolder viewHolder, final int position) {
        SheetTab item = filteredItems.get(position);
        if (position == getItemCount() - 1) {
            viewHolder.paddingView.setVisibility(View.VISIBLE);
        } else {
            viewHolder.paddingView.setVisibility(View.GONE);
        }
        viewHolder.getName().setText(item.getTitle());
        viewHolder.mainView.setTag(position);
        viewHolder.mainView.setOnLongClickListener(longClickListener);
        setAnimation(viewHolder.card, position, R.anim.from_bottom);
    }

    @Override
    public int getItemCount() {
        return filteredItems == null ? 0 : filteredItems.size();
    }

    private void setAnimation(View viewToAnimate, int position, int anim) {
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(viewToAnimate.getContext(), anim);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public void onViewDetachedFromWindow(CardHolder holder) {
        holder.card.clearAnimation();
    }
}
