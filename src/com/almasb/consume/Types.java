package com.almasb.consume;

import com.almasb.fxgl.entity.EntityType;
import com.almasb.fxgl.entity.PropertyKey;

public class Types {

    public enum Type implements EntityType {
        PLAYER, SPAWN_POINT, NEXT_LEVEL_POINT,
        ENEMY, PLATFORM, BLOCK, TRIGGER, POWERUP,
        PROJECTILE
    }

    public enum Powerup {
        RESTORE_HEALTH, RESTORE_MANA,   // powerups
        INC_MAX_HEALTH, INC_MAX_MANA, INC_MANA_REGEN    // upgrades
    }

    public enum Platform {
        DESTRUCTIBLE, INDESTRUCTIBLE
    }

    public enum Property implements PropertyKey {
        SUB_TYPE
    }
}
