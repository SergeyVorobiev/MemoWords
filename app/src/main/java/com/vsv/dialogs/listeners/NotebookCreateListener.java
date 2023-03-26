package com.vsv.dialogs.listeners;

import androidx.annotation.NonNull;

@FunctionalInterface
public interface NotebookCreateListener {

    void createNotebook(@NonNull String name);
}
