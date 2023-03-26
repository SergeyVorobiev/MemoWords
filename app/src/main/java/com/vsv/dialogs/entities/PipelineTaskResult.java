package com.vsv.dialogs.entities;

import androidx.annotation.Nullable;

public class PipelineTaskResult {

    @Nullable
    public final Object taskResult;

    public final int taskIndex;

    public PipelineTaskResult(@Nullable Object taskResult, int taskIndex) {
        this.taskResult = taskResult;
        this.taskIndex = taskIndex;
    }
}
