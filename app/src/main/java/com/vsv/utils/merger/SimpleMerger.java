package com.vsv.utils.merger;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Comparator;

public abstract class SimpleMerger<T> implements Comparator<T>, Merger.Updater<T> {

    private final Merger<T> merger;

    public SimpleMerger(@NonNull ArrayList<T> left, @NonNull ArrayList<T> right) {
        merger = new Merger<>(left, right, this, this);
    }

    public void compare() {
        merger.compare();
    }
}
