package com.almasb.consume.ai;

import com.almasb.consume.Config;
import com.almasb.consume.Config.Speed;
import com.almasb.fxgl.entity.AbstractControl;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.time.TimerManager;

public class DiveReturnControl extends AbstractControl {

	private Entity target;
	private int vel = 0;
	private double dive = 0;
	private boolean notDived = true;
	private double originalY;
	private long startWait = 0;

	public DiveReturnControl(Entity target, double oY) {
		this.target = target;
		originalY = oY;
	}

	@Override	
	public void onUpdate(Entity entity, long now) {
		if (isTargetInRange() && notDived) {
			dive += Config.ENEMY_DIVEBOMB_ACC;
			if (target.getControl(PhysicsControl.class).getVelocity().getX() > 0) {
				dive += Config.ENEMY_DIVEBOMB_ACC;
			} else if (target.getControl(PhysicsControl.class).getVelocity().getX() < 0) {
				dive -= Config.ENEMY_DIVEBOMB_ACC / 2;
			}
			if (target.getTranslateY() <= entity.getTranslateY()) {
				dive = -Config.ENEMY_DIVEBOMB_ACC;
				notDived = false;
			}
			
		} else if (entity.getTranslateY() > originalY) {
			dive -= Config.ENEMY_DIVEBOMB_ACC;
			if ((target.getControl(PhysicsControl.class).getVelocity().getX() <= 0 &&
				target.getTranslateX() < entity.getTranslateX()) || 
				(target.getControl(PhysicsControl.class).getVelocity().getX() > 0 &&
				target.getTranslateX() > entity.getTranslateX())) {
				dive += Config.ENEMY_DIVEBOMB_ACC / 2;
			}
		} else {
			dive = 0;
		}
		
		if(entity.getTranslateX() > target.getTranslateX() && notDived){
			//To Right of player
			vel = -Speed.ENEMY_PATROL + (int)target.getControl(PhysicsControl.class).getVelocity().getX();
		}
		else if (notDived){
			vel = Speed.ENEMY_PATROL + (int)target.getControl(PhysicsControl.class).getVelocity().getX();
		}

		entity.setTranslateX(entity.getTranslateX() + vel);
		// entity.getControl(PhysicsControl.class).moveY((int)dive);
		entity.setTranslateY(entity.getTranslateY() + dive);
		if (entity.getTranslateX() - target.getTranslateX() <= -200 || 
			entity.getTranslateX() - target.getTranslateX() >= 200) {
			vel = (int)target.getControl(PhysicsControl.class).getVelocity().getX();
			if(dive == 0){
				if(startWait == 0){
					startWait = now;
				}
				if(now - startWait >= TimerManager.toNanos(Config.ENEMY_CHARGE_DELAY)){
					notDived = true;
					startWait = 0;
				}
			}
		}
	}

	@Override
	protected void initEntity(Entity entity) {
		// TODO Auto-generated method stub
	}

	private boolean isTargetInRange() {
		return target.getPosition().distance(entity.getPosition()) <= Config.ENEMY_FIRE_RANGE;
	}

	public int getVelocity() {
		return vel;
	}

}