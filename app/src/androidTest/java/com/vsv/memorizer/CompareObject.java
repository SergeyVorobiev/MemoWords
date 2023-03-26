package com.vsv.memorizer;

import androidx.annotation.NonNull;

public class CompareObject {

    public int index;

    public String value;

    public CompareObject(int index, String value) {
        this.index = index;
        this.value = value;
    }

    @NonNull
    @Override
    public String toString() {
        return "(" + index + "; " + value + ")";
    }
}
