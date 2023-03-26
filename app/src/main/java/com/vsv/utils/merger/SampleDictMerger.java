package com.vsv.utils.merger;

import androidx.annotation.NonNull;

import com.vsv.db.entities.Sample;

import java.util.ArrayList;

public class SampleDictMerger extends SimpleMerger<Sample> {

    private final long dictionaryId;

    private final MergeCollection<Sample> mergeCollection = new MergeCollection<>();

    public SampleDictMerger(@NonNull ArrayList<Sample> left, @NonNull ArrayList<Sample> right, long dictionaryId) {
        super(left, right);
        this.dictionaryId = dictionaryId;
    }

    @Override
    public boolean rightUnique(@NonNull Sample item, int number) {
        return true;
    }

    @Override
    public boolean leftUnique(@NonNull Sample item, int number) {
        item.setDictionaryId(dictionaryId);
        item.setId(0);
        mergeCollection.uniqueLefts.add(item);
        return true;
    }

    @Override
    public boolean equal(@NonNull Sample leftItem, @NonNull Sample rightItem, int leftIndex, int rightIndex) {
        boolean updated = false;
        if (leftItem.getLeftPercentage() > rightItem.getLeftPercentage() || leftItem.getRightPercentage() > rightItem.getRightPercentage()) {
            if (leftItem.getLeftPercentage() > rightItem.getLeftPercentage()) {
                rightItem.setLeftPercentage(leftItem.getLeftPercentage());
            }
            if (leftItem.getRightPercentage() > rightItem.getRightPercentage()) {
                rightItem.setRightPercentage(leftItem.getRightPercentage());
            }
            long leftTime = leftItem.answeredDate == null ? - 1 : leftItem.answeredDate.getTime();
            long rightTime = rightItem.answeredDate == null ? -1 : rightItem.answeredDate.getTime();
            if (leftTime > rightTime) {
                rightItem.answeredDate = leftItem.answeredDate;
                rightItem.lastCorrect = leftItem.lastCorrect;
                rightItem.correctSeries = leftItem.correctSeries;
            }
            updated = true;
        }
        if (!leftItem.getExample().isEmpty() && rightItem.getExample().isEmpty()) {
            rightItem.setExample(leftItem.getExample());
            updated = true;
        }
        if (!leftItem.getType().isEmpty() && rightItem.getType().isEmpty()) {
            rightItem.setType(leftItem.getType());
            updated = true;
        }
        if (updated) {
            mergeCollection.equal.add(rightItem);
        }
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
