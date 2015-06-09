package com.almasb.consume;

public final class Config {

    public static final class Speed {
        public static final int PLAYER = 5;
        public static final int ENEMY_SEEK_ACCEL = 1;
        public static final int ENEMY_SEEK_DECEL = 1;
        public static final int ENEMY_SEEK_MAX = PLAYER * 2;

        public static final int ENEMY_PATROL = PLAYER / 2;
    }

    public static final int BLOCK_SIZE = 40;

    public static final int PATROL_RADIUS = 3 * BLOCK_SIZE;
}
