package com.vsv.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Update;

import com.vsv.db.entities.DictNoteSpreadsheetNamesUpdater;
import com.vsv.db.entities.Notebook;
import com.vsv.db.entities.SheetDatesUpdate;

import java.util.List;

@Dao
public interface NotebookDao extends BaseDao<Notebook> {

    @Query("SELECT * FROM notebook")
    LiveData<List<Notebook>> getAllLive();

    @Query("SELECT * FROM notebook WHERE name = :name")
    List<Notebook> findByName(String name);

    @Query("SELECT * FROM notebook WHERE id = :id LIMIT 1")
    LiveData<Notebook> findById(long id);

    @Query("SELECT * FROM notebook")
    List<Notebook> getAll();

    @Query("SELECT * FROM notebook WHERE name LIKE '%' || :name || '%'")
    List<Notebook> findLikeName(String name);

    @Query("SELECT id FROM notebook")
    List<Long> getIds();

    @Query("SELECT COUNT(*) FROM notebook")
    long count();

    @Query("UPDATE notebook SET notes_count = notes_count + 1 WHERE id = :id")
    void incrementNotesCount(long id);

    @Query("UPDATE notebook SET notes_count = notes_count - 1 WHERE id = :id")
    void decrementNotesCount(long id);

    @Update(entity = Notebook.class)
    void updateDateSeveral(List<SheetDatesUpdate> entities);

    @Update(entity = Notebook.class)
    void updateSpreadsheetNames(List<DictNoteSpreadsheetNamesUpdater> entities);
}
