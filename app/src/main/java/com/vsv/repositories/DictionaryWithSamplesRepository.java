package com.vsv.repositories;

import androidx.annotation.NonNull;

import com.vsv.db.dao.DictionaryDao;
import com.vsv.db.dao.SampleDao;
import com.vsv.db.entities.Dictionary;
import com.vsv.db.entities.DictionaryWithSamples;
import com.vsv.db.entities.Sample;
import com.vsv.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class DictionaryWithSamplesRepository {

    private final DictionaryDao dictionaryDao;

    private final SampleDao sampleDao;

    private final ExecutorService executor;

    public DictionaryWithSamplesRepository(DictionaryDao dictionaryDao, SampleDao sampleDao, ExecutorService executor) {
        this.dictionaryDao = dictionaryDao;
        this.sampleDao = sampleDao;
        this.executor = executor;
    }

    public Future<?> insertWithSamples(@NonNull Dictionary dictionary, @NonNull ArrayList<Sample> samples) {
        return executor.submit(() -> {
            dictionary.setCount(samples.size());
            long id = dictionaryDao.insert(dictionary);
            dictionary.setId(id);
            for (Sample sample : samples) {
                sample.setDictionaryId(id);
            }
            sampleDao.insertSeveral(samples);
        });
    }

    public Future<?> insertWithSamples(@NonNull DictionaryWithSamples dictionaryWithSamples) {
        return executor.submit(() -> {
            Dictionary dictionary = dictionaryWithSamples.dictionary;
            if (dictionary == null) {
                return;
            }
            List<Sample> samples = dictionaryWithSamples.samples;
            dictionary.setCount(samples == null ? 0 : samples.size());
            long id = dictionaryDao.insert(dictionary);
            dictionary.setId(id);
            if (samples == null) {
                return;
            }
            for (Sample sample : samples) {
                sample.setDictionaryId(id);
            }
            sampleDao.insertSeveral(samples);
        });
    }

    public void insertWithSamples(long shelfId, String name, String leftLocaleAbb, String rightLocaleAbb, boolean canCopy,
                                  String spreadsheetId, String sheetName, long sheetId, @NonNull ArrayList<Sample> samples) {
        executor.execute(() -> {
            Dictionary dictionary = new Dictionary(shelfId, name);
            dictionary.setLeftLocaleAbb(leftLocaleAbb);
            dictionary.setRightLocaleAbb(rightLocaleAbb);
            dictionary.canCopy = canCopy;
            dictionary.spreadsheetId = spreadsheetId;
            dictionary.sheetName = sheetName;
            dictionary.setCount(samples.size());
            dictionary.setRememberedCount((int) samples.stream().filter(Sample::isRemembered).count());
            dictionary.sheetId = sheetId;
            long id = dictionaryDao.insert(dictionary);
            dictionary.setId(id);
            for (Sample sample : samples) {
                sample.setDictionaryId(id);
            }
            sampleDao.insertSeveral(samples);
        });
    }

    public void insertWithSamplesAndUpdateDate(long shelfId, String name, String leftLocaleAbb,
                                               String rightLocaleAbb, boolean canCopy,
                                               String spreadsheetId, String sheetName, long sheetId,
                                               float passedPercentage, int rememberedCount,
                                               @NonNull ArrayList<Sample> samples) {
        executor.execute(() -> {
            Dictionary dictionary = new Dictionary(shelfId, name);
            dictionary.setLeftLocaleAbb(leftLocaleAbb);
            dictionary.setRightLocaleAbb(rightLocaleAbb);
            dictionary.canCopy = canCopy;
            dictionary.spreadsheetId = spreadsheetId;
            dictionary.sheetName = sheetName;
            dictionary.setCount(samples.size());
            dictionary.setRememberedCount((int) samples.stream().filter(Sample::isRemembered).count());
            dictionary.sheetId = sheetId;
            dictionary.setPassedPercentage(passedPercentage);
            dictionary.setRememberedCount(rememberedCount);
            dictionary.dataDate = DateUtils.getCurrentDate();
            dictionary.successfulUpdateCheck = DateUtils.getCurrentDate();
            dictionary.needUpdate = false;
            long id = dictionaryDao.insert(dictionary);
            dictionary.setId(id);
            for (Sample sample : samples) {
                sample.setDictionaryId(id);
            }
            sampleDao.insertSeveral(samples);
        });
    }
}
