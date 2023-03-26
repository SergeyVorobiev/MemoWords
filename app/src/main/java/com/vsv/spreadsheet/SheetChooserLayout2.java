package com.vsv.spreadsheet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.vsv.db.entities.SpreadSheetInfo;
import com.vsv.dialogs.WaitDialog;
import com.vsv.dialogs.entities.BackgroundTask;
import com.vsv.dialogs.entities.SheetTab;
import com.vsv.memorizer.R;
import com.vsv.memorizer.adapters.RecyclerChooseSheetsAdapter;
import com.vsv.memorizer.adapters.RecyclerChooseSpreadsheetsAdapter;
import com.vsv.memorizer.fragments.SheetsFragment;
import com.vsv.statics.GlobalData;
import com.vsv.utils.GoogleTasksExceptionHandler;
import com.vsv.utils.Spec;
import com.vsv.utils.StaticUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class SheetChooserLayout2 implements SearchView.OnQueryTextListener {

    private final Context context;

    private final RecyclerChooseSpreadsheetsAdapter sheetAdapter;

    private final RecyclerChooseSheetsAdapter tabAdapter;

    private final SearchView searchView;

    private SpreadSheetInfo chosenSheet;

    private final Observer<List<SpreadSheetInfo>> sheetObserver;

    private final ViewGroup errorLayout;

    private final TextView errorMessage;

    private final RecyclerView sheetTabView;

    private final ViewGroup chosenItemsLayout;

    private final View sheetChooseContainer;

    private int currentAdapter = 0;

    private final LiveData<List<SpreadSheetInfo>> spreadSheetInfoLiveData;

    private View currentSheetTab;

    @SuppressLint("ClickableViewAccessibility")
    public SheetChooserLayout2(Context context, View layoutParent, int excType) {
        this.context = context;
        sheetAdapter = new RecyclerChooseSpreadsheetsAdapter(this::clickOnSpreadsheet, excType);
        tabAdapter = new RecyclerChooseSheetsAdapter(this::clickOnSheet);
        searchView = layoutParent.findViewById(R.id.sheetTabSearch);
        errorLayout = layoutParent.findViewById(R.id.errorLayout);
        errorMessage = layoutParent.findViewById(R.id.errorMessage);
        sheetTabView = layoutParent.findViewById(R.id.sheetTabChooser);
        chosenItemsLayout = layoutParent.findViewById(R.id.chosenItemsLayout);
        sheetChooseContainer = layoutParent.findViewById(R.id.sheetChooserContent);

        // To prevent invoking keyboard without focusing a view (because google developers are donkeys).
        searchView.setOnTouchListener(this::onSearchTouch);
        searchView.setOnQueryTextListener(this);
        layoutParent.findViewById(R.id.retryButton).setOnClickListener(this::retry);
        errorLayout.setVisibility(View.GONE);
        sheetChooseContainer.setVisibility(View.GONE);
        AtomicBoolean firstLoad = new AtomicBoolean(true);
        sheetObserver = (sheets) -> {
            firstLoad.set(false);
            sheetAdapter.setItems((ArrayList<SpreadSheetInfo>) sheets);
        };
        spreadSheetInfoLiveData = StaticUtils.getModel().getSpreadsheetsRepository().getAllLive();
        spreadSheetInfoLiveData.observeForever(sheetObserver);
        sheetTabView.setAdapter(sheetAdapter);
    }

    private void loadSheetTabs() {
        Callable<ArrayList<Pair<String, Integer>>> callable = () -> {
            ArrayList<Pair<String, Integer>> titles = new ArrayList<>();
            Sheets sheetsService = SheetsBuilder.buildSheetsService(GlobalData.account);
            Spreadsheet spreadsheet = sheetsService.spreadsheets().get(chosenSheet.spreadSheetId).execute();
            for (Sheet sheet : spreadsheet.getSheets()) {
                titles.add(new Pair<>(sheet.getProperties().getTitle(), sheet.getProperties().getSheetId()));
            }
            return titles;
        };
        BackgroundTask<ArrayList<Pair<String, Integer>>> task = new BackgroundTask<>(10, callable);
        task.setRunMainThreadOnFail((e) -> loadErrorLayout(GoogleTasksExceptionHandler.handle(e)));
        task.setRunMainThreadOnSuccess((titles) -> {
            titles = titles == null ? new ArrayList<>() : titles;
            int tabsCount = titles.size();
            if (tabsCount > SheetsFragment.MAX_SHEETS) {
                titles = (ArrayList<Pair<String, Integer>>) titles.stream().limit(SheetsFragment.MAX_SHEETS)
                        .collect(Collectors.toList());
            }
            if (tabsCount == 0) {
                loadErrorLayout(StaticUtils.getString(R.string.no_sheet));
            } else {
                sheetTabView.setAdapter(tabAdapter);
                ArrayList<SheetTab> tabs = new ArrayList<>();
                for (Pair<String, Integer> title : titles) {
                    SheetTab spreadSheetTabName = new SheetTab(title.first, title.second);
                    spreadSheetTabName.setChecked(true);
                    tabs.add(spreadSheetTabName);
                }
                sheetTabView.setVisibility(View.VISIBLE);
                errorLayout.setVisibility(View.GONE);
                tabAdapter.setItems(tabs);
                boolean result = false;
            }
        });
        WaitDialog dialog = new WaitDialog(context, task);
        dialog.showOver();
    }

    private void loadErrorLayout(String message) {
        sheetTabView.setVisibility(View.GONE);
        searchView.setVisibility(View.GONE);
        errorMessage.setText(message);
        errorLayout.setVisibility(View.VISIBLE);
    }

    private void clickOnSpreadsheet(View view) {
        setupChosenSheet(sheetAdapter.getItem((int) view.getTag()));
        searchView.setVisibility(View.GONE);
        loadSheetTabs();
    }

    private void setupChosenSheet(SpreadSheetInfo sheet) {
        chosenSheet = sheet;
        resetSearch();
        currentAdapter = 1;
        sheetTabView.setAdapter(null);
        View sheetItem = LayoutInflater.from(context).inflate(R.layout.item_chosen_spreadsheet, chosenItemsLayout, false);
        TextView nameView = sheetItem.findViewById(R.id.spreadsheetName);
        TextView idView = sheetItem.findViewById(R.id.spreadsheetId);
        View cancelSheet = sheetItem.findViewById(R.id.cancelSheet);
        cancelSheet.setOnClickListener(this::cancelSpreadsheet);
        nameView.setText(sheet.name);
        idView.setText(sheet.spreadSheetId);
        chosenItemsLayout.addView(sheetItem);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        searchView.clearFocus();
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (currentAdapter == 0) {
            sheetAdapter.applyFilter(newText);
        } else {
            tabAdapter.applyFilter(newText);
        }
        return false;
    }

    // To prevent invoking keyboard without focusing a view (because google developers are donkeys).
    private boolean onSearchTouch(View view, MotionEvent event) {
        return true;
    }

    private void retry(View view) {
        loadSheetTabs();
    }

    private void clickOnSheet(View view) {
        int position = (int) view.getTag();
        SheetTab tabName = tabAdapter.getItem(position);
        tabName.setChecked(!tabName.isChecked());
        tabAdapter.notifyItemChanged(position);
    }

    private void cancelSpreadsheet(View view) {
        chosenSheet = null;
        resetSearch();
        searchView.setVisibility(View.VISIBLE);
        sheetTabView.setVisibility(View.VISIBLE);
        errorLayout.setVisibility(View.GONE);
        chosenItemsLayout.removeAllViews();
        currentAdapter = 0;
        sheetTabView.setAdapter(sheetAdapter);
    }

    private void resetSearch() {
        searchView.setQuery("", false);
        searchView.clearFocus();
        searchView.setIconified(true);
        sheetAdapter.applyFilter("");
        tabAdapter.applyFilter("");
    }

    public void setVisibility(int visibility) {
        sheetChooseContainer.setVisibility(visibility);
    }

    public void removeObservers() {
        spreadSheetInfoLiveData.removeObserver(sheetObserver);
    }

    @Nullable
    public SpreadSheetInfo getChosenSheet() {
        return chosenSheet;
    }

    @NonNull
    public ArrayList<SheetTab> getChosenTabs() {
        ArrayList<SheetTab> result = new ArrayList<>();
        ArrayList<SheetTab> items = tabAdapter.getItems();
        if (items != null) {
            int i = 0;
            for(SheetTab item : items) {
                if (item.isChecked()) {
                    result.add(item);
                    i++;
                    if (i == Spec.MAX_DICTIONARIES) {
                        break;
                    }
                }
            }
        }
        return result;
    }

    public void hideLayout() {
        sheetChooseContainer.setVisibility(View.GONE);
    }
}
