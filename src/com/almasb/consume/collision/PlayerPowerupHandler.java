package com.almasb.consume.collision;

import java.util.logging.Logger;

import com.almasb.consume.Config;
import com.almasb.consume.ConsumeApp;
import com.almasb.consume.Event;
import com.almasb.consume.Types.Element;
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
	private ConsumeApp consApp;

	public PlayerPowerupHandler(ConsumeApp a) {
		super(Type.PLAYER, Type.POWERUP);
		consApp = a;
	}

	@Override
	public void onCollision(Entity player, Entity powerup) {
		if(player.getProperty("eaten") == null || player.<Boolean>getProperty("eaten") == false){
			return;
		}
		
		powerup.fireFXGLEvent(new FXGLEvent(Event.DEATH));

		Powerup type = powerup.getProperty(Property.SUB_TYPE);
		Player playerData = player.getProperty(Property.DATA);
		
		if(playerData.getCurrentHealth() <= 0){
			return;
		}

		switch (type) {
		case INC_MANA_REGEN:
			playerData.increaseManaRegen(Config.MANA_REGEN_INC);
			playerData.restoreHealth(1);
			playerData.restoreMana(playerData.getMaxMana());
			playerData.getUpgrades().add(playerData.getCurrentLevel());
			break;
		case INC_MAX_HEALTH:
			playerData.increaseMaxHealth(Config.MAX_HEALTH_INC);
			playerData.restoreHealth(1);
			playerData.restoreMana(playerData.getMaxMana());
			playerData.getUpgrades().add(playerData.getCurrentLevel());
			break;
		case INC_MAX_MANA:
			playerData.increaseMaxMana(Config.MAX_MANA_INC);
			playerData.restoreHealth(1);
			playerData.restoreMana(playerData.getMaxMana());
			playerData.getUpgrades().add(playerData.getCurrentLevel());
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
		case NEUTRAL2:
			if(!playerData.getPowers().contains(Element.NEUTRAL2)){
				playerData.getPowers().add(Element.NEUTRAL2);
			}
			playerData.getLevsComp().add(playerData.getCurrentLevel());
			consApp.showLevelScreen();
			break;
		case FIRE:
			if(!playerData.getPowers().contains(Element.FIRE)){
				playerData.getPowers().add(Element.FIRE);
			}
			playerData.getLevsComp().add(playerData.getCurrentLevel());
			consApp.showLevelScreen();
			break;
		case EARTH:
			if(!playerData.getPowers().contains(Element.EARTH)){
				playerData.getPowers().add(Element.EARTH);
			}
			playerData.getLevsComp().add(playerData.getCurrentLevel());
			consApp.showLevelScreen();
			break;
		case LIGHTNING: 
			if(!playerData.getPowers().contains(Element.LIGHTNING)){
				playerData.getPowers().add(Element.LIGHTNING);
			}
			playerData.getLevsComp().add(playerData.getCurrentLevel());
			consApp.showLevelScreen();
			break;
		case METAL:
			if(!playerData.getPowers().contains(Element.METAL)){
				playerData.getPowers().add(Element.METAL);
			}
			playerData.getLevsComp().add(playerData.getCurrentLevel());
			consApp.showLevelScreen();
			break;
		case DEATH:
			if(!playerData.getPowers().contains(Element.DEATH)){
				playerData.getPowers().add(Element.DEATH);
			}
			playerData.getLevsComp().add(playerData.getCurrentLevel());
			consApp.showLevelScreen();
			break;
		case CONSUME:
			if(!playerData.getPowers().contains(Element.CONSUME)){
				playerData.getPowers().add(Element.CONSUME);
			}
			playerData.getLevsComp().add(playerData.getCurrentLevel());
			consApp.showLevelScreen();
			break;
		default:
			log.info("Picked up an unknown powerup: " + type);
			break;
		}
	}
}
