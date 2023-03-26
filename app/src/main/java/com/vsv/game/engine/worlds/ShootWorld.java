package com.vsv.game.engine.worlds;

import androidx.annotation.Nullable;

import com.vsv.game.engine.DrawInstruments;
import com.vsv.game.engine.DrawPriority;
import com.vsv.game.engine.GameObject;
import com.vsv.game.engine.ObjectManager;
import com.vsv.game.engine.World;
import com.vsv.game.engine.objects.Background;
import com.vsv.game.engine.objects.Cannon;
import com.vsv.game.engine.objects.TextEnemy;
import com.vsv.game.engine.objects.properties.CannonProperties;

import java.util.ArrayList;

public class ShootWorld implements World {

    private static final int HEALTH_ONE = 950;

    private static final int CANNON_COUNT = 4;

    private static final int HEALTH_ALL = HEALTH_ONE * CANNON_COUNT;

    private static final int[] CANNON_SLOTS = new int[]{0, 1, 2, 3};

    private final WordGenerator wordGenerator;

    public ShootWorld(ArrayList<String> questions) {
        wordGenerator = new WordGenerator(questions);
    }

    private ObjectManager<GameObject> manager;

    private final Background background = new Background();

    @Override
    public void start(ObjectManager<GameObject> objectManager, DrawInstruments<?> drawInstruments) {
        this.manager = objectManager;
        objectManager.addObject(background);
        initCannons(objectManager);
    }

    public int getBackgroundColor() {
        return background.getBackgroundColor();
    }

    private void initCannons(ObjectManager<GameObject> objectManager) {
        for (int i = 0; i < CANNON_COUNT; i++) {
            CannonProperties.spawn(CANNON_SLOTS[i], objectManager);
        }
    }

    public TextEnemy commandShoot(int turret, int enemyId) {
        TextEnemy enemy = wordGenerator.setHealthToEnemy(enemyId, HEALTH_ONE, false);
        wordGenerator.generateEnemyIfLast();
        ArrayList<GameObject> objects = manager.getPriorityGroup(DrawPriority.DYNAMIC_OBJECTS, DrawPriority.DYNAMIC_PRIORITY_CANNONS);
        turret = turret < objects.size() ? turret : objects.size() - 1;
        Cannon cannon = (Cannon) objects.get(turret);
        cannon.commandShoot();
        return enemy;
    }

    @Nullable
    public String getEnemyTextByPosition(float x, float y) {
        return wordGenerator.getEnemyTextByPosition(x, y);
    }

    public boolean isReady() {
        if (wordGenerator.isWordsEnd()) {
            return true;
        }
        return manager != null && !manager.getPriorityGroup(DrawPriority.DYNAMIC_OBJECTS, DrawPriority.DYNAMIC_PRIORITY_ENEMY).isEmpty();
    }

    public void setMainEnemy(int id) {
        ArrayList<GameObject> objects = manager.getPriorityGroup(DrawPriority.DYNAMIC_OBJECTS, DrawPriority.DYNAMIC_PRIORITY_ENEMY);
        for (int i = 0; i < objects.size(); i++) {
            TextEnemy enemy = (TextEnemy) objects.get(i);
            if (enemy.getId() == id) {
                enemy.setMain();
                break;
            }
        }
    }

    public TextEnemy commandShootAll(int enemyId) {
        TextEnemy enemy = wordGenerator.setHealthToEnemy(enemyId, HEALTH_ALL, true);
        wordGenerator.generateEnemyIfLast();
        ArrayList<GameObject> cannons = manager.getPriorityGroup(DrawPriority.DYNAMIC_OBJECTS, DrawPriority.DYNAMIC_PRIORITY_CANNONS);
        for (GameObject cannon : cannons) {
            ((Cannon) cannon).commandShoot();
        }
        return enemy;
    }

    @Override
    public void update(ObjectManager<GameObject> objectManager, DrawInstruments<?> drawInstruments, float dt) {
        wordGenerator.generate(objectManager, drawInstruments, dt);
    }
}
