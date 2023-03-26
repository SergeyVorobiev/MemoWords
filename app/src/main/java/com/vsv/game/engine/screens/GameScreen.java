package com.vsv.game.engine.screens;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vsv.game.engine.World;

public interface GameScreen {

    void setLoopCallback(@Nullable Runnable callback, float invokeFrequency);

    void setOnEmergencyExitCallback(@NonNull Runnable emergency);

    void destroy();

    void setWorld(World world);
}
