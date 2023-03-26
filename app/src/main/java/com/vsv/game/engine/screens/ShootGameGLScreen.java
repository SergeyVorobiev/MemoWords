package com.vsv.game.engine.screens;

import android.annotation.SuppressLint;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vsv.game.engine.GL20DrawInstruments;
import com.vsv.game.engine.ShootAsset;
import com.vsv.game.engine.SimpleRenderer;
import com.vsv.game.engine.World;
import com.vsv.game.engine.worlds.ShootWorld;
import com.vsv.speech.RoboVoice;
import com.vsv.utils.Timer;

import java.util.Locale;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class ShootGameGLScreen extends GLSurfaceView implements GLSurfaceView.Renderer, RoboVoiceGameScreen {

    private long time;

    private float pastTime;

    private final ShootAsset asset = ShootAsset.build();

    private final SimpleRenderer<GL10> renderer = new SimpleRenderer<>(new GL20DrawInstruments());

    private Runnable callback;

    private Runnable emergency;

    float invokeFrequency;

    private volatile boolean run = false;

    private boolean destroy = false;

    private final float[] touchPoint = new float[2];

    private long lastClickTime;

    private Locale locale;

    @SuppressWarnings("FieldCanBeLocal")
    private final float clickTimeout = 0.5f;

    public ShootGameGLScreen(Context context) {
        super(context);
        setup();
    }

    public ShootGameGLScreen(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    @Override
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    private void setup() {
        setEGLContextClientVersion(2);
        setRenderer(this);
        this.setOnClickListener(this::onSurfaceClick);
        lastClickTime = System.nanoTime();
        RoboVoice.getInstance().stopSpeaking();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

    }

    private void onSurfaceClick(@NonNull View view) {
        if (Timer.nanoTimeDiffFromNowInSeconds(lastClickTime) >= clickTimeout) {
            lastClickTime = System.nanoTime();
            String text = ((ShootWorld) renderer.getWorld()).getEnemyTextByPosition(touchPoint[0], touchPoint[1]);
            RoboVoice.getInstance().speak(text, locale);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        touchPoint[0] = event.getX();
        touchPoint[1] = event.getY();
        return super.onTouchEvent(event);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        time = System.nanoTime();
        ScreenParameters.setup(width, height);
        if (run) {
            run = false;
        } else {
            run = this.renderer.initDrawer(gl, width, height);
        }
        if (!run) {
            emergencyExit();
        }
    }

    private void emergencyExit() {
        if (emergency != null) {
            emergency.run();
            Log.d("Emergency", "emergency");
        }
    }

    @Override
    public void setOnEmergencyExitCallback(@NonNull Runnable emergency) {
        this.emergency = emergency;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (run) {
            float dt = Timer.nanoTimeDiffFromNowInSeconds(time);
            time = System.nanoTime();
            pastTime += dt;
            renderer.render(gl, dt);
            if (pastTime > invokeFrequency) {
                pastTime = 0;
                if (callback != null) {
                    callback.run();
                }
            }
        } else {
            try {
                asset.destroy();
                renderer.destroy();
            } catch (Throwable th) {
                // just exit
            }
        }
    }

    @Override
    public void setLoopCallback(@Nullable Runnable callback, float invokeFrequency) {
        this.callback = callback;
        this.invokeFrequency = invokeFrequency;
    }

    @Override
    public void destroy() {
        run = false;
    }

    @Override
    public void setWorld(World world) {
        this.renderer.setWorld(world);
    }
}
