package com.vsv.io;

public class Storage {

    private static StorageCSV storage;

    public static StorageCSV getDocumentsStorage() {
        if (storage == null) {
            storage = new StorageCSV();
        }
        return new StorageCSV();
    }
}
