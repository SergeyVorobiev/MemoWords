package com.vsv.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import com.vsv.db.entities.Shelf;

import java.util.List;

@Dao
public interface ShelfDao extends BaseDao<Shelf> {

    @Query("SELECT * FROM shelf")
    LiveData<List<Shelf>> getAllLive();

    @Query("SELECT * FROM shelf WHERE name = :name")
    List<Shelf> findByName(String name);

    @Query("SELECT * FROM shelf WHERE id = :id LIMIT 1")
    LiveData<Shelf> findById(long id);

    @Query("SELECT * FROM shelf")
    List<Shelf> getAll();

    @Query("SELECT * FROM shelf WHERE name LIKE '%' || :name || '%'")
    List<Shelf> findLikeName(String name);

    @Query("UPDATE shelf SET fontDictTitleIndex=:fontDictTitleIndex WHERE id = :id")
    void updateFontDictTitleIndex(long id, int fontDictTitleIndex);

    @Query("SELECT id FROM shelf")
    List<Long> getIds();
}
