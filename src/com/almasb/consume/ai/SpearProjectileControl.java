package com.almasb.consume.ai;

import com.almasb.consume.Config.Speed;
import com.almasb.consume.Event;
import com.almasb.fxgl.entity.AbstractControl;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.FXGLEvent;

public class SpearProjectileControl extends AbstractControl {
	private boolean facingRight;
	private Entity player;
	private int moveY;

	public SpearProjectileControl(Entity pl) {
		facingRight = pl.getProperty("facingRight");
		player = pl;
		moveY = 0;
	}
	
	public SpearProjectileControl(Entity pl, int mY) {
		facingRight = pl.getProperty("facingRight");
		player = pl;
		moveY = mY;
	}

	@Override
	public void onUpdate(Entity entity, long now) {
		if (Math.abs(entity.getTranslateX() - player.getTranslateX()) >= 350) {
			entity.fireFXGLEvent(new FXGLEvent(Event.DEATH));
		}
	}

	@Override
	protected void initEntity(Entity entity) {
		PhysicsControl pc = entity.getControl(PhysicsControl.class);
		pc.moveX(facingRight ? Speed.PROJECTILE : -Speed.PROJECTILE);
		pc.moveY(moveY == 0 ? -Speed.PROJECTILE : moveY);
	}
}
