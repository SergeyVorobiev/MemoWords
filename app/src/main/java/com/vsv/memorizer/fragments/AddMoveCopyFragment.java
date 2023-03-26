package com.vsv.memorizer.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.vsv.bundle.entities.DictionaryBundle;
import com.vsv.bundle.entities.ShelfBundle;
import com.vsv.db.entities.Dictionary;
import com.vsv.db.entities.DictionaryWithSamples;
import com.vsv.db.entities.Settings;
import com.vsv.db.entities.Shelf;

import com.vsv.dialogs.NewShelfDialog;
import com.vsv.dialogs.PipelineDialog;
import com.vsv.memorizer.R;
import com.vsv.memorizer.adapters.RecyclerAddAdapter;

import com.vsv.models.MainModel;

import com.vsv.overflowmenu.OverflowMenu;
import com.vsv.recyclerentities.AddRecycleList;
import com.vsv.bundle.helpers.BundleNames;
import com.vsv.repositories.DictionariesRepository;
import com.vsv.spreadsheet.SheetLoader;
import com.vsv.statics.CacheData;
import com.vsv.statics.GlobalData;
import com.vsv.statics.GlobalExecutors;
import com.vsv.statics.WeakContext;
import com.vsv.toasts.Toasts;
import com.vsv.utils.GoogleTasksExceptionHandler;
import com.vsv.utils.Spec;
import com.vsv.utils.StaticUtils;

import java.util.ArrayList;
import java.util.TreeMap;

public class AddMoveCopyFragment extends Fragment {

    public static final int MOVE = 0;

    public static final int ADD = 1;

    public static final int COPY = 2;

    private ShelfBundle shelfBundle;

    private int type;

    private RecyclerAddAdapter adapter;

    private final MainModel model;

    private Dictionary dictionary;

    private DictionaryWithSamples dictionaryWithSamples;

    private AddRecycleList recycleList;

    public AddMoveCopyFragment() {
        super();
        AddRecycleList.buildCache();
        model = StaticUtils.getModel();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        dictionaryWithSamples = CacheData.samplesToAdd.get(true);
        if (savedInstanceState != null) {
            dictionary = DictionaryBundle.fromBundleToDictionary(savedInstanceState);
            shelfBundle = ShelfBundle.fromBundle(savedInstanceState);
            type = savedInstanceState.getInt(BundleNames.MOVE_TYPE);
        }
        if (bundle != null) {
            dictionary = DictionaryBundle.fromBundleToDictionaryOrNull(bundle); // Null if add new one.
            shelfBundle = ShelfBundle.fromBundleOrNull(bundle);
            type = bundle.getInt(BundleNames.MOVE_TYPE);
        }
        if (type != ADD) {
            if (dictionary == null) {
                Log.e("AddMoveCopyFragment", "shelf or dictionary id is null");
                throw new RuntimeException("Null dictionary");
            }
        } else if (dictionaryWithSamples == null) {
            Log.e("AddMoveCopyFragment", "dictionaryWithSamples is null");
            StaticUtils.navigateSafe(R.id.action_MoveOrCopy_to_Shelves, null);
        }
    }

    private void calculateDictionaries() {
        DictionariesRepository repo = StaticUtils.getModel().getDictionariesRepository();
        ArrayList<Long> shelfIds = StaticUtils.getModel().getShelvesRepository().getAllIdsOrEmpty(5);
        TreeMap<Long, Pair<Integer, Float>> map = new TreeMap<>();
        for (long id : shelfIds) {
            float percentage = 0;
            ArrayList<Float> percentages = repo.getAllPercentagesOrEmpty(id, 5);
            int count = percentages.size();
            float sum = 0;
            for (float perc : percentages) {
                sum += perc;
            }
            if (count > 0) {
                percentage = sum / count;
            }
            map.put(id, new Pair<>(count, percentage));
        }
        new Handler(Looper.getMainLooper()).postDelayed(() -> adapter.updateCountMap(map), 250);
    }

    @Override
    public void onStart() {
        WeakContext.getMainActivity().hideTabs();
        super.onStart();
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_shelves, container, false);
        view.setBackground(AppCompatResources.getDrawable(this.requireContext(), R.drawable.bg_move));
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FloatingActionButton fab = view.findViewById(R.id.addShelf);
        fab.setOnClickListener(this::showNewShelfDialog);
        recycleList = AddRecycleList.getList();
        recycleList.setData(dictionary, dictionaryWithSamples, shelfBundle, type);
        adapter = recycleList.getAdapter();
        GlobalExecutors.modelsExecutor.execute(this::calculateDictionaries);
        recycleList.attachTo(view.findViewById(R.id.shelfContainer));
        onCreateOptionsMenu();
        observerSetup();
    }

    public void onCreateOptionsMenu() {
        int title;
        if (type == MOVE) { // Move;
            title = R.string.move_title;
        } else if (type == COPY) {
            title = R.string.copy_title;
        } else if (type == ADD) {
            title = R.string.add_title;
        } else {
            throw new RuntimeException("Wrong type");
        }
        OverflowMenu.setTitle(title);
        OverflowMenu.hideAccount();
        OverflowMenu.showMore();
        OverflowMenu.addMenuItem(GlobalData.getSettings().sortShelf ? R.string.action_sort_by_date : R.string.action_sort_by_name, this::clickSort);
        OverflowMenu.setupSearchView(this::onQueryTextChange, null);
        OverflowMenu.setupBackButton(this::clickBack);
    }

    private void clickSort(@NonNull View view) {
        Settings settings = GlobalData.getSettings();
        settings.sortShelf = !settings.sortShelf;
        TextView tv = view.findViewById(R.id.menuItemName);
        tv.setText(settings.sortShelf ? R.string.action_sort_by_date : R.string.action_sort_by_name);
        StaticUtils.updateSettings();
        adapter.update();
    }

    private void clickBack(@Nullable View view) {
        if (type == ADD) {
            StaticUtils.navigateSafe(R.id.action_MoveOrCopy_to_Shelves, null);
        } else {
            navigateToDictionariesFragment();
        }
    }

    private void navigateToDictionariesFragment() {
        StaticUtils.navigateSafe(R.id.action_MoveOrCopy_to_Dictionaries, shelfBundle.toNewBundle());
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        DictionaryBundle.toBundle(outState, dictionary);
        shelfBundle.toBundle(outState);
        outState.putInt(BundleNames.MOVE_TYPE, type);
        super.onSaveInstanceState(outState);
    }

    private void onQueryTextChange(String query) {
        adapter.applyFilter(query);
        updateCounter();
    }

    private void updateCounter() {
        WeakContext.getMainActivity().setTopCount(adapter.getItemCount(), Spec.MAX_SHELVES);
    }

    @Override
    public void onDetach() {
        recycleList.detach();
        recycleList.clearData();
        super.onDetach();
    }

    private void showNewShelfDialog(View view) {
        if (adapter.getAllCount() >= Spec.MAX_SHELVES) {
            Toasts.maxItemsMessage(getString(R.string.shelves_item_name));
        } else {
            NewShelfDialog dialog = new NewShelfDialog(false);
            dialog.createShelfListener((shelfName, spreadsheet, sheets, bindSpreadsheet, loadProgress) -> {
                if (spreadsheet != null && sheets != null && !sheets.isEmpty()) {
                    MainModel model = StaticUtils.getModelOrNull();
                    if (model == null) {
                        Toasts.unexpectedError();
                        return;
                    }
                    Shelf shelf = model.getShelvesRepository().insertWithWaiting(shelfName, 3000);
                    if (shelf == null) {
                        Toasts.cannotAddShelf();
                        return;
                    }
                    PipelineDialog waitDialog = SheetLoader.buildLoadSpreadsheetDataDialog(spreadsheet.spreadSheetId, spreadsheet.name,
                            sheets, null, shelf.getId(), bindSpreadsheet, loadProgress, Spec.MAX_DICTIONARIES);
                    if (waitDialog != null) {
                        waitDialog.setRunOnFinish((object) -> {
                            if (object != null && object.taskResult != null) {

                                // noinspection unchecked
                                calculateDictionariesForShelf((ArrayList<Float>) object.taskResult, shelf.getId());
                            }
                        });
                        waitDialog.setRunMainThreadOnFail((e) -> Toasts.longShowRaw(GoogleTasksExceptionHandler.handle(e)));
                        waitDialog.showInterruptButton();
                        waitDialog.showProgress();
                        waitDialog.showOver();
                    }
                } else {
                    model.getShelvesRepository().insert(shelfName);
                }
            });
            dialog.show();
        }
    }

    private void calculateDictionariesForShelf(@NonNull ArrayList<Float> percentages, long shelfId) {
        float percentage = 0;
        int count = percentages.size();
        float sum = 0;
        for (float perc : percentages) {
            sum += perc;
        }
        if (count > 0) {
            percentage = sum / count;
        }
        float finalPercentage = percentage;
        new Handler(Looper.getMainLooper()).postDelayed(() -> adapter.updateInfoForShelf(shelfId, count, finalPercentage), 0);
    }

    private void observerSetup() {
        model.getShelvesRepository().getAllShelves().observe(getViewLifecycleOwner(),
                shelves -> {
                    adapter.setItems((ArrayList<Shelf>) shelves);
                    updateCounter();
                });
    }
}

