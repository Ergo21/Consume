package com.almasb.consume.ai;

import com.almasb.consume.Config;
import com.almasb.consume.Event;
import com.almasb.consume.Config.Speed;
import com.almasb.fxgl.entity.AbstractControl;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.FXGLEvent;
import com.almasb.fxgl.time.TimerManager;

public class ConsumeControl extends AbstractControl {

	private Entity target;
	private boolean playerSeen;
	private int vel;
	private double lastX;
	private long created;
	
	public ConsumeControl(Entity target) {
		this.target = target;
		vel = Speed.ENEMY_PATROL/2;
		created = 0;
		playerSeen = false;
	}

	@Override
	public void onUpdate(Entity entity, long now) {
		int v = (int)entity.getControl(PhysicsControl.class).getVelocity().getX();
		
		if(v == 0){
			v = vel;
		}
		
		if (isTargetInRange() && !playerSeen) {
			playerSeen = true;
		} else if (!isTargetInRange() && playerSeen) {
			playerSeen = false;
		} else if (playerSeen) {
			if (entity.getPosition().getX() > target.getPosition().getX()) {
				if(v > 0){
					v = -v;
				}
				entity.getControl(PhysicsControl.class).moveX(v);
			} else {
				if(v < 0){
					v = -v;
				}
				entity.getControl(PhysicsControl.class).moveX(v);
			}
			if(now - created > TimerManager.toNanos(Config.CONSUME_DECAY) + TimerManager.toNanos(Config.ENEMY_CHARGE_DELAY)){
				created = now;
				entity.fireFXGLEvent(new FXGLEvent(Event.ENEMY_FIRED));
			}
		} else if(lastX == entity.getPosition().getX()){
			v = (int)-entity.getControl(PhysicsControl.class).getVelocity().getX();
			entity.getControl(PhysicsControl.class).moveX(v);
		}
		else{
			lastX = entity.getPosition().getX();
			entity.getControl(PhysicsControl.class).moveX(v);
		}
		
		entity.setProperty("facingRight", v > 0);		
		
	}

	@Override
	protected void initEntity(Entity entity) {
		// TODO Auto-generated method stub

	}

	private boolean isTargetInRange() {
		return target.getPosition().distance(entity.getPosition()) <= Config.ENEMY_FIRE_RANGE/2;
	}
}