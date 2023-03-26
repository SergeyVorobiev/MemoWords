package com.vsv.memorizer.adapters;

import android.annotation.SuppressLint;

import android.graphics.Typeface;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.TransitionDrawable;
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
import com.vsv.db.entities.Dictionary;
import com.vsv.db.entities.DictionaryWithSamples;
import com.vsv.db.entities.Sample;
import com.vsv.db.entities.Shelf;
import com.vsv.dialogs.SingleWindow;
import com.vsv.dialogs.UpdateShelfDialog;
import com.vsv.dialogs.WaitDialog;
import com.vsv.dialogs.entities.BackgroundTask;
import com.vsv.memorizer.R;
import com.vsv.memorizer.fragments.AddMoveCopyFragment;
import com.vsv.models.MainModel;
import com.vsv.repositories.DictionariesRepository;
import com.vsv.statics.GlobalData;
import com.vsv.statics.WeakContext;
import com.vsv.toasts.Toasts;
import com.vsv.utils.BadgeCollection;
import com.vsv.utils.Spec;
import com.vsv.utils.StaticFonts;
import com.vsv.utils.StaticUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class RecyclerAddAdapter extends AbstractAdapter<RecyclerAddAdapter.CardHolder> {

    private ArrayList<Shelf> items;

    private ArrayList<Shelf> filteredItems;

    private TreeMap<Long, Pair<Integer, Float>> countMap;

    private final View.OnClickListener clickCardListener;

    private final View.OnLongClickListener longClickCardListener;

    private String query;

    private final int badgeSize;

    private int type;

    private Dictionary dictionary;

    private DictionaryWithSamples dictionaryWithSamples;

    private ShelfBundle shelfBundle;

    private int animateItem = -1;

    private final String formatPercentage;

    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    public static class CardHolder extends RecyclerView.ViewHolder {

        private final View card;

        private final View mainView;

        private final TextView name;

        private final Animation animation;

        public final TextView percentage;

        public final TextView dictCount;

        private final View paddingView;

        public final ClipDrawable dictCountDrawable;

        public CardHolder(View view, int badgeSize) {
            super(view);
            name = view.findViewById(R.id.shelfName);
            card = view;
            mainView = view.findViewById(R.id.shelfItem);
            dictCount = view.findViewById(R.id.dictCount);
            dictCountDrawable = (ClipDrawable) ((LayerDrawable) dictCount.getBackground()).getDrawable(0);
            paddingView = view.findViewById(R.id.paddingView);
            percentage = view.findViewById(R.id.percentage);
            percentage.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, BadgeCollection.createRandomBadge(badgeSize), null);
            animation = AnimationUtils.loadAnimation(view.getContext(), R.anim.from_bottom);
            ImageView shelfIcon = view.findViewById(R.id.shelfIcon);
            shelfIcon.setImageDrawable(StaticUtils.getDrawable(R.drawable.ic_shelf_transition));
            TransitionDrawable tr = (TransitionDrawable) shelfIcon.getDrawable();
            LayerDrawable layerDrawable = (LayerDrawable) tr.getDrawable(0);
            layerDrawable.getDrawable(1).setTint(StaticUtils.getRandomInt(RecyclerShelvesAdapter.shelfColors));
        }

        public TextView getName() {
            return name;
        }

        public void animate() {
            card.startAnimation(animation);
        }

        public void clearAnimation() {
            card.clearAnimation();
        }
    }

    public RecyclerAddAdapter() {
        badgeSize = (int) WeakContext.getContext().getResources().getDimension(R.dimen.badge_size);
        formatPercentage = StaticUtils.getString(R.string.percentage);
        this.clickCardListener = this::onShelfClick;
        this.longClickCardListener = this::updateClick;
    }

    public boolean updateClick(View view) {
        int position = (int) view.getTag();
        UpdateShelfDialog dialog = new UpdateShelfDialog(this.getItem(position));
        dialog.updateShelfListener(StaticUtils.getModel().getShelvesRepository()::update);
        dialog.show();
        return true;
    }

    public void setData(@NonNull Dictionary dictionary, @NonNull DictionaryWithSamples dictionaryWithSamples,
                        @NonNull ShelfBundle shelfBundle, int type) {
        this.type = type;
        this.dictionary = dictionary;
        this.dictionaryWithSamples = dictionaryWithSamples;
        this.shelfBundle = shelfBundle;
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

    @SuppressLint("NotifyDataSetChanged")
    public void clearData() {
        this.type = -1;
        this.query = null;
        this.animateItem = -1;
        this.items = null;
        this.filteredItems = null;
        this.dictionary = null;
        this.dictionaryWithSamples = null;
        this.shelfBundle = null;
        notifyDataSetChanged();
    }

    private String getString(int id) {
        return WeakContext.getContext().getString(id);
    }

    public void onShelfClick(View view) {
        if (SingleWindow.isShownToast()) {
            return;
        }
        final MainModel model = StaticUtils.getModel();
        int position = (int) view.getTag();
        long moveShelfId = this.getItem(position).getId();
        if (type == AddMoveCopyFragment.MOVE) {
            Callable<Void> callable = () -> {
                DictionariesRepository repo = model.getDictionariesRepository();
                int count = repo.getAllDictionaries(moveShelfId).get(10, TimeUnit.SECONDS).size();
                if (count >= Spec.MAX_DICTIONARIES) {
                    Toasts.maxItemsMessage(getString(R.string.dictionaries_item_name));
                    return null;
                }
                Dictionary dict = repo.getDictionary(dictionary.getId()).get(5, TimeUnit.SECONDS);
                dict.setShelfId(moveShelfId);
                repo.update(dict).get(5, TimeUnit.SECONDS);
                return null;
            };
            BackgroundTask<Void> task = new BackgroundTask<>(30, callable);
            task.setRunMainThreadOnFail((e) -> Toasts.cannotMoveDictionary());
            task.setRunMainThreadOnSuccess((o) -> Toasts.success());
            WaitDialog waitDialog = new WaitDialog(WeakContext.getContext(), task);
            waitDialog.showOver();
        } else if (type == AddMoveCopyFragment.COPY) {
            Callable<Void> callable = () -> {
                int count = model.getDictionariesRepository().getAllDictionaries(moveShelfId).get(10, TimeUnit.SECONDS).size();
                if (count >= Spec.MAX_DICTIONARIES) {
                    Toasts.maxItemsMessage(getString(R.string.dictionaries_item_name));
                    return null;
                }

                // Make a dictionary copy.
                Dictionary dict = model.getDictionariesRepository().getDictionary(dictionary.getId()).get(5, TimeUnit.SECONDS);
                Dictionary newDictionary = dict.copy();
                newDictionary.timestampForTodayScore = -1;
                newDictionary.todayScore = 0;
                newDictionary.setShelfId(moveShelfId);

                // Take all samples and their copies.
                ArrayList<Sample> samples = model.getSamplesRepository().getSamples(dictionary.getId()).get(5, TimeUnit.SECONDS);
                if (samples == null) {
                    samples = new ArrayList<>();
                }
                ArrayList<Sample> copySamples = new ArrayList<>();
                for (Sample sample : samples) {
                    copySamples.add(sample.copy());
                }

                // Insert all.
                model.getDictionaryWithSamplesRepository().insertWithSamples(newDictionary, copySamples).get(15, TimeUnit.SECONDS);
                return null;
            };
            BackgroundTask<Void> task = new BackgroundTask<>(30, callable);
            task.setRunMainThreadOnFail((e) -> Toasts.cannotCopyDictionary());
            task.setRunMainThreadOnSuccess((o) -> Toasts.success());
            WaitDialog waitDialog = new WaitDialog(WeakContext.getContext(), task);
            waitDialog.showOver();
        } else if (type == AddMoveCopyFragment.ADD) {
            assert dictionaryWithSamples != null;
            Callable<Void> callable = () -> {
                try {
                    long count = model.getDictionariesRepository().countInShelf(moveShelfId).get(10, TimeUnit.SECONDS);
                    if (count >= Spec.MAX_DICTIONARIES) {
                        Toasts.maxItemsMessage(getString(R.string.dictionaries_item_name));
                        return null;
                    }
                    dictionaryWithSamples.dictionary.setShelfId(moveShelfId);
                    model.getDictionaryWithSamplesRepository().insertWithSamples(dictionaryWithSamples)
                            .get(15, TimeUnit.SECONDS);
                } catch (NullPointerException | ExecutionException | InterruptedException | TimeoutException e) {
                    Toasts.cannotAddDictionary();
                }
                return null;
            };
            BackgroundTask<Void> task = new BackgroundTask<>(25, callable);
            task.setRunMainThreadOnFail((e) -> Toasts.cannotAddDictionary());
            task.setRunMainThreadOnSuccess((o) -> Toasts.success());
            WaitDialog waitDialog = new WaitDialog(WeakContext.getContext(), task);
            waitDialog.showOver();
        } else {
            throw new RuntimeException("Wrong type.");
        }
        if (type == AddMoveCopyFragment.ADD) {
            StaticUtils.navigateSafe(R.id.action_MoveOrCopy_to_Shelves, null);
        } else {
            StaticUtils.navigateSafe(R.id.action_MoveOrCopy_to_Dictionaries, shelfBundle.toNewBundle());
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void applyFilter(String query) {
        this.query = query.toLowerCase();
        filter();
        notifyDataSetChanged();
    }

    public Shelf getItem(int position) {
        return filteredItems.get(position);
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
        String filterQuery = this.query;
        if (items == null) {
            filteredItems = null;
            return;
        } else {
            filteredItems = new ArrayList<>(items);
        }
        if (filterQuery != null && !filterQuery.isEmpty()) {
            filteredItems = (ArrayList<Shelf>) items.stream()
                    .filter((shelf) -> shelf.getName().toLowerCase()
                            .contains(filterQuery.toLowerCase())).collect(Collectors.toList());
        }
        if (GlobalData.getSettings().sortShelf) {
            sort();
        }
    }

    private void sort() {
        if (filteredItems != null && filteredItems.size() > 0) {
            Comparator<Shelf> comparator = Comparator.comparing(Shelf::getName);
            filteredItems.sort(comparator);
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
        return new CardHolder(v, badgeSize);
    }

    @Override
    public void onBindViewHolder(CardHolder viewHolder, final int position) {
        Shelf shelf = filteredItems.get(position);
        viewHolder.getName().setText(shelf.getName());
        viewHolder.mainView.setTag(position);
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

    @SuppressLint("NotifyDataSetChanged")
    @MainThread
    public void updateCountMap(TreeMap<Long, Pair<Integer, Float>> map) {
        countMap = map;
        notifyDataSetChanged();
    }

    @Override
    public void onViewDetachedFromWindow(RecyclerAddAdapter.CardHolder viewHolder) {
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
