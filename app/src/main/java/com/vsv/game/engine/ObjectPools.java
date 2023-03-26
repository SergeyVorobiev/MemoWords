package com.vsv.game.engine;

import androidx.annotation.NonNull;

import com.vsv.game.engine.objects.BigFireBullet;
import com.vsv.game.engine.objects.FireBallExplosion;
import com.vsv.game.engine.objects.FireBullet;
import com.vsv.game.engine.objects.Laser;
import com.vsv.game.engine.objects.Rocket;
import com.vsv.game.engine.objects.RocketExplosion;
import com.vsv.game.engine.objects.Star;
import com.vsv.game.engine.objects.Dust;
import com.vsv.game.engine.objects.SmokeObject;
import com.vsv.game.engine.objects.TextEnemy;
import com.vsv.game.engine.objects.Toy;

import java.util.TreeMap;

public final class ObjectPools {

    private final TreeMap<String, Pool<GameObject>> pools = new TreeMap<>();

    public ObjectPools() {
        pools.put(SmokeObject.class.toString(), new Pool<>(SmokeObject::new, 50));
        pools.put(TextEnemy.class.toString(), new Pool<>(TextEnemy::new, 10));
        pools.put(Dust.class.toString(), new Pool<>(Dust::new, 100));
        pools.put(Toy.class.toString(), new Pool<>(Toy::new, 10));
        pools.put(Star.class.toString(), new Pool<>(Star::new, 200));
        pools.put(FireBullet.class.toString(), new Pool<>(FireBullet::new, 100));
        pools.put(Rocket.class.toString(), new Pool<>(Rocket::new, 50));
        pools.put(BigFireBullet.class.toString(), new Pool<>(BigFireBullet::new, 10));
        pools.put(RocketExplosion.class.toString(), new Pool<>(RocketExplosion::new, 10));
        pools.put(FireBallExplosion.class.toString(), new Pool<>(FireBallExplosion::new, 10));
        pools.put(Laser.class.toString(), new Pool<>(Laser::new, 2));
    }

    public void recycle(GameObject gameObject) {
        Pool<GameObject> pool = pools.get(gameObject.getClass().toString());
        if (pool != null) {
            pool.recycle(gameObject);
        }
    }

    @SuppressWarnings({"unchecked", "ConstantConditions"})
    @NonNull
    public <T> T get(Class<T> clazz) {
        return (T) pools.get(clazz.toString()).get();
    }

    public void dispose() {
        for (Pool<?> pool : pools.values()) {
            pool.clear();
        }
    }
}
