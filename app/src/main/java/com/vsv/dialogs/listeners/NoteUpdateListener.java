package com.vsv.dialogs.listeners;

import androidx.annotation.NonNull;

import com.vsv.db.entities.Note;

@FunctionalInterface
public interface NoteUpdateListener {

    void updateNote(@NonNull Note note);
}
