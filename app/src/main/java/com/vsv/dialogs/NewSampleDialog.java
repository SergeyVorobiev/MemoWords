package com.vsv.dialogs;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.vsv.dialogs.listeners.SampleCreateListener;
import com.vsv.memorizer.R;
import com.vsv.toasts.Toasts;

public class NewSampleDialog extends SingleCustomDialog {

    private final TextView leftInput;

    private final TextView rightInput;

    private final TextView kindInput;

    private final TextView exampleInput;

    private SampleCreateListener listener;

    private final Animation rotate;

    public NewSampleDialog() {
        super(R.layout.dialog_new_sample, false, true);
        TextView header = dialogView.findViewById(R.id.newSampleDialogHeader);
        header.setText(R.string.new_sample_dialog_header);
        dialogView.findViewById(R.id.swapSamples).setOnClickListener(this::swap);
        View ok = dialogView.findViewById(R.id.newSampleOk);
        View cancel = dialogView.findViewById(R.id.newSampleCancel);
        leftInput = dialogView.findViewById(R.id.newSampleLeftInput);
        showKeyboardFor(leftInput);
        rightInput = dialogView.findViewById(R.id.newSampleRightInput);
        kindInput = dialogView.findViewById(R.id.newSampleKind);
        exampleInput = dialogView.findViewById(R.id.newSampleExample);
        ok.setOnClickListener(this::onClickOk);
        cancel.setOnClickListener(this::onClickCancel);
        rotate = AnimationUtils.loadAnimation(context, R.anim.rotate);
    }

    private void swap(View view) {
        view.startAnimation(rotate);
        CharSequence text1 = leftInput.getText();
        CharSequence text2 = rightInput.getText();
        leftInput.setText(text2);
        rightInput.setText(text1);
    }

    private void onClickOk(View view) {
        String leftValue = leftInput.getText().toString().trim();
        String rightValue = rightInput.getText().toString().trim();
        String kind = kindInput.getText().toString().trim();
        String example = exampleInput.getText().toString().trim();
        if (leftValue.isEmpty()) {
            leftValue = "?";
            // Toasts.sampleValueEmpty();
        }
        if (rightValue.isEmpty()) {
            // Toasts.sampleValueEmpty();
            rightValue = "?";
        }
        if (listener != null) {
            listener.createSample(leftValue, rightValue, kind, example, false, 0);
        }
        dialog.cancel();

    }

    public void sampleCreateListener(SampleCreateListener listener) {
        this.listener = listener;
    }

    private void onClickCancel(View view) {
        dialog.cancel();
    }

    @Override
    public void setupViews(View dialogView) {

    }

    @Override
    public void setupViewListeners(View dialogView) {

    }

    @Override
    public void setupViewAdjustments(View dialogView) {

    }
}
