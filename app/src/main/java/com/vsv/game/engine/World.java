package com.vsv.game.engine;

public interface World {

    void start(ObjectManager<GameObject> objectManager, DrawInstruments<?> drawInstruments);

    void update(ObjectManager<GameObject> objectManager, DrawInstruments<?> drawInstruments, float dt);
}
