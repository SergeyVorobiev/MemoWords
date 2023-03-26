package com.vsv.recyclerentities;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vsv.bundle.entities.ShelfBundle;
import com.vsv.db.entities.Dictionary;
import com.vsv.db.entities.DictionaryWithSamples;
import com.vsv.memorizer.adapters.RecyclerAddAdapter;

import com.vsv.removeitems.ShelfRemover;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AddRecycleList extends AbstractRecyclerList<ShelfRemover, RecyclerAddAdapter> {

    private static AddRecycleList built;

    private static Future<AddRecycleList> cache;

    private AddRecycleList(int poolSize) {
        super(poolSize);
    }

    public static void buildCache() {
        built = null;

        // noinspection unchecked
        cache = (Future<AddRecycleList>) new AddRecycleList(15).build();
    }

    @NonNull
    @MainThread
    public static AddRecycleList getList() {
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
    public ShelfRemover buildRemover(RecyclerView recyclerView) {
        return new ShelfRemover(false);
    }

    @Override
    public RecyclerAddAdapter buildAdapter(RecyclerView recyclerView) {
        return new RecyclerAddAdapter();
    }

    public void setData(Dictionary dictionary, DictionaryWithSamples dictionaryWithSamples, @NonNull ShelfBundle shelfBundle, int type) {
        adapter.setData(dictionary, dictionaryWithSamples, shelfBundle, type);
    }
}
