package com.vsv.memorizer.adapters;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vsv.bundle.entities.DictionaryBundle;
import com.vsv.bundle.entities.ShelfBundle;
import com.vsv.bundle.helpers.BundleNames;
import com.vsv.db.entities.Dictionary;
import com.vsv.db.entities.Sample;
import com.vsv.dialogs.AuthorPageDialog;
import com.vsv.dialogs.DictionaryGraphPopup;
import com.vsv.dialogs.SingleWindow;
import com.vsv.dialogs.UpdateDictionaryDialog;
import com.vsv.memorizer.R;
import com.vsv.statics.GlobalData;
import com.vsv.statics.WeakContext;
import com.vsv.toasts.Toasts;
import com.vsv.utils.AppLink;
import com.vsv.utils.LinkBuilder;
import com.vsv.utils.Spec;
import com.vsv.utils.StaticFonts;
import com.vsv.utils.StaticUtils;
import com.vsv.utils.Symbols;
import com.vsv.utils.Timer;
import com.vsv.viewutils.StopVerticalScrollAnimator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class RecyclerDictionariesAdapter extends AbstractAdapter<RecyclerDictionariesAdapter.ViewHolder> {

    private ArrayList<Dictionary> items;

    private ArrayList<Dictionary> filteredItems;

    private TreeMap<Long, Integer> trainedCountMap;

    public static final int[] dictColors = new int[] {0xFFFFEFE5, 0xFFFFF2D1, 0xFFEDFFE0, 0xFFE2FFF9, 0xFFE6F0FF, 0xFFF0E6FF, 0xFFFFEAFA, 0xFFFFE8E8};

    private final View.OnClickListener clickCardListener;

    private final View.OnClickListener changeTitleFontListener;

    private final View.OnClickListener onGraphListener;

    private final View.OnClickListener onAuthorListener;

    private final View.OnLongClickListener longClickCardListener;

    private ShelfBundle shelfBundle;

    private final RecyclerView owner;

    private int animateItem = -1;

    private final String percentageFormat;

    private final Animation waveAnim;

    private final Object trainKey = new Object();

    private Thread thread;

    private CountDownTimer timer;

    private final AtomicBoolean updateFlag;

    public final TreeMap<Integer, RecyclerDictionariesAdapter.ViewHolder> holders = new TreeMap<>();

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private static final Drawable updateDrawable = StaticUtils.getDrawable(R.drawable.bg_question);

        private static final String updateString = StaticUtils.getString(R.string.update);

        private final View card;

        private final View mainView;

        private final TextView dictName;

        private final TextView dictLanguage;

        public final TextView percentage;

        public final ClipDrawable rateView;

        public final ImageView rateImage;

        public final ImageView graphButton;

        public final ImageView authorButton;

        public final ClipDrawable rateTrainView;

        public final ClipDrawable rateRememberView;

        private final Animation animation;

        private final TextView update;

        private final TextView spreadsheetIdView;

        private final TextView sheetNameView;

        private final TextView spreadsheetNameView;

        private final TextView rememberedCountView;

        private final TextView trainedCountView;

        private final TextView dictCounterView;

        private final View paddingView;

        public boolean isReverse;

        private long transitionTime;

        private final TransitionDrawable iconGraphTransition;

        public ViewHolder(View view, View.OnClickListener onClickListener,
                          View.OnLongClickListener onLongClickListener,
                          View.OnClickListener onDictChangeFontListener,
                          View.OnClickListener onGraphListener, View.OnClickListener authorButtonListener) {
            super(view);
            card = view;
            mainView = view.findViewById(R.id.dict);
            mainView.setBackground(StaticUtils.getDrawable(R.drawable.bg_item_dictionary_transition));
            iconGraphTransition = (TransitionDrawable) ((ImageView) view.findViewById(R.id.graph)).getDrawable();
            mainView.setOnClickListener(onClickListener);
            mainView.setOnLongClickListener(onLongClickListener);
            dictName = card.findViewById(R.id.dictName);
            dictLanguage = card.findViewById(R.id.dictLanguage);
            percentage = card.findViewById(R.id.percentage);
            spreadsheetIdView = card.findViewById(R.id.spreadsheetId);
            sheetNameView = card.findViewById(R.id.sheetName);
            spreadsheetNameView = card.findViewById(R.id.spreadsheetName);
            update = card.findViewById(R.id.update);
            update.setOnClickListener(onLongClickListener::onLongClick);
            rememberedCountView = card.findViewById(R.id.rememberedCounter);
            dictCounterView = card.findViewById(R.id.dictCounter);
            trainedCountView = card.findViewById(R.id.trainedCounter);
            animation = AnimationUtils.loadAnimation(view.getContext(), R.anim.from_bottom);
            rateImage = card.findViewById(R.id.rateImage);
            graphButton = card.findViewById(R.id.graph);
            authorButton = card.findViewById(R.id.btnAuthor);
            authorButton.setOnClickListener(authorButtonListener);
            graphButton.setOnClickListener(onGraphListener);
            rateView = (ClipDrawable) ((LayerDrawable) rateImage.getDrawable()).getDrawable(1);
            rateTrainView = (ClipDrawable) ((LayerDrawable) trainedCountView.getBackground()).getDrawable(0);
            rateRememberView = (ClipDrawable) ((LayerDrawable) rememberedCountView.getBackground()).getDrawable(0);
            paddingView = card.findViewById(R.id.paddingView);
            ImageView imageView = view.findViewById(R.id.dictIcon);
            imageView.setOnClickListener(onDictChangeFontListener);
            ((LayerDrawable) imageView.getDrawable()).getDrawable(1).setTint(StaticUtils.getRandomInt(dictColors));
        }

        @SuppressLint("SetTextI18n")
        private void setDeveloperInfo(@NonNull Dictionary dictionary) {
            card.findViewById(R.id.vis).setVisibility(View.VISIBLE);
            ((TextView) card.findViewById(R.id.textView5)).setText("Updated date: " + (dictionary.dataDate == null ? "-1" : dictionary.dataDate.toString()));
            ((TextView) card.findViewById(R.id.textView6)).setText("Checked date: " + (dictionary.successfulUpdateCheck == null ? "-1" : dictionary.successfulUpdateCheck.toString()));
            ((TextView) card.findViewById(R.id.textView7)).setText("Need update: " + dictionary.needUpdate);
            ((TextView) card.findViewById(R.id.textView8)).setText("Sheet id: " + dictionary.sheetId);
        }

        public void setLayoutData(@NonNull Dictionary dictionary, int trainedCount, int position) {
            //setDeveloperInfo(dictionary);
            if (dictionary.hasOwner()) {
                String string = StaticUtils.getString(R.string.spreadsheet_id_format, Symbols.ID, dictionary.spreadsheetId);
                spreadsheetIdView.setText(string);
                sheetNameView.setText(dictionary.sheetName);
                spreadsheetNameView.setText(dictionary.spreadsheetName);
            } else {
                spreadsheetIdView.setText(null);
                sheetNameView.setText(null);
                spreadsheetNameView.setText(null);
            }

            if (trainedCount == 0) {
                trainedCount = dictionary.getRememberedCount();
            }
            trainedCountView.setText(StaticUtils.getString(R.string.counter, trainedCount, dictionary.getCount()));
            dictCounterView.setText(String.valueOf(dictionary.getCount()));
            rememberedCountView.setText(String.valueOf(dictionary.getRememberedCount()));
            if (dictionary.needUpdate) {
                update.setVisibility(View.VISIBLE);
                update.setTag(position);
            } else {
                update.setVisibility(View.GONE);
            }
            float all = dictionary.getCount();
            int trainLevel = 0;
            int rememberLevel = 0;
            int maxLevel = 10000;
            if (all > 0) {
                float perc = dictionary.getRememberedCount() / all;
                rememberLevel = (int) (maxLevel * perc);
                perc = trainedCount / all;
                trainLevel = (int) (maxLevel * perc);
            }
            rateRememberView.setLevel(rememberLevel);
            rateTrainView.setLevel(trainLevel);
        }

        public void animate() {
            card.startAnimation(animation);
        }

        public void clearAnimation() {
            card.clearAnimation();
        }

        public TextView getDictName() {
            return dictName;
        }

        public TextView getDictLanguage() {
            return dictLanguage;
        }
    }

    // Not deep copy, only new collection, the items will be the same.
    @MainThread
    public @Nullable
    ArrayList<Dictionary> getCopyItems() {
        if (items == null) {
            return null;
        }
        return new ArrayList<>(items);
    }

    public RecyclerDictionariesAdapter(@NonNull RecyclerView owner) {
        StopVerticalScrollAnimator.setRecycleViewAnimation(owner, Spec.MIN_SCROLL_HIT, R.anim.from_bottom2, R.anim.from_top);
        this.percentageFormat = StaticUtils.getString(R.string.all_percentage);
        this.owner = owner;
        this.updateFlag = new AtomicBoolean(true);
        this.clickCardListener = this::onDictionaryClick;
        this.longClickCardListener = this::updateClick;
        this.changeTitleFontListener = this::changeTitleFont;
        this.onAuthorListener = this::onAuthorClick;
        this.onGraphListener = this::onGraphListener;
        this.waveAnim = AnimationUtils.loadAnimation(WeakContext.getContext(), R.anim.wave);
    }

    public void disableItemsAnimation() {
        this.animateItem = Integer.MAX_VALUE;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void changeTitleFont(@NonNull View view) {
        shelfBundle.fontDictTitleIndex += 1;
        if (shelfBundle.fontDictTitleIndex >= StaticFonts.fonts.length) {
            shelfBundle.fontDictTitleIndex = 0;
        }
        StaticUtils.getModel().getShelvesRepository().updateFontDictTitleIndex(shelfBundle.id, shelfBundle.fontDictTitleIndex);
        notifyDataSetChanged();
    }

    private void onGraphListener(@NonNull View view) {
        new DictionaryGraphPopup(this.getItem((int) view.getTag())).show(view);
    }

    private void onAuthorClick(@NonNull View view) {
        new AuthorPageDialog((AppLink) view.getTag()).show();
    }

    private boolean updateClick(View view) {
        int position = (int) view.getTag();
        if (SingleWindow.isShownToast()) {
            return true;
        }
        Dictionary dictionary = this.getItem(position);
        ArrayList<Sample> samples;
        try {
            samples = StaticUtils.getModel().getSamplesRepository().getSamples(dictionary.getId()).get(15, TimeUnit.SECONDS);
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            Log.e("Read from dictionary", e.toString());
            Toasts.readFromDictionaryError(dictionary.getName());
            return true;
        }
        UpdateDictionaryDialog dialog = new UpdateDictionaryDialog(dictionary, samples == null ? new ArrayList<>() : samples);
        dialog.updateDictionaryListener(StaticUtils.getModel().getDictionariesRepository()::update);
        dialog.show();
        return true;
    }

    public void onDictionaryClick(View view) {
        if (SingleWindow.isShownToast()) {
            return;
        }
        int position = (int) view.getTag();
        Bundle bundle = shelfBundle.toNewBundle();
        DictionaryBundle.toBundle(bundle, this.getItem(position));
        LinearLayoutManager manager = (LinearLayoutManager) owner.getLayoutManager();
        if (manager != null) {
            bundle.putInt(BundleNames.SCROLL_POSITION, manager.findFirstCompletelyVisibleItemPosition());
        }
        StaticUtils.navigateSafe(R.id.action_Dictionaries_to_Samples, bundle);
    }

    public void setData(ShelfBundle shelfBundle) {
        this.shelfBundle = shelfBundle;
        this.timer = createTimer();
        this.timer.start();
        thread = new Thread(() -> {
            while (!thread.isInterrupted() && thread.isAlive()) {
                try {
                    // noinspection BusyWait
                    Thread.sleep(10000);
                    int count = owner.getChildCount();
                    if (count <= 0) {
                        continue;
                    }
                    int i = StaticUtils.random.nextInt(count);
                    ViewHolder holder = (ViewHolder) owner.getChildViewHolder(owner.getChildAt(i));
                    if (holder != null) {
                        holder.rateImage.startAnimation(waveAnim);
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clearData() {
        this.updateFlag.set(false);
        this.holders.clear();
        this.timer.cancel();
        this.shelfBundle = null;
        this.trainedCountMap = null;
        this.animateItem = -1;
        this.items = null;
        this.filteredItems = null;
        if (thread != null) {
            this.thread.interrupt();
            this.thread = null;
        }
        this.notifyDataSetChanged();
    }

    private CountDownTimer createTimer() {
        return new CountDownTimer(Integer.MAX_VALUE, 1000) {

            private final Handler handler = new Handler(Looper.getMainLooper());

            @Override
            public void onTick(long millisUntilFinished) {
                int size = RecyclerDictionariesAdapter.this.holders.values().size();
                if (size == 0) {
                    return;
                }
                int picked = StaticUtils.random.nextInt(size);
                handler.post(() -> {
                    int i = 0;
                    int duration = 3000;
                    for (RecyclerDictionariesAdapter.ViewHolder holder : RecyclerDictionariesAdapter.this.holders.values()) {
                        int elapsedTime = (int) Timer.nanoTimeDiffFromNowInMilliseconds(holder.transitionTime);
                        TransitionDrawable drawable = (TransitionDrawable) holder.mainView.getBackground();
                        TransitionDrawable iconDrawable = holder.iconGraphTransition;
                        if (i == picked) {
                            if (holder.isReverse && elapsedTime >= duration) {
                                holder.transitionTime = System.nanoTime();
                                drawable.reverseTransition(duration);
                                iconDrawable.reverseTransition(duration);
                                holder.isReverse = false;
                            } else if (!holder.isReverse && elapsedTime >= duration) {
                                holder.transitionTime = System.nanoTime();
                                drawable.startTransition(duration);
                                iconDrawable.startTransition(duration);
                                holder.isReverse = true;
                            }
                        }
                        else {
                            if (holder.isReverse && elapsedTime >= duration) {
                                holder.transitionTime = System.nanoTime();
                                drawable.reverseTransition(duration);
                                iconDrawable.reverseTransition(duration);
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

    @SuppressLint("NotifyDataSetChanged")
    @MainThread
    public int applyFilter(String query) {
        GlobalData.dictSearchQuery.put(shelfBundle.id, query.toLowerCase());
        filter();
        int size = getItemCount();
        notifyDataSetChanged();
        return size;
    }

    @MainThread
    public Dictionary getItem(int position) {
        return filteredItems.get(position);
    }

    @SuppressLint("NotifyDataSetChanged")
    public int setItems(ArrayList<Dictionary> dictionaries) {
        this.items = dictionaries;
        filter();
        notifyDataSetChanged();
        return getItemCount();
    }

    public void setTrainCountMap(TreeMap<Long, Integer> map) {
        synchronized (trainKey) {
            this.trainedCountMap = map;
        }
    }

    private int getTrainCountOrZero(long id) {
        synchronized (trainKey) {
            if (trainedCountMap == null) {
                return 0;
            }
            Integer value = this.trainedCountMap.getOrDefault(id, 0);
            return value == null ? 0 : value;
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public int update() {
        int size = filter();
        notifyDataSetChanged();
        return size;
    }

    private void sort() {
        if (shelfBundle.sorted && filteredItems != null && filteredItems.size() > 0) {
            Comparator<Dictionary> comparator = Comparator.comparing(Dictionary::getName);
            filteredItems.sort(comparator);
        }
    }

    private int filter() {
        if (items == null) {
            filteredItems = null;
            return 0;
        } else {
            filteredItems = new ArrayList<>(items);
        }
        String filterQuery = GlobalData.dictSearchQuery.getOrDefault(shelfBundle.id, null);
        if (filterQuery != null && !filterQuery.isEmpty()) {
            filteredItems = (ArrayList<Dictionary>) items.stream()
                    .filter((dictionary) -> dictionary.getName().toLowerCase()
                            .contains(filterQuery.toLowerCase())).collect(Collectors.toList());
        }
        if (shelfBundle.hideRemembered && !filteredItems.isEmpty()) {
            filteredItems = (ArrayList<Dictionary>) filteredItems.stream()
                    .filter((dictionary) -> dictionary.getPassedPercentage() != 100).collect(Collectors.toList());
        }
        sort();
        return filteredItems.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return create(parent);
    }

    private ViewHolder create(ViewGroup parent) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dictionary, parent, false);
        return new ViewHolder(v, clickCardListener, longClickCardListener, changeTitleFontListener,
                onGraphListener, onAuthorListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        Dictionary dictionary = filteredItems.get(position);
        ((TransitionDrawable) viewHolder.mainView.getBackground()).resetTransition();
        viewHolder.isReverse = false;
        viewHolder.transitionTime = System.nanoTime();
        holders.put(viewHolder.hashCode(), viewHolder);
        viewHolder.getDictName().setText(dictionary.getName());
        String leftLanguage = dictionary.getLeftLocaleAbb();
        String rightLanguage = dictionary.getRightLocaleAbb();
        String language = leftLanguage + " - " + rightLanguage;
        viewHolder.setLayoutData(dictionary, this.getTrainCountOrZero(dictionary.getId()), position);
        viewHolder.getDictLanguage().setText(language);
        viewHolder.mainView.setTag(position);
        viewHolder.graphButton.setTag(position);
        AppLink appLink = LinkBuilder.buildLinkFromString(dictionary.author);
        viewHolder.authorButton.setTag(appLink);
        if (appLink == null) {
            viewHolder.authorButton.setVisibility(View.GONE);
            viewHolder.authorButton.setImageDrawable(null);
        } else {
            viewHolder.authorButton.setVisibility(View.VISIBLE);
            viewHolder.authorButton.setImageDrawable(appLink.icon);
        }
        viewHolder.percentage.setText(String.format(percentageFormat, dictionary.getPassedPercentage()));
        viewHolder.dictName.setTypeface(StaticFonts.fonts[shelfBundle.fontDictTitleIndex], Typeface.BOLD);
        viewHolder.rateView.setLevel((int) (dictionary.getPassedPercentage() * 100));
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
    public void onViewDetachedFromWindow(ViewHolder viewHolder) {
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