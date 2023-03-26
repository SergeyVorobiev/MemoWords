package com.vsv.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.vsv.db.entities.Tracker;
import com.vsv.graphs.DictionaryGraph;
import com.vsv.memorizer.R;
import com.vsv.statics.GlobalData;
import com.vsv.statics.WeakContext;
import com.vsv.utils.StaticUtils;
import com.vsv.utils.Symbols;
import com.vsv.utils.TimeStringConverter;

import java.util.Comparator;
import java.util.List;

public class EndTrainDialog extends SingleCustomDialog {

    private final Handler handler;

    private final int percentage;

    private TextView percentView;

    private TextView addPercentage;

    private final String percentageFormat;

    private final float progress;

    private final Animation animation;

    private TextView averageScore;

    private Thread thread;

    private final LiveData<List<Tracker>> liveData;

    public EndTrainDialog(long dictionaryId, long score, long nowScore, int averageScore, float progress, int answered, int all, float reactionSpeed, long trainTimeSeconds) {
        super(R.layout.dialog_end_train, false, true);
        handler = new Handler(Looper.getMainLooper());
        TextView answeredView = dialogView.findViewById(R.id.correctAnsweredResult);
        answeredView.setText(String.format(context.getResources().getString(R.string.correct_answered), answered, all));
        TextView scoreView = dialogView.findViewById(R.id.score);
        scoreView.setText(StaticUtils.getString(R.string.score, score, nowScore));
        TextView reactionSpeedTextView = dialogView.findViewById(R.id.reactionSpeed);
        TextView trainTime = dialogView.findViewById(R.id.trainTime);
        trainTime.setText(StaticUtils.getString(R.string.train_time, TimeStringConverter.fromSeconds(trainTimeSeconds)));
        reactionSpeedTextView.setText(StaticUtils.getString(R.string.reaction_speed, reactionSpeed));
        TextView averageScoreView = dialogView.findViewById(R.id.averageScore);
        averageScoreView.setText(StaticUtils.getString(R.string.average_score, averageScore));
        percentage = (int) (((float) answered / all) * 100.0f);
        dialog.setOnShowListener(this::dialogShown);
        this.progress = progress;
        this.animation = AnimationUtils.loadAnimation(context, R.anim.scaling);
        percentageFormat = context.getResources().getString(R.string.percentage);

        DictionaryGraph graph = dialogView.findViewById(R.id.graph);
        liveData = StaticUtils.getModel().getTrackerRepository().getAllLive(dictionaryId);
        liveData.observe(WeakContext.getMainActivity(), trackers -> {
            Log.d("Tracker", "track the data");
            if (trackers == null || trackers.isEmpty()) {
                return;
            }
            trackers.sort(Comparator.comparingLong(a -> a.timestamp));
            float[] floats = new float[trackers.size()];
            int i = 0;
            for (Tracker tracker : trackers) {
                floats[i++] = (float) tracker.progress;
            }
            graph.setupData(floats);
        });
    }
    @Override
    public void setOnDismissListener(@NonNull DialogInterface.OnDismissListener listener) {

        super.setOnDismissListener((dialogInterface) -> {
            liveData.removeObservers(WeakContext.getMainActivity());
            listener.onDismiss(dialogInterface);
        });
    }

    private void dialogShown(DialogInterface dialogInterface) {
        percentView.startAnimation(animation);
        thread = new Thread(() -> {
            for (int p = 1; p <= percentage; p += 2) {
                try {
                    Thread.sleep(16);
                } catch (InterruptedException e) {
                    return;
                }
                if (Thread.currentThread().isInterrupted() || !dialog.isShowing()) {
                    return;
                }
                final int fp = p;
                handler.post(() -> setPercentageText(fp));
            }
            handler.post(() -> {
                setPercentageText(percentage);
                if (progress > 0) {
                    addPercentage.setTextColor(Color.parseColor("#0A8E00"));
                    addPercentage.setText(StaticUtils.getString(R.string.positive_percentage, Symbols.EDUCATION, progress));
                } else if (progress < 0) {
                    addPercentage.setTextColor(Color.parseColor("#B60000"));
                    addPercentage.setText(StaticUtils.getString(R.string.negative_percentage, Symbols.EDUCATION, Math.abs(progress)));
                }
            });
        });
        thread.setDaemon(true);
        thread.start();
    }

    public Dialog getDialog() {
        return dialog;
    }

    private void setPercentageText(int percentage) {
        percentView.setText(String.format(percentageFormat, percentage));
    }

    @Override
    public void setupViews(View dialogView) {
        percentView = dialogView.findViewById(R.id.percentResult);
        addPercentage = dialogView.findViewById(R.id.addPerc);
    }

    @Override
    public void setupViewListeners(View dialogView) {
        dialogView.findViewById(R.id.endTrainButton).setOnClickListener((view) -> {
            if (thread != null) {
                thread.interrupt();
            }
            dialog.cancel();
        });
    }

    @Override
    public void setupViewAdjustments(View dialogView) {

    }
}
