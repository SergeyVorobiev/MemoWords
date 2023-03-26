package com.vsv.game.engine.screens;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vsv.game.engine.CanvasDrawInstruments;
import com.vsv.game.engine.GameLoop;
import com.vsv.game.engine.ShootAsset;
import com.vsv.game.engine.SimpleRenderer;
import com.vsv.game.engine.World;

public class ShootGameScreen extends SurfaceView implements SurfaceHolder.Callback, GameScreen {

    private final GameLoop loop = new GameLoop();

    private final ShootAsset asset = ShootAsset.build();

    private final SimpleRenderer<Canvas> renderer = new SimpleRenderer<>(new CanvasDrawInstruments());

    public ShootGameScreen(Context context) {
        super(context);
        this.getHolder().addCallback(this);
    }

    public ShootGameScreen(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.getHolder().addCallback(this);
    }

    public ShootGameScreen(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.getHolder().addCallback(this);
    }

    public ShootGameScreen(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.getHolder().addCallback(this);
    }

    // Start to draw here
    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        Log.d("Surface", "Changed: " + "w - " + width + " h - " + height);
        ScreenParameters.setup(width, height);
        loop.start(holder, renderer);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

    }

    @Override
    public void setLoopCallback(@Nullable Runnable callback, float invokeFrequency) {
        this.loop.setLoopCallback(callback, invokeFrequency);
    }

    @Override
    public void setOnEmergencyExitCallback(@NonNull Runnable emergency) {

    }

    @Override
    public void destroy() {
        loop.finish();
        asset.destroy();
    }

    @Override
    public void setWorld(World world) {
        this.renderer.setWorld(world);
    }
}
