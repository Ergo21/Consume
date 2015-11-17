package com.almasb.consume.ai;

import com.almasb.consume.Config.Speed;
import com.almasb.consume.Event;
import com.almasb.fxgl.entity.AbstractControl;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.FXGLEvent;

public class SpearProjectileControl extends AbstractControl {
	private boolean facingRight;
	private Entity player;
	private boolean customVelocity;
	private int moveX;
	private int moveY;

	public SpearProjectileControl(Entity pl) {
		facingRight = pl.getProperty("facingRight");
		player = pl;
		customVelocity = false;
	}
	
	public SpearProjectileControl(Entity pl, int mX, int mY) {
		facingRight = pl.getProperty("facingRight");
		player = pl;
		moveX = mX;
		moveY = mY;
		customVelocity = true;
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
		if(customVelocity){
			pc.moveX(facingRight ? moveX : -moveX);
			pc.moveY(moveY);
		}
		else{
			pc.moveX(facingRight ? Speed.PROJECTILE : -Speed.PROJECTILE);
			pc.moveY(-Speed.PROJECTILE);
		}
		
	}
}
