package com.vsv.memorizer.fragments;

import android.content.Context;
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
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.vsv.db.entities.DataToAdd;
import com.vsv.db.entities.Dictionary;
import com.vsv.db.entities.DictionaryWithSamples;
import com.vsv.db.entities.NotebookWithNotes;
import com.vsv.db.entities.Sample;
import com.vsv.db.entities.Settings;
import com.vsv.db.entities.Shelf;

import com.vsv.db.entities.SpreadSheetInfo;
import com.vsv.dialogs.AddDictionaryNotebookDialog;
import com.vsv.dialogs.FindWordShelvesDialog;
import com.vsv.dialogs.HelpDialog;
import com.vsv.dialogs.LanguageDialog;
import com.vsv.dialogs.NewShelfDialog;
import com.vsv.dialogs.PipelineDialog;
import com.vsv.io.Storage;
import com.vsv.memorizer.MainActivity;
import com.vsv.memorizer.R;
import com.vsv.memorizer.adapters.RecyclerShelvesAdapter;

import com.vsv.models.MainModel;

import com.vsv.bundle.helpers.BundleNames;
import com.vsv.overflowmenu.OverflowMenu;
import com.vsv.recyclerentities.ShelvesRecycleList;
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
import com.vsv.utils.Symbols;

import java.util.ArrayList;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ShelvesFragment extends Fragment {

    private RecyclerShelvesAdapter adapter;

    private final MainModel model;

    private ShelvesRecycleList recycleList;

    private ViewGroup shelfContainer;

    private View mainView;

    private FloatingActionButton fab;

    private View progress;

    public ShelvesFragment() {
        super();
        model = StaticUtils.getModel();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        if (recycleList != null) {
            recycleList.detachAndClear();
        }
        super.onDetach();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.fragment_shelves, container, false);
        return mainView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        fab = mainView.findViewById(R.id.addShelf);
        progress = mainView.findViewById(R.id.progress);
        progress.setVisibility(View.INVISIBLE);
        fab.setVisibility(View.INVISIBLE);
        shelfContainer = view.findViewById(R.id.shelfContainer);
        optionsMenuFirst();
        String contentPath = CacheData.cachedContentPath.get(true);
        if (ShelvesRecycleList.isBuilt() && contentPath == null) {
            loadList(ShelvesRecycleList.getList());
        } else {
            progress.setVisibility(View.VISIBLE);
            GlobalExecutors.modelsExecutor.submit(() -> {
                DataToAdd dataToAdd = null;
                try {
                    dataToAdd = Storage.getDocumentsStorage().getDataFromContentResolver(contentPath, this.requireContext());
                    if (contentPath != null && dataToAdd == null) {
                        new Handler(Looper.getMainLooper()).post(Toasts::nothingToAdd);
                    }
                } catch (Exception e) {
                    new Handler(Looper.getMainLooper()).post(Toasts::cannotReadBrokenFile);
                    Log.e("Error", e.toString());
                }
                if (dataToAdd != null) {
                    if (dataToAdd.dictionaryWithSamples != null) {
                        CacheData.samplesToAdd.set(dataToAdd.dictionaryWithSamples);
                    } else if (dataToAdd.notebookWithNotes != null) {
                        CacheData.notesToAdd.set(dataToAdd.notebookWithNotes);
                    } else if (dataToAdd.spreadsheet != null) {
                        CacheData.spreadsheetToAdd.set(dataToAdd.spreadsheet);
                    }
                }
                ShelvesRecycleList list = ShelvesRecycleList.getList();
                Handler handler = new Handler(Looper.getMainLooper());
                boolean result = handler.post(() -> loadList(list));
                if (!result) {
                    Log.e("ShelvesFragment", "handler false");
                }
            });
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

    public void loadList(ShelvesRecycleList recycleList) {
        WeakContext.getMainActivity().setTopCount(0, Spec.MAX_SHELVES);
        mainView.setBackground(GlobalData.bg_shelf);
        progress.setVisibility(View.INVISIBLE);
        fab.setVisibility(View.VISIBLE);
        fab.setOnClickListener(this::showNewShelfDialog);
        recycleList.attachTo(shelfContainer);
        adapter = recycleList.getAdapter();
        adapter.setup();
        this.recycleList = recycleList;
        observerSetup();
        onCreateOptionsMenu();
        NotebookWithNotes notebookWithNotes = CacheData.notesToAdd.get(true);
        DictionaryWithSamples dictionaryWithSamples = CacheData.samplesToAdd.get(false);
        SpreadSheetInfo spreadsheet = CacheData.spreadsheetToAdd.get(true);

        // Means we want to add new shelf from file.
        if (dictionaryWithSamples != null) {
            AddDictionaryNotebookDialog dialog = new AddDictionaryNotebookDialog(dictionaryWithSamples.dictionary.getName(), AddDictionaryNotebookDialog.DICTIONARY);
            dialog.setCancelListener(CacheData.samplesToAdd::clear);
            dialog.setOkListener((dictName) -> {
                if (!dictName.isEmpty()) {
                    Objects.requireNonNull(CacheData.samplesToAdd.get(false))
                            .dictionary.setName(dictName);
                }
                Bundle bundle = new Bundle();
                bundle.putInt(BundleNames.MOVE_TYPE, AddMoveCopyFragment.ADD);
                StaticUtils.navigateSafe(R.id.action_Shelves_to_MoveOrCopy, bundle);
            });
            dialog.show();
        } else if (notebookWithNotes != null) {
            CacheData.samplesToAdd.clear();
            AddDictionaryNotebookDialog dialog = new AddDictionaryNotebookDialog(notebookWithNotes.notebook.getName(), AddDictionaryNotebookDialog.NOTEBOOK);
            dialog.setOkListener((notebookName) -> {
                if (!notebookName.isEmpty()) {
                    notebookWithNotes.notebook.setName(notebookName);
                }
                notebookWithNotes.notebook.notesCount = notebookWithNotes.notes.size();
                StaticUtils.getModel().getNotebookWithNotesRepository().insertWithSamples(notebookWithNotes.notebook, notebookWithNotes.notes);
                StaticUtils.navigateSafe(R.id.action_Shelves_to_Notebooks);
            });
            dialog.show();
        } else if (spreadsheet != null) {
            AddDictionaryNotebookDialog dialog = new AddDictionaryNotebookDialog(spreadsheet.getName(), AddDictionaryNotebookDialog.SPREADSHEET);
            dialog.setOkListener((spreadsheetName) -> {
                ArrayList<SpreadSheetInfo> spreadsheets;
                try {
                    spreadsheets = StaticUtils.getModel().getSpreadsheetsRepository().getAll().get(5, TimeUnit.SECONDS);
                } catch (ExecutionException | InterruptedException | TimeoutException e) {
                    Toasts.cannotAddSpreadsheet();
                    return;
                }
                if (spreadsheets != null) {
                    for (SpreadSheetInfo spreadSheetInfo : spreadsheets) {
                        if (spreadSheetInfo.spreadSheetId.equals(spreadsheet.spreadSheetId)) {
                            Toasts.spreadsheetExists();
                            return;
                        }
                    }
                }
                spreadsheet.name = spreadsheetName;
                StaticUtils.getModel().getSpreadsheetsRepository().insert(spreadsheet);
                StaticUtils.navigateSafe(R.id.action_Shelves_to_Spreadsheets);
            });
            dialog.show();
        } else {
            Settings settings = GlobalData.getSettings();
            if (settings.startIndex == 0) { // First opening
                createDefaultShelf();
                settings.startIndex = 1;
                StaticUtils.getModel().update(settings);
                GlobalData.setSettings(settings);
                new HelpDialog().show();
            }
        }
    }

    public void createDefaultShelf() {
        Shelf shelf = model.getShelvesRepository().insertWithWaiting(StaticUtils.getString(R.string.shelf_example_name, Symbols.getRandomNameSymbol()), 10000);
        String[] values = new String[] {"zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten"};
        if (shelf != null) {
            ArrayList<Sample> samples = new ArrayList<>();
            for (int i = 0; i <= 10; i++) {
                String number = String.valueOf(i);
                Sample sample = new Sample(0, values[i] , number, 0, 0);
                sample.setType("noun");
                sample.setExample("It is a number " + values[i] + ".");
                samples.add(sample);

            }
            Dictionary dictionary = new Dictionary(shelf.getId(), StaticUtils.getString(R.string.dictionary_example_name, Symbols.getRandomNameSymbol()));
            dictionary.setLeftLocaleAbb("en_US");
            dictionary.setRightLocaleAbb("en_US");
            dictionary.canCopy = true;
            model.getDictionaryWithSamplesRepository().insertWithSamples(dictionary, samples);
        }
    }

    public void optionsMenuFirst() {
        OverflowMenu.setTitle(R.string.app_header);
        OverflowMenu.hideMore();
        OverflowMenu.hideAccount();
        // OverflowMenu.hideNotebook();
        OverflowMenu.hideSearch();
        OverflowMenu.hideBackButton();
    }

    public void onCreateOptionsMenu() {
        OverflowMenu.showMore();
        OverflowMenu.hideBackButton();
        OverflowMenu.addMenuItem(R.string.action_find_word, this::clickFindWord);
        OverflowMenu.addMenuItem(GlobalData.getSettings().sortShelf ? R.string.action_sort_by_date : R.string.action_sort_by_name, this::clickSort);
        OverflowMenu.addMenuItem(R.string.action_language, this::changeLanguage);
        OverflowMenu.addMenuItem(R.string.action_help, this::helpClick);
        OverflowMenu.setupSearchView(this::onQueryTextChange, GlobalData.shelfSearchQuery);
        OverflowMenu.showAccount();
    }

    private void changeLanguage(@Nullable View view) {
        LanguageDialog languageDialog = new LanguageDialog();
        languageDialog.show();
    }

    private void notebooksClick(@Nullable View view) {
        StaticUtils.navigateSafe(R.id.action_Shelves_to_Notebooks);
    }

    public void helpClick(@Nullable View view) {
        new HelpDialog().show();
    }

    @Override
    public void onResume() {
        WeakContext.getMainActivity().setTab(MainActivity.SHELVES_TAB);
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }


    private void clickFindWord(@Nullable View view) {
        FindWordShelvesDialog dialog = new FindWordShelvesDialog(adapter.getCopyItems(), model, false);
        dialog.show();
    }

    private void clickSpreadsheet(@Nullable View view) {
        GlobalData.lastFragmentId = R.id.action_Spreadsheets_to_Shelves;
        StaticUtils.navigateSafe(R.id.action_Shelves_to_Spreadsheets, null);
    }

    private void clickSort(@NonNull View view) {
        Settings settings = GlobalData.getSettings();
        settings.sortShelf = !settings.sortShelf;
        TextView tv = view.findViewById(R.id.menuItemName);
        tv.setText(settings.sortShelf ? R.string.action_sort_by_date : R.string.action_sort_by_name);
        StaticUtils.updateSettings();
        adapter.update();
    }

    private void onQueryTextChange(String query) {
        adapter.applyFilter(query);
        updateCounter();
    }

    private void updateCounter() {
        WeakContext.getMainActivity().setTopCount(adapter.getItemCount(), Spec.MAX_SHELVES);
    }

    private void showNewShelfDialog(View view) {
        if (adapter.getAllCount() >= Spec.MAX_SHELVES) {
            Toasts.maxItemsMessage(getString(R.string.shelves_item_name));
        } else {
            NewShelfDialog dialog = new NewShelfDialog(true);
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

    private void observerSetup() {
        model.getShelvesRepository().getAllShelves().observe(getViewLifecycleOwner(),
                shelves -> {
                    adapter.setItems((ArrayList<Shelf>) shelves);
                    updateCounter();
                    GlobalExecutors.modelsExecutor.execute(this::calculateDictionaries);
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
