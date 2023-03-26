package com.vsv.recyclerentities;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.vsv.bundle.entities.ShelfBundle;
import com.vsv.memorizer.adapters.RecyclerSpreadsheetsAdapter;

import com.vsv.removeitems.SpreadsheetRemover;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SpreadsheetRecyclerList extends AbstractRecyclerList<SpreadsheetRemover, RecyclerSpreadsheetsAdapter> {

    private static SpreadsheetRecyclerList built;

    private static Future<SpreadsheetRecyclerList> cache;

    private SpreadsheetRecyclerList(int poolSize) {
        super(poolSize);
    }

    public static void buildCache() {
        built = null;

        // noinspection unchecked
        cache = (Future<SpreadsheetRecyclerList>) new SpreadsheetRecyclerList(15).build();
    }

    @NonNull
    @MainThread
    public static SpreadsheetRecyclerList getList() {
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
    public SpreadsheetRemover buildRemover(RecyclerView recyclerView) {
        return new SpreadsheetRemover();
    }

    @Override
    public RecyclerSpreadsheetsAdapter buildAdapter(@NonNull RecyclerView recyclerView) {
        return new RecyclerSpreadsheetsAdapter(recyclerView);
    }

    @MainThread
    public void setData(@Nullable ShelfBundle shelfBundle) {
        adapter.setData(shelfBundle);
    }
}
