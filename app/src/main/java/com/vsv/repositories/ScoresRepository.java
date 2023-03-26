package com.vsv.repositories;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.vsv.db.dao.ScoreDayDao;
import com.vsv.db.entities.ScoreDay;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class ScoresRepository {

    private final ScoreDayDao dao;

    private final ExecutorService executor;

    public ScoresRepository(ScoreDayDao dao, ExecutorService executor) {
        this.dao = dao;
        this.executor = executor;
    }

    public LiveData<List<ScoreDay>> getAllLive() {
        return dao.getAllLive();
    }

    public void addOrUpdate(@NonNull ScoreDay scoreDay) {
        executor.submit(() -> {
            dao.addOrUpdate(scoreDay);
        });
    }
}
