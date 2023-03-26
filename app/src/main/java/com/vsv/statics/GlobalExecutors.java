package com.vsv.statics;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class GlobalExecutors {

    public static final int THREADS = 8;

    private GlobalExecutors() {

    }

    public static final ExecutorService singleExecutor = Executors.newSingleThreadExecutor((runnable) -> {
        Thread thread = new Thread(runnable);
        thread.setName("SingleE");
        thread.setDaemon(true);
        return thread;
    });

    public static final ExecutorService viewsExecutor = Executors.newSingleThreadExecutor((runnable) -> {
        Thread thread = new Thread(runnable);
        thread.setName("ViewsE");
        thread.setDaemon(true);
        return thread;
    });

    public static final ExecutorService modelsExecutor = Executors.newCachedThreadPool((runnable) -> {
        Thread thread = new Thread(runnable);
        thread.setName("ModelsE");
        thread.setDaemon(true);
        return thread;
    });
}
