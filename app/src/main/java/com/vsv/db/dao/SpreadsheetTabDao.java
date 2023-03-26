package com.vsv.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import com.vsv.db.entities.SpreadSheetTabName;

import java.util.List;

@Dao
public interface SpreadsheetTabDao extends BaseDao<SpreadSheetTabName> {

    @Query("SELECT * FROM SpreadSheetTabName")
    LiveData<List<SpreadSheetTabName>> getAllLive();

    @Query("SELECT * FROM SpreadSheetTabName WHERE spreadSheetInfoId = :sheetId")
    LiveData<List<SpreadSheetTabName>> getAllLiveFromSheet(long sheetId);

    @Query("SELECT * FROM SpreadSheetTabName WHERE name = :name")
    List<SpreadSheetTabName> findByName(String name);

    @Query("SELECT * FROM SpreadSheetTabName WHERE id = :id LIMIT 1")
    LiveData<SpreadSheetTabName> findById(long id);

    @Query("SELECT * FROM SpreadSheetTabName WHERE name LIKE '%' || :name || '%'")
    List<SpreadSheetTabName> findLikeName(String name);
}
