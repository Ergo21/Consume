package com.almasb.consume;

import com.almasb.fxgl.entity.EntityType;
import com.almasb.fxgl.entity.PropertyKey;

public class Types {

	public enum Type implements EntityType {
		PLAYER, SPAWN_POINT, NEXT_LEVEL_POINT, ENEMY, PLATFORM, BLOCK, TRIGGER, POWERUP, PLAYER_PROJECTILE, ENEMY_PROJECTILE
	}

	public enum Powerup {
		// powerups
		RESTORE_HEALTH_12, RESTORE_HEALTH_25, RESTORE_HEALTH_50, RESTORE_MANA_12, RESTORE_MANA_25, RESTORE_MANA_50,

		// upgrades
		INC_MAX_HEALTH, INC_MAX_MANA, INC_MANA_REGEN
	}

	public enum Platform {
		DESTRUCTIBLE, INDESTRUCTIBLE
	}

	public enum Block {
		BARRIER, LADDER, SCENE
	}

	public enum Property implements PropertyKey {
		DATA, SUB_TYPE, ENABLE_GRAVITY
	}

	public enum Element {
		NEUTRAL, NEUTRAL2, FIRE, EARTH, LIGHTNING, METAL, DEATH, CONSUME
	}
	
	public enum Actions {
		LEFT, RIGHT, UP, DOWN, JUMP, INTERACT, SHOOT, CHPOWP, CHPOWN
	}
}
