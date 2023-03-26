package com.vsv.spreadsheet.entities;

import androidx.annotation.NonNull;

import com.vsv.db.entities.Note;
import com.vsv.utils.SheetDataBuilder;

import java.util.ArrayList;

public class SSNotebookData {

    @NonNull
    public final SheetDataBuilder.NotebookData notebookData;

    @NonNull
    public final ArrayList<Note> notes;

    public SSNotebookData(@NonNull SheetDataBuilder.NotebookData notebookData, @NonNull ArrayList<Note> notes) {
        this.notebookData = notebookData;
        this.notes = notes;
    }
}
