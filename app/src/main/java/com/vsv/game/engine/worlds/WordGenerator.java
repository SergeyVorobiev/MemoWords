package com.vsv.game.engine.worlds;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vsv.game.engine.DrawInstruments;
import com.vsv.game.engine.DrawPriority;
import com.vsv.game.engine.GL20DrawInstruments;
import com.vsv.game.engine.GameObject;
import com.vsv.game.engine.ObjectManager;
import com.vsv.game.engine.objects.TextEnemy;
import com.vsv.game.engine.objects.properties.TextEnemyProperties;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class WordGenerator {

    private final ArrayList<String> questions;

    private float timer = 0;

    private static final float TIME_SPAWN = 4.5f; // Default 4.5

    private static final int MAX_ENEMIES = 3; // Default 3

    private int index = 0;

    private int textureCounter = 1;

    private ObjectManager<GameObject> manager;

    private final AtomicBoolean needGenerateIfLast = new AtomicBoolean();

    public WordGenerator(@NonNull ArrayList<String> questions) {
        this.questions = questions;
    }

    private boolean last = false;

    public TextEnemy setHealthToEnemy(int id, float health, boolean isCorrect) {
        ArrayList<GameObject> objects = manager.getPriorityGroup(DrawPriority.DYNAMIC_OBJECTS, DrawPriority.DYNAMIC_PRIORITY_ENEMY);
        if (objects.isEmpty()) {
            throw new RuntimeException("Cannot set health to not existed object");
        }
        for (int i = 0; i < objects.size(); i++) {
            TextEnemy enemy = (TextEnemy) objects.get(i);
            int enemyId = enemy.getId();
            if (enemyId == id) {
                enemy.setHealth(health, isCorrect);
                return enemy;
            }
        }
        throw new RuntimeException("Cannot find enemy with id: " + id);
    }

    @Nullable
    public String getEnemyTextByPosition(float x, float y) {
        ArrayList<GameObject> objects = manager.getPriorityGroup(DrawPriority.DYNAMIC_OBJECTS, DrawPriority.DYNAMIC_PRIORITY_ENEMY);
        for (int i = 0; i < objects.size(); i++) {
            TextEnemy enemy = (TextEnemy) objects.get(i);
            if (enemy.interact() && enemy.getPosition().contains(x, y)) {
                enemy.rotate360();
                return enemy.getText();
            }
        }
        return null;
    }

    public void generateEnemyIfLast() {
        needGenerateIfLast.set(true);
    }

    public void generate(ObjectManager<GameObject> manager, DrawInstruments<?> drawInstruments, float dt) {
        this.manager = manager;
        timer += dt;
        if (index == questions.size()) {
            return;
        }
        int size = canInteractSize();
        boolean needSpawn = size == 0;
        if (needSpawn || timer >= TIME_SPAWN) {
            if (size < MAX_ENEMIES) {
                generateEnemy(manager, drawInstruments);
            }
        }
    }

    private int canInteractSize() {
        int result = 0;
        ArrayList<GameObject> objects = manager.getPriorityGroup(DrawPriority.DYNAMIC_OBJECTS, DrawPriority.DYNAMIC_PRIORITY_ENEMY);
        for (int i = 0; i < objects.size(); i++) {
            if (objects.get(i).interact()) {
                result++;
            }
        }
        return result;
    }

    public boolean isWordsEnd() {
        return last;
    }

    private void generateEnemy(ObjectManager<GameObject> manager, DrawInstruments<?> drawInstruments) {
        timer = 0;
        TextEnemy enemy = manager.getObjectFromPool(TextEnemy.class);
        TextEnemyProperties.defaultSetup(enemy, index, questions.get(index++), textureCounter);
        GL20DrawInstruments gl20DrawInstruments = (GL20DrawInstruments) drawInstruments;
        gl20DrawInstruments.reloadTexture(enemy.getTextBitmap(), textureCounter, false);
        manager.addObject(enemy);
        textureCounter += 1;
        if (textureCounter > 4) {
            textureCounter = 1;
        }
        if (index == questions.size()) {
            last = true;
        }
    }
}
