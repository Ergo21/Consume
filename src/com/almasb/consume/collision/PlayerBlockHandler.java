package com.almasb.consume.collision;

import java.util.ArrayList;
import java.util.function.Consumer;

import com.almasb.consume.ConsumeApp;
import com.almasb.consume.Event;
import com.almasb.consume.Types.Block;
import com.almasb.consume.Types.Property;
import com.almasb.consume.Types.Type;
import com.almasb.consume.ai.PhysicsControl;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.FXGLEvent;
import com.almasb.fxgl.physics.CollisionHandler;


public class PlayerBlockHandler extends CollisionHandler {

	private ConsumeApp consApp;
	private Consumer<String> changeScene;
	public ArrayList<Entity> laddersOn;

	public PlayerBlockHandler(ConsumeApp cA, Consumer<String> chSc) {
		super(Type.PLAYER, Type.BLOCK);
		consApp = cA;
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
			if(!player.<Boolean>getProperty("climb") && block.<Boolean>getProperty("top")){
				double movingDown = player.getControl(PhysicsControl.class).getVelocity().getY();
				if (movingDown > 0) {
					player.setTranslateY(block.getTranslateY() - (player.getHeight())); 
					player.getControl(PhysicsControl.class).moveY(0);
					player.setProperty("jumping", false);
				} 		
			}				
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
			if(!laddersOn.contains(block)){
				laddersOn.add(block);
			}
			if(!player.<Boolean>getProperty("climb") && block.<Boolean>getProperty("top")){
				double movingDown = player.getControl(PhysicsControl.class).getVelocity().getY();
				if (movingDown > 0) {
					player.setTranslateY(block.getTranslateY() - (player.getHeight())); 
					player.getControl(PhysicsControl.class).moveY(0);
					player.setProperty("jumping", false);
				} 		
			}
		}
		else if(block.getProperty(Property.SUB_TYPE) == Type.ENEMY_SPAWNER){
			block.fireFXGLEvent(new FXGLEvent(Event.ENEMY_FIRED));
			consApp.getSceneManager().removeEntity(block);
		}
	}

	@Override
	public void onCollisionEnd(Entity player, Entity block) {
		if (block.getProperty(Property.SUB_TYPE) == Block.LADDER) {
			laddersOn.remove(block);
			if(laddersOn.isEmpty()){
				player.setProperty("climb", false);
				player.setProperty("climbing", false);
				player.setProperty(Property.ENABLE_GRAVITY, true);
			}
		}
		else if(block.getProperty(Property.SUB_TYPE) == Block.BARRIER){
			if("left".equals(block.getProperty("start"))){
				if(player.getTranslateX() > block.getTranslateX() + block.getWidth()){
					consApp.activateBarrier(block);
				}
				else{
					block.setProperty("start", "none");
					block.setProperty("state", "idle");
				}
			}
			else if("right".equals(block.getProperty("start"))){
				if(player.getTranslateX() + player.getWidth() < block.getTranslateX()){
					consApp.activateBarrier(block);
				}
				else{
					block.setProperty("start", "none");
					block.setProperty("state", "idle");
				}
			}
		}
	}
	
}
