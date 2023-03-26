package com.vsv.dialogs.listeners;

import com.vsv.db.entities.Shelf;

@FunctionalInterface
public interface UpdateShelfListener {

    void updateShelf(Shelf shelf);
}