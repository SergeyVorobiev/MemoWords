package com.vsv.recyclerentities;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vsv.memorizer.adapters.RecyclerNotesAdapter;
import com.vsv.removeitems.NoteRemover;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class NotesRecycleList extends AbstractRecyclerList<NoteRemover, RecyclerNotesAdapter> {

    private static NotesRecycleList built;

    private static Future<NotesRecycleList> cache;

    private NotesRecycleList(int poolSize) {
        super(poolSize);
    }

    public static void buildCache() {
        built = null;

        // noinspection unchecked
        cache = (Future<NotesRecycleList>) new NotesRecycleList(15).build();
    }

    @NonNull
    @MainThread
    public static NotesRecycleList getList() {
        try {
            if (built == null) {
                built = cache.get(60, TimeUnit.SECONDS);
                cache = null;
            }
            return built;
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            throw new RuntimeException("Cannot load notes list.", e);
        }
    }

    @Override
    public NoteRemover buildRemover(RecyclerView recyclerView) {
        return new NoteRemover();
    }

    @Override
    public RecyclerNotesAdapter buildAdapter(@NonNull RecyclerView recyclerView) {
        return new RecyclerNotesAdapter(recyclerView);
    }
}
