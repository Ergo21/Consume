package com.almasb.consume;

import javafx.util.Duration;

public final class Config {

	public static final class Speed {
		public static final double GRAVITY_ACCEL = 0.2;
		public static final int GRAVITY_MAX = 10;

		public static final int PLAYER_MOVE = 3;
		public static final int PLAYER_JUMP = 6;

		public static final double ENEMY_SEEK_ACCEL = 1.5;
		public static final double ENEMY_SEEK_DECEL = 0.05;
		public static final int ENEMY_SEEK_MAX = PLAYER_MOVE * 3;

		public static final int ENEMY_PATROL = PLAYER_MOVE + 1;

		public static final int ENEMY_JUMP = 5;

		public static final int PROJECTILE = PLAYER_MOVE + 2;
	}

	public static final int BLOCK_SIZE = 40;

	public static final int MAX_LEVELS = 24;

	public static final int PATROL_RADIUS = 3 * BLOCK_SIZE;
	public static final int ENEMY_CHARGE_RANGE = 7 * BLOCK_SIZE;
	public static final Duration ENEMY_CHARGE_DELAY = Duration.seconds(0.5);

	public static final int ENEMY_FIRE_RANGE = 7 * BLOCK_SIZE;
	public static final Duration ENEMY_FIRE_DELAY = Duration.seconds(0.5);

	public static final double ENEMY_DIVEBOMB_ACC = 0.1;

	public static final Duration ENEMY_JUMP_DELAY = Duration.seconds(1.5);

	public static final int MAX_HEALTH_INC = 10;
	public static final int MAX_MANA_INC = 10;
	public static final int MANA_REGEN_INC = 5;

	public static final Duration REGEN_TIME_INTERVAL = Duration.seconds(1);

	// public static final int POWER_DAMAGE = 4;
	// public static final int NEUTRAL_DAMAGE = 4;
	public static final int SPEAR_DAMAGE = 3;
	public static final int KNIFE_DAMAGE = 6;

	public static final int FIREBALL_COST = 3;
	public static final int FIREBALL_DAMAGE = 4;

	public static final int SAND_COST = 5;
	public static final int SAND_DAMAGE = 3;
	public static final Duration SAND_DELAY = Duration.seconds(0.5);
	public static final Duration SAND_DECAY = Duration.seconds(2);

	public static final int LIGHTNING_COST = 7;
	public static final int LIGHTNING_DAMAGE = 6;
	public static final Duration LIGHTNING_DELAY = Duration.seconds(0.5);
	public static final Duration LIGHTNING_DECAY = Duration.seconds(1.5);

	public static final int BULLET_COST = 0;
	public static final int BULLET_DAMAGE = 5;

	public static final Duration CONSUME_DECAY = Duration.seconds(0.5);

	public static final int DEATH_DAMAGE = 0;

	public static final Duration ENEMY_SPEAR_DECAY = Duration.seconds(1.5);

	public static final Duration ENEMY_SCORPION_DELAY = Duration.seconds(0.35);
	public static final Duration ENEMY_SCORPION_DECAY = Duration.seconds(2);
	
	public static final Duration ENEMY_STONE_THROW_DELAY = Duration.seconds(0.33);
	public static final Duration ENEMY_STONE_THROW_RECHARGE = Duration.seconds(3);

}
