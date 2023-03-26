package com.vsv.repositories;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.vsv.db.dao.ShelfDao;
import com.vsv.db.entities.Shelf;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ShelvesRepository {

    private final ShelfDao dao;

    private final LiveData<List<Shelf>> allShelves;

    private final ExecutorService executor;

    public ShelvesRepository(ShelfDao dao, ExecutorService executor) {
        this.dao = dao;
        allShelves = this.dao.getAllLive();
        this.executor = executor;
    }

    public void delete(Shelf shelf) {
        executor.execute(() -> dao.delete(shelf));
    }

    public void insert(String name) {
        executor.execute(() -> {
            Shelf shelf = new Shelf(name);
            long id = dao.insert(shelf);
            shelf.setId(id);
        });
    }

    @Nullable
    public Shelf insertWithWaiting(String name, int timeoutMilliseconds) {
        try {
            return executor.submit(() -> {
                Shelf shelf = new Shelf(name);
                long id = dao.insert(shelf);
                shelf.setId(id);
                return shelf;
            }).get(timeoutMilliseconds, TimeUnit.MILLISECONDS);
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            return null;
        }
    }

    public void update(Shelf shelf) {
        executor.execute(() -> dao.update(shelf));
    }

    public void updateFontDictTitleIndex(long id, int fontDictTitleIndex) {
        executor.execute(() -> dao.updateFontDictTitleIndex(id, fontDictTitleIndex));
    }

    public Future<ArrayList<Long>> getAllIds() {
        return executor.submit(() -> (ArrayList<Long>) dao.getIds());
    }

    @NonNull
    public ArrayList<Long> getAllIdsOrEmpty(int timeoutInSeconds) {
        try {
            ArrayList<Long> result = getAllIds().get(timeoutInSeconds, TimeUnit.SECONDS);
            return result == null ? new ArrayList<>() : result;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @NonNull
    public LiveData<List<Shelf>> getAllShelves() {
        return allShelves;
    }
}
