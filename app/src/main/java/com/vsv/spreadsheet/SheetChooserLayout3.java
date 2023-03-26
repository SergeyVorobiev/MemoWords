package com.vsv.spreadsheet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;

import com.vsv.db.entities.SpreadSheetInfo;
import com.vsv.memorizer.R;
import com.vsv.memorizer.adapters.RecyclerChooseSpreadsheetsAdapter;
import com.vsv.utils.StaticUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class SheetChooserLayout3 implements SearchView.OnQueryTextListener {

    private final Context context;

    private final RecyclerChooseSpreadsheetsAdapter sheetAdapter;

    private final SearchView searchView;

    private SpreadSheetInfo chosenSheet;

    private final Observer<List<SpreadSheetInfo>> sheetObserver;

    private final ViewGroup chosenItemsLayout;

    private final View sheetChooseContainer;

    private final LiveData<List<SpreadSheetInfo>> spreadSheetInfoLiveData;

    private final RecyclerView sheetTabView;

    private View currentSheetTab;

    @SuppressLint("ClickableViewAccessibility")
    public SheetChooserLayout3(Context context, View layoutParent, int excType) {
        this.context = context;
        sheetAdapter = new RecyclerChooseSpreadsheetsAdapter(this::clickOnSpreadsheet, excType);
        searchView = layoutParent.findViewById(R.id.sheetTabSearch);
        chosenItemsLayout = layoutParent.findViewById(R.id.chosenItemsLayout);
        sheetChooseContainer = layoutParent.findViewById(R.id.sheetChooserContent);
        sheetTabView = layoutParent.findViewById(R.id.sheetTabChooser);

        // To prevent invoking keyboard without focusing a view (because google developers are donkeys).
        searchView.setOnTouchListener(this::onSearchTouch);
        searchView.setOnQueryTextListener(this);
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

    private void loadErrorLayout(String message) {
        searchView.setVisibility(View.GONE);
    }

    private void clickOnSpreadsheet(View view) {
        setupChosenSheet(sheetAdapter.getItem((int) view.getTag()));
        searchView.setVisibility(View.GONE);
    }

    private void setupChosenSheet(SpreadSheetInfo sheet) {
        chosenSheet = sheet;
        resetSearch();
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
        sheetAdapter.applyFilter(newText);
        return false;
    }

    // To prevent invoking keyboard without focusing a view (because google developers are donkeys).
    private boolean onSearchTouch(View view, MotionEvent event) {
        return true;
    }

    private void cancelSpreadsheet(View view) {
        chosenSheet = null;
        resetSearch();
        sheetTabView.setVisibility(View.VISIBLE);
        sheetTabView.setAdapter(sheetAdapter);
        searchView.setVisibility(View.VISIBLE);
        chosenItemsLayout.removeAllViews();
    }

    private void resetSearch() {
        searchView.setQuery("", false);
        searchView.clearFocus();
        searchView.setIconified(true);
        sheetAdapter.applyFilter("");
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

    public void hideLayout() {
        sheetChooseContainer.setVisibility(View.GONE);
    }
}
