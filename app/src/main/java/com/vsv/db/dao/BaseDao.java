package com.vsv.db.dao;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Update;

import java.util.List;

public interface BaseDao<T> {

    @Insert
    void insertSeveral(List<T> items);

    @Insert
    long insert(T item);

    @Update
    void update(T item);

    @Update
    void updateSeveral(List<T> items);

    @Delete
    void delete(T item);

    @Delete
    void deleteSeveral(List<T> items);
}
