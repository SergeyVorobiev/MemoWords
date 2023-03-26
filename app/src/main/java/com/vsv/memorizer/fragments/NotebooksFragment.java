package com.vsv.memorizer.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.vsv.db.entities.DictNoteSpreadsheetNamesUpdater;
import com.vsv.db.entities.Notebook;
import com.vsv.db.entities.Settings;
import com.vsv.db.entities.SheetDatesUpdate;
import com.vsv.db.entities.SpreadSheetInfo;
import com.vsv.dialogs.NewNotebookDialog;
import com.vsv.memorizer.MainActivity;
import com.vsv.memorizer.R;
import com.vsv.memorizer.adapters.RecyclerNotebooksAdapter;
import com.vsv.models.MainModel;
import com.vsv.overflowmenu.OverflowMenu;
import com.vsv.recyclerentities.NotebooksRecycleList;
import com.vsv.spreadsheet.SheetLoader;
import com.vsv.statics.GlobalData;
import com.vsv.statics.SheetDataUpdater;
import com.vsv.statics.WeakContext;
import com.vsv.toasts.Toasts;
import com.vsv.utils.Spec;
import com.vsv.utils.StaticUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class NotebooksFragment extends Fragment {

    private final MainModel model;

    private RecyclerNotebooksAdapter adapter;

    private NotebooksRecycleList recycleList;

    public NotebooksFragment() {
        model = StaticUtils.getModel();
    }

    private boolean isUpdated;

    private boolean updatedSpreadsheetNames = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notebooks, container, false);
        view.setBackground(GlobalData.bg_notebook);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FloatingActionButton fab = view.findViewById(R.id.addNotebook);
        fab.setOnClickListener(this::showNewNotebookDialog);
        // WeakContext.getMainActivity().setTopCount(0, Spec.MAX_NOTEBOOKS);
        recycleList = NotebooksRecycleList.getList();
        recycleList.attachTo(view.findViewById(R.id.notebooksContainer));
        adapter = recycleList.getAdapter();
        adapter.setup();
        observerSetup();
        onCreateOptionsMenu();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle bundle) {

    }

    public void onCreateOptionsMenu() {
        // OverflowMenu.setTitle(R.string.notebooks_title);
        // OverflowMenu.setupBackButton(this::navigateToShelvesFragment);
        OverflowMenu.setTitle(R.string.app_header);
        OverflowMenu.showAccount();
        OverflowMenu.showMore();
        OverflowMenu.hideBackButton();
        OverflowMenu.addMenuItem(GlobalData.getSettings().sortNotebooks ? R.string.action_sort_by_date : R.string.action_sort_by_name, this::clickSort);
        // OverflowMenu.addMenuItem(R.string.action_spreadsheets, this::clickSpreadsheet);
        OverflowMenu.setupSearchView(this::onQueryTextChange, GlobalData.notebookSearchQuery);
    }

    private void clickSpreadsheet(@Nullable View view) {
        GlobalData.lastFragmentId = R.id.action_Spreadsheets_to_Notebooks;
        StaticUtils.navigateSafe(R.id.action_Notebooks_to_Spreadsheets, null);
    }

    private void clickSort(@NonNull View view) {
        Settings settings = GlobalData.getSettings();
        settings.sortNotebooks = !settings.sortNotebooks;
        TextView tv = view.findViewById(R.id.menuItemName);
        tv.setText(settings.sortNotebooks ? R.string.action_sort_by_date : R.string.action_sort_by_name);
        StaticUtils.updateSettings();
        adapter.update();
    }

    private void navigateToShelvesFragment(View view) {
        StaticUtils.navigateSafe(R.id.action_Notebooks_to_Shelves, null);
    }

    private void onQueryTextChange(String query) {
        updateCounter(adapter.applyFilter(query));
    }

    @Override
    public void onDetach() {
        recycleList.detach();
        recycleList.clearData();
        super.onDetach();
    }

    @Override
    public void onResume() {
        WeakContext.getMainActivity().setTab(MainActivity.NOTEBOOKS_TAB);
        super.onResume();
    }

    private void updateCounter(int size) {
        WeakContext.getMainActivity().setTopCount(adapter.getItemCount(), Spec.MAX_NOTEBOOKS);
    }

    private void showNewNotebookDialog(View view) {
        int count = adapter.getAllCount();
        if (count >= Spec.MAX_NOTEBOOKS) {
            Toasts.maxItemsMessage(StaticUtils.getString(R.string.notebooks_item_name));
        } else {
            NewNotebookDialog dialog = new NewNotebookDialog();
            dialog.setNotebookCreateListener((name) -> model.getNotebooksRepository().insert(name, true, null, null));
            dialog.setNotebookLoadFromSpreadsheetListener(((notebookName, spreadsheetId, spreadsheetName, sheetName, sheetId, bindSpreadsheet) ->
                    SheetLoader.loadNotes(GlobalData.account, notebookName, spreadsheetId, spreadsheetName, sheetName, sheetId, bindSpreadsheet, 30)));
            dialog.show();
        }
    }

    private void updateSpreadsheetNames(@Nullable ArrayList<Notebook> notebooks) {
        if (notebooks == null || notebooks.isEmpty()) {
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
            for (Notebook notebook : notebooks) {
                String spreadsheetName;
                if (notebook.hasOwner()) {
                    spreadsheetName = map.get(notebook.spreadsheetId);
                    if (spreadsheetName == null || spreadsheetName.isEmpty()) {
                        if (notebook.spreadsheetName != null && !notebook.spreadsheetName.isEmpty()) {
                            continue; // Spreadsheet name is already existed and we do not need to change it to default because notebook has owner.
                        }
                        spreadsheetName = defaultSpreadsheetName;
                    }
                } else {
                    spreadsheetName = null;
                }
                if (spreadsheetName == null) {
                    if (notebook.spreadsheetName == null) {
                        continue; // Nothing to update
                    }
                    // Need to update
                } else if (spreadsheetName.equals(notebook.spreadsheetName)) {
                    continue; // Nothing to update
                }
                DictNoteSpreadsheetNamesUpdater namesUpdater = new DictNoteSpreadsheetNamesUpdater(notebook.getId(), spreadsheetName);
                updaters.add(namesUpdater);
            }
            if (!updaters.isEmpty()) {
                model.getNotebooksRepository().updateSpreadsheetNames(updaters);
            }
        });
    }

    private void observerSetup() {
        model.getNotebooksRepository().getAllNotebooks().observe(getViewLifecycleOwner(),
                notebooks -> {
                    ArrayList<Notebook> list = (ArrayList<Notebook>) notebooks;
                    if (!updatedSpreadsheetNames) {
                        updatedSpreadsheetNames = true;
                        updateSpreadsheetNames(list);
                    }
                    if (!isUpdated) {
                        isUpdated = true;
                        ArrayList<SheetDatesUpdate> toUpdate = SheetDataUpdater.updateDates(new ArrayList<>(list));
                        if (!toUpdate.isEmpty()) {
                            model.getNotebooksRepository().updateDateSeveral(toUpdate);
                        } else {
                            updateCounter(adapter.setItems(list));
                        }
                    } else {
                        updateCounter(adapter.setItems(list));
                    }
                });
    }
}