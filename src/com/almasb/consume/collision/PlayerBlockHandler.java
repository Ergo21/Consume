package com.almasb.consume.collision;

import java.util.ArrayList;
import java.util.function.Consumer;

import com.almasb.consume.Types.Block;
import com.almasb.consume.Types.Property;
import com.almasb.consume.Types.Type;
import com.almasb.consume.ai.PhysicsControl;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.physics.CollisionHandler;

public class PlayerBlockHandler extends CollisionHandler {

	private Consumer<String> changeScene;
	private ArrayList<Entity> laddersOn;

	public PlayerBlockHandler(Consumer<String> chSc) {
		super(Type.PLAYER, Type.BLOCK);
		changeScene = chSc;
		laddersOn = new ArrayList<>();
	}

	@Override
	public void onCollision(Entity player, Entity block) {
		if (block.getProperty(Property.SUB_TYPE) == Block.BARRIER) {
			block.setProperty("state", "passing");

			if ("none".equals(block.getProperty("start"))) {
				if (player.getTranslateX() <= block.getTranslateX()) {
					block.setProperty("start", "left");
				} else {
					block.setProperty("start", "right");
				}
			}
		} else if (block.getProperty(Property.SUB_TYPE) == Block.LADDER) {
			player.setProperty("climb", true);
			player.setProperty(Property.ENABLE_GRAVITY, false);
		}
	}

	@Override
	public void onCollisionBegin(Entity player, Entity block) {
		if (block.getProperty(Property.SUB_TYPE) == Block.SCENE && !(boolean) block.getProperty("played")) {
			block.setProperty("played", true);
			block.setVisible(false);
			String sNam = block.getProperty("sceneName");
			changeScene.accept(sNam);
		} else if (block.getProperty(Property.SUB_TYPE) == Block.LADDER) {
			player.setProperty("climb", true);
			if(!laddersOn.contains(block)){
				laddersOn.add(block);
			}
			player.setProperty(Property.ENABLE_GRAVITY, false);
			player.getControl(PhysicsControl.class).moveY(0);
		}
	}

	@Override
	public void onCollisionEnd(Entity player, Entity block) {
		if (block.getProperty(Property.SUB_TYPE) == Block.LADDER) {
			laddersOn.remove(block);
			if(laddersOn.isEmpty()){
				player.setProperty("climbing", false);
				player.setProperty("climb", false);
				player.setProperty(Property.ENABLE_GRAVITY, true);
			}
		}
	}
}
