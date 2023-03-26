package com.vsv.db.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "scoreDay")
public class ScoreDay {

    @PrimaryKey
    public long timestamp;

    public long score;

    public ScoreDay(long timestamp, long score) {
        this.timestamp = timestamp;
        this.score = score;
    }
}