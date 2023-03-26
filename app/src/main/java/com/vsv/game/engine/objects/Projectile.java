package com.vsv.game.engine.objects;

import androidx.annotation.NonNull;

import com.vsv.game.engine.GameObject;
import com.vsv.game.engine.ObjectManager;

public interface Projectile extends GameObject {

    void explode(float x, float y, @NonNull ObjectManager<GameObject> objectManager);

    @NonNull
    Projectile setup(float x, float y, @NonNull GameObject parent, @NonNull GameObject target,
                     float projectileSpeed, float shootAngle, float damage);
}
