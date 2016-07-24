package com.almasb.consume.ai;

import com.almasb.consume.Config;
import com.almasb.consume.Config.Speed;
import com.almasb.consume.Event;
import com.almasb.fxgl.entity.AbstractControl;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.FXGLEvent;
import com.almasb.fxgl.time.TimerManager;

public class ChargeControl extends AbstractControl {

	private Entity target;
	private int vel = 0;
	private boolean seenPlayer = false; // Stores when the enemy first sees the player
	private long startAttack = -1;
	private long decelerate = -1;

	public ChargeControl(Entity target) {
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
		if(!seenPlayer && isTargetInRange()){
			seenPlayer = true;
		}
		
		if(seenPlayer && startAttack == -1){
			entity.fireFXGLEvent(new FXGLEvent(Event.ENEMY_SAW_PLAYER));
			startAttack = now;
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
					startAttack = -1;
					decelerate = -1;
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
					startAttack = -1;
					decelerate = -1;
				}
			}
		}
		else if(startAttack != -1 && now - startAttack >= TimerManager.toNanos(Config.ENEMY_CHARGE_DELAY)){
			decelerate = - 1;
			vel += right ? Speed.ENEMY_SEEK_ACCEL : -Speed.ENEMY_SEEK_ACCEL;

			if (Math.abs(vel) >= Speed.ENEMY_SEEK_MAX)
				vel = (int) Math.signum(vel) * (Speed.ENEMY_SEEK_MAX);

			entity.getControl(PhysicsControl.class).moveX(vel);
		}
		
		entity.setProperty("facingRight", vel > 0);
	}

	private boolean isTargetInRange() {
		return target.getPosition().distance(entity.getPosition()) <= Config.ENEMY_CHARGE_RANGE + Config.BLOCK_SIZE;
	}

	public int getVelocity() {
		return vel;
	}
}
