package com.vsv.utils.merger;

import androidx.annotation.NonNull;

import java.util.Collection;
import java.util.Comparator;

public class FastComparator<T> {

    private final TwoFastComparator<T, T> fastComparator;

    public FastComparator(@NonNull Collection<T> leftCollection, @NonNull Collection<T> rightCollection,
                          @NonNull Comparator<T> comparator, @NonNull TwoFastComparator.CompareProcess<T, T> process) {
        fastComparator = new TwoFastComparator<>(leftCollection, rightCollection, comparator, comparator, comparator::compare, process);
    }

    public void compare() {
        fastComparator.compare();
    }
}
