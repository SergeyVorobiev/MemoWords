package com.vsv.repositories;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.vsv.db.dao.DictionaryDao;
import com.vsv.db.dao.DictionarySamplesDao;
import com.vsv.db.entities.Dictionary;
import com.vsv.db.entities.DictionaryScoreUpdater;
import com.vsv.db.entities.DictNoteSpreadsheetNamesUpdater;
import com.vsv.db.entities.Sample;
import com.vsv.db.entities.SheetDatesUpdate;
import com.vsv.statics.WeakContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class DictionariesRepository {

    private final DictionaryDao dao;

    private final DictionarySamplesDao dictSampleDao;

    private final ExecutorService executor;

    public DictionariesRepository(@NonNull DictionaryDao dao, @NonNull DictionarySamplesDao dictSampleDao, @NonNull ExecutorService executor) {
        this.dao = dao;
        this.dictSampleDao = dictSampleDao;
        this.executor = executor;
    }

    @NonNull
    public LiveData<List<Dictionary>> getAllLiveFromShelf(long shelfId) {
        return this.dao.getAllLiveFromShelf(shelfId);
    }

    public void delete(Dictionary dictionary) {
        executor.execute(() -> dao.delete(dictionary));
    }

    public void moveSampleFromTo(@NonNull Dictionary fromDictionary, @NonNull Dictionary toDictionary, @NonNull Sample sample) {
        executor.execute(() -> dictSampleDao.moveSample(fromDictionary, toDictionary, sample));
    }

    public void insert(long shelfId, String name, String leftLocaleAbb, String rightLocaleAbb, String spreadsheetId, String sheetName, long sheetId, boolean canCopy) {
        executor.execute(() -> {
            Dictionary dictionary = new Dictionary(shelfId, name);
            dictionary.setLeftLocaleAbb(leftLocaleAbb);
            dictionary.setRightLocaleAbb(rightLocaleAbb);
            dictionary.canCopy = canCopy;
            dictionary.spreadsheetId = spreadsheetId;
            dictionary.sheetName = sheetName;
            long id = dao.insert(dictionary);
            dictionary.setId(id);
        });
    }

    public Future<Dictionary> getDictionary(long dictId) {
        return executor.submit(() -> dao.findById(dictId));
    }

    public Future<Dictionary> insert(Dictionary dictionary) {
        return executor.submit(() -> {
            long id = dao.insert(dictionary);
            dictionary.setId(id);
            return dictionary;
        });
    }

    public Future<?> update(Dictionary dictionary) {
        return executor.submit(() -> dao.update(dictionary));
    }

    public Future<ArrayList<Dictionary>> getAllDictionaries(long shelfId) {
        return executor.submit(() -> (ArrayList<Dictionary>) dao.getAllFromShelf(shelfId));
    }

    public Future<ArrayList<Long>> getAllIds(long shelfId) {
        return executor.submit(() -> (ArrayList<Long>) dao.getIds(shelfId));
    }

    public Future<Long> count() {
        return executor.submit(dao::count);
    }

    public Future<Long> countInShelf(long shelfId) {
        return executor.submit(() -> dao.countInShelf(shelfId));
    }

    public long countInShelfOrDefault(long shelfId, int timeoutInSeconds, long defValue) {
        try {
            return countInShelf(shelfId).get(timeoutInSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            return defValue;
        }
    }

    public Future<ArrayList<Float>> getAllPercentages(long shelfId) {
        return executor.submit(() -> (ArrayList<Float>) dao.getPercentages(shelfId));
    }

    public Future<ArrayList<String>> getAllNames(long shelfId) {
        return executor.submit(() -> (ArrayList<String>) dao.getNames(shelfId));
    }

    @NonNull
    public ArrayList<String> getAllNamesOrEmpty(long shelfId, int timeoutInSeconds) {
        try {
            ArrayList<String> result = getAllNames(shelfId).get(timeoutInSeconds, TimeUnit.SECONDS);
            return result == null ? new ArrayList<>() : result;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @NonNull
    public ArrayList<Float> getAllPercentagesOrEmpty(long shelfId, int timeoutInSeconds) {
        try {
            ArrayList<Float> result = getAllPercentages(shelfId).get(timeoutInSeconds, TimeUnit.SECONDS);
            return result == null ? new ArrayList<>() : result;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public void countAndThen(Consumer<Long> mainThreadRunnable) {
        executor.execute(() -> {
            long count = dao.count();
            WeakContext.getMainActivity().runOnUiThread(() -> mainThreadRunnable.accept(count));
        });
    }

    public void updateDateSeveral(List<SheetDatesUpdate> entities) {
        executor.execute(() -> dao.updateDateSeveral(entities));
    }

    public void updateSpreadsheetNames(List<DictNoteSpreadsheetNamesUpdater> entities) {
        executor.execute(() -> dao.updateSpreadsheetNames(entities));
    }

    public void updateScore(ArrayList<DictionaryScoreUpdater> scoreUpdaters) {
        executor.execute(() -> dao.updateScore(scoreUpdaters));
    }

    public long countOrDefault(int timeoutInSeconds, long defValue) {
        try {
            return count().get(timeoutInSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            return defValue;
        }
    }
}
