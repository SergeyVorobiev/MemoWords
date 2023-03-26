package com.vsv.memorizer.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetResponse;
import com.vsv.bundle.entities.ShelfBundle;
import com.vsv.db.entities.Settings;
import com.vsv.dialogs.NewSheetDialog;
import com.vsv.dialogs.UpdateSheetDialog;
import com.vsv.dialogs.WaitDialog;
import com.vsv.dialogs.entities.BackgroundTask;
import com.vsv.dialogs.entities.SheetTab;
import com.vsv.memorizer.R;

import com.vsv.memorizer.adapters.RecyclerSheetsAdapter;
import com.vsv.overflowmenu.OverflowMenu;
import com.vsv.removeitems.SheetRemover;
import com.vsv.spreadsheet.SheetLoader;
import com.vsv.spreadsheet.SheetUpdater;
import com.vsv.bundle.helpers.BundleNames;
import com.vsv.statics.GlobalData;
import com.vsv.statics.WeakContext;
import com.vsv.toasts.Toasts;
import com.vsv.utils.GoogleTasksExceptionHandler;
import com.vsv.utils.Spec;
import com.vsv.utils.StaticUtils;
import com.vsv.utils.Timer;
import com.vsv.viewutils.StopVerticalScrollAnimator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;

public class SheetsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private TextView counterView;

    public static final int MAX_SHEETS = 500;

    private SwipeRefreshLayout refreshLayout;

    private TextView errorMessage;

    private RecyclerSheetsAdapter adapter;

    private String sheetName = "";

    private String spreadsheetId = null;

    private String loginMessage;

    private @Nullable
    ShelfBundle shelfBundle;

    private boolean dialogShown;

    private long lastRefreshTime;

    private static final int REFRESH_TIMEOUT = 5; // in sec.

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lastRefreshTime = System.nanoTime();
        Bundle bundle = getArguments();
        if (bundle != null) {
            shelfBundle = ShelfBundle.fromBundleOrNull(bundle);
        }
        if (savedInstanceState != null) {
            this.spreadsheetId = savedInstanceState.getString(BundleNames.SHEET_WEB_ID, null);
            this.sheetName = savedInstanceState.getString(BundleNames.SHEET_NAME, "");
            shelfBundle = ShelfBundle.fromBundleOrNull(savedInstanceState);
        }
        loginMessage = getResources().getString(R.string.login_to_see_sheets);
        loadSheetId();
        WeakContext.getMainActivity().hideTabs();
    }

    @Override
    public void onRefresh() {
        float pastTime = Timer.nanoTimeDiffFromNowInSeconds(lastRefreshTime);
        if (pastTime < REFRESH_TIMEOUT) {
            Toasts.secondsRemain(REFRESH_TIMEOUT - pastTime);
            refreshLayout.setRefreshing(false);
            return;
        }
        lastRefreshTime = System.nanoTime();
        adapter.setItems(new ArrayList<>());
        dismissErrorMessage();
        if (GlobalData.account == null) {
            showBar(loginMessage);
            refreshLayout.setRefreshing(false);
        } else {
            this.loadTabs();
        }
        updateCounter();
    }

    private void showBar(String message) {
        showText(message);
    }

    private void showText(String message) {
        errorMessage.setText(message);
        errorMessage.setVisibility(View.VISIBLE);
    }

    private void dismissErrorMessage() {
        errorMessage.setVisibility(View.GONE);
        errorMessage.setText("");
    }

    private BackgroundTask<ArrayList<SheetTab>> prepareLoadTabsTask() {
        BackgroundTask<ArrayList<SheetTab>> task = SheetLoader.loadSheetTabs(spreadsheetId);
        task.setRunMainThreadOnFail((e) -> {
            refreshLayout.setRefreshing(false);
            adapter.setItems(null);
            updateCounter();
            showBar(GoogleTasksExceptionHandler.handle(e));
        });
        task.setRunMainThreadOnSuccess((tabs) -> {
            tabs = tabs == null ? new ArrayList<>() : tabs;
            int tabsCount = tabs.size();
            if (tabsCount > MAX_SHEETS) {
                tabs = (ArrayList<SheetTab>) tabs.stream().limit(MAX_SHEETS).collect(Collectors.toList());
                tabsCount = tabs.size();
            }
            if (tabsCount == 0) {
                showBar(StaticUtils.getString(R.string.no_sheet));
            } else {
                dismissErrorMessage();
            }
            refreshLayout.setRefreshing(false);
            adapter.setItems(tabs);
            updateCounter();

        });
        return task;
    }

    private void loadTabs() {
        WaitDialog dialog = new WaitDialog(this.requireContext(), prepareLoadTabsTask());
        dialog.showOver();
    }

    private void loadSheetId() {
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            String id = bundle.getString(BundleNames.SHEET_WEB_ID, null);
            String name = bundle.getString(BundleNames.SHEET_NAME, "");
            if (id != null) {
                spreadsheetId = id;
            }
            if (!name.isEmpty()) {
                sheetName = name;
            }
        }
        if (spreadsheetId == null) {
            throw new RuntimeException("Sheet id cannot be null");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        bundle.putString(BundleNames.SHEET_WEB_ID, spreadsheetId);
        bundle.putString(BundleNames.SHEET_NAME, sheetName);
        if (shelfBundle != null) {
            shelfBundle.toBundle(bundle);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sheets, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        counterView = view.findViewById(R.id.sheetCounter);
        ((TextView) view.findViewById(R.id.spreadsheetName)).setText(sheetName);
        ((TextView) view.findViewById(R.id.spreadsheetId)).setText(spreadsheetId);
        refreshLayout = view.findViewById(R.id.refreshSheetList);
        RecyclerView listSheets = view.findViewById(R.id.listSheets);
        view.findViewById(R.id.addSheet).setOnClickListener(this::showNewSheetTabDialog);
        errorMessage = view.findViewById(R.id.errorMessage);
        errorMessage.setVisibility(View.GONE);
        adapter = new RecyclerSheetsAdapter(this::updateTab);
        refreshLayout.setOnRefreshListener(this);
        listSheets.setAdapter(adapter);
        StopVerticalScrollAnimator.setRecycleViewAnimation(listSheets, Spec.MIN_SCROLL_HIT, R.anim.from_bottom2, R.anim.from_top);
        counterView.setText(String.format(getString(R.string.empty_counter), MAX_SHEETS));
        updateCounter();
        new SheetRemover(listSheets, spreadsheetId, this::deleteTab);
        if (GlobalData.account == null) {
            showBar(loginMessage);
        } else {
            this.loadTabs();
        }
        onCreateOptionsMenu();
    }

    public void onCreateOptionsMenu() {
        OverflowMenu.setTitle(R.string.sheets_fragment_title);
        OverflowMenu.setupBackButton(this::navigateToSpreadsheets);
        OverflowMenu.setupSearchView(this::onQueryTextChange, GlobalData.spreadsheetTabQuery);
        int id = GlobalData.getSettings().sortTabs ? R.string.action_sort_origin : R.string.action_sort_by_name;
        OverflowMenu.addMenuItem(id, this::menuClickSort);
        OverflowMenu.showAccount();
    }

    private void menuClickSort(@NonNull View view) {
        Settings settings = GlobalData.getSettings();
        settings.sortTabs = !settings.sortTabs;
        TextView tv = view.findViewById(R.id.menuItemName);
        tv.setText(settings.sortTabs ? R.string.action_sort_origin : R.string.action_sort_by_name);
        StaticUtils.updateSettings();
        adapter.update();
    }

    private void navigateToSpreadsheets(@Nullable View view) {
        Bundle bundle = null;
        if (shelfBundle != null) {
            bundle = shelfBundle.toNewBundle();
        }
        StaticUtils.navigateSafe(R.id.action_Sheets_to_Spreadsheets, bundle);
    }

    private void updateCounter() {
        String counter = getString(R.string.counter);
        counterView.setText(String.format(counter, adapter.getItemCount(), MAX_SHEETS));
    }

    @SuppressLint("NotifyDataSetChanged")
    private void deleteTab(BackgroundTask<BatchUpdateSpreadsheetResponse> deleteTask) {
        Object position = deleteTask.getExtraData();
        deleteTask.setRunMainThreadOnFail((e) -> {
            Toasts.shortShowRaw(GoogleTasksExceptionHandler.handle(e));
            if (position == null) {
                adapter.notifyDataSetChanged();
            } else {
                adapter.notifyItemChanged((int) position);
            }
        });
        deleteTask.setRunMainThreadOnSuccess((r) -> Toasts.success());
        BackgroundTask<?> loadTabsTask = prepareLoadTabsTask();
        BackgroundTask.buildMultiTasksWaitDialog(this.requireContext(), Arrays.asList(deleteTask, loadTabsTask)).showOver();
    }

    private boolean updateTab(View view) {
        if (GlobalData.getAccountOrToast() == null) {
            return true;
        }
        int position = (int) view.getTag();
        SheetTab tab = adapter.getItem(position);
        long sheetTabId = tab.getId();
        if (GlobalData.getAccountOrToast() != null) {
            UpdateSheetDialog dialog = new UpdateSheetDialog(tab.getTitle());
            dialog.setCreateSheetTabListener((tabName) -> {
                GoogleSignInAccount account = GlobalData.getAccountOrToast();
                if (account != null) {
                    BackgroundTask<?> updateSheet = SheetUpdater.updateTabTask(account, spreadsheetId, sheetTabId, tabName);
                    updateSheet.setRunMainThreadOnFail((e) -> Toasts.longShowRaw(GoogleTasksExceptionHandler.handle(e)));
                    updateSheet.setRunMainThreadOnSuccess((r) -> Toasts.success());
                    BackgroundTask<?> updateScreen = prepareLoadTabsTask();
                    BackgroundTask.buildMultiTasksWaitDialog(this.requireContext(), Arrays.asList(updateSheet, updateScreen)).showOver();
                }
            });
            dialog.show();
        }
        return true;
    }

    private void showNewSheetTabDialog(View view) {
        if (adapter.getItemCount() >= MAX_SHEETS) {
            Toasts.maxItemsMessage(getResources().getString(R.string.sheets));
        } else if (GlobalData.getAccountOrToast() != null && !dialogShown) {
            NewSheetDialog dialog = new NewSheetDialog();
            dialog.setCreateSheetTabListener((tabName) -> {
                GoogleSignInAccount account = GlobalData.getAccountOrToast();
                if (account != null) {
                    BackgroundTask<?> addTask = SheetUpdater.addTabTask(account, spreadsheetId, tabName);
                    addTask.setRunMainThreadOnFail((e) -> Toasts.longShowRaw(GoogleTasksExceptionHandler.handle(e)));
                    addTask.setRunMainThreadOnSuccess((r) -> Toasts.success());
                    BackgroundTask<?> updateTask = prepareLoadTabsTask();
                    BackgroundTask.buildMultiTasksWaitDialog(this.requireContext(), Arrays.asList(addTask, updateTask)).showOver();
                }
            });
            dialog.setOnDismissListener((dialogInterface) -> dialogShown = false);
            dialogShown = true;
            dialog.show();
        }
    }

    @Override
    public void onStart() {
        WeakContext.getMainActivity().hideTabs();
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void onQueryTextChange(String query) {
        adapter.applyFilter(query);
        updateCounter();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
