package com.vsv.repositories;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.vsv.db.dao.TrackerDao;
import com.vsv.db.entities.Tracker;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class TrackerRepository {

    private final TrackerDao dao;

    private final ExecutorService executor;

    public TrackerRepository(TrackerDao dao, ExecutorService executor) {
        this.dao = dao;
        this.executor = executor;
    }

    public LiveData<List<Tracker>> getAllLive(long dictionaryId) {
        return dao.getAllLiveFromDictionary(dictionaryId);
    }

    public void insert(@NonNull Tracker tracker) {
        executor.submit(() -> {
            dao.insert(tracker);
        });
    }
}
