package com.almasb.consume.ai;

import com.almasb.consume.Config;
import com.almasb.consume.Config.Speed;
import com.almasb.consume.ConsumeApp;
import com.almasb.fxgl.entity.AbstractControl;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.time.TimerManager;
import com.ergo21.consume.FileNames;

public class ComplexJumpControl extends AbstractControl {

	private ConsumeApp consApp;
	private Entity target;
	private int vel = 0;
	private int jump;
	private long lastJumped = -1; // Stores when the enemy first sees the player

	public ComplexJumpControl(ConsumeApp cA, Entity tar, int jum) {
		consApp = cA;
		target = tar;
		jump = jum;
	}

	@Override
	public void onUpdate(Entity entity, long now) {
		if (entity.getControl(PhysicsControl.class).getJump() != jump) {
			entity.getControl(PhysicsControl.class).setJump(jump);
		}
		if (lastJumped == -1) {
			lastJumped = now;
		}

		if (now - lastJumped > TimerManager.toNanos(Config.ENEMY_JUMP_DELAY)) {
			entity.getControl(PhysicsControl.class).moveX(calculateJumpSpeed(entity));
			
			entity.getControl(PhysicsControl.class).jump();
			lastJumped = now;
			consApp.soundManager.playSFX(FileNames.JUMP);
		} else if (!(boolean) entity.getProperty("jumping")) {
			entity.getControl(PhysicsControl.class).moveX(0);
		}
		
		if(target.getPosition().getX() > entity.getPosition().getX()){
			entity.setProperty("facingRight", true);
		}
		else{
			entity.setProperty("facingRight", false);
		}
	}

	@Override
	protected void initEntity(Entity entity) {
		// TODO Auto-generated method stub

	}
	
	private int calculateJumpSpeed(Entity entity){
		double dist = target.getPosition().getX() - entity.getPosition().getX();
		if(dist < Speed.ENEMY_PATROL*60 && dist > -Speed.ENEMY_PATROL*60){
			if((int) dist/60 == 0 && dist > 0){
				return 1;
			}
			else if((int) dist/60 == 0 && dist < 0){
				return -1;
			}
			return (int) dist/60;
		}
		else if(dist >= Speed.ENEMY_PATROL*60){
			return Speed.ENEMY_PATROL;
		}
		else{
			return -Speed.ENEMY_PATROL;
		}
	}

	public int getVelocity() {
		return vel;
	}

}