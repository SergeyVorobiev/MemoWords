package com.vsv.utils.merger;

import android.content.res.Resources;
import android.graphics.ImageDecoder;
import android.graphics.drawable.AnimatedImageDrawable;

import androidx.annotation.NonNull;

import com.vsv.db.entities.Sample;

import java.util.ArrayList;

public class SampleSheetMerger extends SimpleMerger<Sample> {

    private final long dictionaryId;

    private final MergeCollection<Sample> mergeCollection = new MergeCollection<>();

    private final boolean replaceExamples;

    public SampleSheetMerger(@NonNull ArrayList<Sample> olds, @NonNull ArrayList<Sample> news, long dictionaryId, boolean replaceExamples) {
        super(olds, news);
        this.replaceExamples = replaceExamples;
        this.dictionaryId = dictionaryId;
    }

    @Override
    public boolean rightUnique(@NonNull Sample item, int number) {
        item.setDictionaryId(dictionaryId);
        mergeCollection.uniqueRights.add(item);
        return true;
    }

    @Override
    public boolean leftUnique(@NonNull Sample item, int number) {
        mergeCollection.uniqueLefts.add(item);
        return true;
    }

    @Override
    public boolean equal(@NonNull Sample updatedItem, @NonNull Sample newItem, int leftIndex, int rightIndex) {
        updatedItem.setType(newItem.getType());
        if (replaceExamples) {
            updatedItem.setExample(newItem.getExample());
        }
        mergeCollection.equal.add(updatedItem);
        return true;
    }

    @NonNull
    public MergeCollection<Sample> merge() {
        compare();
        return mergeCollection;
    }

    @Override
    public int compare(Sample left, Sample right) {
        if (left == null && right == null) {
            return 0;
        } else if (left != null && right != null) {
            if (left.getLeftValue().compareToIgnoreCase(right.getLeftValue()) == 0 && left.getRightValue().compareToIgnoreCase(right.getRightValue()) == 0) {
                return 0;
            } else if (left.getLeftValue().compareToIgnoreCase(right.getRightValue()) == 0 && left.getRightValue().compareToIgnoreCase(right.getLeftValue()) == 0) {
                return 0;
            } else {
                String string1 = left.getLeftValue() + left.getRightValue();
                String string2 = right.getLeftValue() + right.getRightValue();
                return string1.compareToIgnoreCase(string2);
            }
        } else if (left != null) {
            return 1;
        } else {
            return -1;
        }
    }
}
