package com.vsv.dialogs.entities;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.vsv.bundle.entities.DictionaryBundle;
import com.vsv.bundle.entities.ShelfBundle;
import com.vsv.bundle.helpers.BundleNames;
import com.vsv.db.entities.Dictionary;
import com.vsv.db.entities.Sample;
import com.vsv.memorizer.R;
import com.vsv.memorizer.adapters.RecyclerFindDictAdapter;
import com.vsv.models.MainModel;
import com.vsv.statics.WeakContext;
import com.vsv.utils.StaticUtils;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public class ShelfWordFinder {

    private final MainModel model;

    private final ArrayList<Dictionary> dictionaries;

    private final TextView dictNameView;

    private final ProgressBar loadBar;

    private final AtomicBoolean changed = new AtomicBoolean(false);

    private final AtomicBoolean full = new AtomicBoolean(false);

    private final AtomicBoolean stop = new AtomicBoolean(false);

    private final RecyclerFindDictAdapter adapter;

    private final TextView wordCounter;

    private final ExecutorService executor = Executors.newSingleThreadExecutor((r) -> {
        Thread thread = new Thread(r);
        thread.setDaemon(true);
        thread.setName("ShelfWord");
        return thread;
    });

    private Future<?> searchProcess;

    private String query;

    private final Object sync = new Object();

    private static final int MAX_ITEMS = 200;

    private final ShelfBundle shelfBundle;

    private Runnable onTransitionListener;

    private final String counterFormat;

    private final RecyclerView samplesList;

    private int minHeight;

    private final AtomicBoolean onlyFromStartFlag = new AtomicBoolean(false);

    public ShelfWordFinder(@NonNull View parent,
                           @NonNull MainModel model,
                           @Nullable ArrayList<Dictionary> dictionaries, @NonNull ShelfBundle shelfBundle) {
        this.shelfBundle = shelfBundle;
        this.model = model;
        if (dictionaries == null) {
            dictionaries = new ArrayList<>();
        }
        this.minHeight = 0;
        this.dictionaries = dictionaries;
        dictNameView = parent.findViewById(R.id.dictName);
        loadBar = parent.findViewById(R.id.loadBar);
        wordCounter = parent.findViewById(R.id.wordCounter);
        loadBar.setMax(100);
        samplesList = parent.findViewById(R.id.samplesList);
        adapter = new RecyclerFindDictAdapter(MAX_ITEMS, this::clickOnItem);
        samplesList.setAdapter(adapter);
        onTransitionListener = () -> {
        };
        counterFormat = StaticUtils.getString(R.string.counter);
    }

    public void start() {
        searchProcess = executor.submit(this::search);
    }

    public void updateQuery(String query) {
        synchronized (sync) {
            this.query = query;
            changed.set(true);
        }
    }

    public void findWayChanged(boolean onlyFromStart) {
        synchronized (sync) {
            onlyFromStartFlag.set(onlyFromStart);
            changed.set(true);
        }
    }

    public Object[] getUpdatedQuery() {
        synchronized (sync) {
            changed.set(false);
            boolean flag = onlyFromStartFlag.get();
            return new Object[]{this.query.trim().toLowerCase(), flag};
        }
    }

    public void close() {
        stop.set(true);
        if (searchProcess != null) {
            searchProcess.cancel(true);
            executor.shutdown();
        }
    }

    public void clickOnItem(FoundWordItem item) {
        onTransitionListener.run();
        Bundle bundle = new Bundle();
        DictionaryBundle.toBundle(bundle, item.dictionary);
        bundle.putLong(BundleNames.SAMPLE_ID, item.id);
        StaticUtils.navigateSafe(R.id.action_Dictionaries_to_Samples, shelfBundle.toBundle(bundle));
    }

    public void setOnTransitionListener(Runnable onTransitionListener) {
        this.onTransitionListener = onTransitionListener;
    }

    // Started in another process
    private void search() {
        Handler handler = new Handler(WeakContext.getContext().getMainLooper());
        int dictSize = dictionaries.size();
        if (dictSize == 0) {
            return;
        }
        while (!Thread.currentThread().isInterrupted() && !stop.get()) {
            if (changed.get()) {
                Object[] objects = getUpdatedQuery();
                String query = objects[0].toString();
                boolean startFlag = (boolean) objects[1];
                adapter.setFromStart(startFlag);
                full.set(false);
                handler.post(() -> {
                    dictNameView.setText("");
                    adapter.setFilter(query);
                    adapter.clearItems();
                    wordCounter.setText(String.format(counterFormat, adapter.getItemCount(), MAX_ITEMS));
                    loadBar.setProgress(0);
                });
                if (!query.isEmpty()) {
                    int counter = 0;
                    for (Dictionary dictionary : dictionaries) {
                        counter++;
                        handler.post(() -> dictNameView.setText(dictionary.getName()));
                        if (Thread.currentThread().isInterrupted() || stop.get()) {
                            return;
                        }
                        if (changed.get() || full.get()) {
                            break;
                        }
                        try {
                            ArrayList<Sample> samples = model.getSamplesRepository().getSamples(dictionary.getId()).get(30, TimeUnit.SECONDS);
                            if (samples == null) {
                                samples = new ArrayList<>();
                            }
                            ArrayList<FoundWordItem> items = new ArrayList<>();
                            for (Sample sample : samples) {
                                FoundWordItem item = new FoundWordItem();
                                item.dictionary = dictionary;
                                item.id = sample.getId();
                                item.pathName = item.dictionary.getName();
                                item.leftValue = sample.getLeftValue();
                                item.rightValue = sample.getRightValue();
                                item.percentage = sample.getLeftPercentage() + "% / " + sample.getRightPercentage() + "%";
                                item.kind = sample.getType();
                                items.add(item);
                            }
                            if (Thread.currentThread().isInterrupted() || stop.get()) {
                                return;
                            }
                            if (changed.get()) {
                                break;
                            }
                            final int finalCounter = counter;
                            handler.post(() -> {
                                int progress = (int) (((float) finalCounter / dictSize) * 100.0f);
                                loadBar.setProgress(progress);
                                adapter.appendItems(items);
                                int itemCount = adapter.getItemCount();
                                if (itemCount == MAX_ITEMS) {
                                    full.set(true);
                                }
                                wordCounter.setText(String.format(counterFormat, itemCount, MAX_ITEMS));
                            });
                        } catch (ExecutionException | InterruptedException | TimeoutException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                    handler.post(() -> {
                        int height = samplesList.getHeight();
                        if (height > minHeight) {
                            minHeight = height;
                            samplesList.setMinimumHeight(minHeight - 5);
                        }
                        dictNameView.setText("");
                        loadBar.setProgress(100);
                    });
                } else {
                    handler.post(adapter::setEmpty);
                }
            } else {
                try {

                    // noinspection BusyWait
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
