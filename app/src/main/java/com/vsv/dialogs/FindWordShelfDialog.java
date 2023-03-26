package com.vsv.dialogs;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.SwitchCompat;

import com.vsv.bundle.entities.ShelfBundle;
import com.vsv.db.entities.Dictionary;
import com.vsv.dialogs.entities.ShelfWordFinder;
import com.vsv.memorizer.R;
import com.vsv.models.MainModel;

import java.util.ArrayList;

public class FindWordShelfDialog extends SingleCustomDialog implements SearchView.OnQueryTextListener {

    private SearchView search;

    private final ShelfWordFinder finder;

    public FindWordShelfDialog(@Nullable ArrayList<Dictionary> dictionaries,
                               @NonNull MainModel model, @NonNull ShelfBundle shelfBundle) {
        super(R.layout.dialog_shelf_find_word, false, true);
        finder = new ShelfWordFinder(dialogView, model, dictionaries, shelfBundle);
        finder.setOnTransitionListener(dialog::cancel);
        finder.start();
    }

    @Override
    public void setupViews(View dialogView) {
        search = dialogView.findViewById(R.id.searchWord);
    }

    @Override
    public void setupViewListeners(View dialogView) {
        search.setOnQueryTextListener(this);
        dialogView.findViewById(R.id.cancelFindWordButton).setOnClickListener((v) -> dialog.cancel());
        dialog.setOnDismissListener(this::close);
        SwitchCompat onlyStartSwitcher = dialogView.findViewById(R.id.onlyStartSwitcher);
        onlyStartSwitcher.setOnCheckedChangeListener((button, state) -> finder.findWayChanged(state));
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void setupViewAdjustments(View dialogView) {

        // To prevent invoking keyboard without focusing a view (because google developers are donkeys).
        search.setOnTouchListener((a, b) -> true);
        search.setIconified(false);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        search.clearFocus();
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        finder.updateQuery(newText);
        return true;
    }

    private void close(DialogInterface dialogInterface) {
        finder.close();
    }
}
