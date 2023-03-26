package com.vsv.memorizer.adapters;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.vsv.bundle.entities.ShelfBundle;
import com.vsv.bundle.helpers.BundleNames;
import com.vsv.db.entities.Settings;
import com.vsv.db.entities.SpreadSheetInfo;
import com.vsv.dialogs.SingleWindow;
import com.vsv.dialogs.UpdateSpreadsheetDialog;
import com.vsv.memorizer.R;
import com.vsv.statics.GlobalData;
import com.vsv.statics.WeakContext;
import com.vsv.utils.BadgeCollection;
import com.vsv.utils.Spec;
import com.vsv.utils.StaticFonts;
import com.vsv.utils.StaticUtils;
import com.vsv.utils.Timer;
import com.vsv.viewutils.StopVerticalScrollAnimator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class RecyclerSpreadsheetsAdapter extends AbstractAdapter<RecyclerSpreadsheetsAdapter.CardHolder> {

    private ArrayList<SpreadSheetInfo> items;

    private ArrayList<SpreadSheetInfo> filteredItems;

    private final View.OnClickListener onSSClick;

    private final View.OnLongClickListener onSSLongClick;

    private final View.OnClickListener iconClickListener;

    private int lastPosition = -1;

    private @Nullable
    ShelfBundle shelfBundle;

    private final int badgeSize;

    private CountDownTimer timer;

    public final TreeMap<Integer, RecyclerSpreadsheetsAdapter.CardHolder> holders = new TreeMap<>();

    public static class CardHolder extends RecyclerView.ViewHolder {

        private final View card;

        private final TextView name;

        private final View mainView;

        public final TextView type;

        private final TextView id;

        private final Animation animation;

        public final TransitionDrawable iconTransitionDrawable;

        public long transitionTime;

        public boolean isReverse;

        private final View paddingView;

        public CardHolder(View view, int badgeSize, View.OnClickListener iconClickListener) {
            super(view);
            name = view.findViewById(R.id.spreadsheetName);
            mainView = view.findViewById(R.id.spreadsheetItem);
            id = view.findViewById(R.id.id);
            card = view;
            animation = AnimationUtils.loadAnimation(view.getContext(), R.anim.from_bottom);
            transitionTime = System.nanoTime();
            paddingView = view.findViewById(R.id.paddingView);
            ImageView imageView = view.findViewById(R.id.icon);
            imageView.setOnClickListener(iconClickListener);
            iconTransitionDrawable = (TransitionDrawable) imageView.getDrawable();
            iconTransitionDrawable.setCrossFadeEnabled(true);
            type = view.findViewById(R.id.type);
            type.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, BadgeCollection.createRandomBadge(badgeSize), null);
        }

        public void animate() {
            card.startAnimation(animation);
        }

        public void clearAnimation() {
            card.clearAnimation();
        }

        public TextView getId() {
            return id;
        }

        public TextView getName() {
            return name;
        }
    }

    public RecyclerSpreadsheetsAdapter(@NonNull RecyclerView owner) {
        StopVerticalScrollAnimator.setRecycleViewAnimation(owner, Spec.MIN_SCROLL_HIT, R.anim.from_bottom2, R.anim.from_top);
        badgeSize = (int) WeakContext.getContext().getResources().getDimension(R.dimen.badge_size);
        this.onSSClick = this::onSpreadsheetClick;
        this.onSSLongClick = this::updateClick;
        this.iconClickListener = this::updateFont;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateFont(@NonNull View view) {
        Settings settings = GlobalData.getSettings();
        settings.fontSpreadsheetTitleIndex += 1;
        if (settings.fontSpreadsheetTitleIndex >= StaticFonts.fonts.length) {
            settings.fontSpreadsheetTitleIndex = 0;
        }
        StaticUtils.getModel().updateFontSpreadsheetIndex(settings.id, settings.fontDictTitleIndex);
        notifyDataSetChanged();
    }

    // Create new ArrayList and place all items in it (not deep copy, just references).
    public @Nullable
    ArrayList<SpreadSheetInfo> copyAll() {
        return items == null ? null : new ArrayList<>(items);
    }

    public boolean updateClick(View view) {
        SpreadSheetInfo sheet = this.getItem((int) view.getTag());
        UpdateSpreadsheetDialog dialog = new UpdateSpreadsheetDialog(sheet, items);
        dialog.setUpdateSheetListener(StaticUtils.getModel().getSpreadsheetsRepository()::update);
        dialog.show();
        return true;
    }

    public void setData(@Nullable ShelfBundle shelfBundle) {
        this.shelfBundle = shelfBundle;
        this.timer = createTimer();
        this.timer.start();
    }

    private CountDownTimer createTimer() {
        return new CountDownTimer(Integer.MAX_VALUE, 1000) {

            private final Handler handler = new Handler(Looper.getMainLooper());

            @Override
            public void onTick(long millisUntilFinished) {
                int size = RecyclerSpreadsheetsAdapter.this.holders.values().size();
                if (size == 0) {
                    return;
                }
                int picked = StaticUtils.random.nextInt(size);
                handler.post(() -> {
                    int i = 0;
                    int duration = 2000;
                    // int random = StaticUtils.random.nextInt(2);
                    for (RecyclerSpreadsheetsAdapter.CardHolder holder: RecyclerSpreadsheetsAdapter.this.holders.values()) {
                        int elapsedTime = (int) Timer.nanoTimeDiffFromNowInMilliseconds(holder.transitionTime);
                        TransitionDrawable drawable = holder.iconTransitionDrawable;
                        if (i == picked /* && random == 0 */) {
                            if (holder.isReverse && elapsedTime >= duration) {
                                holder.transitionTime = System.nanoTime();
                                drawable.reverseTransition(duration);
                                holder.isReverse = false;
                            } else if (!holder.isReverse && elapsedTime >= duration) {
                                holder.transitionTime = System.nanoTime();
                                drawable.startTransition(duration);
                                holder.isReverse = true;
                            }
                        } else {
                            if (holder.isReverse && elapsedTime >= duration) {
                                holder.transitionTime = System.nanoTime();
                                drawable.reverseTransition(duration);
                                holder.isReverse = false;
                            }
                        }
                        i++;
                    }
                });
            }

            @Override
            public void onFinish() {

            }
        };
    }

    public void onSpreadsheetClick(View view) {
        if (SingleWindow.isShownToast()) {
            return;
        }
        SpreadSheetInfo sheet = this.getItem((int) view.getTag());
        Bundle bundle = new Bundle();
        bundle.putLong(BundleNames.SHEET_ID, sheet.id);
        bundle.putString(BundleNames.SHEET_WEB_ID, sheet.spreadSheetId);
        bundle.putString(BundleNames.SHEET_NAME, sheet.name);
        if (shelfBundle != null) {
            shelfBundle.toBundle(bundle);
        }
        StaticUtils.navigateSafe(R.id.action_Spreadsheets_to_Sheets, bundle);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void applyFilter(String query) {
        GlobalData.spreadsheetQuery = query.toLowerCase();
        filter();
        notifyDataSetChanged();
    }

    public SpreadSheetInfo getItem(int position) {
        return filteredItems.get(position);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setItems(ArrayList<SpreadSheetInfo> items) {
        this.items = items;
        filter();
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void clearData() {
        this.timer.cancel();
        this.holders.clear();
        this.shelfBundle = null;
        this.items = null;
        this.filteredItems = null;
        lastPosition = -1;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void update() {
        filter();
        notifyDataSetChanged();
    }

    private void filter() {
        initFiltered();
        filterContains();
        sort();
    }

    private void initFiltered() {
        if (items == null) {
            filteredItems = null;
        } else {
            filteredItems = new ArrayList<>(items);
        }
    }

    @SuppressWarnings("unused")
    private void filterFromStart() {
        String filterQuery = GlobalData.spreadsheetQuery;
        if (items != null && filterQuery != null && !filterQuery.isEmpty()) {
            filteredItems = (ArrayList<SpreadSheetInfo>) items.stream()
                    .filter((item) -> item.spreadSheetId.toLowerCase()
                            .startsWith(filterQuery.toLowerCase()) || item.name.toLowerCase().startsWith(filterQuery.toLowerCase())).collect(Collectors.toList());
        }
    }

    private void filterContains() {
        String filterQuery = GlobalData.spreadsheetQuery;
        if (items != null && filterQuery != null && !filterQuery.isEmpty()) {
            filteredItems = (ArrayList<SpreadSheetInfo>) items.stream()
                    .filter((item) -> item.spreadSheetId.toLowerCase()
                            .contains(filterQuery.toLowerCase()) || item.name.toLowerCase().contains(filterQuery.toLowerCase())).collect(Collectors.toList());
        }
    }

    private void sort() {
        if (GlobalData.getSettings().sortSheets && filteredItems != null && filteredItems.size() > 0) {
            Comparator<SpreadSheetInfo> comparator = Comparator.comparing(SpreadSheetInfo::getName);
            filteredItems.sort(comparator);
        }
    }

    @NonNull
    @Override
    public RecyclerSpreadsheetsAdapter.CardHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_spreadsheet, parent, false);
        View mainView = v.findViewById(R.id.spreadsheetItem);
        mainView.setOnClickListener(onSSClick);
        mainView.setOnLongClickListener(onSSLongClick);
        return new RecyclerSpreadsheetsAdapter.CardHolder(v, badgeSize, iconClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerSpreadsheetsAdapter.CardHolder viewHolder, final int position) {
        SpreadSheetInfo item = filteredItems.get(position);
        holders.put(viewHolder.hashCode(), viewHolder);
        if (position == getItemCount() - 1) {
            viewHolder.paddingView.setVisibility(View.VISIBLE);
        } else {
            viewHolder.paddingView.setVisibility(View.GONE);
        }
        viewHolder.name.setTypeface(StaticFonts.fonts[GlobalData.getSettings().fontSpreadsheetTitleIndex], Typeface.BOLD);
        viewHolder.getName().setText(item.name);
        viewHolder.getId().setText(item.spreadSheetId);
        viewHolder.mainView.setTag(position);
        setAnimation(viewHolder, position);
        if (item.type > 0) {
            viewHolder.type.setText(GlobalData.TYPES[item.type]);
        } else {
            viewHolder.type.setText("");
        }
    }

    private void setAnimation(RecyclerSpreadsheetsAdapter.CardHolder holder, int position) {
        if (position > lastPosition) {
            holder.animate();
            lastPosition = position;
        }
    }

    @Override
    public void onViewDetachedFromWindow(CardHolder holder) {
        holder.clearAnimation();
    }

    @Override
    public int getItemCount() {
        return filteredItems == null ? 0 : filteredItems.size();
    }

    public int getAllCount() {
        return items == null ? 0 : items.size();
    }
}
