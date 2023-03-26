package com.vsv.dialogs;

import android.view.View;
import android.widget.RadioButton;

import androidx.annotation.NonNull;

import com.vsv.db.entities.Dictionary;
import com.vsv.memorizer.R;

import java.util.function.Consumer;

public class SortByDialog extends SingleCustomDialog {

    private View ok;

    private Consumer<Integer> okConsumer = (value) -> {
    };

    private RadioButton byDateButton;

    private RadioButton byKindButton;

    private RadioButton byLeftButton;

    private RadioButton byRightButton;

    private int sortedType;

    public SortByDialog(int sortedType) {
        super(R.layout.dialog_sort_by, false, true);
        this.sortedType = sortedType;
        setChecked();
    }

    private void setChecked() {
        switch (sortedType) {
            case Dictionary.SORTED_BY_KIND:
                byKindButton.setChecked(true);
                break;
            case Dictionary.SORTED_BY_LEFT:
                byLeftButton.setChecked(true);
                break;
            case Dictionary.SORTED_BY_RIGHT:
                byRightButton.setChecked(true);
                break;
            default:
                byDateButton.setChecked(true);
        }
    }

    @Override
    public void setupViews(View dialogView) {
        ok = dialogView.findViewById(R.id.sortByOk);
        byDateButton = dialogView.findViewById(R.id.sortByDate);
        byKindButton = dialogView.findViewById(R.id.sortByKind);
        byLeftButton = dialogView.findViewById(R.id.sortByLeft);
        byRightButton = dialogView.findViewById(R.id.sortByRight);
    }

    @Override
    public void setupViewListeners(View dialogView) {
        ok.setOnClickListener((view) -> {
            okConsumer.accept(sortedType);
            cancel();
        });
        byDateButton.setOnCheckedChangeListener((button, isChecked) -> {
            if (isChecked) {
                sortedType = Dictionary.SORTED_BY_DATE;
            }
        });
        byKindButton.setOnCheckedChangeListener((button, isChecked) -> {
            if (isChecked) {
                sortedType = Dictionary.SORTED_BY_KIND;
            }
        });
        byLeftButton.setOnCheckedChangeListener((button, isChecked) -> {
            if (isChecked) {
                sortedType = Dictionary.SORTED_BY_LEFT;
            }
        });
        byRightButton.setOnCheckedChangeListener((button, isChecked) -> {
            if (isChecked) {
                sortedType = Dictionary.SORTED_BY_RIGHT;
            }
        });
    }

    @Override
    public void setupViewAdjustments(View dialogView) {

    }

    public void setOnOkListener(@NonNull Consumer<Integer> consumer) {
        this.okConsumer = consumer;
    }
}
