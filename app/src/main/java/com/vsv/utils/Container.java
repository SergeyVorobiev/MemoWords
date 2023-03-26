package com.vsv.utils;

public class Container<T> {

    private T object;

    public T get() {
        return object;
    }

    public void set(T object) {
        this.object = object;
    }
}
