package com.almasb.consume.ai;

import com.almasb.consume.Config;
import com.almasb.consume.Event;
import com.almasb.consume.Config.Speed;
import com.almasb.fxgl.entity.AbstractControl;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.FXGLEvent;
import com.almasb.fxgl.time.TimerManager;

public class MummyControl extends AbstractControl {

	private Entity target;
	private boolean playerSeen;
	private int vel;
	private double lastX;
	private long created;
	
	public MummyControl(Entity target) {
		this.target = target;
		vel = -Speed.ENEMY_PATROL/3;
		created = 0;
		playerSeen = false;
	}

	@Override
	public void onUpdate(Entity entity, long now) {
		if (isTargetInRange() && !playerSeen) {
			playerSeen = true;
		} else if (!isTargetInRange() && playerSeen) {
			playerSeen = false;
		} else if (playerSeen) {
			if (entity.getPosition().getX() > target.getPosition().getX()) {
				entity.getControl(PhysicsControl.class).moveX(-Speed.ENEMY_PATROL/3);
				entity.setProperty("facingRight", false);				
			} else {
				entity.getControl(PhysicsControl.class).moveX(Speed.ENEMY_PATROL/3);
				entity.setProperty("facingRight", true);
			}
			if(now - created > TimerManager.toNanos(Config.CONSUME_DECAY)){
				created = now;
				entity.fireFXGLEvent(new FXGLEvent(Event.ENEMY_FIRED));
			}
		} else if(lastX == entity.getPosition().getX()){
			vel = -vel;
			entity.getControl(PhysicsControl.class).moveX(vel);
			entity.setProperty("facingRight", !(boolean)entity.getProperty("facingRight"));
		}
		else{
			lastX = entity.getPosition().getX();
			entity.getControl(PhysicsControl.class).moveX(vel);
		}
		
	}

	@Override
	protected void initEntity(Entity entity) {
		// TODO Auto-generated method stub

	}

	private boolean isTargetInRange() {
		return target.getPosition().distance(entity.getPosition()) <= Config.ENEMY_FIRE_RANGE*2/3;
	}
}