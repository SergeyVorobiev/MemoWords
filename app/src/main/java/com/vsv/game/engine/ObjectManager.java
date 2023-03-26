package com.vsv.game.engine;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public interface ObjectManager<T> {

    void addObject(@NonNull T object);

    @NonNull
    ArrayList<T> getPriorityGroup(@NonNull GameObject gameObject);

    @NonNull
    ArrayList<T> getPriorityGroup(int group, int priority);

    @NonNull
    ArrayList<ArrayList<T>> getGroup(@NonNull GameObject gameObject);

    @NonNull
    <B> B getObjectFromPool(@NonNull Class<B> clazz);
}
