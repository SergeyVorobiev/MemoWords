package com.vsv.db.entities;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class DictionaryWithSamples {

    @Embedded
    public Dictionary dictionary;

    @Relation(parentColumn = "id", entityColumn = "dictionaryId")
    public List<Sample> samples;
}
