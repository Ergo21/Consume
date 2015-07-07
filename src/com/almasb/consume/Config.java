package com.almasb.consume;

import com.almasb.fxgl.GameApplication;

public final class Config {

    public static final class Speed {
        public static final int GRAVITY_ACCEL = 1;
        public static final int GRAVITY_MAX = 10;

        public static final int PLAYER_MOVE = 4;
        public static final int PLAYER_JUMP = 30;

        public static final double ENEMY_SEEK_ACCEL = 1.5;
        public static final double ENEMY_SEEK_DECEL = 0.05;
        public static final int ENEMY_SEEK_MAX = PLAYER_MOVE * 3;

        public static final int ENEMY_PATROL = PLAYER_MOVE + 1;

        public static final int PROJECTILE = PLAYER_MOVE + 2;
    }

    public static final int BLOCK_SIZE = 40;

    public static final int MAX_LEVELS = 2;

    public static final int PATROL_RADIUS = 3 * BLOCK_SIZE;
    public static final int ENEMY_CHARGE_RANGE = 7 * BLOCK_SIZE;
    public static final double ENEMY_CHARGE_DELAY = 0.5 * GameApplication.SECOND;
    
    public static final int ENEMY_FIRE_RANGE = 7 * BLOCK_SIZE;
    public static final double ENEMY_FIRE_DELAY = 0.5 * GameApplication.SECOND;

    public static final int MAX_HEALTH_INC = 10;
    public static final int MAX_MANA_INC = 10;
    public static final int MANA_REGEN_INC = 5;

    public static final long REGEN_TIME_INTERVAL = 1 * GameApplication.SECOND;

    public static final int POWER_DAMAGE = 4;
}
