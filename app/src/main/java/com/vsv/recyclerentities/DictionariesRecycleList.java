package com.vsv.recyclerentities;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vsv.bundle.entities.ShelfBundle;
import com.vsv.memorizer.adapters.RecyclerDictionariesAdapter;

import com.vsv.removeitems.DictionaryRemover;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DictionariesRecycleList extends AbstractRecyclerList<DictionaryRemover, RecyclerDictionariesAdapter> {

    private static DictionariesRecycleList built;

    private static Future<DictionariesRecycleList> cache;

    private DictionariesRecycleList(int poolSize) {
        super(poolSize);
    }

    public static void buildCache() {
        built = null;

        // noinspection unchecked
        cache = (Future<DictionariesRecycleList>) new DictionariesRecycleList(10).build();
    }

    @NonNull
    @MainThread
    public static DictionariesRecycleList getList() {
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
    public DictionaryRemover buildRemover(RecyclerView recyclerView) {
        return new DictionaryRemover();
    }

    @Override
    public RecyclerDictionariesAdapter buildAdapter(RecyclerView recyclerView) {
        return new RecyclerDictionariesAdapter(recyclerView);
    }

    @MainThread
    public void setData(@NonNull ShelfBundle shelfBundle) {
        adapter.setData(shelfBundle);
        remover.setData(shelfBundle);
    }
}
