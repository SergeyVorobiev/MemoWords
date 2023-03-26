package com.vsv.utils.merger;

import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

public class TwoFastComparator<T, B> {

    public interface LeftRightComparator<T, B> {
        int compare(@Nullable T left, @Nullable B right);
    }

    public interface CompareProcess<T, B> {

        boolean leftBigger(@Nullable T left, @Nullable B right, int leftIndex, int rightIndex);

        boolean rightBigger(@Nullable T left, @Nullable B right, int leftIndex, int rightIndex);

        boolean equal(@Nullable T left, @Nullable B right, int leftIndex, int rightIndex);

        boolean left(@Nullable T left, int leftIndex);

        boolean right(@Nullable B right, int rightIndex);
    }

    private final Collection<T> leftCollection;

    private final Collection<B> rightCollection;

    private final Comparator<T> leftComparator;

    private final Comparator<B> rightComparator;

    private final LeftRightComparator<T, B> lrComparator;

    private final CompareProcess<T, B> process;

    public TwoFastComparator(@NonNull Collection<T> leftCollection, @NonNull Collection<B> rightCollection,
                             @NonNull Comparator<T> leftComparator, @NonNull Comparator<B> rightComparator,
                             @NonNull LeftRightComparator<T, B> lrComparator, @NonNull CompareProcess<T, B> process) {
        this.leftCollection = leftCollection;
        this.rightCollection = rightCollection;
        this.leftComparator = leftComparator;
        this.rightComparator = rightComparator;
        this.lrComparator = lrComparator;
        this.process = process;
    }

    public void compare() {
        Pair<ArrayList<T>, ArrayList<B>> lists = fillAndSort();
        ArrayList<T> left = lists.first;
        ArrayList<B> right = lists.second;
        int leftSize = left.size();
        int rightSize = right.size();
        int leftIndex = 0;
        int rightIndex = 0;
        boolean run = true;
        while (run) {
            T leftValue = null;
            B rightValue = null;
            if (leftIndex >= leftSize && rightIndex >= rightSize) {
                break;
            }
            if (leftIndex < leftSize) {
                leftValue = left.get(leftIndex);
            }
            if (rightIndex < rightSize) {
                rightValue = right.get(rightIndex);
            }
            if (leftIndex >= leftSize) {
                run = process.right(rightValue, rightIndex);
                rightIndex++;
            } else if (rightIndex >= rightSize) {
                run = process.left(leftValue, leftIndex);
                leftIndex++;
            } else {
                int result = lrComparator.compare(leftValue, rightValue);
                if (result > 0) { // Left value is more than right value.
                    run = process.leftBigger(leftValue, rightValue, leftIndex, rightIndex);
                    rightIndex++;
                } else if (result < 0) { // Right value is more than left value.
                    run = process.rightBigger(leftValue, rightValue, leftIndex, rightIndex);
                    leftIndex++;
                } else {
                    run = process.equal(leftValue, rightValue, leftIndex, rightIndex);
                    leftIndex++;
                    rightIndex++;
                }
            }
        }
    }

    private Pair<ArrayList<T>, ArrayList<B>> fillAndSort() {
        ArrayList<T> firstList = new ArrayList<>(leftCollection);
        ArrayList<B> secondList = new ArrayList<>(rightCollection);
        firstList.sort(leftComparator);
        secondList.sort(rightComparator);
        return new Pair<>(firstList, secondList);
    }
}
