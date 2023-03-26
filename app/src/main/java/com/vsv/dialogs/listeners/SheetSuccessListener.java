package com.vsv.dialogs.listeners;

@FunctionalInterface
public interface SheetSuccessListener<T> {

    void onSuccess(T result);
}
