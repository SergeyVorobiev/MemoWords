package com.vsv.db.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "tracker", foreignKeys = {@ForeignKey(entity = Dictionary.class,
        parentColumns = "id",
        childColumns = "dictionaryId",
        onDelete = ForeignKey.CASCADE)},
        indices = {
                @Index("dictionaryId"),
        })
public class Tracker {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public long dictionaryId;

    public int progress;

    public long timestamp;

    public long score;

    public Tracker(long id, long dictionaryId, int progress, long score, long timestamp) {
        this.id = id;
        this.dictionaryId = dictionaryId;
        this.progress = progress;
        this.timestamp = timestamp;
        this.score = score;
    }

    @Ignore
    public Tracker(long dictionaryId, int progress, long score, long timestamp) {
        this.dictionaryId = dictionaryId;
        this.progress = progress;
        this.timestamp = timestamp;
        this.score = score;
    }
}
