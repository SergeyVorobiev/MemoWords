package com.vsv.memorizer.adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.Typeface;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
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
import androidx.recyclerview.widget.RecyclerView;

import com.vsv.bundle.entities.ShelfBundle;
import com.vsv.db.entities.Dictionary;
import com.vsv.db.entities.Sample;
import com.vsv.dialogs.entities.BackgroundTask;
import com.vsv.memorizer.R;
import com.vsv.repositories.DictionariesRepository;
import com.vsv.repositories.SamplesRepository;
import com.vsv.toasts.Toasts;
import com.vsv.utils.Spec;
import com.vsv.utils.StaticFonts;
import com.vsv.utils.StaticUtils;
import com.vsv.utils.Symbols;
import com.vsv.utils.merger.MergeCollection;
import com.vsv.utils.merger.SampleDictMerger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class RecyclerDictionaryMergeAdapter extends AbstractAdapter<RecyclerDictionaryMergeAdapter.ViewHolder> {

    private ArrayList<Dictionary> items;

    private ArrayList<Dictionary> filteredItems;

    private final View.OnClickListener clickCardListener;

    private ShelfBundle shelfBundle;

    private String filterQuery;

    private int animateItem = -1;

    private final long mergeDictId;

    private final String percentageFormat;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private static final Drawable updateDrawable = StaticUtils.getDrawable(R.drawable.bg_question);

        private static final String updateString = StaticUtils.getString(R.string.update);

        private final View card;

        private final TextView dictName;

        private final TextView dictLanguage;

        public final ImageView authorButton;

        private final TextView spreadsheetNameView;

        public final TextView percentage;

        public final ClipDrawable rateView;

        public final ImageView rateImage;

        public final ClipDrawable rateTrainView;

        public final ClipDrawable rateRememberView;

        private final Animation animation;

        private final TextView update;

        private final TextView spreadsheetIdView;

        private final TextView sheetNameView;

        private final TextView rememberedCountView;

        private final TextView dictCounterView;

        private final TextView trainedCountView;

        public ViewHolder(View view, View.OnClickListener onClickListener) {
            super(view);
            card = view;
            card.setOnClickListener(onClickListener);
            dictName = card.findViewById(R.id.dictName);
            dictLanguage = card.findViewById(R.id.dictLanguage);
            percentage = card.findViewById(R.id.percentage);
            spreadsheetIdView = card.findViewById(R.id.spreadsheetId);
            sheetNameView = card.findViewById(R.id.sheetName);
            rememberedCountView = card.findViewById(R.id.rememberedCounter);
            spreadsheetNameView = card.findViewById(R.id.spreadsheetName);
            trainedCountView = card.findViewById(R.id.trainedCounter);
            dictCounterView = card.findViewById(R.id.dictCounter);
            authorButton = card.findViewById(R.id.btnAuthor);
            update = card.findViewById(R.id.update);
            card.findViewById(R.id.graph).setVisibility(View.INVISIBLE); // To keep layout order the same
            card.findViewById(R.id.btnAuthor).setVisibility(View.GONE);
            card.findViewById(R.id.update).setVisibility(View.GONE);
            animation = AnimationUtils.loadAnimation(view.getContext(), R.anim.from_bottom);
            rateImage = card.findViewById(R.id.rateImage);
            rateView = (ClipDrawable) ((LayerDrawable) rateImage.getDrawable()).getDrawable(1);
            rateTrainView = (ClipDrawable) ((LayerDrawable) trainedCountView.getBackground()).getDrawable(0);
            rateRememberView = (ClipDrawable) ((LayerDrawable) rememberedCountView.getBackground()).getDrawable(0);
            trainedCountView.setCompoundDrawablesRelativeWithIntrinsicBounds(StaticUtils.getDrawable(R.drawable.ic_ab), null, null, null);
            ImageView imageView = view.findViewById(R.id.dictIcon);
            ((LayerDrawable) imageView.getDrawable()).getDrawable(1).setTint(StaticUtils.getRandomInt(RecyclerDictionariesAdapter.dictColors));
        }

        public final void setLayoutData(@NonNull Dictionary dictionary) {
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
            authorButton.setVisibility(View.GONE);
            authorButton.setImageDrawable(null);
            trainedCountView.setText(StaticUtils.getString(R.string.counter, dictionary.getCount(), Spec.MAX_SAMPLES));
            rememberedCountView.setText(String.valueOf(dictionary.getRememberedCount()));
            dictCounterView.setText(String.valueOf(dictionary.getCount()));
            update.setText("");
            update.setBackground(null);
            int maxLevel = 10000;
            float all = dictionary.getCount();
            float percAll = all / Spec.MAX_SAMPLES;
            int allLevel = (int) (maxLevel * percAll);
            int rememberLevel = 0;
            if (all > 0) {
                float perc = dictionary.getRememberedCount() / all;
                rememberLevel = (int) (maxLevel * perc);
            }
            rateTrainView.setLevel(allLevel);
            rateRememberView.setLevel(rememberLevel);
        }

        public void animate() {
            card.startAnimation(animation);
        }

        public void clearAnimation() {
            card.clearAnimation();
        }

        public View getCard() {
            return card;
        }

        public TextView getDictName() {
            return dictName;
        }

        public TextView getDictLanguage() {
            return dictLanguage;
        }
    }

    public RecyclerDictionaryMergeAdapter(ShelfBundle shelfBundle, long mergeDictId) {
        this.percentageFormat = StaticUtils.getString(R.string.all_percentage);
        this.mergeDictId = mergeDictId;
        this.shelfBundle = shelfBundle;
        this.clickCardListener = this::onDictionaryClick;
    }

    public void onDictionaryClick(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        Dictionary dictionary = getItem((int) view.getTag());
        builder.setMessage(StaticUtils.getString(R.string.merge_dict_header, dictionary.getName()));
        builder.setPositiveButton(StaticUtils.getString(R.string.general_dialog_button_merge), (dialog, which) -> mergeDict(dictionary.getId()))
                .setNegativeButton(StaticUtils.getString(R.string.general_dialog_button_cancel2), (dialog, which) -> {
                })
                .show();
    }

    private void mergeDict(long toMergeDictId) {
        DictionariesRepository dictRepo = StaticUtils.getModel().getDictionariesRepository();
        SamplesRepository samplesRepository = StaticUtils.getModel().getSamplesRepository();
        final int success = 0;
        final int nothingToAdd = 1;
        final int notAllHasBeenAdded = 2;
        BackgroundTask<Integer> task = new BackgroundTask<>(30, () -> {
            int returnResult = success;
            ArrayList<Sample> samplesToMerge = samplesRepository.getSamples(mergeDictId).get(15, TimeUnit.SECONDS);
            ArrayList<Sample> samples = samplesRepository.getSamples(toMergeDictId).get(15, TimeUnit.SECONDS);
            if (samplesToMerge == null || samplesToMerge.isEmpty()) {
                return nothingToAdd;
            }
            if (samples == null) {
                samples = new ArrayList<>();
            }
            SampleDictMerger dictMerger = new SampleDictMerger(samplesToMerge, samples, toMergeDictId);
            MergeCollection<Sample> mergeCollection = dictMerger.merge();
            Dictionary dictionary = dictRepo.getDictionary(toMergeDictId).get(10, TimeUnit.SECONDS);
            boolean needDictUpdate = false;
            if (mergeCollection.equal.size() > 0) {
                samplesRepository.updateSeveralWaiting(mergeCollection.equal, 10);
                samples = samplesRepository.getSamples(toMergeDictId).get(15, TimeUnit.SECONDS);
                dictionary.setPassedPercentage(Dictionary.calculatePercentage(samples));
                dictionary.setRememberedCount((int) samples.stream().filter(Sample::isRemembered).count());
                needDictUpdate = true;
            }
            ArrayList<Sample> toAdd = mergeCollection.uniqueLefts;
            int size = toAdd.size();
            if (size > 0) {
                if (samples.size() >= Spec.MAX_SAMPLES) {
                    return notAllHasBeenAdded;
                }
                int maxAdd = Spec.MAX_SAMPLES - samples.size();
                if (maxAdd < size) {
                    returnResult = notAllHasBeenAdded;
                }
                toAdd = (ArrayList<Sample>) toAdd.stream().limit(maxAdd).collect(Collectors.toList());
                samples.addAll(toAdd);
                dictionary.setCount(samples.size());
                dictionary.setPassedPercentage(Dictionary.calculatePercentage(samples));
                dictionary.setRememberedCount((int) samples.stream().filter(Sample::isRemembered).count());
                samplesRepository.insertSeveralWaiting(toAdd, 10);
                needDictUpdate = true;
            }
            if (needDictUpdate) {
                dictRepo.update(dictionary);
            }
            return returnResult;
        });
        task.setRunMainThreadOnFail(exception -> {
            Log.e("MergeDictError", exception.toString());
            Toasts.cannotMergeDictionary();
        });
        task.setRunMainThreadOnSuccess(res -> {
            assert res != null;
            if (nothingToAdd == res) {
                Toasts.nothingToAdd();
            } else if (success == res) {
                Toasts.success();
            } else {
                Toasts.notAllSamplesAdded();
            }
        });
        task.buildWaitDialog().showOver();
        StaticUtils.navigateSafe(R.id.action_DictionaryMerge_to_Dictionaries, shelfBundle.toNewBundle());
    }

    public void clearData() {
        this.filterQuery = null;
        this.shelfBundle = null;
        this.animateItem = -1;
        this.items = null;
        this.filteredItems = null;
    }

    @SuppressLint("NotifyDataSetChanged")
    @MainThread
    public int applyFilter(String query) {
        filterQuery = query.toLowerCase();
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
        if (filterQuery != null && !filterQuery.isEmpty()) {
            filteredItems = (ArrayList<Dictionary>) items.stream()
                    .filter((dictionary) -> dictionary.getName().toLowerCase()
                            .contains(filterQuery.toLowerCase())).collect(Collectors.toList());
        }
        if (!filteredItems.isEmpty()) {
            filteredItems = (ArrayList<Dictionary>) filteredItems.stream()
                    .filter((dictionary) -> dictionary.getId() != mergeDictId).collect(Collectors.toList());
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
        return new ViewHolder(v, clickCardListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        Dictionary dictionary = filteredItems.get(position);
        viewHolder.getDictName().setText(dictionary.getName());
        String leftLanguage = dictionary.getLeftLocaleAbb();
        String rightLanguage = dictionary.getRightLocaleAbb();
        String language = leftLanguage + " - " + rightLanguage;
        viewHolder.setLayoutData(dictionary);
        viewHolder.getDictLanguage().setText(language);
        viewHolder.getCard().setTag(position);
        viewHolder.percentage.setText(String.format(percentageFormat, dictionary.getPassedPercentage()));
        viewHolder.dictName.setTypeface(StaticFonts.fonts[shelfBundle.fontDictTitleIndex], Typeface.BOLD);
        viewHolder.rateView.setLevel((int) (dictionary.getPassedPercentage() * 100));
        if (animateItem < position) {
            animateItem++;
            viewHolder.animate();
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