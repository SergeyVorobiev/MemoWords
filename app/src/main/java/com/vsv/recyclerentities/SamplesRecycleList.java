package com.vsv.recyclerentities;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vsv.bundle.entities.ShelfBundle;
import com.vsv.memorizer.adapters.RecyclerSamplesAdapter;

import com.vsv.removeitems.SampleRemover;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SamplesRecycleList extends AbstractRecyclerList<SampleRemover, RecyclerSamplesAdapter> {

    private static Future<SamplesRecycleList> cache;

    private SamplesRecycleList(int poolSize) {
        super(poolSize);
    }

    private static SamplesRecycleList built;

    public static void buildCache() {
        built = null;

        // noinspection unchecked
        cache = (Future<SamplesRecycleList>) new SamplesRecycleList(15).build();
    }

    @NonNull
    @MainThread
    public static SamplesRecycleList getList() {
        try {
            if (built == null) {
                built = cache.get(60, TimeUnit.SECONDS);
                cache = null;
            }
            return built;
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            throw new RuntimeException("Cannot load dictionaries list.", e);
        }
    }

    @Override
    public SampleRemover buildRemover(RecyclerView recyclerView) {
        return new SampleRemover();
    }

    @Override
    public RecyclerSamplesAdapter buildAdapter(RecyclerView recyclerView) {
        return new RecyclerSamplesAdapter(recyclerView);
    }

    @MainThread
    public void setData(@NonNull ShelfBundle shelfBundle) {
        remover.setData(shelfBundle);
    }
}
