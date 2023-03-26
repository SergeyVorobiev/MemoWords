package com.vsv.game.engine;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class GameLoop {

    private Thread thread;

    private SurfaceHolder surfaceHolder;

    private Renderer<Canvas> renderer;

    private long lastFrameTime;

    private boolean finished = true;

    private Runnable callback;

    private float pastTime;

    private float callbackInvokeFrequency;

    public GameLoop() {
        this.pastTime = 0;
    }

    public void setLoopCallback(@Nullable Runnable callback, float invokeFrequency) {
        this.callback = callback;
        this.callbackInvokeFrequency = invokeFrequency;
    }

    public synchronized void start(@NonNull SurfaceHolder surfaceHolder, @NonNull Renderer<Canvas> renderer) {
        if (!finished) {
            return;
        }
        if (thread != null) {
            thread.interrupt();
            try {
                thread.join(5000);
                renderer.destroy();
            } catch (InterruptedException e) {
                //
            }
        }
        finished = false;
        lastFrameTime = -1;
        this.renderer = renderer;
        thread = new Thread(this::run);
        this.surfaceHolder = surfaceHolder;
        thread.setDaemon(true);
        thread.setName("GameLoop");
        thread.start();
    }

    public synchronized void finish() {
        if (finished) {
            return;
        }
        finished = true;
        thread.interrupt();
        try {
            thread.join(3000);
            renderer.destroy();
        } catch (InterruptedException e) {
            //
        } finally {
            thread = null;
        }
    }

    private float getDelta() {
        float dt;
        if (lastFrameTime == -1) {
            dt = 0;
        } else {
            dt = (System.nanoTime() - lastFrameTime) / 1000000000.0f;
        }
        lastFrameTime = System.nanoTime();
        return dt;
    }

    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            Canvas canvas;
            canvas = surfaceHolder.lockCanvas();
            if (canvas != null) {
                float delta = getDelta();
                pastTime += delta;
                renderer.render(canvas, delta);
                surfaceHolder.unlockCanvasAndPost(canvas);
                if (callback != null) {
                    if (pastTime >= callbackInvokeFrequency) {
                        pastTime = 0;
                        callback.run();
                    }
                }
            } else {
                break;
            }
        }
    }
}
