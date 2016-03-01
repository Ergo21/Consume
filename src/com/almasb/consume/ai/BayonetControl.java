package com.almasb.consume.ai;

import com.almasb.consume.Config;
import com.almasb.consume.Config.Speed;
import com.almasb.consume.Event;
import com.almasb.fxgl.entity.AbstractControl;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.FXGLEvent;
import com.almasb.fxgl.time.TimerManager;

public class BayonetControl extends AbstractControl {

	private Entity target;
	private int vel = 0;
	private long firstTimeSaw = -1; // Stores when the enemy first sees the player
	private long decelerate = -1;
	private long lastAttacked = -1;

	public BayonetControl(Entity target) {
		this.target = target;
	}

	@Override
	protected void initEntity(Entity entity) {
		
	}

	private int frames = 10;
	@Override
	public void onUpdate(Entity entity, long now){
		frames++;
		if(frames >= 5){
			actualUpdate(entity, now);
			frames = 0;
		}
	}
	
	public void actualUpdate(Entity entity, long now) {
		if(isTargetInRange()){
			if (firstTimeSaw == -1) {
				firstTimeSaw = now;
				entity.fireFXGLEvent(new FXGLEvent(Event.ENEMY_SAW_PLAYER));
			} 
		}
		else{
			firstTimeSaw = -1;
		}
		
		vel = (int)entity.getControl(PhysicsControl.class).getVelocity().getX();
		boolean right = target.getPosition().getX() - entity.getPosition().getX() > 0;
		
		if(vel < 0 && right){
			if(decelerate == -1){
				decelerate = now;
			}
			
			if(now - decelerate > TimerManager.toNanos(Config.ENEMY_CHARGE_DELAY.divide(2))){
				vel += Speed.ENEMY_SEEK_DECEL;
				entity.getControl(PhysicsControl.class).moveX(vel);
				if(vel >= 0){
					firstTimeSaw = -1;
				}
			}
		}
		else if(vel > 0 && !right){
			if(decelerate == -1){
				decelerate = now;
			}
			
			if(now - decelerate > TimerManager.toNanos(Config.ENEMY_CHARGE_DELAY.divide(2))){
				vel -= Speed.ENEMY_SEEK_DECEL;
				entity.getControl(PhysicsControl.class).moveX(vel);
				if(vel <= 0){
					firstTimeSaw = -1;
				}
			}
		}
		else if(firstTimeSaw != -1 && now - firstTimeSaw >= TimerManager.toNanos(Config.ENEMY_CHARGE_DELAY)){
			decelerate = - 1;
			vel += right ? Speed.ENEMY_SEEK_ACCEL : -Speed.ENEMY_SEEK_ACCEL;
			
			entity.setProperty("facingRight", right);

			if (Math.abs(vel) >= Speed.ENEMY_SEEK_MAX - 1)
				vel = (int) Math.signum(vel) * (Speed.ENEMY_SEEK_MAX - 5);

			entity.getControl(PhysicsControl.class).moveX(vel);
			
			if(isTargetClose()){
				if(lastAttacked == -1 || now - lastAttacked > TimerManager.toNanos(Config.ENEMY_FIRE_DELAY)){
					entity.fireFXGLEvent(new FXGLEvent(Event.ENEMY_FIRED));
					lastAttacked = now;
				}
			}
		}
	}

	private boolean isTargetInRange() {
		return target.getPosition().distance(entity.getPosition()) <= Config.ENEMY_CHARGE_RANGE;
	}
	
	private boolean isTargetClose(){
		return target.getPosition().distance(entity.getPosition()) <= Config.ENEMY_CHARGE_RANGE/2;
	}

	public int getVelocity() {
		return vel;
	}
}
