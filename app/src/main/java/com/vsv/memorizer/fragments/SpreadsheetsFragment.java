package com.vsv.memorizer.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.vsv.bundle.entities.ShelfBundle;
import com.vsv.db.entities.Settings;
import com.vsv.db.entities.SpreadSheetInfo;
import com.vsv.dialogs.SSHelpBottomDialog;
import com.vsv.dialogs.NewSpreadsheetDialog;

import com.vsv.dialogs.SSPresetDialog;
import com.vsv.dialogs.SaveLoadSpreadsheetsDialog;
import com.vsv.memorizer.MainActivity;
import com.vsv.memorizer.R;
import com.vsv.memorizer.adapters.RecyclerSpreadsheetsAdapter;
import com.vsv.models.MainModel;
import com.vsv.overflowmenu.OverflowMenu;
import com.vsv.recyclerentities.SpreadsheetRecyclerList;
import com.vsv.statics.GlobalData;
import com.vsv.statics.WeakContext;
import com.vsv.toasts.Toasts;
import com.vsv.utils.Spec;
import com.vsv.utils.StaticUtils;

import java.util.ArrayList;

public class SpreadsheetsFragment extends Fragment {

    private RecyclerSpreadsheetsAdapter adapter;

    private final MainModel model;

    @Nullable
    private ShelfBundle shelfBundle;

    public SpreadsheetsFragment() {
        model = StaticUtils.getModel();
    }

    private SpreadsheetRecyclerList list;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            shelfBundle = ShelfBundle.fromBundleOrNull(bundle);
        }
        if (savedInstanceState != null) {
            shelfBundle = ShelfBundle.fromBundleOrNull(savedInstanceState);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_spreadsheets, container, false);
        view.setBackground(GlobalData.bg_spreadsheet);
        return view;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FloatingActionButton fab = view.findViewById(R.id.addSpreadsheet);
        fab.setOnClickListener(this::showNewSheetDialog);
        list = SpreadsheetRecyclerList.getList();
        list.setData(shelfBundle);
        list.attachTo(view.findViewById(R.id.spreadsheetContainer));
        adapter = list.getAdapter();
        onCreateOptionsMenu();
        observerSetup();
    }

    public void onCreateOptionsMenu() {
        OverflowMenu.setTitle(R.string.app_header);
        OverflowMenu.setupSearchView(this::onQueryTextChange, GlobalData.spreadsheetQuery);
        OverflowMenu.hideBackButton();
        // OverflowMenu.setupBackButton(this::navigateToFragment);
        int id = GlobalData.getSettings().sortSheets ? R.string.action_sort_by_date : R.string.action_sort_by_name;
        OverflowMenu.addMenuItem(id, this::menuClickSort);
        OverflowMenu.addMenuItem(R.string.action_spreadsheet_save_load, this::openSaveLoadSpreadsheetsDialog);
        OverflowMenu.addMenuItem(R.string.action_spreadsheet_presets, this::openPresetsDialog);
        OverflowMenu.addMenuItem(R.string.action_spreadsheet_help, this::openHelpDialog);
    }

    private void openSaveLoadSpreadsheetsDialog(@Nullable View view) {
        if (GlobalData.getAccountOrToast() == null) {
            return;
        }
        SaveLoadSpreadsheetsDialog dialog = new SaveLoadSpreadsheetsDialog();
        dialog.show();
    }

    private void openPresetsDialog(@Nullable View view) {
        SSPresetDialog dialog = new SSPresetDialog(adapter.copyAll());
        dialog.show();
    }

    private void openHelpDialog(@Nullable View view) {
        SSHelpBottomDialog dialog = new SSHelpBottomDialog();
        dialog.show();
    }

    public void onResume() {
        WeakContext.getMainActivity().setTab(MainActivity.SPREADSHEETS_TAB);
        super.onResume();
    }

    private void menuClickSort(@NonNull View view) {
        Settings settings = GlobalData.getSettings();
        settings.sortSheets = !settings.sortSheets;
        TextView tv = view.findViewById(R.id.menuItemName);
        tv.setText(settings.sortSheets ? R.string.action_sort_by_date : R.string.action_sort_by_name);
        StaticUtils.updateSettings();
        adapter.update();
    }

    @Override
    public void onDetach() {
        list.detach();
        list.clearData();
        super.onDetach();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle bundle) {
        if (shelfBundle != null) {
            shelfBundle.toBundle(bundle);
        }
    }

    private void navigateToFragment(View view) {
        Bundle bundle = null;
        if (shelfBundle != null) {
            bundle = shelfBundle.toNewBundle();
        }
        StaticUtils.navigateSafe(GlobalData.lastFragmentId, bundle);
    }

    private void onQueryTextChange(String query) {
        adapter.applyFilter(query);
        updateCounter();
    }

    private void updateCounter() {
        WeakContext.getMainActivity().setTopCount(adapter.getItemCount(), Spec.MAX_SPREADSHEETS);
    }

    private void showNewSheetDialog(View view) {
        if (adapter.getAllCount() >= Spec.MAX_SPREADSHEETS) {
            Toasts.maxItemsMessage(getResources().getString(R.string.spreadsheets));
        } else {
            NewSpreadsheetDialog dialog = new NewSpreadsheetDialog(adapter.copyAll());
            dialog.setCreateSheetListener((name, id, type) -> model.getSpreadsheetsRepository().insert(id, name, type));
            dialog.show();
        }
    }

    private void observerSetup() {
        model.getSpreadsheetsRepository().getAllLive().observe(getViewLifecycleOwner(),
                sheets -> {
                    adapter.setItems((ArrayList<SpreadSheetInfo>) sheets);
                    updateCounter();
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}