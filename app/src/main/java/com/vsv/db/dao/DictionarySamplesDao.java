package com.vsv.db.dao;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.vsv.db.entities.Dictionary;
import com.vsv.db.entities.DictionaryWithSamples;
import com.vsv.db.entities.Sample;

import java.util.List;

@Dao
public abstract class DictionarySamplesDao {

    @Update
    public abstract void update(Sample item);

    @Update
    public abstract void update(Dictionary item);

    @Transaction
    @Query("SELECT * FROM dictionary")
    public abstract List<DictionaryWithSamples> getAll();

    @Transaction
    @Query("SELECT * FROM dictionary WHERE id= :id LIMIT 1")
    public abstract DictionaryWithSamples getDictionaryWithSamples(long id);

    @Transaction
    public void moveSample(Dictionary from, Dictionary to, Sample sample) {
        from.setCount(from.getCount() - 1);
        to.setCount(to.getCount() + 1);
        sample.setDictionaryId(to.getId());
        update(from);
        update(to);
        update(sample);
    }
}
