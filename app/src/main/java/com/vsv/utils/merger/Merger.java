package com.vsv.utils.merger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Comparator;

public class Merger<T> {

    public interface Updater<T> {

        boolean rightUnique(@NonNull T rightItem, int index);

        boolean leftUnique(@NonNull T leftItem, int index);

        boolean equal(@NonNull T leftItem, @NonNull T rightItem, int leftIndex, int rightIndex);

    }

    private final FastComparator<T> fastComparator;

    public Merger(@NonNull ArrayList<T> left, @NonNull ArrayList<T> right, @NonNull Comparator<T> comparator, @NonNull Updater<T> updater) {
        TwoFastComparator.CompareProcess<T, T> process = new TwoFastComparator.CompareProcess<T, T>() {

            @Override
            public boolean leftBigger(@Nullable T left, @Nullable T right, int leftIndex, int rightIndex) {
                if (right != null) {
                    return updater.rightUnique(right, rightIndex);
                }
                return true;
            }

            @Override
            public boolean rightBigger(@Nullable T left, @Nullable T right, int leftIndex, int rightIndex) {
                if (left != null) {
                    return updater.leftUnique(left, leftIndex);
                }
                return true;
            }

            @Override
            public boolean equal(@Nullable T left, @Nullable T right, int leftIndex, int rightIndex) {
                if (left == null || right == null) {
                    return true;
                }
                return updater.equal(left, right, leftIndex, rightIndex);
            }

            @Override
            public boolean left(@Nullable T left, int leftIndex) {
                if (left != null) {
                    return updater.leftUnique(left, leftIndex);
                }
                return true;
            }

            @Override
            public boolean right(@Nullable T right, int rightIndex) {
                if (right != null) {
                    return updater.rightUnique(right, rightIndex);
                }
                return true;
            }
        };
        fastComparator = new FastComparator<>(left, right, comparator, process);
    }

    public void compare() {
        fastComparator.compare();
    }
}
