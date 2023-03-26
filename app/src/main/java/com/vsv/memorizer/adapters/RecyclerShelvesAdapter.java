package com.vsv.memorizer.adapters;

import android.annotation.SuppressLint;

import android.graphics.Typeface;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.vsv.bundle.entities.ShelfBundle;
import com.vsv.db.entities.Settings;
import com.vsv.db.entities.Shelf;
import com.vsv.dialogs.SingleCustomDialog;
import com.vsv.dialogs.UpdateShelfDialog;
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

public class RecyclerShelvesAdapter extends AbstractAdapter<RecyclerShelvesAdapter.CardHolder> {

    private ArrayList<Shelf> items;

    private TreeMap<Long, Pair<Integer, Float>> countMap;

    private ArrayList<Shelf> filteredItems;

    public static final int[] shelfColors = new int[] {0xFFFFEFE5, 0xFFFFF2D1, 0xFFEDFFE0, 0xFFE2FFF9, 0xFFE6F0FF, 0xFFF0E6FF, 0xFFFFEAFA, 0xFFFFE8E8};

    private final View.OnClickListener clickCardListener;

    private final View.OnLongClickListener longClickCardListener;

    private int animateItem = -1;

    private final int badgeSize;

    private final String formatPercentage;

    private final View.OnClickListener shelfClickListener;

    private CountDownTimer timer;

    public final TreeMap<Integer, RecyclerShelvesAdapter.CardHolder> holders = new TreeMap<>();

    public static class CardHolder extends RecyclerView.ViewHolder {

        public boolean isReverse;

        private final View mainView;

        private final View parent;

        public final ImageView shelfIcon;

        private final TextView name;

        private final View paddingView;

        private final Animation animation;

        public final TextView percentage;

        public final TextView dictCount;

        public long transitionTime;

        public final ClipDrawable dictCountDrawable;

        public CardHolder(View view, int badgeSize, View.OnClickListener shelfClickListener) {
            super(view);
            parent = view;
            name = view.findViewById(R.id.shelfName);
            mainView = view.findViewById(R.id.shelfItem);
            animation = AnimationUtils.loadAnimation(view.getContext(), R.anim.from_bottom);
            percentage = view.findViewById(R.id.percentage);
            dictCount = view.findViewById(R.id.dictCount);
            dictCountDrawable = (ClipDrawable) ((LayerDrawable) dictCount.getBackground()).getDrawable(0);
            percentage.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, BadgeCollection.createRandomBadge(badgeSize), null);
            shelfIcon = view.findViewById(R.id.shelfIcon);
            paddingView = view.findViewById(R.id.paddingView);
            shelfIcon.setOnClickListener(shelfClickListener);
            shelfIcon.setImageDrawable(StaticUtils.getDrawable(R.drawable.ic_shelf_transition));
            TransitionDrawable tr = (TransitionDrawable) shelfIcon.getDrawable();
            LayerDrawable layerDrawable = (LayerDrawable) tr.getDrawable(0);
            layerDrawable.getDrawable(1).setTint(StaticUtils.getRandomInt(shelfColors));
        }

        public TextView getName() {
            return name;
        }

        public void animate() {
            parent.startAnimation(animation);
        }

        public void clearAnimation() {
            parent.clearAnimation();
        }

    }

    public RecyclerShelvesAdapter(@NonNull RecyclerView owner) {
        StopVerticalScrollAnimator.setRecycleViewAnimation(owner, Spec.MIN_SCROLL_HIT, R.anim.from_bottom2, R.anim.from_top);
        badgeSize = (int) WeakContext.getContext().getResources().getDimension(R.dimen.badge_size);
        formatPercentage = StaticUtils.getString(R.string.percentage);
        this.clickCardListener = this::onShelfClick;
        this.longClickCardListener = this::updateClick;
        this.shelfClickListener = this::updateFont;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateFont(@NonNull View view) {
        Settings settings = GlobalData.getSettings();
        settings.fontDictTitleIndex += 1;
        if (settings.fontDictTitleIndex >= StaticFonts.fonts.length) {
            settings.fontDictTitleIndex = 0;
        }
        StaticUtils.getModel().updateFontDictIndex(settings.id, settings.fontDictTitleIndex);
        notifyDataSetChanged();
    }

    private boolean updateClick(View view) {
        int position = (int) view.getTag();
        UpdateShelfDialog dialog = new UpdateShelfDialog(this.getItem(position));
        dialog.updateShelfListener(StaticUtils.getModel().getShelvesRepository()::update);
        dialog.show();
        return true;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clearData() {
        this.holders.clear();
        this.timer.cancel();
        this.animateItem = -1;
        this.items = null;
        this.filteredItems = null;
        this.notifyDataSetChanged();
    }

    public void setup() {
        this.timer = createTimer();
        this.timer.start();
    }

    private CountDownTimer createTimer() {
        return new CountDownTimer(Integer.MAX_VALUE, 1000) {

            private final Handler handler = new Handler(Looper.getMainLooper());

            @Override
            public void onTick(long millisUntilFinished) {
                int size = RecyclerShelvesAdapter.this.holders.values().size();
                if (size == 0) {
                    return;
                }
                int picked = StaticUtils.random.nextInt(size);
                handler.post(() -> {
                    int i = 0;
                    int duration = 2000;
                    for (RecyclerShelvesAdapter.CardHolder holder: RecyclerShelvesAdapter.this.holders.values()) {
                        int elapsedTime = (int) Timer.nanoTimeDiffFromNowInMilliseconds(holder.transitionTime);
                        TransitionDrawable drawable = (TransitionDrawable) holder.shelfIcon.getDrawable();
                        if (i == picked) {
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

    private void onShelfClick(View view) {
        if (SingleCustomDialog.isShownToast()) {
            return;
        }
        int position = (int) view.getTag();
        StaticUtils.navigateSafe(R.id.action_Shelves_to_Dictionaries, ShelfBundle.toNewBundle(this.getItem(position)));
    }

    @SuppressLint("NotifyDataSetChanged")
    public void applyFilter(String query) {
        GlobalData.shelfSearchQuery = query.toLowerCase();
        filter();
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    @MainThread
    public void updateCountMap(TreeMap<Long, Pair<Integer, Float>> map) {
        countMap = map;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    @MainThread
    public void updateInfoForShelf(long shelfId, int count, float percentage) {
        if (countMap == null) {
            countMap = new TreeMap<>();
        }
        countMap.put(shelfId, new Pair<>(count, percentage));
        notifyDataSetChanged();
    }

    @Nullable
    private Pair<Integer, Float> getDictionariesData(long shelfId) {
        if (countMap != null) {
            return countMap.getOrDefault(shelfId, null);
        }
        return null;
    }

    public Shelf getItem(int position) {
        return filteredItems.get(position);
    }

    // Not a deep copy, only new array.
    public @Nullable
    ArrayList<Shelf> getCopyItems() {
        if (items == null || items.isEmpty()) {
            return null;
        }
        return new ArrayList<>(items);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setItems(ArrayList<Shelf> items) {
        this.items = items;
        filter();
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

    private void sort() {
        if (GlobalData.getSettings().sortShelf && filteredItems != null && filteredItems.size() > 0) {
            Comparator<Shelf> comparator = Comparator.comparing(Shelf::getName);
            filteredItems.sort(comparator);
        }
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
        String filterQuery = GlobalData.shelfSearchQuery;
        if (items != null && filterQuery != null && !filterQuery.isEmpty()) {
            filteredItems = (ArrayList<Shelf>) items.stream()
                    .filter((item) -> item.getName().toLowerCase()
                            .startsWith(filterQuery.toLowerCase())).collect(Collectors.toList());
        }
    }

    private void filterContains() {
        String filterQuery = GlobalData.shelfSearchQuery;
        if (items != null && filterQuery != null && !filterQuery.isEmpty()) {
            filteredItems = (ArrayList<Shelf>) items.stream()
                    .filter((item) -> item.getName().toLowerCase()
                            .contains(filterQuery.toLowerCase())).collect(Collectors.toList());
        }
    }

    @NonNull
    @Override
    public CardHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shelf, parent, false);
        View shelfItem = v.findViewById(R.id.shelfItem);
        shelfItem.setOnClickListener(clickCardListener);
        shelfItem.setOnLongClickListener(longClickCardListener);
        return new CardHolder(v, badgeSize, shelfClickListener);
    }

    @Override
    public void onBindViewHolder(CardHolder viewHolder, final int position) {
        Shelf shelf = filteredItems.get(position);
        viewHolder.getName().setText(shelf.getName());
        viewHolder.mainView.setTag(position);
        viewHolder.transitionTime = System.nanoTime();
        holders.put(viewHolder.hashCode(), viewHolder);
        viewHolder.name.setTypeface(StaticFonts.fonts[GlobalData.getSettings().fontDictTitleIndex], Typeface.BOLD);
        Pair<Integer, Float> data = getDictionariesData(shelf.getId());
        if (data != null) {
            viewHolder.dictCount.setText(String.valueOf(data.first));
            viewHolder.percentage.setText(String.format(formatPercentage, (int) data.second.floatValue()));
            float perc = data.first / (float) Spec.MAX_DICTIONARIES;
            int level = (int) (perc * 10000);
            viewHolder.dictCountDrawable.setLevel(level);
        } else {
            viewHolder.dictCount.setText("0");
            viewHolder.percentage.setText("0%");
            viewHolder.dictCountDrawable.setLevel(0);
        }
        if (animateItem < position) {
            animateItem++;
            viewHolder.animate();
        }
        if (position == getItemCount() - 1) {
            viewHolder.paddingView.setVisibility(View.VISIBLE);
        } else {
            viewHolder.paddingView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onViewDetachedFromWindow(CardHolder viewHolder) {
        viewHolder.clearAnimation();
    }

    @Override
    public int getItemCount() {
        return filteredItems == null ? 0 : filteredItems.size();
    }

    public int getAllCount() {
        return items == null ? 0 : items.size();
    }
}
