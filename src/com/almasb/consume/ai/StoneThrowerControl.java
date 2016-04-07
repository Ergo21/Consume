package com.almasb.consume.ai;

import com.almasb.consume.Config;
import com.almasb.consume.Event;
import com.almasb.consume.Config.Speed;
import com.almasb.fxgl.entity.AbstractControl;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.FXGLEvent;
import com.almasb.fxgl.time.TimerManager;

public class StoneThrowerControl extends AbstractControl {

	private Entity target;
	private int vel;
	private long whenThrown;
	private boolean stonesThrown;
	
	public StoneThrowerControl(Entity target) {
		this.target = target;
		vel = -Speed.PLAYER_MOVE/3;
		whenThrown = 0;
		stonesThrown = false;
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
		if (isTargetInRange() && !stonesThrown) {
			stonesThrown = true;
			whenThrown = now;
			entity.fireFXGLEvent(new FXGLEvent(Event.ENEMY_FIRED));
		} else if (isTargetInRange()) {
			if(now - whenThrown < TimerManager.toNanos(Config.ENEMY_CHARGE_DELAY) || target.getPosition().distance(entity.getPosition()) > Config.ENEMY_FIRE_RANGE/2){
				entity.getControl(PhysicsControl.class).moveX(0);
			}
			else if (entity.getPosition().getX() > target.getPosition().getX()) {
				entity.getControl(PhysicsControl.class).moveX(Speed.PLAYER_MOVE/3);
				entity.setProperty("facingRight", false);				
			} else {
				entity.getControl(PhysicsControl.class).moveX(-Speed.PLAYER_MOVE/3);
				entity.setProperty("facingRight", true);
			}
			
		} else if(entity.getPosition().getX() > target.getPosition().getX()){
			entity.getControl(PhysicsControl.class).moveX(vel);
			entity.setProperty("facingRight", false);
		}
		else{
			entity.getControl(PhysicsControl.class).moveX(-vel);
			entity.setProperty("facingRight", true);
		}
	}
	
	public void setStonesThrown(boolean b){
		stonesThrown = b;
	}

	@Override
	protected void initEntity(Entity entity) {
		// TODO Auto-generated method stub

	}

	private boolean isTargetInRange() {
		return target.getPosition().distance(entity.getPosition()) <= Config.ENEMY_FIRE_RANGE;
	}
}