package com.vsv.dialogs;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.vsv.db.entities.Dictionary;
import com.vsv.db.entities.Tracker;
import com.vsv.graphs.DictionaryGraph;
import com.vsv.memorizer.MainActivity;
import com.vsv.memorizer.R;
import com.vsv.statics.WeakContext;
import com.vsv.utils.DateUtils;
import com.vsv.utils.StaticUtils;

import java.util.Comparator;
import java.util.List;

public class DictionaryGraphPopup extends SingleWindow {

    private final PopupWindow window;

    private final DictionaryGraph graph;

    private final TextView averageView;

    private final TextView todayScoreView;

    private final TextView trainingsView;

    public DictionaryGraphPopup(Dictionary dictionary) {
        DisplayMetrics displayMetrics = WeakContext.buildDisplayMetrics();
        MainActivity activity = WeakContext.getMainActivity();
        window = new PopupWindow(activity);
        View layout = View.inflate(activity, R.layout.item_graph, null);
        graph = layout.findViewById(R.id.graph);
        averageView = layout.findViewById(R.id.averageScore);
        todayScoreView = layout.findViewById(R.id.todayScore);
        trainingsView = layout.findViewById(R.id.trainings);
        graph.setMinimumWidth(displayMetrics.widthPixels);
        window.setOnDismissListener(() -> SingleWindow.setShown(false));
        window.setFocusable(true);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setOutsideTouchable(true);
        window.setOverlapAnchor(true);
        window.setContentView(layout);
        averageView.setText(StaticUtils.getString(R.string.average_per_sample, 0));
        todayScoreView.setText(StaticUtils.getString(R.string.today_score, 0));
        trainingsView.setText(StaticUtils.getString(R.string.trainings, 0));
        setupGraphData(dictionary);
    }

    private void setupGraphData(Dictionary dictionary) {
        LiveData<List<Tracker>> liveData = StaticUtils.getModel().getTrackerRepository().getAllLive(dictionary.getId());
        liveData.observe(WeakContext.getMainActivity(), trackers -> {
            if (trackers == null || trackers.isEmpty()) {
                return;
            }
            trackers.sort(Comparator.comparingLong(a -> a.timestamp));
            float[] floats = new float[trackers.size()];
            int i = 0;
            float sum = 0;
            for (Tracker tracker : trackers) {
                float progress = (float) tracker.progress;
                sum += progress;
                floats[i++] = progress;
            }
            float average = sum == 0 ? 0 : sum / floats.length;
            int todayScore = 0;
            if (dictionary.timestampForTodayScore == DateUtils.getTimestampInDays()) {
                todayScore = (int) dictionary.todayScore;
            }
            averageView.setText(StaticUtils.getString(R.string.average_per_sample, (int) average));
            todayScoreView.setText(StaticUtils.getString(R.string.today_score, todayScore));
            trainingsView.setText(StaticUtils.getString(R.string.trainings, floats.length));
            graph.setupData(floats);
        });
    }

    public void show(@NonNull View anchor) {
        if (!SingleWindow.isShown()) {
            SingleWindow.setShown(true);
            graph.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            window.showAsDropDown(anchor, -graph.getMeasuredWidth(), 0);
        }
    }
}
