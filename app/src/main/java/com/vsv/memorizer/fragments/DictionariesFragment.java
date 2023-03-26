package com.vsv.memorizer.fragments;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.vsv.bundle.entities.ShelfBundle;
import com.vsv.db.entities.DictNoteSpreadsheetNamesUpdater;
import com.vsv.db.entities.Dictionary;
import com.vsv.db.entities.SheetDatesUpdate;
import com.vsv.db.entities.Sample;
import com.vsv.db.entities.SpreadSheetInfo;
import com.vsv.dialogs.FindWordShelfDialog;
import com.vsv.dialogs.NewDictionaryDialog;
import com.vsv.dialogs.entities.SheetTab;
import com.vsv.memorizer.R;
import com.vsv.memorizer.adapters.RecyclerDictionariesAdapter;
import com.vsv.models.MainModel;
import com.vsv.overflowmenu.OverflowMenu;
import com.vsv.recyclerentities.DictionariesRecycleList;
import com.vsv.spreadsheet.SheetLoader;
import com.vsv.bundle.helpers.BundleNames;
import com.vsv.statics.GlobalData;
import com.vsv.statics.SheetDataUpdater;
import com.vsv.statics.WeakContext;
import com.vsv.toasts.Toasts;
import com.vsv.utils.Spec;
import com.vsv.utils.StaticUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DictionariesFragment extends Fragment {

    private final MainModel model;

    private RecyclerDictionariesAdapter adapter;

    private TextView counterView;

    private TextView samplesCounter;

    private int scrollPosition = -1;

    private DictionariesRecycleList recycleList;

    public DictionariesFragment() {
        model = StaticUtils.getModel();
    }

    private ShelfBundle shelfBundle;

    private CountDownTimer timer;

    private boolean isUpdated = false;

    private boolean updatedSpreadsheetNames = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            shelfBundle = ShelfBundle.fromBundle(savedInstanceState);
        }
        loadShelfId();
        timer = new CountDownTimer(Integer.MAX_VALUE, 60000) {

            @Override
            public void onTick(long millisUntilFinished) {
                try {
                    MainModel model = StaticUtils.getModelOrNull();
                    if (model == null) {
                        return;
                    }
                    TreeMap<Long, Integer> map = new TreeMap<>();
                    ArrayList<Long> ids = model.getDictionariesRepository().getAllIds(shelfBundle.id).get(10, TimeUnit.SECONDS);
                    if (ids != null) {
                        for (long id : ids) {
                            ArrayList<Sample> unlocked = new ArrayList<>();
                            ArrayList<Sample> samples = model.getSamplesRepository().getSamplesWithTimeout(id, 5);
                            int trainedCount = 0;
                            if (samples != null && !samples.isEmpty()) {
                                for (Sample sample : samples) {
                                    if (sample.isRemembered()) {
                                        trainedCount += 1;
                                    } else if (sample.isLocked()) {
                                        if (sample.unlockIfNeed()) {
                                            unlocked.add(sample);
                                        } else {
                                            trainedCount += 1;
                                        }
                                    }
                                }
                            }
                            map.put(id, unlocked.size() + trainedCount);
                            model.getSamplesRepository().updateSeveral(unlocked);
                        }
                    }
                    adapter.setTrainCountMap(map);
                    new Handler(Looper.getMainLooper()).postDelayed(adapter::notifyDataSetChanged, 1000);
                } catch (ExecutionException | TimeoutException | InterruptedException | NullPointerException e) {
                    Log.e("Count updater", e.toString());
                }
            }

            @Override
            public void onFinish() {
                Log.d("Timer", "Timer finished");
            }
        };
        WeakContext.getMainActivity().hideTabs();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dictionaries, container, false);
        view.setBackground(GlobalData.bg_dict);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FloatingActionButton fab = view.findViewById(R.id.addDict);
        fab.setOnClickListener(this::showNewDictionaryDialog);
        counterView = view.findViewById(R.id.dictCounter);
        samplesCounter = view.findViewById(R.id.samplesCounter);
        TextView shelfNameView = view.findViewById(R.id.shelfName);
        shelfNameView.setText(shelfBundle.name);
        counterView.setText(String.format(getString(R.string.empty_counter), Spec.MAX_DICTIONARIES));
        recycleList = DictionariesRecycleList.getList();
        recycleList.setData(shelfBundle);
        recycleList.attachTo(view.findViewById(R.id.dictContainer));
        adapter = recycleList.getAdapter();
        observerSetup();
        onCreateOptionsMenu();
        timer.start();
    }

    private void loadShelfId() {
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            shelfBundle = ShelfBundle.fromBundle(bundle);
            scrollPosition = bundle.getInt(BundleNames.SCROLL_POSITION, -1);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle bundle) {
        shelfBundle.toBundle(bundle);
    }

    public void onCreateOptionsMenu() {
        OverflowMenu.setTitle(R.string.dict_fragment_title);
        OverflowMenu.setupBackButton(this::navigateToShelvesFragment);
        OverflowMenu.showAccount();
        OverflowMenu.showMore();
        OverflowMenu.addMenuItem(R.string.action_find_word, this::menuClickFindWord);
        OverflowMenu.addMenuItem(R.string.action_spreadsheets, this::onSpreadsheetMenuClick);
        int id = shelfBundle.sorted ? R.string.action_sort_by_date : R.string.action_sort_by_name;
        OverflowMenu.addMenuItem(id, this::clickSort);
        id = shelfBundle.hideRemembered ? R.string.action_show_remembered : R.string.action_hide_remembered;
        OverflowMenu.addMenuItem(id, this::showHideRemembered);
        OverflowMenu.setupSearchView(this::onQueryTextChange, GlobalData.dictSearchQuery.getOrDefault(shelfBundle.id, null));
    }

    private void onSpreadsheetMenuClick(@Nullable View view) {
        GlobalData.lastFragmentId = R.id.action_Spreadsheets_to_Dictionaries;
        StaticUtils.navigateSafe(R.id.action_Dictionaries_to_Spreadsheets, shelfBundle.toNewBundle());
    }

    private void showHideRemembered(@NonNull View view) {
        shelfBundle.hideRemembered = !shelfBundle.hideRemembered;
        TextView tv = view.findViewById(R.id.menuItemName);
        tv.setText(shelfBundle.hideRemembered ? R.string.action_show_remembered : R.string.action_hide_remembered);
        model.getShelvesRepository().update(shelfBundle.convertToShelf());
        updateCounter(adapter.update());
    }

    private void clickSort(@NonNull View view) {
        shelfBundle.sorted = !shelfBundle.sorted;
        TextView tv = view.findViewById(R.id.menuItemName);
        tv.setText(shelfBundle.sorted ? R.string.action_sort_by_date : R.string.action_sort_by_name);
        model.getShelvesRepository().update(shelfBundle.convertToShelf());
        adapter.update();
    }

    private void menuClickFindWord(@Nullable View view) {
        FindWordShelfDialog dialog = new FindWordShelfDialog(adapter.getCopyItems(), model, shelfBundle);
        dialog.show();
    }

    private void navigateToShelvesFragment(View view) {
        StaticUtils.navigateSafe(R.id.action_Dictionaries_to_Shelves, null);
    }

    private void onQueryTextChange(String query) {
        updateCounter(adapter.applyFilter(query));
    }

    @Override
    public void onDetach() {
        timer.cancel();
        recycleList.detach();
        recycleList.clearData();
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        timer.cancel();
        super.onDestroy();
    }

    @Override
    public void onStart() {
        WeakContext.getMainActivity().hideTabs();
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void updateCounter(int size) {
        if (scrollPosition != -1) {
            try {
                recycleList.getRecyclerView().scrollToPosition(scrollPosition);
                adapter.disableItemsAnimation();
            } catch (Exception e) {
                //
            }
            scrollPosition = -1;
        }
        String counter = getResources().getString(R.string.counter);
        counterView.setText(String.format(counter, size, Spec.MAX_DICTIONARIES));
    }

    private void showNewDictionaryDialog(View view) {
        int count = adapter.getAllCount();
        if (count >= Spec.MAX_DICTIONARIES) {
            Toasts.maxItemsMessage(StaticUtils.getString(R.string.dictionaries_item_name));
        } else {
            NewDictionaryDialog dialog = new NewDictionaryDialog();
            dialog.setDictionaryCreateListener((name, leftLocaleAbb, rightLocaleAbb) -> model.getDictionariesRepository().insert(shelfBundle.id, name, leftLocaleAbb, rightLocaleAbb, null, null, -1, true));
            dialog.setDictionaryLoadFromSpreadsheetListener(this::loadFromSpreadsheet);
            dialog.show();
        }
    }

    private boolean loadFromSpreadsheet(String dictName, String leftLocaleAbb, String rightLocaleAbb,
                                        SheetTab sheetTab, String spreadsheetId, @Nullable String spreadsheetName, boolean loadProgress,
                                        boolean bindSpreadsheet) {
        return SheetLoader.loadSheetData(dictName, leftLocaleAbb, rightLocaleAbb, sheetTab,
                spreadsheetId, spreadsheetName, shelfBundle.id, loadProgress, bindSpreadsheet);
    }

    private void updateSpreadsheetNames(@Nullable ArrayList<Dictionary> dictionaries) {
        if (dictionaries == null || dictionaries.isEmpty()) {
            return;
        }
        LiveData<List<SpreadSheetInfo>> liveData = model.getSpreadsheetsRepository().getAllLive();
        liveData.observe(getViewLifecycleOwner(), (spreadsheets) -> {
            liveData.removeObservers(getViewLifecycleOwner());
                ArrayList<DictNoteSpreadsheetNamesUpdater> updaters = new ArrayList<>();
                String defaultSpreadsheetName = StaticUtils.getString(R.string.default_spreadsheet_name);
                TreeMap<String, String> map = new TreeMap<>();
                if (spreadsheets != null) {
                    for (SpreadSheetInfo spreadSheetInfo : spreadsheets) {
                        map.put(spreadSheetInfo.spreadSheetId, spreadSheetInfo.name);
                    }
                }
                for (Dictionary dictionary : dictionaries) {
                    String spreadsheetName;
                    if (dictionary.hasOwner()) {
                        spreadsheetName = map.get(dictionary.spreadsheetId);
                        if (spreadsheetName == null || spreadsheetName.isEmpty()) {
                            if (dictionary.spreadsheetName != null && !dictionary.spreadsheetName.isEmpty()) {
                                continue; // Spreadsheet name is already existed and we do not need to change it to default because dictionary has owner.
                            }
                            spreadsheetName = defaultSpreadsheetName;
                        }
                    } else {
                        spreadsheetName = null;
                    }
                    if (spreadsheetName == null) {
                        if (dictionary.spreadsheetName == null) {
                            continue; // Nothing to update
                        }
                        // Need to update
                    } else if (spreadsheetName.equals(dictionary.spreadsheetName)) {
                        continue; // Nothing to update
                    }
                    DictNoteSpreadsheetNamesUpdater namesUpdater = new DictNoteSpreadsheetNamesUpdater(dictionary.getId(), spreadsheetName);
                    updaters.add(namesUpdater);
                }
                if (!updaters.isEmpty()) {
                    model.getDictionariesRepository().updateSpreadsheetNames(updaters);
                }
        });
    }

    private void updateSamplesSize(ArrayList<Dictionary> dictionaries) {
        if (dictionaries != null && !dictionaries.isEmpty()) {
            int size = 0;
            for (Dictionary dictionary : dictionaries) {
                size += dictionary.getCount();
            }
            samplesCounter.setText(String.valueOf(size));
        } else {
            samplesCounter.setText("0");
        }
    }

    private void observerSetup() {
        model.getDictionariesRepository().getAllLiveFromShelf(shelfBundle.id).observe(getViewLifecycleOwner(),
                dictionaries -> {
                    ArrayList<Dictionary> list = (ArrayList<Dictionary>) dictionaries;
                    if (!updatedSpreadsheetNames) {
                        updatedSpreadsheetNames = true;
                        updateSpreadsheetNames(list);
                    }
                    if (!isUpdated) {
                        isUpdated = true;
                        ArrayList<SheetDatesUpdate> toUpdate = SheetDataUpdater.updateDates(new ArrayList<>(list));
                        if (!toUpdate.isEmpty()) {
                            model.getDictionariesRepository().updateDateSeveral(toUpdate);
                        } else {
                            updateCounter(adapter.setItems(list));
                            updateSamplesSize(list);
                        }
                    } else {
                        updateCounter(adapter.setItems(list));
                        updateSamplesSize(list);
                    }
                });
    }
}