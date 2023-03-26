package com.vsv.game.engine;

import androidx.annotation.NonNull;

public interface GameObject extends PositionInfo {

    int getState();

    int getTextureId();

    boolean interact();

    int getId();

    void setId(int id);

    void act(float dt, @NonNull ObjectManager<GameObject> manager);

    boolean canRemoveFromRendering();

    void draw(@NonNull DrawInstruments<?> drawInstruments);

    void destroy();

    void takeHit(float damage);

    default void removedFromRendering(@NonNull ObjectPools objectPools, @NonNull GameObject object) {
        objectPools.recycle(object);
    }

    @NonNull
    DrawPriority getDrawPriority();
}
