package com.vsv.repositories;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vsv.db.dao.NoteDao;
import com.vsv.db.dao.NotebookDao;
import com.vsv.db.entities.Note;
import com.vsv.db.entities.Notebook;
import com.vsv.spreadsheet.entities.SSNotebookData;
import com.vsv.utils.DateUtils;
import com.vsv.utils.SheetDataBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class NotebookWithNotesRepository {

    private final NotebookDao notebookDao;

    private final NoteDao noteDao;

    private final ExecutorService executor;

    public NotebookWithNotesRepository(NotebookDao notebookDao, NoteDao noteDao, ExecutorService executor) {
        this.notebookDao = notebookDao;
        this.noteDao = noteDao;
        this.executor = executor;
    }

    public void insertWithSamples(@NonNull String notebookName, boolean canCopy, @Nullable String spreadsheetId, @Nullable String sheetName, long sheetId, @NonNull ArrayList<Note> notes) {
        executor.execute(() -> {
            Notebook notebook = new Notebook(notebookName);
            notebook.spreadsheetId = spreadsheetId;
            notebook.sheetName = sheetName;
            notebook.canCopy = canCopy;
            notebook.notesCount = notes.size();
            long id = notebookDao.insert(notebook);
            for (Note note : notes) {
                note.setNotebookId(id);
            }
            noteDao.insertSeveral(notes);
        });
    }

    public void insertWithSamples(@NonNull SSNotebookData ssNotebookData) {
        executor.execute(() -> {
            SheetDataBuilder.NotebookData notebookData = ssNotebookData.notebookData;
            Notebook notebook = new Notebook(notebookData.notebookName);
            notebook.spreadsheetName = notebookData.spreadsheetName;
            notebook.spreadsheetId = notebookData.spreadsheetId;
            notebook.sheetName = notebookData.sheetName;
            notebook.canCopy = notebookData.canCopy;
            notebook.notesCount = ssNotebookData.notes.size();
            notebook.sheetId = notebookData.sheetId;
            notebook.dataDate = notebookData.dataDate;
            notebook.updateCheck = notebookData.updateCheck;
            notebook.needUpdate = false;
            notebook.author = notebookData.author;
            long id = notebookDao.insert(notebook);
            for (Note note : ssNotebookData.notes) {
                note.setNotebookId(id);
            }
            noteDao.insertSeveral(ssNotebookData.notes);
        });
    }

    // It does not update note counts for notebook.
    public void insertWithSamples(@NonNull Notebook notebook, @NonNull List<Note> notes) {
        executor.execute(() -> {
            long id = notebookDao.insert(notebook);
            for (Note note : notes) {
                note.setNotebookId(id);
            }
            noteDao.insertSeveral(notes);
        });
    }
}
