package com.vsv.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import com.vsv.db.entities.ScoreDay;

import java.util.List;

@Dao
public interface ScoreDayDao extends BaseDao<ScoreDay> {

    @Query("SELECT * FROM scoreDay")
    LiveData<List<ScoreDay>> getAllLive();


    @Query("SELECT * FROM scoreDay")
    List<ScoreDay> getAll();

    @Query("SELECT * FROM scoreDay WHERE timestamp = :timestamp LIMIT 1")
    ScoreDay findById(long timestamp);

    @Query("SELECT COUNT(*) FROM scoreDay")
    long count();

    @Transaction
    default void addOrUpdate(ScoreDay scoreDay) {
        ScoreDay existed = findById(scoreDay.timestamp);
        if (existed == null) {
            insert(scoreDay);
        } else {
            scoreDay.score += existed.score;
            update(scoreDay);
        }
    }
}
