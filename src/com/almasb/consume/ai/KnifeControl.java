package com.almasb.consume.ai;

import com.almasb.consume.Config;
import com.almasb.consume.Event;
import com.almasb.consume.Config.Speed;
import com.almasb.fxgl.entity.AbstractControl;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.FXGLEvent;
import com.almasb.fxgl.time.TimerManager;

public class KnifeControl extends AbstractControl {

	private Entity target;
	private int vel;
	private long created;
	private long decelerate = -1;
	
	public KnifeControl(Entity target) {
		this.target = target;
		vel = -Speed.PLAYER_MOVE;
		created = -1;
	}

	@Override
	public void onUpdate(Entity entity, long now) {
		if (isTargetInRange()) {
			boolean right = entity.getPosition().getX() > target.getPosition().getX();
			
			if(right && vel > 0){
				if(decelerate == -1){
					decelerate = now;
				}
				
				if(now - decelerate > TimerManager.toNanos(Config.ENEMY_CHARGE_DELAY.divide(2))){
					vel -= Speed.ENEMY_SEEK_DECEL;
					if(vel <= 0){
						entity.setProperty("facingRight", false);
						vel = 0;
					}
					entity.getControl(PhysicsControl.class).moveX(vel);
				}
			}
			else if(right){
				vel = -Speed.PLAYER_MOVE;
				entity.setProperty("facingRight", false);
				entity.getControl(PhysicsControl.class).moveX(vel);
				decelerate = -1;
			}else if (!right && vel < 0) {
				if(decelerate == -1){
					decelerate = now;
				}
				
				if(now - decelerate > TimerManager.toNanos(Config.ENEMY_CHARGE_DELAY.divide(2))){
					vel += Speed.ENEMY_SEEK_DECEL;
					if(vel >= 0){
						entity.setProperty("facingRight", true);
						vel = 0;
					}
					entity.getControl(PhysicsControl.class).moveX(vel);
				}			
			} else if(!right){
				vel = Speed.PLAYER_MOVE;
				entity.setProperty("facingRight", true);
				entity.getControl(PhysicsControl.class).moveX(vel);
				decelerate = -1;
			}
			if(isTargetClose() && (created == -1 || now - created > TimerManager.toNanos(Config.CONSUME_DECAY) + TimerManager.toNanos(Config.ENEMY_CHARGE_DELAY))){
				created = now;
				entity.fireFXGLEvent(new FXGLEvent(Event.ENEMY_FIRED));
			}
		}
		else{
			entity.getControl(PhysicsControl.class).moveX(vel);
		}
		
	}

	@Override
	protected void initEntity(Entity entity) {
		// TODO Auto-generated method stub
		entity.addFXGLEventHandler(Event.COLLIDED_PLATFORM, (event) -> {
			
		});
	}

	private boolean isTargetInRange() {
		return target.getPosition().distance(entity.getPosition()) <= Config.ENEMY_FIRE_RANGE;
	}
	
	private boolean isTargetClose() {
		return target.getPosition().distance(entity.getPosition()) <= Config.ENEMY_FIRE_RANGE/2;
	}
}