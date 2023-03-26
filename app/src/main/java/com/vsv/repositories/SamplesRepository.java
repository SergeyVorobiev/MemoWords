package com.vsv.repositories;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.vsv.db.dao.SampleDao;
import com.vsv.db.entities.Sample;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SamplesRepository {

    private final SampleDao dao;

    private final ExecutorService executor;

    public SamplesRepository(SampleDao dao, ExecutorService executor) {
        this.dao = dao;
        this.executor = executor;
    }

    @NonNull
    public Future<ArrayList<Sample>> getSamples(long dictionaryId) {
        return executor.submit(() -> (ArrayList<Sample>) dao.getAllfromDictionary(dictionaryId));
    }

    @Nullable
    public ArrayList<Sample> getSamplesWithTimeout(long dictionaryId, int timeout) {
        try {
            return getSamples(dictionaryId).get(timeout, TimeUnit.SECONDS);
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            return null;
        }
    }

    public void delete(Sample sample) {
        executor.execute(() -> dao.delete(sample));
    }

    public void insert(long dictionaryId, String leftValue, String rightValue, String kind, String example, boolean lastCorrect, int correctSeries) {
        executor.execute(() -> {
            Sample sample = new Sample(dictionaryId, leftValue, rightValue, 0, 0);
            sample.setType(kind);
            sample.setExample(example);
            sample.lastCorrect = lastCorrect;
            sample.correctSeries = correctSeries;
            long id = dao.insert(sample);
            sample.setId(id);
        });
    }

    public void update(Sample sample) {
        executor.execute(() -> dao.update(sample));
    }

    public void updateSeveral(@NonNull List<Sample> samples) {
        executor.execute(() -> dao.updateSeveral(samples));
    }

    public void deleteSeveral(@NonNull List<Sample> samples) {
        executor.execute(() -> dao.deleteSeveral(samples));
    }

    public void insertSeveral(@NonNull List<Sample> samples) {
        executor.execute(() -> dao.insertSeveral(samples));
    }

    public boolean updateSeveralWaiting(@NonNull List<Sample> samples, int timeout) {
        try {
            executor.submit(() -> dao.updateSeveral(samples)).get(timeout, TimeUnit.SECONDS);
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            return false;
        }
        return true;
    }

    public boolean deleteSeveralWaiting(@NonNull List<Sample> samples, int timeout) {
        try {
            executor.submit(() -> dao.deleteSeveral(samples)).get(timeout, TimeUnit.SECONDS);
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            return false;
        }
        return true;
    }

    public boolean insertSeveralWaiting(@NonNull List<Sample> samples, int timeout) {
        try {
            executor.submit(() -> dao.insertSeveral(samples)).get(timeout, TimeUnit.SECONDS);
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            return false;
        }
        return true;
    }

    @NonNull
    public LiveData<List<Sample>> getLiveSamples(long dictId) {
        return dao.getAllLiveFromDictionary(dictId);
    }

    public Future<Long> getAllSamplesCount() {
        return executor.submit(dao::count);
    }

    public Future<Long> count() {
        return executor.submit(dao::count);
    }

    public Future<Long> countInDictionary(long dictionaryId) {
        return executor.submit(() -> dao.countInDictionary(dictionaryId));
    }

    public long count(int timeout) throws Exception {
        return executor.submit(dao::count).get(timeout, TimeUnit.SECONDS);
    }

    public long countOrZero(int timeoutInSeconds) {
        try {
            return count().get(timeoutInSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            return 0;
        }
    }
}
