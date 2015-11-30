package com.almasb.consume.ai;

import com.almasb.consume.Config;
import com.almasb.consume.Event;
import com.almasb.consume.Config.Speed;
import com.almasb.fxgl.entity.AbstractControl;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.FXGLEvent;
import com.almasb.fxgl.time.TimerManager;

public class MagicianControl extends AbstractControl {

	private Entity target;
	private int vel;
	private long whenThrown;
	private boolean spearThrown;
	
	public MagicianControl(Entity target) {
		this.target = target;
		vel = Speed.ENEMY_PATROL/2;
		whenThrown = 0;
		spearThrown = false;
	}

	@Override
	public void onUpdate(Entity entity, long now) {
		if (isTargetInRange() && !spearThrown) {
			spearThrown = true;
			whenThrown = now;
			entity.fireFXGLEvent(new FXGLEvent(Event.ENEMY_FIRED));
		}
		
		if(now - whenThrown < TimerManager.toNanos(Config.ENEMY_CHARGE_DELAY)){
			entity.getControl(PhysicsControl.class).moveX(0);
		} else if(entity.getPosition().getX() > target.getPosition().getX()){
			if(isTargetTooClose()){
				entity.getControl(PhysicsControl.class).moveX(vel);
				entity.setProperty("facingRight", false);
			}
			else if(!isTargetInRange()){
				entity.getControl(PhysicsControl.class).moveX(-vel);
				entity.setProperty("facingRight", false);
			}
			else{
				entity.getControl(PhysicsControl.class).moveX(0);
				entity.setProperty("facingRight", false);
			}
		}
		else if(entity.getPosition().getX() < target.getPosition().getX()){
			if(isTargetTooClose()){
				entity.getControl(PhysicsControl.class).moveX(-vel);
				entity.setProperty("facingRight", true);
			}
			else if(!isTargetInRange()){
				entity.getControl(PhysicsControl.class).moveX(vel);
				entity.setProperty("facingRight", true);
			}
			else{
				entity.getControl(PhysicsControl.class).moveX(0);
				entity.setProperty("facingRight", true);
			}
		}
		
		if(target.getPosition().getY() + 10 <= entity.getPosition().getY() && !(boolean) entity.getProperty("jumping")){
			entity.getControl(PhysicsControl.class).setJump(Speed.ENEMY_JUMP);
			entity.getControl(PhysicsControl.class).jump();
		}
	}
	
	public void setSpearThrown(boolean b){
		spearThrown = b;
	}

	@Override
	protected void initEntity(Entity entity) {
		// TODO Auto-generated method stub

	}

	private boolean isTargetInRange() {
		return target.getPosition().distance(entity.getPosition()) <= Config.ENEMY_FIRE_RANGE;
	}
	
	private boolean isTargetTooClose() {
		return target.getPosition().distance(entity.getPosition()) <= Config.ENEMY_FIRE_RANGE - 10;
	}
}