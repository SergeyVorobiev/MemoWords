package com.vsv.dialogs.listeners;

import androidx.annotation.Nullable;

public interface ExecuteOnResultListener<T> {

    void execute(@Nullable T result, @Nullable Exception exception);
}
