package com.vsv.memorizer;

import android.content.Context;
import android.util.Log;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.vsv.db.DictionaryDB;
import com.vsv.db.entities.Dictionary;
import com.vsv.db.entities.DictionaryWithSamples;
import com.vsv.db.entities.Sample;
import com.vsv.db.entities.Shelf;
import com.vsv.db.entities.Tracker;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class DBTest {

    private DictionaryDB db;

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, DictionaryDB.class).build();
    }

    @After
    public void closeDb() throws IOException {
        db.close();
    }

    private void live(List<Dictionary> dict) {
        List<String> result = dict.stream().map(Dictionary::getName).collect(Collectors.toList());
    }

    @Test
    public  void trackerTest() {
        Shelf shelf = new Shelf("shelf3");
        long shelfId = db.shelfDao().insert(shelf);
        Dictionary dictionary1 = new Dictionary(shelfId, "eng3");
        Dictionary dictionary2 = new Dictionary(shelfId, "elo4");
        long id1 = db.dictionaryDao().insert(dictionary1);
        long id2 = db.dictionaryDao().insert(dictionary2);
        Tracker tracker = new Tracker(id1, 15, 4, 1);
        Tracker tracker1 = new Tracker(id1, 20, 2, 1);
        Tracker tracker2 = new Tracker(id2, 30, 6, 1);
        db.trackerDao().insert(tracker);
        db.trackerDao().insert(tracker1);
        db.trackerDao().insert(tracker2);
        List<Tracker> trackers = db.trackerDao().getEarlierTracker();
        System.out.println(trackers.get(0).progress);
        System.out.println(trackers.get(0).id);
        System.out.println(trackers.get(0).timestamp);
    }

    @Test
    public void liveTest() throws Exception {
        Shelf shelf = new Shelf("shelf3");
        long shelfId = db.shelfDao().insert(shelf);
        db.dictionaryDao().getAllLiveFromShelf(shelfId).observeForever(this::live);
        Dictionary dictionary1 = new Dictionary(shelfId, "eng1");
        Dictionary dictionary2 = new Dictionary(shelfId, "eng2");
        Dictionary dictionary3 = new Dictionary(shelfId, "eng3");
        Dictionary dictionary4 = new Dictionary(shelfId, "elo4");
        long id1 = db.dictionaryDao().insert(dictionary1);
        long id2 = db.dictionaryDao().insert(dictionary2);
        long id3 = db.dictionaryDao().insert(dictionary3);
        long id4 = db.dictionaryDao().insert(dictionary4);
        dictionary1.setId(id1);
        dictionary2.setId(id2);
        dictionary3.setId(id3);
        dictionary4.setId(id4);
        List<Dictionary> dicts = db.dictionaryDao().findLikeNameFromShelf(shelfId, "el");
        List<String> result = dicts.stream().map(Dictionary::getName).collect(Collectors.toList());
        Log.d("Dict2", result.toString());
        db.dictionaryDao().findByName(shelfId, "eng2");
        db.dictionaryDao().delete(dictionary1);
        dictionary3.setName("eng4");
        db.dictionaryDao().update(dictionary3);
    }

    @Test
    public void dictionaryTest() throws Exception {
        Shelf shelf = new Shelf("shelf3");
        long shelfId = db.shelfDao().insert(shelf);
        Dictionary dictionary = new Dictionary(shelfId, "eng");
        Dictionary dictionary2 = new Dictionary(shelfId, "eng2");
        long id = db.dictionaryDao().insert(dictionary);
        long id2 = db.dictionaryDao().insert(dictionary2);
        dictionary.setId(id);
        dictionary2.setId(id2);
        Sample sample = new Sample(dictionary.getId(), "hello", "world", 0, 0);
        Sample sample2 = new Sample(dictionary.getId(), "hello2", "world", 0, 0);
        Sample sample3 = new Sample(dictionary2.getId(), "hello3", "world2", 0, 0);
        Sample sample4 = new Sample(dictionary2.getId(), "hello4", "world2", 0, 0);
        db.sampleDao().insert(sample);
        db.sampleDao().insert(sample2);
        db.sampleDao().insert(sample3);
        db.sampleDao().insert(sample4);

        List<Dictionary> dict = db.dictionaryDao().findByName(shelfId,"eng");
        List<Dictionary> dict2 = db.dictionaryDao().findByName(shelfId,"eng2");
        assertThat(dict.size(), equalTo(1));
        assertThat(dict2.size(), equalTo(1));
        assertThat(dict.get(0).getName(), equalTo("eng"));
        assertThat(dict2.get(0).getName(), equalTo("eng2"));

        List<Sample> samples = db.sampleDao().findByValue(1,"hello");
        List<Sample> samples2 = db.sampleDao().findByValue(1,"world");
        List<Sample> samples3 = db.sampleDao().getAll();
        assertThat(samples.size(), equalTo(1));
        assertThat(samples2.size(), equalTo(2));
        assertThat(samples3.size(), equalTo(4));

        List<Sample> dictSamples = db.sampleDao().getAllfromDictionary(dictionary.getId());
        assertThat(dictSamples.size(), equalTo(2));

        List<DictionaryWithSamples> dictsWithSamples = db.dictionaryWithSamplesDao().getAll();
        assertThat(dictsWithSamples.size(), equalTo(2));

        db.dictionaryDao().delete(dictionary);
        List<Sample> allSamples = db.sampleDao().getAll();
        ArrayList<Dictionary> allDictionaries = (ArrayList<Dictionary>) db.dictionaryDao().getAllFromShelf(shelfId);
        assertThat(allDictionaries.size(), equalTo(1));
        assertThat(allSamples.size(), equalTo(2));
    }
}
