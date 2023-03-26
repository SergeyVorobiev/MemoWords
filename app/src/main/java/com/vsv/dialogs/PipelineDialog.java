package com.vsv.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;

import com.vsv.dialogs.entities.PipelineTask;
import com.vsv.dialogs.entities.PipelineTaskResult;
import com.vsv.memorizer.R;
import com.vsv.utils.StaticUtils;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class PipelineDialog extends SingleWindow {

    private final Dialog dialog;

    private final TextView interruptButton;

    private final TextView message;

    private volatile boolean interrupt = false;

    private boolean showProgress = false;

    private final String counterFormat;

    private Consumer<PipelineTaskResult> runMainThreadOnInterrupt;

    private Consumer<PipelineTaskResult> runMainThreadOnFinish; // Include on interrupt.

    private Consumer<PipelineTaskResult> runOnFinish; // Include on interrupt.

    private Consumer<Throwable> runOnFail;

    private Consumer<Throwable> runMainThreadOnFail;

    private int timeout;

    private final ExecutorService executor = Executors.newSingleThreadExecutor((runnable) -> {
        Thread thread = new Thread(runnable);
        thread.setDaemon(true);
        thread.setName("WaitDialog");
        return thread;
    });

    private final @NonNull List<PipelineTask> tasks;

    private final Handler handler;

    public PipelineDialog(@NonNull Context context, @NonNull List<PipelineTask> tasks) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = View.inflate(context, R.layout.dialog_wait, null);
        interruptButton = dialogView.findViewById(R.id.interrupt);
        interruptButton.setVisibility(View.GONE);
        interruptButton.setOnClickListener((view) -> {
            view.setEnabled(false);
            interrupt = true;
        });
        runMainThreadOnInterrupt = (a) -> {};
        runMainThreadOnFinish = (a) -> {};
        runOnFinish = (a) -> {};
        runOnFail = (a) -> {};
        runMainThreadOnFail = (a) -> {};
        message = dialogView.findViewById(R.id.message);
        message.setVisibility(View.GONE);
        builder.setCancelable(false);
        counterFormat = StaticUtils.getString(R.string.counter);
        builder.setView(dialogView);
        handler = new Handler(context.getMainLooper());
        this.tasks = tasks;
        dialog = builder.create();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setOnDismissListener((dialogInterface) -> setShown(false));
    }

    @MainThread
    public void setRunMainThreadOnInterrupt(@NonNull Consumer<PipelineTaskResult> consumer) {
        this.runMainThreadOnInterrupt = consumer;
    }

    @MainThread
    public void setTimeoutBetweenTasks(int timeoutInMilliseconds) {
        this.timeout = timeoutInMilliseconds;
    }

    @MainThread
    public void setRunMainThreadOnFinish(@NonNull Consumer<PipelineTaskResult> consumer) {
        this.runMainThreadOnFinish = consumer;
    }

    @MainThread
    public void setRunOnFinish(@NonNull Consumer<PipelineTaskResult> consumer) {
        this.runOnFinish = consumer;
    }

    @MainThread
    public void setRunOnFail(@NonNull Consumer<Throwable> consumer) {
        this.runOnFail = consumer;
    }

    @MainThread
    public void setRunMainThreadOnFail(@NonNull Consumer<Throwable> consumer) {
        this.runMainThreadOnFail = consumer;
    }

    @MainThread
    public void showInterruptButton() {
        interruptButton.setVisibility(View.VISIBLE);
    }

    private void setMessage(int taskNumber) {
        handler.post(() -> message.setText(String.format(counterFormat, taskNumber, tasks.size())));
    }

    @MainThread
    public void showProgress() {
        showProgress = true;
        message.setVisibility(View.VISIBLE);
    }

    @MainThread
    public void showOver() {
        show(true);
    }

    @MainThread
    public void show() {
        show(false);
    }

    @MainThread
    private void show(boolean allowOver) {
        if (!allowOver && isShownToast()) {
            return;
        }
        setShown(true);
        dialog.show();
        Thread thread = new Thread(() -> {
            int i = 0;
            PipelineTaskResult forNextTask = null;
            for (PipelineTask task : tasks) {
                i++;
                if (showProgress) {
                    setMessage(i);
                }
                Object object = execute(task, forNextTask);
                if (object instanceof Throwable) {
                    runOnFail.accept((Throwable) object);
                    handler.post(() -> runMainThreadOnFail.accept((Throwable) object));
                    break;
                }
                PipelineTaskResult result = new PipelineTaskResult(object, i - 1);
                if (interrupt) {
                    handler.post(() -> runMainThreadOnInterrupt.accept(result));
                    break;
                }
                if (timeout != 0) {
                    try {
                        Thread.sleep(timeout);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
                forNextTask = result;
            }
            PipelineTaskResult forEnd = forNextTask;
            handler.post(() -> runMainThreadOnFinish.accept(forEnd));
            runOnFinish.accept(forEnd);
            executor.shutdown();
            dialog.cancel();
        });
        thread.setDaemon(true);
        thread.setName("BTExec");
        thread.start();
    }

    private void intermediateResult(Object object) {

    }

    private Object execute(PipelineTask task, PipelineTaskResult previousResult) {
        Object object;
        try {
            Future<?> future = executor.submit(() -> task.function.apply(previousResult));
            return future.get(task.timeout, TimeUnit.SECONDS);
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            return e;
        }
    }
}
