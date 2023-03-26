package com.vsv.spreadsheet.entities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vsv.db.entities.Sample;
import com.vsv.utils.SheetDataBuilder;

import java.util.ArrayList;

public class SSDictData {

    @Nullable
    public final SheetDataBuilder.DictData dictionaryData;

    @NonNull
    public final ArrayList<Sample> samples;

    public SSDictData(@Nullable SheetDataBuilder.DictData dictionaryData, @NonNull ArrayList<Sample> samples) {
        this.dictionaryData = dictionaryData;
        this.samples = samples;
    }
}
