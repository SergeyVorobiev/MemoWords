package com.vsv.db.entities;

import androidx.room.ColumnInfo;

public class DictionaryScoreUpdater {

    public long id;

    @ColumnInfo(name = "today_score")
    public long todayScore;

    @ColumnInfo(name = "timestamp_for_today_score")
    public long timestampForTodayScore;

    public DictionaryScoreUpdater(long id, long todayScore, long timestampForTodayScore) {
        this.id = id;
        this.todayScore = todayScore;
        this.timestampForTodayScore = timestampForTodayScore;
    }
}
