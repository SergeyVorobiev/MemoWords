package com.vsv.models;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.vsv.db.DictionaryDB;
import com.vsv.db.dao.DictionaryDao;
import com.vsv.db.dao.SampleDao;
import com.vsv.db.dao.SettingsDao;
import com.vsv.db.entities.Settings;
import com.vsv.repositories.DictionariesRepository;
import com.vsv.repositories.DictionaryWithSamplesRepository;
import com.vsv.repositories.NotebookWithNotesRepository;
import com.vsv.repositories.NotebooksRepository;
import com.vsv.repositories.NotesRepository;
import com.vsv.repositories.SamplesRepository;
import com.vsv.repositories.ScoresRepository;
import com.vsv.repositories.ShelvesRepository;
import com.vsv.repositories.SpreadsheetsRepository;
import com.vsv.repositories.TrackerRepository;
import com.vsv.statics.GlobalExecutors;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MainModel extends AndroidViewModel {

    private final ShelvesRepository shelvesRepository;

    private final DictionariesRepository dictionariesRepository;

    private final DictionaryWithSamplesRepository dictionaryWithSamplesRepository;

    private final NotebookWithNotesRepository notebookWithNotesRepository;

    private final SpreadsheetsRepository spreadsheetsRepository;

    private final SamplesRepository samplesRepository;

    private final SettingsDao settingsDao;

    private final NotesRepository notesRepository;

    private final NotebooksRepository notebooksRepository;

    private final ScoresRepository scoresRepository;

    private final TrackerRepository trackerRepository;

    private final ExecutorService executor = Executors.newSingleThreadExecutor((runnable) -> {
        Thread thread = new Thread(runnable);
        thread.setName(getClass().getName());
        return thread;
    });

    public MainModel(Application application) {
        super(application);
        DictionaryDB db = DictionaryDB.getDatabase(application);
        this.shelvesRepository = new ShelvesRepository(db.shelfDao(), GlobalExecutors.modelsExecutor);
        DictionaryDao dictionaryDao = db.dictionaryDao();
        SampleDao sampleDao = db.sampleDao();
        this.dictionariesRepository = new DictionariesRepository(dictionaryDao, db.dictionaryWithSamplesDao(), GlobalExecutors.modelsExecutor);
        this.dictionaryWithSamplesRepository = new DictionaryWithSamplesRepository(dictionaryDao, sampleDao, GlobalExecutors.modelsExecutor);
        this.samplesRepository = new SamplesRepository(sampleDao, GlobalExecutors.modelsExecutor);
        this.scoresRepository = new ScoresRepository(db.scoreDayDao(), GlobalExecutors.modelsExecutor);
        this.trackerRepository = new TrackerRepository(db.trackerDao(), GlobalExecutors.modelsExecutor);
        this.spreadsheetsRepository = new SpreadsheetsRepository(db.spreadSheetDao(), GlobalExecutors.modelsExecutor);
        this.notesRepository = new NotesRepository(db.noteDao(), db.notebookDao(), GlobalExecutors.modelsExecutor);
        this.notebooksRepository = new NotebooksRepository(db.notebookDao(), GlobalExecutors.modelsExecutor);
        this.notebookWithNotesRepository = new NotebookWithNotesRepository(db.notebookDao(), db.noteDao(), GlobalExecutors.modelsExecutor);
        settingsDao = DictionaryDB.getDatabase(application).settingsDao();
    }

    @NonNull
    public Future<List<Settings>> getSettings() {
        return executor.submit(settingsDao::getAll);
    }

    public void update(Settings settings) {
        executor.submit(() -> settingsDao.update(settings));
    }

    public long insert(Settings settings) {
        try {
            return executor.submit(() -> settingsDao.insert(settings)).get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public Future<?> updateAndGetFuture(Settings settings) {
        return executor.submit(() -> settingsDao.update(settings));
    }

    public void updateFontDictIndex(long id, int fontIndex) {
        executor.submit(() -> settingsDao.updateFontDictTitleIndex(id, fontIndex));
    }

    public void updateFontSpreadsheetIndex(long id, int fontIndex) {
        executor.submit(() -> settingsDao.updateFontSpreadsheetTitleIndex(id, fontIndex));
    }

    public void updateFontNotebookIndex(long id, int fontIndex) {
        executor.submit(() -> settingsDao.updateFontNotebookTitleIndex(id, fontIndex));
    }

    @NonNull
    public Future<Settings> getSingleSettings() {
        return executor.submit(settingsDao::getSingle);
    }

    @NonNull
    public ShelvesRepository getShelvesRepository() {
        return shelvesRepository;
    }

    @NonNull
    public NotesRepository getNotesRepository() {
        return notesRepository;
    }

    @NonNull
    public NotebooksRepository getNotebooksRepository() {
        return notebooksRepository;
    }

    @NonNull
    public ScoresRepository getScoresRepository() {
        return scoresRepository;
    }

    @NonNull
    public TrackerRepository getTrackerRepository() {
        return trackerRepository;
    }

    @NonNull
    public DictionariesRepository getDictionariesRepository() {
        return dictionariesRepository;
    }

    @NonNull
    public DictionaryWithSamplesRepository getDictionaryWithSamplesRepository() {
        return dictionaryWithSamplesRepository;
    }

    @NonNull
    public NotebookWithNotesRepository getNotebookWithNotesRepository() {
        return notebookWithNotesRepository;
    }

    @NonNull
    public SamplesRepository getSamplesRepository() {
        return samplesRepository;
    }

    @NonNull
    public SpreadsheetsRepository getSpreadsheetsRepository() {
        return spreadsheetsRepository;
    }
}
