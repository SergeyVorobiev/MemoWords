package com.vsv.dialogs.listeners;

import com.vsv.db.entities.Notebook;

@FunctionalInterface
public interface UpdateNotebookListener {

    void updateNotebook(Notebook notebook);
}
