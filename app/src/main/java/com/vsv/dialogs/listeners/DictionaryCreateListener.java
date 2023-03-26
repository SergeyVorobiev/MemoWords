package com.vsv.dialogs.listeners;

import androidx.annotation.NonNull;

@FunctionalInterface
public interface DictionaryCreateListener {

    void createDictionary(@NonNull String name, @NonNull String leftLanguageAbb, @NonNull String rightLanguageAbb);
}
