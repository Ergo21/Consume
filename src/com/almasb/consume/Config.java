package com.almasb.consume;

public final class Config {

    public static final class Speed {
        public static final int GRAVITY_ACCEL = 1;
        public static final int GRAVITY_MAX = 10;

        public static final int PLAYER_MOVE = 5;
        public static final int PLAYER_JUMP = 30;

        public static final int ENEMY_SEEK_ACCEL = 1;
        public static final int ENEMY_SEEK_DECEL = 1;
        public static final int ENEMY_SEEK_MAX = PLAYER_MOVE * 2;

        public static final int ENEMY_PATROL = PLAYER_MOVE / 2;
    }

    public static final int BLOCK_SIZE = 40;

    public static final int MAX_LEVELS = 2;

    public static final int PATROL_RADIUS = 3 * BLOCK_SIZE;

    public static final int MAX_HEALTH_INC = 10;
    public static final int MAX_MANA_INC = 10;
    public static final int MANA_REGEN_INC = 1;
}
