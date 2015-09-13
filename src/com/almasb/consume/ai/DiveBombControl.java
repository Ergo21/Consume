package com.almasb.consume.ai;

import com.almasb.consume.Config;
import com.almasb.consume.Config.Speed;
import com.almasb.fxgl.entity.AbstractControl;
import com.almasb.fxgl.entity.Entity;

public class DiveBombControl extends AbstractControl {

	private Entity target;
	private int vel = 0;
	private double dive = 0;
	private boolean notDived = true;
	private double originalY;

	public DiveBombControl(Entity target, double oY) {
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
				dive = 0;
				notDived = false;
			}
		} else if (entity.getTranslateY() > originalY) {
			dive -= Config.ENEMY_DIVEBOMB_ACC;
			if (target.getControl(PhysicsControl.class).getVelocity().getX() <= 0) {
				dive += Config.ENEMY_DIVEBOMB_ACC / 2;
			}
		} else {
			dive = 0;
			vel = -Speed.ENEMY_PATROL;
		}

		entity.setTranslateX(entity.getTranslateX() + vel);
		// entity.getControl(PhysicsControl.class).moveY((int)dive);
		entity.setTranslateY(entity.getTranslateY() + dive);
		if (entity.getTranslateX() - target.getTranslateX() <= -400) {
			entity.setTranslateX(target.getTranslateX() + 400);
			notDived = true;
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