package com.vsv.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import com.vsv.db.entities.Sample;

import java.util.List;

@Dao
public interface SampleDao extends BaseDao<Sample> {

    @Query("Select * FROM sample")
    List<Sample> getAll();

    @Query("Select * FROM sample WHERE dictionaryId = :dictionaryId")
    List<Sample> getAllfromDictionary(long dictionaryId);

    @Query("Select * FROM sample WHERE dictionaryId = :dictionaryId")
    LiveData<List<Sample>> getAllLiveFromDictionary(long dictionaryId);

    @Query("SELECT * FROM sample WHERE dictionaryId = :dictionaryId AND (left_value = :value OR right_value = :value)")
    List<Sample> findByValue(long dictionaryId, String value);

    @Query("SELECT * FROM sample WHERE left_value LIKE '%' || :value || '%' OR right_value LIKE '%' || :value || '%'")
    List<Sample> findLikeValue(String value);

    @Query("SELECT COUNT(*) FROM sample")
    long count();

    @Query("SELECT COUNT(*) FROM sample WHERE dictionaryId = :dictionaryId")
    long countInDictionary(long dictionaryId);
}
