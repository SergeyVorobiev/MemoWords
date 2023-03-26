package com.vsv.recyclerentities;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vsv.memorizer.adapters.RecyclerShelvesAdapter;
import com.vsv.removeitems.ShelfRemover;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ShelvesRecycleList extends AbstractRecyclerList<ShelfRemover, RecyclerShelvesAdapter> {

    private static ShelvesRecycleList built;

    private static Future<ShelvesRecycleList> cache;

    private ShelvesRecycleList(int poolSize) {
        super(poolSize);
    }

    public static void buildCache() {
        built = null;

        // noinspection unchecked
        cache = (Future<ShelvesRecycleList>) new ShelvesRecycleList(10).build();
    }

    @NonNull
    @MainThread
    public static ShelvesRecycleList getList() {
        try {
            if (built == null) {
                built = cache.get(60, TimeUnit.SECONDS);
                cache = null;
            }
            return built;
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            throw new RuntimeException("Cannot load shelves list.", e);
        }
    }

    public static boolean isBuilt() {
        return built != null;
    }

    @Override
    public ShelfRemover buildRemover(RecyclerView recyclerView) {
        return new ShelfRemover(true);
    }

    @Override
    public RecyclerShelvesAdapter buildAdapter(@NonNull RecyclerView recyclerView) {
        return new RecyclerShelvesAdapter(recyclerView);
    }
}
