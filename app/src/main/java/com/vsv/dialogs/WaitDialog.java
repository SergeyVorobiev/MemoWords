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

import com.vsv.dialogs.entities.BackgroundTask;
import com.vsv.memorizer.R;
import com.vsv.utils.StaticUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class WaitDialog extends SingleWindow {

    private final Dialog dialog;

    private final TextView interruptButton;

    private final TextView message;

    private volatile boolean interrupt = false;

    private boolean showProgress = false;

    private final String counterFormat;

    private Runnable runMainThreadOnInterrupt;

    private Runnable runMainThreadOnFinish; // Include on interrupt.

    private Runnable runOnFinish; // Include on interrupt.

    private int timeout;

    private final ExecutorService executor = Executors.newSingleThreadExecutor((runnable) -> {
        Thread thread = new Thread(runnable);
        thread.setDaemon(true);
        thread.setName("WaitDialog");
        return thread;
    });

    private final List<BackgroundTask<?>> tasks;

    private final Handler handler;

    public WaitDialog(@NonNull Context context, BackgroundTask<?> task) {
        this(context, Collections.singletonList(task));
    }

    public WaitDialog(@NonNull Context context, List<BackgroundTask<?>> tasks) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = View.inflate(context, R.layout.dialog_wait, null);
        interruptButton = dialogView.findViewById(R.id.interrupt);
        interruptButton.setVisibility(View.GONE);
        interruptButton.setOnClickListener((view) -> {
            view.setEnabled(false);
            interrupt = true;
        });
        runMainThreadOnInterrupt = () -> {};
        runMainThreadOnFinish = () -> {};
        runOnFinish = () -> {};
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
    public void setRunMainThreadOnInterrupt(@NonNull Runnable runnable) {
        this.runMainThreadOnInterrupt = runnable;
    }

    @MainThread
    public void setTimeoutBetweenTasks(int timeoutInMilliseconds) {
        this.timeout = timeoutInMilliseconds;
    }

    @MainThread
    public void setRunMainThreadOnFinish(@NonNull Runnable runnable) {
        this.runMainThreadOnFinish = runnable;
    }

    @MainThread
    public void setRunOnFinish(@NonNull Runnable runnable) {
        this.runOnFinish = runnable;
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
            for (BackgroundTask<?> task : tasks) {
                i++;
                if (showProgress) {
                    setMessage(i);
                }
                if (!execute(task)) {
                    break;
                }
                if (timeout != 0) {
                    try {
                        Thread.sleep(timeout);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
                if (interrupt) {
                    handler.post(runMainThreadOnInterrupt);
                    break;
                }
            }
            handler.post(runMainThreadOnFinish);
            runOnFinish.run();
            executor.shutdown();
            dialog.cancel();
        });
        thread.setDaemon(true);
        thread.setName("BTExec");
        thread.start();
    }

    private void intermediateResult(Object object) {

    }
    private <B> boolean execute(BackgroundTask<B> task) {
        B object;
        try {
            Future<B> future = executor.submit(task.callable);
            object = future.get(task.timeout, TimeUnit.SECONDS);
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            task.runBackgroundOnFail(e);
            handler.post(() -> task.runMainThreadOnFail(e));
            return false;
        }
        task.runBackgroundOnSuccess(object);
        handler.post(() -> task.runMainThreadOnSuccess(object));
        return true;
    }
}
