package com.vsv.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Update;

import com.vsv.db.entities.Dictionary;
import com.vsv.db.entities.DictionaryScoreUpdater;
import com.vsv.db.entities.DictNoteSpreadsheetNamesUpdater;
import com.vsv.db.entities.SheetDatesUpdate;

import java.util.List;

@Dao
public interface DictionaryDao extends BaseDao<Dictionary> {

    @Query("SELECT * FROM dictionary WHERE shelfId = :shelfId")
    LiveData<List<Dictionary>> getAllLiveFromShelf(long shelfId);

    @Query("SELECT * FROM dictionary WHERE shelfId = :shelfId AND name = :name")
    List<Dictionary> findByName(long shelfId, String name);

    @Query("SELECT * FROM dictionary WHERE shelfId = :shelfId")
    List<Dictionary> getAllFromShelf(long shelfId);

    @Query("SELECT * FROM dictionary WHERE shelfId = :shelfId AND (name LIKE '%' || :name || '%')")
    List<Dictionary> findLikeNameFromShelf(long shelfId, String name);

    @Query("SELECT * FROM dictionary WHERE id = :id LIMIT 1")
    LiveData<Dictionary> findByIdLive(long id);

    @Query("SELECT * FROM dictionary WHERE id = :id LIMIT 1")
    Dictionary findById(long id);

    @Query("SELECT COUNT(*) FROM dictionary")
    long count();

    @Query("SELECT COUNT(*) FROM dictionary WHERE shelfId = :shelfId")
    long countInShelf(long shelfId);

    @Query("SELECT id FROM dictionary WHERE shelfId = :shelfId")
    List<Long> getIds(long shelfId);

    @Query("SELECT passed_percentage FROM dictionary WHERE shelfId = :shelfId")
    List<Float> getPercentages(long shelfId);

    @Query("SELECT name FROM dictionary WHERE shelfId = :shelfId")
    List<String> getNames(long shelfId);

    @Update(entity = Dictionary.class)
    void updateDateSeveral(List<SheetDatesUpdate> entities);

    @Update(entity = Dictionary.class)
    void updateSpreadsheetNames(List<DictNoteSpreadsheetNamesUpdater> entities);

    @Update(entity = Dictionary.class)
    void updateScore(List<DictionaryScoreUpdater> scoreUpdaters);
}
