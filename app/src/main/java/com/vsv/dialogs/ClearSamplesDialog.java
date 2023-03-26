package com.vsv.dialogs;

import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;

import com.vsv.memorizer.R;
import com.vsv.toasts.Toasts;

import java.util.function.Consumer;

public class ClearSamplesDialog extends SingleCustomDialog {

    public static class ClearSettings {

        public final boolean isLeft;

        public final boolean clearExamples;

        public final boolean clearKinds;

        ClearSettings(boolean isLeft, boolean clearExamples, boolean clearKinds) {
            this.isLeft = isLeft;
            this.clearExamples = clearExamples;
            this.clearKinds = clearKinds;
        }
    }

    private CheckBox confirm;

    private CheckBox clearExamples;

    private CheckBox clearKinds;

    private View clear;

    // first - left / right values, second - clear examples or not.
    private Consumer<ClearSettings> clearListener;

    private RadioButton left;

    private RadioButton right;

    public ClearSamplesDialog() {
        super(R.layout.dialog_clear_samples, false, true);
        clearListener = (settings) -> {
        };
    }

    private void onClickCancel(View view) {
        dialog.cancel();
    }

    @Override
    public void setupViews(View dialogView) {
        confirm = dialogView.findViewById(R.id.clearConfirm);
        clear = dialogView.findViewById(R.id.clear);
        clearExamples = dialogView.findViewById(R.id.clearExamples);
        clearKinds = dialogView.findViewById(R.id.clearKinds);
        left = dialogView.findViewById(R.id.clearLeft);
        right = dialogView.findViewById(R.id.clearRight);
    }

    @Override
    public void setupViewListeners(View dialogView) {
        clear.setOnClickListener(this::onClickOk);
        dialogView.findViewById(R.id.cancel).setOnClickListener(this::onClickCancel);
        confirm.setOnCheckedChangeListener((buttonView, isChecked) -> {
            clear.setClickable(isChecked);
            clear.setActivated(isChecked);
        });
    }

    @Override
    public void setupViewAdjustments(View dialogView) {
        clear.setClickable(false);
        clear.setActivated(false);
    }

    public void setOnClearListener(Consumer<ClearSettings> clearListener) {
        this.clearListener = clearListener;
    }

    private void onClickOk(View view) {
        if (!left.isChecked() && !right.isChecked()) {
            Toasts.shortShow(R.string.toast_please_choose_column);
            return;
        }
        clearListener.accept(new ClearSettings(left.isChecked(), clearExamples.isChecked(), clearKinds.isChecked()));
        dialog.cancel();
    }
}

