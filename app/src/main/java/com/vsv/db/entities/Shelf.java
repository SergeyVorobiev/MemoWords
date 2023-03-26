package com.vsv.db.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "shelf")
public class Shelf {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "name")
    private String name;

    public boolean sorted;

    public boolean hideRemembered;

    public int fontDictTitleIndex;

    public Shelf(long id, String name) {
        this.id = id;
        this.name = name;
        this.fontDictTitleIndex = 0;
    }

    @Ignore
    public Shelf(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
