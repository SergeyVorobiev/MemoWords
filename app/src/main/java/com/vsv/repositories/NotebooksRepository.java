package com.vsv.repositories;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.vsv.db.dao.NotebookDao;
import com.vsv.db.entities.DictNoteSpreadsheetNamesUpdater;
import com.vsv.db.entities.Notebook;
import com.vsv.db.entities.SheetDatesUpdate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class NotebooksRepository {

    private final NotebookDao dao;

    private final LiveData<List<Notebook>> allNotebooks;

    private final ExecutorService executor;

    public NotebooksRepository(NotebookDao dao, ExecutorService executor) {
        this.dao = dao;
        allNotebooks = this.dao.getAllLive();
        this.executor = executor;
    }

    public void delete(Notebook notebook) {
        executor.execute(() -> dao.delete(notebook));
    }

    public void incrementNotesCount(long id) {
        executor.execute(() -> dao.incrementNotesCount(id));
    }

    public void decrementNotesCount(long id) {
        executor.execute(() -> dao.decrementNotesCount(id));
    }

    public void updateDateSeveral(List<SheetDatesUpdate> entities) {
        executor.execute(() -> dao.updateDateSeveral(entities));
    }

    public void updateSpreadsheetNames(List<DictNoteSpreadsheetNamesUpdater> entities) {
        executor.execute(() -> dao.updateSpreadsheetNames(entities));
    }

    public void insert(@NonNull String name, boolean canCopy, @Nullable String spreadsheetId, @Nullable String sheetName) {
        executor.execute(() -> {
            Notebook notebook = new Notebook(name);
            notebook.spreadsheetId = spreadsheetId;
            notebook.sheetName = sheetName;
            notebook.canCopy = canCopy;
            long id = dao.insert(notebook);
            notebook.setId(id);
        });
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

    public Future<ArrayList<Long>> getAllIds() {
        return executor.submit(() -> (ArrayList<Long>) dao.getIds());
    }

    public Future<Long> count() {
        return executor.submit(dao::count);
    }

    public long countOrDefault(int timeoutInSeconds, long defValue) {
        try {
            return count().get(timeoutInSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            return defValue;
        }
    }

    public void insert(@NonNull Notebook notebook) {
        executor.execute(() -> dao.insert(notebook));
    }

    public void update(Notebook notebook) {
        executor.execute(() -> dao.update(notebook));
    }

    @NonNull
    public LiveData<List<Notebook>> getAllNotebooks() {
        return allNotebooks;
    }
}
