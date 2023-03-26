package com.vsv.game.engine;

public class DrawPriority {

    public static final int BACKGROUND = 0;

    public static final int BACKGROUND_PRIORITY_STARS = 0;

    public static final int BACKGROUND_PRIORITY_TOYS = 1;

    public static final int BACKGROUND_PRIORITY_DUST = 2;

    public static final int STATIC_OBJECTS = 1;

    public static final int STATIC_PRIORITY_PLATFORM = 0;

    public static final int STATIC_PRIORITY_BASEMENT = 1;

    public static final int DYNAMIC_OBJECTS = 2;

    public static final int DYNAMIC_PRIORITY_ENEMY = 0;

    public static final int DYNAMIC_PRIORITY_PROJECTILES = 1;

    public static final int DYNAMIC_PRIORITY_LASER = 2;

    public static final int DYNAMIC_PRIORITY_EFFECTS = 3;

    public static final int DYNAMIC_PRIORITY_CANNONS = 4;

    public final int group;

    public final int priority;

    public DrawPriority(int group, int priority) {
        this.group = group;
        this.priority = priority;
    }
}
