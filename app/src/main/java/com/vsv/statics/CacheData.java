package com.vsv.statics;

import com.vsv.db.entities.DictionaryWithSamples;
import com.vsv.db.entities.NotebookWithNotes;
import com.vsv.db.entities.Sample;
import com.vsv.db.entities.SpreadSheetInfo;
import com.vsv.entities.TrainSample;

import java.util.ArrayList;

public class CacheData {

    private CacheData() {

    }

    public static final Cache<ArrayList<TrainSample>> cachedTrains = new Cache<>();

    public static final Cache<String> cachedContentPath = new Cache<>();

    public static final Cache<ArrayList<Sample>> cachedSamples = new Cache<>();

    public static final Cache<DictionaryWithSamples> samplesToAdd = new Cache<>();

    public static final Cache<NotebookWithNotes> notesToAdd = new Cache<>();

    public static final Cache<SpreadSheetInfo> spreadsheetToAdd = new Cache<>();

    public static void clearAll() {
        cachedTrains.clear();
        cachedSamples.clear();
    }
}
