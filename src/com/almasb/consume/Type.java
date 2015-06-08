package com.almasb.consume;

import com.almasb.fxgl.entity.EntityType;

public enum Type implements EntityType {
    SPAWN_POINT, NEXT_LEVEL, DESTRUCTIBLE_BLOCK, INDESTRUCTIBLE_BLOCK, LADDER,
    INCREASE_MAX_HEALTH, INCREASE_MAX_MANA, INCREASE_MANA_REGEN
}
