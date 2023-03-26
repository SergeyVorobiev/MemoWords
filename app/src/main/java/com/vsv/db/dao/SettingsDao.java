package com.vsv.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import com.vsv.db.entities.Settings;

import java.util.List;

@Dao
public interface SettingsDao extends BaseDao<Settings> {

    @Query("SELECT * FROM settings")
    LiveData<List<Settings>> getAllLive();

    @Query("SELECT * FROM settings")
    List<Settings> getAll();

    @Query("SELECT * FROM settings LIMIT 1")
    Settings getSingle();

    @Query("UPDATE settings SET fontDictTitleIndex=:fontDictTitleIndex WHERE id = :id")
    void updateFontDictTitleIndex(long id, int fontDictTitleIndex);

    @Query("UPDATE settings SET fontSpreadsheetTitleIndex=:fontSpreadsheetTitleIndex WHERE id = :id")
    void updateFontSpreadsheetTitleIndex(long id, int fontSpreadsheetTitleIndex);

    @Query("UPDATE settings SET fontNotebookTitleIndex=:fontNotebookTitleIndex WHERE id = :id")
    void updateFontNotebookTitleIndex(long id, int fontNotebookTitleIndex);
}
