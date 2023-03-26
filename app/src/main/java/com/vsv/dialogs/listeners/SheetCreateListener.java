package com.vsv.dialogs.listeners;

import androidx.annotation.NonNull;

@FunctionalInterface
public interface SheetCreateListener {

    void createSheet(@NonNull String name, @NonNull String id, int type);
}