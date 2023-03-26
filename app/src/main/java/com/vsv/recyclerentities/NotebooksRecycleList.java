package com.vsv.recyclerentities;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vsv.memorizer.adapters.RecyclerNotebooksAdapter;
import com.vsv.removeitems.NotebookRemover;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class NotebooksRecycleList extends AbstractRecyclerList<NotebookRemover, RecyclerNotebooksAdapter> {

    private static NotebooksRecycleList built;

    private static Future<NotebooksRecycleList> cache;

    private NotebooksRecycleList(int poolSize) {
        super(poolSize);
    }

    public static void buildCache() {
        built = null;

        // noinspection unchecked
        cache = (Future<NotebooksRecycleList>) new NotebooksRecycleList(10).build();
    }

    @NonNull
    @MainThread
    public static NotebooksRecycleList getList() {
        try {
            if (built == null) {
                built = cache.get(60, TimeUnit.SECONDS);
                cache = null;
            }
            return built;
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            throw new RuntimeException("Cannot load notebooks list.", e);
        }
    }

    @Override
    public NotebookRemover buildRemover(RecyclerView recyclerView) {
        return new NotebookRemover();
    }

    @Override
    public RecyclerNotebooksAdapter buildAdapter(@NonNull RecyclerView recyclerView) {
        return new RecyclerNotebooksAdapter(recyclerView);
    }
}
