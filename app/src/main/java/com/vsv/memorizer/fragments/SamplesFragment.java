package com.vsv.memorizer.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.vsv.bundle.entities.DictionaryBundle;
import com.vsv.bundle.entities.ShelfBundle;
import com.vsv.db.entities.Dictionary;
import com.vsv.db.entities.Sample;
import com.vsv.db.entities.Shelf;
import com.vsv.dialogs.ClearSamplesDialog;
import com.vsv.dialogs.FindWordShelvesDialog;
import com.vsv.dialogs.NewSampleDialog;
import com.vsv.dialogs.SortByDialog;
import com.vsv.dialogs.StartTrainDialog;
import com.vsv.memorizer.R;
import com.vsv.memorizer.adapters.RecyclerSamplesAdapter;
import com.vsv.models.MainModel;
import com.vsv.overflowmenu.OverflowMenu;
import com.vsv.recyclerentities.SamplesRecycleList;
import com.vsv.speech.RoboVoice;
import com.vsv.bundle.helpers.BundleNames;
import com.vsv.speech.SupportedLanguages;
import com.vsv.statics.CacheData;
import com.vsv.statics.GlobalData;
import com.vsv.statics.WeakContext;
import com.vsv.toasts.Toasts;
import com.vsv.utils.Spec;
import com.vsv.utils.StaticUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SamplesFragment extends Fragment {

    private final MainModel model;

    private RecyclerSamplesAdapter adapter;

    public static final int MIN_TRAIN_SAMPLES = 4;

    private TextView sampleCounter;

    private FloatingActionButton reverseButton;

    private TextView allPercentageView;

    private int dictScrollPosition = -1;

    private ShelfBundle shelfBundle;

    private Drawable swapEnable;

    private Drawable swapDisable;

    private String formatCounter;

    private DictionaryBundle dictionaryBundle;

    private boolean dialogShown = false;

    private SamplesRecycleList list;

    private Animation reverseAnimation;

    private final CountDownTimer timer;

    private String allPercentageFormat;

    private TextView leftLanguageView;

    private TextView rightLanguageView;

    private long sampleIdToScrollTo = -1;

    public SamplesFragment() {
        super();
        this.model = StaticUtils.getModel();
        timer = new CountDownTimer(Integer.MAX_VALUE, 30000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (adapter != null) {
                    WeakContext.getMainActivity().runOnUiThread(adapter::unlockSamples);
                }
            }

            @Override
            public void onFinish() {
            }
        };
        timer.start();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            this.dictionaryBundle = DictionaryBundle.fromBundle(savedInstanceState);
            this.shelfBundle = ShelfBundle.fromBundle(savedInstanceState);
        }
        Context context = this.requireContext();
        allPercentageFormat = StaticUtils.getString(R.string.all_percentage);
        swapEnable = AppCompatResources.getDrawable(context, R.drawable.btn_swap_enable);
        swapDisable = AppCompatResources.getDrawable(context, R.drawable.btn_swap_disable);
        formatCounter = getString(R.string.counter);
        loadBundle();
        RoboVoice.setLanguages(dictionaryBundle.getLeftLocale(), dictionaryBundle.getRightLocale());
        reverseAnimation = AnimationUtils.loadAnimation(context, R.anim.rotate);
        WeakContext.getMainActivity().hideTabs();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_samples, container, false);
        // view.setBackground(GlobalData.bg_default);
        return view;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sampleCounter = view.findViewById(R.id.sampleCounter);
        sampleCounter.setText(String.format(getString(R.string.empty_counter), Spec.MAX_SAMPLES));
        reverseButton = view.findViewById(R.id.reverseSamples);
        allPercentageView = view.findViewById(R.id.allPercentage);
        leftLanguageView = view.findViewById(R.id.leftLanguage);
        rightLanguageView = view.findViewById(R.id.rightLanguage);
        reverseButton.setOnClickListener(this::reverseSamples);
        setLanguageText();
        long dictionaryId = dictionaryBundle.id;
        setReverseButtonDrawable(GlobalData.getReverse(dictionaryId));
        TextView dictNameView = view.findViewById(R.id.dictName);
        dictNameView.setText(dictionaryBundle.name);
        view.findViewById(R.id.startTrain).setOnClickListener(this::navigateToTrainFragment);
        view.findViewById(R.id.addSample).setOnClickListener(this::showSampleCreateDialog);
        list = SamplesRecycleList.getList();
        list.setData(shelfBundle);
        adapter = list.getAdapter();
        adapter.setData(dictionaryBundle, sampleIdToScrollTo);
        list.attachTo(view.findViewById(R.id.samplesContainer));
        onCreateOptionsMenu();
        observerSetup();
    }

    private void setLanguageText() {
        boolean reverse = GlobalData.getReverse(dictionaryBundle.id);
        int leftIndex = SupportedLanguages.getIndex(dictionaryBundle.leftLocaleAbb);
        int rightIndex = SupportedLanguages.getIndex(dictionaryBundle.rightLocaleAbb);
        String leftLanguage = "";
        String rightLanguage = "";
        if (leftIndex != 0) {
            leftLanguage = SupportedLanguages.getLanguages()[leftIndex];
        }
        if (rightIndex != 0) {
            rightLanguage = SupportedLanguages.getLanguages()[rightIndex];
        }
        if (reverse) {
            leftLanguageView.setText(rightLanguage);
            rightLanguageView.setText(leftLanguage);
        } else {
            leftLanguageView.setText(leftLanguage);
            rightLanguageView.setText(rightLanguage);
        }
    }

    public void onCreateOptionsMenu() {
        OverflowMenu.setTitle(R.string.samples_title);
        OverflowMenu.setupBackButton(this::navigateToDictionaryFragment);
        OverflowMenu.setupSearchView(this::onQueryTextChange, GlobalData.sampleSearchQuery.getOrDefault(dictionaryBundle.id, null));
        OverflowMenu.addMenuItem(R.string.action_find_word, this::clickFindWord);
        OverflowMenu.addMenuItem(R.string.action_clear_samples, this::clickClearSamples);
        OverflowMenu.hideAccount();
        OverflowMenu.showMore();
        int stringId = dictionaryBundle.hideRemembered == 1 ? R.string.action_show_remembered : R.string.action_hide_remembered;
        OverflowMenu.addMenuItem(R.string.action_sort_by, this::sort);
        OverflowMenu.addMenuItem(stringId, this::hideShowRemembered);
    }

    private void clickClearSamples(@Nullable View view) {
        ClearSamplesDialog dialog = new ClearSamplesDialog();
        dialog.setOnClearListener(settings -> {
            ArrayList<Sample> items = adapter.getAllItems();
            if (items == null || items.isEmpty()) {
                return;
            }
            for (Sample sample : items) {
                if (settings.isLeft) {
                    sample.setLeftValue("?");
                } else {
                    sample.setRightValue("?");
                }
                if (settings.clearExamples) {
                    sample.setExample("");
                }
                if (settings.clearKinds) {
                    sample.setType("");
                }
                sample.setLeftPercentage(0);
                sample.setRightPercentage(0);
                sample.setRightAnswered(0);
                sample.setLeftAnswered(0);
                sample.correctSeries = 0;
                sample.answeredDate = null;
                sample.lastCorrect = false;
            }
            model.getSamplesRepository().updateSeveral(items);
            dictionaryBundle.todayScore = 0;
            dictionaryBundle.passedPercentage = 0;
            dictionaryBundle.rememberedCount = 0;
            dictionaryBundle.author = null;
            dictionaryBundle.successfulUpdateCheck = null;
            dictionaryBundle.dataDate = null;
            model.getDictionariesRepository().update(dictionaryBundle.convertToDictionary());
        });
        dialog.show();
    }

    private void clickFindWord(@Nullable View view) {
        try {
            List<Shelf> result = StaticUtils.getModel().getShelvesRepository().getAllShelves().getValue();
            FindWordShelvesDialog dialog = new FindWordShelvesDialog((ArrayList<Shelf>) result, model, true);
            dialog.show();
        } catch (Throwable th) {
            // Skip
        }
    }

    private void sort(@Nullable View view) {
        SortByDialog dialog = new SortByDialog(dictionaryBundle.sortedType);
        dialog.setOnOkListener((sortedType) -> {
            dictionaryBundle.sortedType = sortedType;
            StaticUtils.getModel().getDictionariesRepository().update(dictionaryBundle.convertToDictionary());
            adapter.sort(sortedType);
        });
        dialog.show();
    }

    private void hideShowRemembered(@NonNull View view) {
        dictionaryBundle.hideRemembered = dictionaryBundle.hideRemembered == 1 ? 0 : 1;
        StaticUtils.getModel().getDictionariesRepository().update(dictionaryBundle.convertToDictionary());
        adapter.filterRemembered(dictionaryBundle.hideRemembered);
        TextView tv = view.findViewById(R.id.menuItemName);
        tv.setText(dictionaryBundle.hideRemembered == 1 ? R.string.action_show_remembered : R.string.action_hide_remembered);
        updateCounter();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle bundle) {
        dictionaryBundle.toBundle(bundle);
        shelfBundle.toBundle(bundle);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void reverseSamples(@NonNull View view) {
        boolean reverse = GlobalData.getReverse(dictionaryBundle.id);
        reverse = !reverse;
        GlobalData.reverse.put(dictionaryBundle.id, reverse);
        setLanguageText();
        setReverseButtonDrawable(reverse);
        reverseButton.startAnimation(reverseAnimation);
        adapter.notifyDataSetChanged();
    }

    private void setReverseButtonDrawable(boolean reverse) {
        if (reverse) {
            reverseButton.setImageDrawable(swapEnable);
        } else {
            reverseButton.setImageDrawable(swapDisable);
        }
    }

    private void loadBundle() {
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            this.dictScrollPosition = bundle.getInt(BundleNames.SCROLL_POSITION, -1);
            this.dictionaryBundle = DictionaryBundle.fromBundle(bundle);
            this.sampleIdToScrollTo = bundle.getLong(BundleNames.SAMPLE_ID, -1);
            this.shelfBundle = ShelfBundle.fromBundle(bundle);
        }
    }

    private void addSample(String firstValue, String secondValue, String kind, String example, boolean lastCorrect, int correctSeries) {
        model.getSamplesRepository().insert(dictionaryBundle.id, firstValue, secondValue, kind, example, lastCorrect, correctSeries);
    }

    private void navigateToTrainFragment(@NonNull View view) {
        view.startAnimation(reverseAnimation);
        ArrayList<Sample> samples = this.adapter.getAllItems();
        int trainCount;
        int count = 0;
        ArrayList<Sample> toTrain = null;
        if (samples != null && !samples.isEmpty()) {
            count = samples.size();
            toTrain = (ArrayList<Sample>) samples.stream().filter(sample -> !sample.isLocked()).collect(Collectors.toList());
        }
        trainCount = toTrain == null ? 0 : toTrain.size();
        if (trainCount > 0 && count >= MIN_TRAIN_SAMPLES) {
            CacheData.clearAll();

            // Cache all to prepare answers from all of them.
            CacheData.cachedSamples.set(samples);
            long rememberedCount = toTrain.stream().filter(Sample::isRemembered).count();
            boolean reverse = GlobalData.getReverse(dictionaryBundle.id);
            long wrongSize = toTrain.stream().filter((sample -> {
                if (reverse) {
                    return sample.getLeftAnswered() == 1;
                } else {
                    return sample.getRightAnswered() == 1;
                }
            })).count();
            new StartTrainDialog(dictionaryBundle, shelfBundle, wrongSize, rememberedCount, trainCount).show();
        } else {
            Toasts.notEnoughSamples(count >= MIN_TRAIN_SAMPLES ? 1 : MIN_TRAIN_SAMPLES);
        }
    }

    private void showSampleCreateDialog(View view) {
        if (adapter.getAllCount() >= Spec.MAX_SAMPLES) {
            Toasts.maxItemsMessage("samples");
        } else if (!dialogShown) {
            NewSampleDialog dialog = new NewSampleDialog();
            dialog.sampleCreateListener(this::addSample);
            dialog.setOnDismissListener((dialogInterface) -> dialogShown = false);
            dialogShown = true;
            dialog.show();
        }
    }

    private void navigateToDictionaryFragment(View view) {
        Bundle bundle = shelfBundle.toNewBundle();
        bundle.putInt(BundleNames.SCROLL_POSITION, dictScrollPosition);
        StaticUtils.navigateSafe(R.id.action_Samples_to_Dictionaries, bundle);
    }

    @Override
    public void onStart() {
        WeakContext.getMainActivity().hideTabs();
        super.onStart();
    }

    @Override
    public void onStop() {
        RoboVoice.stopSpeaking();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        timer.onFinish();
        timer.cancel();
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        timer.onFinish();
        timer.cancel();
        list.detach();
        list.clearData();
        super.onDetach();
    }

    private void observerSetup() {
        model.getSamplesRepository().getLiveSamples(dictionaryBundle.id).observe(getViewLifecycleOwner(),
                samples -> {
                    ArrayList<Sample> unlocked = new ArrayList<>();
                    if (samples != null && !samples.isEmpty()) {
                        for (Sample sample : samples) {
                            if (sample.unlockIfNeed()) {
                                unlocked.add(sample);
                            }
                        }
                    }
                    if (!unlocked.isEmpty()) {
                        StaticUtils.getModel().getSamplesRepository().updateSeveral(unlocked);
                        return;
                    }
                    adapter.setItems((ArrayList<Sample>) samples);
                    updateCounter();
                    updatePercentage();
                });
    }

    private void onQueryTextChange(String query) {
        adapter.applyFilter(query);
        updateCounter();
    }

    private void updateCounter() {
        sampleCounter.setText(String.format(formatCounter, adapter.getItemCount(), Spec.MAX_SAMPLES));
    }

    private void updatePercentage() {
        ArrayList<Sample> samples = adapter.getAllItems();
        dictionaryBundle.passedPercentage = Dictionary.calculatePercentage(samples);
        dictionaryBundle.count = samples == null ? 0 : samples.size();
        dictionaryBundle.rememberedCount = (int) (samples == null ? 0 : samples.stream().filter(Sample::isRemembered).count());
        model.getDictionariesRepository().update(dictionaryBundle.convertToDictionary());
        if (dictionaryBundle.passedPercentage > 0) {
            allPercentageView.setText(String.format(allPercentageFormat, dictionaryBundle.passedPercentage));
        } else {
            allPercentageView.setText("");
        }
    }
}