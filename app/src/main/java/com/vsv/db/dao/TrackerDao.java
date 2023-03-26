package com.vsv.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import com.vsv.db.entities.Tracker;

import java.util.List;

@Dao
public interface TrackerDao extends BaseDao<Tracker> {

    @Query("SELECT * FROM tracker WHERE dictionaryId = :dictionaryId")
    LiveData<List<Tracker>> getAllLiveFromDictionary(long dictionaryId);

    @Query("SELECT * FROM tracker WHERE dictionaryId = :dictionaryId")
    List<Tracker> getAllFromDictionary(long dictionaryId);

    @Query("SELECT COUNT(*) FROM tracker")
    long count();

    @Query("SELECT COUNT(*) FROM tracker WHERE dictionaryId = :dictionaryId")
    long countInDictionary(long dictionaryId);

    @Query("SELECT *, MIN(timestamp) FROM tracker")
    List<Tracker> getEarlierTracker();
}
