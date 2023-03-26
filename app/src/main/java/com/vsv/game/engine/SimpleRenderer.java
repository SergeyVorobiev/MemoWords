package com.vsv.game.engine;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vsv.utils.Timer;

import java.util.ArrayList;
import java.util.function.Predicate;

public class SimpleRenderer<T> implements Renderer<T>, ObjectManager<GameObject> {

    private final ObjectPools objectPools = new ObjectPools();

    private final Predicate<GameObject> objectRemover = (object) -> {
        if (object.canRemoveFromRendering()) {
            object.removedFromRendering(objectPools, object);
            return true;
        }
        return false;
    };

    private final ObjectsKeeper<GameObject> objectsKeeper;

    private World world;

    private final DrawInstruments<T> drawInstruments;

    private boolean first = true;

    public SimpleRenderer(DrawInstruments<T> drawInstruments) {
        this.drawInstruments = drawInstruments;
        objectsKeeper = new ObjectsKeeper<>(3, 5);
    }

    public World getWorld() {
        return world;
    }

    public void setWorld(@Nullable World world) {
        this.first = true;
        this.world = world;
    }

    public boolean initDrawer(@NonNull T drawer, float width, float height) {
        return drawInstruments.initDrawer(drawer, width, height);
    }

    private void act(float dt) {
        for (int i = 0; i < objectsKeeper.getMaxGroups(); i++) {
            ArrayList<ArrayList<GameObject>> group = objectsKeeper.groups.get(i);
            for (int j = 0; j < objectsKeeper.getMaxPriorities(); j++) {
                ArrayList<GameObject> priority = group.get(j);
                for (int k = 0; k < priority.size(); k++) {
                    priority.get(k).act(dt, this);
                }
            }
        }
    }

    private void remove() {
        for (int i = 0; i < objectsKeeper.getMaxGroups(); i++) {
            ArrayList<ArrayList<GameObject>> group = objectsKeeper.groups.get(i);
            for (int j = 0; j < objectsKeeper.getMaxPriorities(); j++) {
                group.get(j).removeIf(objectRemover);
            }
        }
    }

    private void draw() {
        for (int i = 0; i < objectsKeeper.getMaxGroups(); i++) {
            ArrayList<ArrayList<GameObject>> group = objectsKeeper.groups.get(i);
            for (int j = 0; j < objectsKeeper.getMaxPriorities(); j++) {
                ArrayList<GameObject> priority = group.get(j);
                for (int k = 0; k < priority.size(); k++) {
                    priority.get(k).draw(drawInstruments);
                }
            }
        }
        drawInstruments.commitDrawing();
    }

    private void worldAct(float dt) {
        if (first) {
            if (world == null) {
                return;
            }
            world.start(this, drawInstruments);
            first = false;
        } else {
            if (world != null) {
                world.update(this, drawInstruments, dt);
            } else {
                first = true;
            }
        }
    }

    @Override
    public void render(@NonNull T drawer, float dt) {
        drawInstruments.setCurrentDrawer(drawer);
        worldAct(dt);
        act(dt);
        remove();
        draw();
    }

    @Override
    public void addObject(@NonNull GameObject object) {
        DrawPriority drawPriority = object.getDrawPriority();
        objectsKeeper.addObject(object, drawPriority.group, drawPriority.priority);
        // checkUniqueness(drawPriority.group, drawPriority.priority); // TODO Delete this
    }

    private void checkUniqueness(int group, int priority) {
        ArrayList<String> objectIds = new ArrayList<>();
        for (GameObject object : objectsKeeper.groups.get(group).get(priority)) {
            objectIds.add(object.toString());
        }
        objectIds.sort(String::compareTo);
        for (int i = 1; i < objectIds.size(); i++) {
            String id1 = objectIds.get(i - 1);
            String id2 = objectIds.get(i);
            if (id1.equals(id2)) {
                throw new RuntimeException(id1 + " is duplicated");
            }
        }
    }

    @NonNull
    @Override
    public ArrayList<GameObject> getPriorityGroup(@NonNull GameObject gameObject) {
        DrawPriority drawPriority = gameObject.getDrawPriority();
        return objectsKeeper.groups.get(drawPriority.group).get(drawPriority.priority);
    }

    @NonNull
    @Override
    public ArrayList<GameObject> getPriorityGroup(int group, int priority) {
        return objectsKeeper.groups.get(group).get(priority);
    }

    @NonNull
    @Override
    public ArrayList<ArrayList<GameObject>> getGroup(@NonNull GameObject gameObject) {
        return objectsKeeper.groups.get(gameObject.getDrawPriority().group);
    }

    @NonNull
    @Override
    public <B> B getObjectFromPool(@NonNull Class<B> clazz) {
        return objectPools.get(clazz);
    }

    @Override
    public void destroy() {
        drawInstruments.releaseResources();
        for (int i = 0; i < objectsKeeper.getMaxGroups(); i++) {
            ArrayList<ArrayList<GameObject>> group = objectsKeeper.groups.get(i);
            for (int j = 0; j < objectsKeeper.getMaxPriorities(); j++) {
                ArrayList<GameObject> priority = group.get(j);
                for (int k = 0; k < priority.size(); k++) {
                    priority.get(k).destroy();
                }
            }
        }
        objectPools.dispose();
    }
}
