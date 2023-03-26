package com.vsv.db.dao;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import com.vsv.db.entities.Note;

import java.util.List;

@Dao
public interface NoteDao extends BaseDao<Note> {

    @Query("SELECT * FROM note WHERE notebookId = :notebookId")
    LiveData<List<Note>> getAllLiveFromNotebook(long notebookId);

    @Query("SELECT * FROM note WHERE notebookId = :notebookId AND name = :name")
    List<Note> findByName(long notebookId, String name);

    @Query("SELECT * FROM note WHERE notebookId = :notebookId")
    List<Note> getAllFromNotebook(long notebookId);

    @Query("SELECT * FROM note WHERE notebookId = :notebookId AND (name LIKE '%' || :name || '%')")
    List<Note> findLikeNameFromNotebook(long notebookId, String name);

    @Query("SELECT * FROM note WHERE id = :id LIMIT 1")
    LiveData<Note> findByIdLive(long id);

    @Query("SELECT * FROM note WHERE id = :id LIMIT 1")
    Note findById(long id);

    @Query("SELECT COUNT(*) FROM note")
    long count();

    @Query("SELECT COUNT(*) FROM note WHERE notebookId = :id")
    long countFromNotebook(long id);

    @Transaction
    default void deleteAndDecreaseCount(Note note, NotebookDao notebookDao) {
        delete(note);
        notebookDao.decrementNotesCount(note.getNotebookId());
    }
}
