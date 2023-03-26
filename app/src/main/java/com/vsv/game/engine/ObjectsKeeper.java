package com.vsv.game.engine;

import java.util.ArrayList;

public class ObjectsKeeper<T> {

    public final ArrayList<ArrayList<ArrayList<T>>> groups;

    private final int groupsCount;

    private final int prioritiesCount;

    public ObjectsKeeper(int groupsCount, int prioritiesCount) {
        this.groupsCount = groupsCount;
        this.prioritiesCount = prioritiesCount;
        groups = new ArrayList<>();
        for (int i = 0; i < groupsCount; i++) {
            ArrayList<ArrayList<T>> priorities = new ArrayList<>();
            for (int j = 0; j < prioritiesCount; j++) {
                ArrayList<T> objectKeeper = new ArrayList<>();
                priorities.add(objectKeeper);
            }
            priorities.trimToSize();
            groups.add(priorities);
        }
        groups.trimToSize();
    }

    public int getMaxGroups() {
        return groupsCount;
    }

    public int getMaxPriorities() {
        return prioritiesCount;
    }

    public void addObject(T object, int group, int priority) {
        groups.get(group).get(priority).add(object);
    }
}
