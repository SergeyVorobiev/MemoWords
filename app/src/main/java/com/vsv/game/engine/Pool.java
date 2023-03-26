package com.vsv.game.engine;

import android.util.Log;

import com.vsv.game.engine.objects.FireBullet;
import com.vsv.game.engine.objects.Projectile;

import java.util.LinkedList;
import java.util.function.Supplier;

public class Pool<T> {

    private final LinkedList<T> pool = new LinkedList<>();

    private final Supplier<T> supplier;

    public final int capacity;

    private int maxSize = 0;

    public Pool(Supplier<T> supplier, int capacity) {
        this.supplier = supplier;
        this.capacity = capacity;
    }

    public T get() {
        if (pool.isEmpty()) {
            return supplier.get();
        }
        return pool.removeFirst();
    }

    public void clear() {
        pool.clear();
    }

    public int size() {
        return pool.size();
    }

    public int maxUsage() {
        return maxSize;
    }

    public void recycle(T object) {
        if (pool.size() < capacity) {
            pool.add(object);
            if (maxSize < pool.size()) {
                maxSize = pool.size();
            }
        }
    }
}
