package com.vsv.memorizer.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.vsv.dialogs.entities.FoundWordItem;
import com.vsv.memorizer.R;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class RecyclerFindDictAdapter extends RecyclerView.Adapter<RecyclerFindDictAdapter.CardHolder> {

    private ArrayList<FoundWordItem> items;

    private String filter;

    private final int maxSize;

    private final Consumer<FoundWordItem> consumer;

    private int lastPosition = -1;

    private boolean fromStart = false;

    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    public static class CardHolder extends RecyclerView.ViewHolder {

        private final View card;

        public final TextView leftView;

        public final TextView rightView;

        public final TextView dictView;

        public final TextView kind;

        public final TextView percentage;

        public CardHolder(View view) {
            super(view);
            dictView = view.findViewById(R.id.dictName);
            leftView = view.findViewById(R.id.leftValue);
            rightView = view.findViewById(R.id.rightValue);
            kind = view.findViewById(R.id.kind);
            percentage = view.findViewById(R.id.percentage);
            card = view;
        }

        public View getCard() {
            return card;
        }
    }

    public RecyclerFindDictAdapter(int maxSize, Consumer<FoundWordItem> consumer) {
        this.consumer = consumer;
        this.maxSize = maxSize;
    }

    public void setFilter(@Nullable String filter) {
        this.filter = filter;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setEmpty() {
        this.items = new ArrayList<>();
        notifyDataSetChanged();
    }

    public synchronized void clearItems() {
        this.items = null;
    }

    private synchronized @Nullable
    FoundWordItem getCopyItem(int position) {
        if (this.items != null && this.items.size() > position) {
            FoundWordItem item = this.items.get(position);
            return item.copy();
        }
        return null;
    }

    @MainThread
    @SuppressLint("NotifyDataSetChanged")
    public void appendItems(@Nullable ArrayList<FoundWordItem> items) {
        if (filter == null || filter.isEmpty() || items == null) {
            return;
        } else {
            items = (ArrayList<FoundWordItem>) items.stream().filter(this::filter).collect(Collectors.toList());
        }
        if (this.items == null && !items.isEmpty()) {
            this.items = items;
        } else if (this.items != null) {
            this.items.addAll(items);
            this.items = (ArrayList<FoundWordItem>) this.items.stream()
                    .limit(maxSize).collect(Collectors.toList());
        }
        lastPosition = -1;
        notifyDataSetChanged();
    }

    private boolean filter(FoundWordItem item) {
        if (item == null || filter == null || filter.isEmpty()) {
            return false;
        } else if (filter.length() < 2 || fromStart) {
            return item.leftValue.toLowerCase().startsWith(filter) || item.rightValue.toLowerCase().startsWith(filter);
        } else {
            return item.leftValue.toLowerCase().contains(filter) || item.rightValue.toLowerCase().contains(filter);
        }
    }

    public void setFromStart(boolean fromStart) {
        this.fromStart = fromStart;
    }

    @NonNull
    @Override
    public RecyclerFindDictAdapter.CardHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        // Create a new view.
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_found_word, parent, false);
        return new RecyclerFindDictAdapter.CardHolder(v);
    }

    @Override
    public void onBindViewHolder(CardHolder viewHolder, final int position) {
        FoundWordItem item = items.get(position);
        viewHolder.leftView.setText(item.leftValue);
        viewHolder.rightView.setText(item.rightValue);
        viewHolder.dictView.setText(item.pathName);
        viewHolder.percentage.setText(item.percentage);
        viewHolder.kind.setText(item.kind);
        viewHolder.getCard().setTag(position);
        viewHolder.getCard().setOnClickListener(this::onItemClick);
        // setAnimation(viewHolder.card, position, R.anim.appearing);
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

    private void onItemClick(View view) {
        int position = (int) view.getTag();
        FoundWordItem item = getCopyItem(position);
        if (item != null) {
            consumer.accept(item);
        }
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }
}

