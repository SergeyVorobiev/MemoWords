package com.vsv.dialogs.entities;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vsv.dialogs.WaitDialog;
import com.vsv.statics.WeakContext;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class BackgroundTask<T> {

    public final Callable<T> callable;

    public final int timeout;

    private Consumer<T> backgroundSuccess;

    private Consumer<T> mainSuccess;

    private Consumer<Exception> backgroundFail;

    private Consumer<Exception> mainFail;

    private Object extraData;

    // timeout in seconds.
    public BackgroundTask(int timeout, Callable<T> callable) {
        this.callable = callable;
        this.timeout = timeout;
        this.backgroundSuccess = (object) -> {
        };
        this.mainSuccess = (object) -> {
        };
        this.backgroundFail = (object) -> {
        };
        this.mainFail = (object) -> {
        };
    }

    public void runBackgroundOnSuccess(@Nullable T result) {
        this.backgroundSuccess.accept(result);
    }

    public void runMainThreadOnSuccess(@Nullable T result) {
        this.mainSuccess.accept(result);
    }

    public void runBackgroundOnFail(@NonNull Exception e) {
        this.backgroundFail.accept(e);
    }

    public void runMainThreadOnFail(@NonNull Exception e) {
        this.mainFail.accept(e);
    }

    public void setRunBackgroundOnSuccess(Consumer<T> backgroundSuccess) {
        this.backgroundSuccess = backgroundSuccess;
    }

    public void setRunMainThreadOnSuccess(Consumer<T> mainSuccess) {
        this.mainSuccess = mainSuccess;
    }

    public void setRunBackgroundOnFail(Consumer<Exception> backgroundFail) {
        this.backgroundFail = backgroundFail;
    }

    public void setRunMainThreadOnFail(Consumer<Exception> mainFail) {
        this.mainFail = mainFail;
    }

    public void setExtraData(@Nullable Object extraData) {
        this.extraData = extraData;
    }

    public @Nullable
    Object getExtraData() {
        return this.extraData;
    }

    public @NonNull
    Object getExtraDataOrDefault(@NonNull Object defaultObject) {
        return this.extraData == null ? defaultObject : extraData;
    }

    public WaitDialog buildWaitDialog(@NonNull Context context) {
        return new WaitDialog(context, this);
    }

    public WaitDialog buildWaitDialog() {
        return new WaitDialog(WeakContext.getContext(), this);
    }

    public static WaitDialog buildMultiTasksWaitDialog(@NonNull Context context, List<BackgroundTask<?>> tasks) {
        return new WaitDialog(context, tasks);
    }
}
