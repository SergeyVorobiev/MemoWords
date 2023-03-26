package com.vsv.repositories;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.vsv.db.dao.NoteDao;
import com.vsv.db.dao.NotebookDao;
import com.vsv.db.entities.Note;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class NotesRepository {

    private final NoteDao dao;

    private final NotebookDao notebookDao;

    private final ExecutorService executor;

    public NotesRepository(NoteDao dao, NotebookDao notebookDao, ExecutorService executor) {
        this.dao = dao;
        this.notebookDao = notebookDao;
        this.executor = executor;
    }

    public void delete(Note note) {
        executor.execute(() -> dao.delete(note));
    }

    public void insert(long notebookId, String name, String content, int number) {
        executor.execute(() -> {
            Note note = new Note(notebookId, name, content, number);
            long id = dao.insert(note);
            note.setId(id);
        });
    }

    public void deleteAndDecrementNotesCount(Note note) {
        executor.execute(() -> {
            dao.deleteAndDecreaseCount(note, notebookDao);
        });
    }
    public void update(Note note) {
        executor.execute(() -> dao.update(note));
    }

    @NonNull
    public LiveData<List<Note>> getAllLiveFromNotebook(long notebookId) {
        return this.dao.getAllLiveFromNotebook(notebookId);
    }

    @NonNull
    public Future<ArrayList<Note>> getNotes(long notebookId) {
        return executor.submit(() -> (ArrayList<Note>) dao.getAllFromNotebook(notebookId));
    }

    public Future<?> deleteSeveral(List<Note> notes) {
        return executor.submit(() -> dao.deleteSeveral(notes));
    }

    public Future<?> insertSeveral(List<Note> notes) {
        return executor.submit(() -> dao.insertSeveral(notes));
    }

    public Future<?> updateSeveral(List<Note> notes) {
        return executor.submit(() -> dao.updateSeveral(notes));
    }

    public long count(int timeoutInSeconds) throws Exception {
        return executor.submit(dao::count).get(timeoutInSeconds, TimeUnit.SECONDS);
    }

    public long countOrZero(int timeoutInSeconds) {
        try {
            return count(timeoutInSeconds);
        } catch (Exception e) {
            return 0;
        }
    }

    public long countFromNotebook(long id, int timeoutInSeconds) throws Exception {
        return executor.submit(() -> dao.countFromNotebook(id)).get(timeoutInSeconds, TimeUnit.SECONDS);
    }

    public long countFromNotebookOrZero(long id, int timeoutInSeconds) {
        try {
            return countFromNotebook(id, timeoutInSeconds);
        } catch (Exception e) {
            return 0;
        }
    }
}
