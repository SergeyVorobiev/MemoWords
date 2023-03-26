package com.vsv.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import com.vsv.db.entities.SpreadSheetInfo;

import java.util.List;

@Dao
public interface SpreadsheetDao extends BaseDao<SpreadSheetInfo> {

    @Query("SELECT * FROM SpreadSheetInfo")
    List<SpreadSheetInfo> getAll();

    @Query("SELECT * FROM SpreadSheetInfo")
    LiveData<List<SpreadSheetInfo>> getAllLive();

    @Query("SELECT * FROM SpreadSheetInfo WHERE name = :name")
    List<SpreadSheetInfo> findByName(String name);

    @Query("SELECT * FROM SpreadSheetInfo WHERE id = :id LIMIT 1")
    LiveData<SpreadSheetInfo> findById(long id);

    @Query("SELECT * FROM SpreadSheetInfo WHERE name LIKE '%' || :name || '%'")
    List<SpreadSheetInfo> findLikeName(String name);
}
