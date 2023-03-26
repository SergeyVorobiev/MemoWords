package com.vsv.dialogs.entities;

import androidx.annotation.NonNull;

import com.vsv.dialogs.PipelineDialog;
import com.vsv.statics.WeakContext;

import java.util.ArrayList;

public class PipelineTask {

    public final ExceptionalFunction<PipelineTaskResult, Object> function;

    public final int timeout;

    // timeout in seconds.
    public PipelineTask(int timeout, ExceptionalFunction<PipelineTaskResult, Object> function) {
        this.function = function;
        this.timeout = timeout;
    }

    public static PipelineDialog buildPipelineDialog(@NonNull ArrayList<PipelineTask> tasks) {
        return new PipelineDialog(WeakContext.getContext(), tasks);
    }
}
