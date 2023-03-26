package com.vsv.game.engine;

import androidx.annotation.NonNull;

public interface Renderer<T> {

    boolean initDrawer(@NonNull T drawer, float width, float height);

    void render(@NonNull T drawer, float dt);

    void destroy();
}
