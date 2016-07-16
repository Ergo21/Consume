package com.almasb.consume.ai;

import com.almasb.consume.Config;
import com.almasb.consume.Event;
import com.almasb.consume.Config.Speed;
import com.almasb.consume.ConsumeApp;
import com.almasb.fxgl.entity.AbstractControl;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.FXGLEvent;
import com.almasb.fxgl.time.TimerManager;

public class RifleControl extends AbstractControl {

	private ConsumeApp consApp;
	private Entity target;
	private int vel;
	private long whenThrown;
	private boolean spearThrown;
	private boolean passive;
	private boolean sceneStarted;
	
	public RifleControl(ConsumeApp cA, Entity target, boolean pass) {
		this.consApp = cA;
		this.target = target;
		vel = Speed.ENEMY_PATROL/2;
		whenThrown = 0;
		spearThrown = false;
		passive = pass;
		sceneStarted = false;
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
		if(passive){
			if(sceneStarted){
				if(!(boolean) consApp.player.getProperty("scenePlaying")){
					passive = false;
				}
			}
			else{
				sceneStarted = (boolean) consApp.player.getProperty("scenePlaying");
			}
			return;
		}
		
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