package com.vsv.memorizer.adapters;

import android.annotation.SuppressLint;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.vsv.bundle.entities.DictionaryBundle;
import com.vsv.db.entities.Dictionary;
import com.vsv.db.entities.Sample;
import com.vsv.dialogs.SingleWindow;
import com.vsv.dialogs.UpdateSampleDialog;
import com.vsv.memorizer.R;
import com.vsv.speech.RoboVoice;
import com.vsv.statics.GlobalData;
import com.vsv.toasts.Toasts;
import com.vsv.utils.DateUtils;
import com.vsv.utils.Spec;
import com.vsv.utils.StaticUtils;
import com.vsv.utils.Timer;
import com.vsv.viewutils.StopVerticalScrollAnimator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Locale;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class RecyclerSamplesAdapter extends AbstractAdapter<RecyclerSamplesAdapter.SampleHolder> {

    private ArrayList<Sample> items;

    private ArrayList<Sample> filteredItems;

    private final View.OnLongClickListener longClickSampleListener;

    private final int correctAnsweredTextColor;

    private final int wrongAnsweredTextColor;

    private final int notAnsweredTextColor;

    private long dictionaryId;

    private int hideRemembered;

    private int sortedType;

    private Locale leftLocale;

    private Locale rightLocale;

    private final RecyclerView listView;

    private int lastPosition = -1;

    private final View.OnClickListener leftSoundClick = this::leftSoundClick;

    private final View.OnClickListener rightSoundClick = this::rightSoundClick;

    private final View.OnClickListener onSampleClick = this::onSampleClick;

    private final String leftRightPercentage;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

    private final String notAnsweredDate;

    private final String todayAnsweredFormat;

    private final String daysAnsweredFormat;

    private final Drawable medalDrawable;

    private final Drawable gymDrawable;

    private final Drawable lockDrawable;

    private final String timeFormat;

    private CountDownTimer timer;

    public final TreeMap<Integer, SampleHolder> holders = new TreeMap<>();

    private long sampleIdToScrollTo;

    public RecyclerSamplesAdapter(@NonNull RecyclerView listView) {
        StopVerticalScrollAnimator.setRecycleViewAnimation(listView, Spec.MIN_SCROLL_HIT, R.anim.from_bottom2, R.anim.from_top);
        this.listView = listView;
        leftRightPercentage = StaticUtils.getString(R.string.left_right_percentage);
        this.longClickSampleListener = this::updateSample;
        this.correctAnsweredTextColor = listView.getResources().getColor(R.color.correctAnswered, listView.getContext().getTheme());
        this.wrongAnsweredTextColor = listView.getResources().getColor(R.color.wrongAnswered, listView.getContext().getTheme());
        this.notAnsweredTextColor = listView.getResources().getColor(R.color.notAnswered, listView.getContext().getTheme());
        this.notAnsweredDate = StaticUtils.getString(R.string.sample_train_empty);
        this.todayAnsweredFormat = StaticUtils.getString(R.string.sample_date_today_format);
        this.daysAnsweredFormat = StaticUtils.getString(R.string.sample_date_format);
        this.medalDrawable = StaticUtils.getDrawable(R.drawable.ic_sample_icon_medal);
        this.gymDrawable = StaticUtils.getDrawable(R.drawable.ic_sample_icon_gym);
        this.lockDrawable = StaticUtils.getDrawable(R.drawable.ic_sample_icon_lock);
        this.timeFormat = StaticUtils.getString(R.string.time);
    }

    private boolean updateSample(View view) {
        if (SingleWindow.isShownToast()) {
            return true;
        }
        int position = (int) view.getTag();
        UpdateSampleDialog dialog = new UpdateSampleDialog(this.getSample(position));
        dialog.updateSampleListener(StaticUtils.getModel().getSamplesRepository()::update);
        dialog.show();
        return true;
    }

    @SuppressLint("NotifyDataSetChanged")
    @MainThread
    @Override
    public void clearData() {
        this.sampleIdToScrollTo = -1;
        this.sortedType = -1;
        this.holders.clear();
        this.hideRemembered = -1;
        this.dictionaryId = -1;
        this.lastPosition = -1;
        this.items = null;
        this.filteredItems = null;
        this.leftLocale = null;
        this.rightLocale = null;
        this.timer.cancel();
        this.timer = null;
        notifyDataSetChanged();
    }

    @MainThread
    @SuppressLint("NotifyDataSetChanged")
    public void filterRemembered(int hideRemembered) {
        this.hideRemembered = hideRemembered;
        filter();
        notifyDataSetChanged();
    }

    @MainThread
    @SuppressLint("NotifyDataSetChanged")
    public void sort(int sortType) {
        this.sortedType = sortType;
        filter();
        notifyDataSetChanged();
    }

    public void setData(@NonNull DictionaryBundle dictionaryBundle, long sampleIdToScrollTo) {
        this.sampleIdToScrollTo = sampleIdToScrollTo;
        this.sortedType = dictionaryBundle.sortedType;
        this.hideRemembered = dictionaryBundle.hideRemembered;
        this.dictionaryId = dictionaryBundle.id;
        this.leftLocale = dictionaryBundle.getLeftLocale();
        this.rightLocale = dictionaryBundle.getRightLocale();
        timer = createTimer();
        timer.start();
    }

    private CountDownTimer createTimer() {
        return new CountDownTimer(Integer.MAX_VALUE, 1500) {

            private final Handler handler = new Handler(Looper.getMainLooper());

            @Override
            public void onTick(long millisUntilFinished) {
                handler.post(() -> {
                    for (SampleHolder holder : RecyclerSamplesAdapter.this.holders.values()) {
                        int duration = 2000;
                        int elapsedTime = duration;
                        if (holder.transitionTime != -1) {
                            elapsedTime = (int) Timer.nanoTimeDiffFromNowInMilliseconds(holder.transitionTime);
                        }
                        TransitionDrawable drawable = (TransitionDrawable) holder.mainView.getBackground();
                        TransitionDrawable drawablePerc = (TransitionDrawable) holder.percentageView.getBackground();
                        if (holder.isReverse && elapsedTime >= duration) {
                            holder.transitionTime = System.nanoTime();
                            drawable.reverseTransition(duration);
                            drawablePerc.reverseTransition(duration);
                            holder.isReverse = false;
                        } else if (!holder.isReverse && elapsedTime >= duration) {
                            holder.transitionTime = System.nanoTime();
                            drawable.startTransition(duration);
                            drawablePerc.startTransition(duration);
                            holder.isReverse = true;
                        }
                    }
                });
            }

            @Override
            public void onFinish() {

            }
        };
    }

    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    public static class SampleHolder extends RecyclerView.ViewHolder {

        public final View sample;

        public final View mainView;

        public boolean isReverse;

        public final TextView leftValue;

        public final TextView rightValue;

        public final TextView percentageView;

        public final TextView kindView;

        public final TextView badge;

        public final View sampleExampleLayout;

        public final TextView exampleView;

        public final View leftSound;

        public final View rightSound;

        public final TextView dateView;

        public final TextView exampleLabel;

        private final Animation animationFirst;

        private final Animation animationSecond;

        public final ImageView arrowView;

        public final TransitionDrawable transitionDrawablePerc;

        public final TransitionDrawable transitionDrawable;

        public long transitionTime;

        public final View paddingView;

        public SampleHolder(View view, View.OnClickListener leftSoundClick, View.OnClickListener rightSoundClick) {
            super(view);
            sample = view;
            mainView = view.findViewById(R.id.sampleMainLayout);
            transitionDrawable = (TransitionDrawable) StaticUtils.getDrawable(R.drawable.bg_item_sample_transition);
            transitionDrawablePerc = (TransitionDrawable) StaticUtils.getDrawable(R.drawable.bg_item_perc_transition);
            paddingView = view.findViewById(R.id.paddingView);
            leftValue = view.findViewById(R.id.leftValue);
            rightValue = view.findViewById(R.id.rightValue);
            percentageView = view.findViewById(R.id.memoPercentage);
            transitionTime = -1;
            arrowView = view.findViewById(R.id.arrow);
            kindView = view.findViewById(R.id.kindLabel);
            sampleExampleLayout = view.findViewById(R.id.sampleExampleLayout);
            exampleView = view.findViewById(R.id.sampleExample);
            leftSound = view.findViewById(R.id.leftSound);
            badge = view.findViewById(R.id.badge);
            rightSound = view.findViewById(R.id.rightSound);
            exampleLabel = view.findViewById(R.id.exampleLabel);
            dateView = view.findViewById(R.id.sampleDate);
            animationFirst = AnimationUtils.loadAnimation(view.getContext(), R.anim.from_left);
            animationSecond = AnimationUtils.loadAnimation(view.getContext(), R.anim.from_right);
            leftSound.setOnClickListener(leftSoundClick);
            rightSound.setOnClickListener(rightSoundClick);
        }

        public void animate() {
            Animation animation = StaticUtils.random.nextInt(2) == 0 ? animationFirst : animationSecond;
            sample.startAnimation(animation);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @MainThread
    public void unlockSamples() {
        if (items == null || items.isEmpty()) {
            return;
        }
        ArrayList<Sample> toUnlock = new ArrayList<>();
        for (Sample sample : items) {
            if (sample.isLocked() && sample.needUnlock()) {
                sample.correctSeries = 0;
                toUnlock.add(sample);
            }
        }
        if (!toUnlock.isEmpty()) {
            StaticUtils.getModel().getSamplesRepository().updateSeveral(toUnlock);
        } else {
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public SampleHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        // Create a new view.
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sample, parent, false);
        View mainView = v.findViewById(R.id.sampleMainLayout);
        mainView.setOnLongClickListener(longClickSampleListener);
        mainView.setOnClickListener(onSampleClick);
        return new SampleHolder(v, leftSoundClick, rightSoundClick);
    }

    @Nullable
    public ArrayList<Sample> getItems() {
        return filteredItems;
    }

    @MainThread
    @SuppressLint("NotifyDataSetChanged")
    public void setItems(ArrayList<Sample> items) {
        this.items = items;
        filter();
        if (sampleIdToScrollTo != -1) {
            int position = findItemPositionById(sampleIdToScrollTo);
            if (position != -1) {
                this.scrollToPosition(position);
            }
            sampleIdToScrollTo = -1;
        }
        notifyDataSetChanged();
    }

    public int getAllItemsCount() {
        return items == null ? 0 : items.size();
    }

    private int findItemPositionById(long id) {
        if (filteredItems == null || filteredItems.isEmpty()) {
            return -1;
        }
        for (int i = 0; i < filteredItems.size(); i++) {
            if (filteredItems.get(i).getId() == id) {
                return i;
            }
        }
        return -1;
    }

    @Nullable
    @MainThread
    public ArrayList<Sample> getAllItems() {
        return items;
    }

    public Sample getSample(int position) {
        return this.filteredItems.get(position);
    }

    private void setArrow(@NonNull ImageView arrowView, boolean isShown, boolean hasExample) {
        if (isShown && hasExample) {
            arrowView.setImageDrawable(StaticUtils.getDrawable(R.drawable.ic_arrow_up));
        } else if (!isShown && !hasExample) {
            arrowView.setImageDrawable(StaticUtils.getDrawable(R.drawable.ic_arrow_down_gray));
        } else if (!isShown) {
            arrowView.setImageDrawable(StaticUtils.getDrawable(R.drawable.ic_arrow_down));
        } else {
            arrowView.setImageDrawable(StaticUtils.getDrawable(R.drawable.ic_arrow_up_gray));
        }
    }

    @Override
    public void onBindViewHolder(SampleHolder viewHolder, final int position) {
        View viewSample = viewHolder.sample;
        if (position == getItemCount() - 1) {
            viewHolder.paddingView.setVisibility(View.VISIBLE);
        } else {
            viewHolder.paddingView.setVisibility(View.GONE);
        }
        TextView leftValue = viewHolder.leftValue;
        TextView rightValue = viewHolder.rightValue;
        TextView percentageView = viewHolder.percentageView;
        TextView kindView = viewHolder.kindView;
        TextView exampleLabel = viewHolder.exampleLabel;
        viewHolder.leftSound.setTag(position);
        viewHolder.rightSound.setTag(position);
        View sampleExampleLayout = viewHolder.sampleExampleLayout;
        TextView exampleView = viewHolder.exampleView;
        Sample sample = filteredItems.get(position);
        viewHolder.badge.setText(null);
        if (sample.isRemembered()) {
            percentageView.setBackground(viewHolder.transitionDrawablePerc);
            viewHolder.badge.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, medalDrawable, null);
            viewHolder.mainView.setBackground(viewHolder.transitionDrawable);
            holders.put(viewHolder.hashCode(), viewHolder);
        } else {
            percentageView.setBackground(null);
            viewHolder.mainView.setBackground(StaticUtils.getDrawable(R.drawable.bg_item_sample));
            holders.remove(viewHolder.hashCode());
            if (sample.isLocked()) {
                long remainingTime = sample.getRemainedLockTime();
                viewHolder.badge.setText(String.format(timeFormat, remainingTime));
            } else {
                int series = sample.getPastTime() < Sample.SERIES_TIME ? sample.correctSeries : 0;
                viewHolder.badge.setText(StaticUtils.getString(R.string.counter, series, Sample.LOCK_SERIES_COUNT));
            }
            viewHolder.badge.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, sample.isLocked() ? lockDrawable : gymDrawable, null);
        }
        if (sample.answeredDate != null) {
            int pastDays = DateUtils.getDaysBetweenDatesOrZero(sample.answeredDate, Calendar.getInstance().getTime());
            if (pastDays > 0) {
                viewHolder.dateView.setText(String.format(daysAnsweredFormat, dateFormat.format(sample.answeredDate), pastDays));
            } else {
                viewHolder.dateView.setText(String.format(todayAnsweredFormat, dateFormat.format(sample.answeredDate)));
            }
        } else {
            viewHolder.dateView.setText(notAnsweredDate);
        }
        String percentageText;
        boolean reverse = GlobalData.getReverse(dictionaryId);
        if (reverse) {
            leftValue.setText(sample.getRightValue());
            rightValue.setText(sample.getLeftValue());
            setTextColor(leftValue, sample.getRightAnswered());
            setTextColor(rightValue, sample.getLeftAnswered());
            percentageText = String.format(leftRightPercentage, sample.getRightPercentage(), sample.getLeftPercentage());
        } else {
            leftValue.setText(sample.getLeftValue());
            rightValue.setText(sample.getRightValue());
            setTextColor(leftValue, sample.getLeftAnswered());
            setTextColor(rightValue, sample.getRightAnswered());
            percentageText = String.format(leftRightPercentage, sample.getLeftPercentage(), sample.getRightPercentage());
        }
        percentageView.setText(percentageText);
        String kind = sample.getType();
        kindView.setText(kind);
        // kindView.setText(String.valueOf(position + 1));
        viewHolder.mainView.setTag(position);
        String example = sample.getExample();
        if (example.isEmpty()) {
            exampleView.setText("");
            exampleView.setVisibility(View.GONE);
            exampleLabel.setVisibility(View.GONE);
        } else {
            exampleView.setText(example);
            exampleView.setVisibility(View.VISIBLE);
            exampleLabel.setVisibility(View.VISIBLE);
        }
        if (sample.showExample) {
            sampleExampleLayout.setVisibility(View.VISIBLE);

        } else {
            sampleExampleLayout.setVisibility(View.GONE);
        }
        setArrow(viewHolder.arrowView, sample.showExample, !example.isEmpty());
        setAnimation(viewHolder, position);
    }

    private void setAnimation(SampleHolder sampleHolder, int position) {
        if (position > lastPosition) {
            lastPosition = position;
            sampleHolder.animate();
        }
    }

    @Override
    public void onViewDetachedFromWindow(SampleHolder holder) {
        holder.sample.clearAnimation();
    }

    private void onSampleClick(View view) {
        int position = (int) view.getTag();
        Sample sample = this.getSample(position);
        sample.showExample = !sample.showExample;
        View exampleView = view.findViewById(R.id.sampleExampleLayout);
        if (sample.showExample) {
            exampleView.setVisibility(View.VISIBLE);
        } else {
            exampleView.setVisibility(View.GONE);
        }
        if (position == filteredItems.size() - 1) {
            listView.postDelayed(() -> listView.scrollToPosition(position), 10);
        }
        setArrow(view.findViewById(R.id.arrow), sample.showExample, !sample.getExample().isEmpty());
    }

    public void scrollToPosition(int position) {
        try {
            listView.scrollToPosition(position);
        } catch (Exception e) {
            //
        }
    }

    private void leftSoundClick(View view) {
        int position = (int) view.getTag();
        Sample sample = this.getSample(position);
        boolean reverse = GlobalData.getReverse(dictionaryId);
        String string;
        Locale locale;
        if (reverse) {
            string = sample.getRightValue();
            locale = rightLocale;
        } else {
            string = sample.getLeftValue();
            locale = leftLocale;
        }
        if (locale == null) {
            Toasts.setTheLanguage();
        } else {
            RoboVoice.getInstance().speak(string, locale);
        }
    }

    private void rightSoundClick(View view) {
        int position = (int) view.getTag();
        Sample sample = this.getSample(position);
        String string;
        boolean reverse = GlobalData.getReverse(dictionaryId);
        Locale locale;
        if (reverse) {
            string = sample.getLeftValue();
            locale = leftLocale;
        } else {
            string = sample.getRightValue();
            locale = rightLocale;
        }
        if (locale == null) {
            Toasts.setTheLanguage();
        } else {
            RoboVoice.getInstance().speak(string, locale);
        }
    }

    private void setTextColor(TextView view, int value) {
        switch (value) {
            case 1:
                view.setTextColor(wrongAnsweredTextColor);
                break;
            case 2:
                view.setTextColor(correctAnsweredTextColor);
                break;
            default:
                view.setTextColor(notAnsweredTextColor);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void applyFilter(String query) {
        GlobalData.sampleSearchQuery.put(dictionaryId, query.toLowerCase());
        filter();
        notifyDataSetChanged();
    }

    private void filter() {
        if (items == null) {
            filteredItems = null;
            return;
        } else {
            filteredItems = new ArrayList<>(items);
        }
        String filterQuery = GlobalData.sampleSearchQuery.getOrDefault(dictionaryId, null);
        if (filterQuery != null && !filterQuery.isEmpty()) {
            filteredItems = (ArrayList<Sample>) items.stream()
                    .filter((sample) -> sample.getLeftValue().toLowerCase()
                            .contains(filterQuery.toLowerCase()) || sample.getRightValue().toLowerCase().contains(filterQuery.toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (!filteredItems.isEmpty() && hideRemembered == 1) {
            filteredItems = (ArrayList<Sample>) filteredItems.stream().filter((sample) -> !sample.isRemembered()).collect(Collectors.toList());
        }
        sort();
    }

    private void sort() {
        if (filteredItems == null || filteredItems.isEmpty() || sortedType == Dictionary.SORTED_BY_DATE) {
            return;
        }
        Comparator<Sample> comparator = getSortComparator();
        if (comparator != null) {
            filteredItems.sort(comparator);
        }
    }

    private @Nullable
    Comparator<Sample> getSortComparator() {
        switch (sortedType) {
            case Dictionary.SORTED_BY_KIND:
                return Comparator.comparing(Sample::getType);
            case Dictionary.SORTED_BY_LEFT:
                return Comparator.comparing(Sample::getLeftValue);
            case Dictionary.SORTED_BY_RIGHT:
                return Comparator.comparing(Sample::getRightValue);
            default:
                return null;
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return filteredItems == null ? 0 : filteredItems.size();
    }

    public int getAllCount() {
        return items == null ? 0 : items.size();
    }
}