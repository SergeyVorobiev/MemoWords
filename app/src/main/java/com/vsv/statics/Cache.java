package com.vsv.statics;

import javax.annotation.Nullable;

// Not thread safe
public final class Cache<T> {

    @SuppressWarnings("unchecked")
    private final T[] cache = (T[]) new Object[1];

    public Cache() {

    }

    public void set(@Nullable T samples) {
        cache[0] = samples;
    }

    public void clear() {
        this.cache[0] = null;
    }

    public @Nullable
    T get(boolean clear) {
        T temp = this.cache[0];
        if (clear) {
            this.cache[0] = null;
        }
        return temp;
    }
}
