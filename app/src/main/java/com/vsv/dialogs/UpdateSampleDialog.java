package com.vsv.dialogs;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.TextView;

import com.vsv.db.entities.Sample;
import com.vsv.dialogs.listeners.UpdateSampleListener;
import com.vsv.memorizer.R;

public class UpdateSampleDialog extends SingleCustomDialog {

    private final TextView leftInput;

    private final TextView rightInput;

    private final TextView kindInput;

    private final TextView exampleInput;

    private final CheckBox resetProgress;

    private UpdateSampleListener listener;

    private final Sample sample;

    private final Animation rotate;

    public UpdateSampleDialog(Sample sample) {
        super(R.layout.dialog_new_sample, false, true);
        this.sample = sample;
        TextView header = dialogView.findViewById(R.id.newSampleDialogHeader);
        header.setText(R.string.update_sample_dialog_header);
        dialogView.findViewById(R.id.swapSamples).setOnClickListener(this::swap);
        View ok = dialogView.findViewById(R.id.newSampleOk);
        View cancel = dialogView.findViewById(R.id.newSampleCancel);
        leftInput = dialogView.findViewById(R.id.newSampleLeftInput);
        resetProgress = dialogView.findViewById(R.id.resetProgress);
        resetProgress.setVisibility(View.VISIBLE);
        rightInput = dialogView.findViewById(R.id.newSampleRightInput);
        kindInput = dialogView.findViewById(R.id.newSampleKind);
        exampleInput = dialogView.findViewById(R.id.newSampleExample);
        leftInput.setText(sample.getLeftValue());
        rightInput.setText(sample.getRightValue());
        String kind = sample.getType();
        if (!kind.isEmpty()) {
            kindInput.setText(kind);
        }
        String example = sample.getExample();
        if (!example.isEmpty()) {
            exampleInput.setText(example);
        }
        rotate = AnimationUtils.loadAnimation(context, R.anim.rotate);
        ok.setOnClickListener(this::onClickOk);
        cancel.setOnClickListener(this::onClickCancel);
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
            rightValue = "?";
            // Toasts.sampleValueEmpty();
        }
        boolean same = sample.getLeftValue().equals(leftValue) && sample.getRightValue().equals(rightValue);
        boolean sameReverse = sample.getLeftValue().equals(rightValue) && sample.getRightValue().equals(leftValue);
        // Values swapped need to swap answers also.
        if (sameReverse) {
            int leftAnswered = sample.getLeftAnswered();
            sample.setLeftAnswered(sample.getRightAnswered());
            sample.setRightAnswered(leftAnswered);
            int leftPercentage = sample.getLeftPercentage();
            sample.setLeftPercentage(sample.getRightPercentage());
            sample.setRightPercentage(leftPercentage);
        } else {

            // Values changed need to reset answered states.
            if (!same) {
                sample.answeredDate = null;
                sample.setLeftAnswered(0);
                sample.setRightAnswered(0);
                sample.setRightPercentage(0);
                sample.setLeftPercentage(0);
                sample.lastCorrect = false;
                sample.correctSeries = 0;
            }
        }
        if ((same || sameReverse) && resetProgress.isChecked()) {
            sample.setLeftAnswered(0);
            sample.setRightAnswered(0);
            sample.setRightPercentage(0);
            sample.setLeftPercentage(0);
        }

        sample.setLeftValue(leftValue);
        sample.setRightValue(rightValue);
        sample.setType(kind);
        sample.setExample(example);
        listener.updateSample(sample);
        dialog.cancel();

    }

    public void updateSampleListener(UpdateSampleListener listener) {
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
