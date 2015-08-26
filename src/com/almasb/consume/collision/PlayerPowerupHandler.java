package com.almasb.consume.collision;

import java.util.logging.Logger;

import com.almasb.consume.Config;
import com.almasb.consume.Event;
import com.almasb.consume.Types.Powerup;
import com.almasb.consume.Types.Property;
import com.almasb.consume.Types.Type;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.FXGLEvent;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.util.FXGLLogger;
import com.ergo21.consume.Player;

public class PlayerPowerupHandler extends CollisionHandler {

	private static final Logger log = FXGLLogger.getLogger("PlayerPowerupHandler");

	public PlayerPowerupHandler() {
		super(Type.PLAYER, Type.POWERUP);
	}

	@Override
	public void onCollision(Entity player, Entity powerup) {
		powerup.fireFXGLEvent(new FXGLEvent(Event.DEATH));

		Powerup type = powerup.getProperty(Property.SUB_TYPE);
		Player playerData = player.getProperty(Property.DATA);

		switch (type) {
		case INC_MANA_REGEN:
			playerData.increaseManaRegen(Config.MANA_REGEN_INC);
			break;
		case INC_MAX_HEALTH:
			playerData.increaseMaxHealth(Config.MAX_HEALTH_INC);
			break;
		case INC_MAX_MANA:
			playerData.increaseMaxMana(Config.MAX_MANA_INC);
			break;
		case RESTORE_HEALTH_12:
			playerData.restoreHealth(0.125);
			break;
		case RESTORE_HEALTH_25:
			playerData.restoreHealth(0.25);
			break;
		case RESTORE_HEALTH_50:
			playerData.restoreHealth(0.5);
			break;
		case RESTORE_MANA_12:
			playerData.restoreMana(0.125);
			break;
		case RESTORE_MANA_25:
			playerData.restoreMana(0.25);
			break;
		case RESTORE_MANA_50:
			playerData.restoreMana(0.5);
			break;
		default:
			log.info("Picked up an unknown powerup: " + type);
			break;
		}
	}
}
