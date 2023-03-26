package com.vsv.dialogs.listeners;

import androidx.annotation.NonNull;

@FunctionalInterface
public interface NoteCreateListener {

    void createNote(@NonNull String header, @NonNull String content, int number);
}
