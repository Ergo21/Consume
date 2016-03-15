package com.almasb.consume.ai;

import com.almasb.consume.Config.Speed;
import com.almasb.consume.Event;
import com.almasb.fxgl.entity.AbstractControl;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.FXGLEvent;


public class SpearProjectileControl extends AbstractControl {
	private boolean facingRight;
	private Entity player;
	private Entity picBox;
	private boolean customVelocity;
	private int moveX;
	private int moveY;
	private int change;

	public SpearProjectileControl(Entity pl, Entity picB) {
		facingRight = pl.getProperty("facingRight");
		player = pl;
		picBox = picB;
		customVelocity = false;
		change = 2;
	}
	
	public SpearProjectileControl(Entity pl, int mX, int mY) {
		facingRight = pl.getProperty("facingRight");
		player = pl;
		moveX = mX;
		moveY = mY;
		customVelocity = true;
		change = 4;
	}
	
	public SpearProjectileControl(Entity pl, Entity picB, int mX, int mY) {
		facingRight = pl.getProperty("facingRight");
		player = pl;
		picBox = picB;
		moveX = mX;
		moveY = mY;
		customVelocity = true;
		change = 4;
	}

	@Override
	public void onUpdate(Entity entity, long now){
		actualUpdate(entity, now);
	}
	
	public void actualUpdate(Entity entity, long now) {
		PhysicsControl pc = entity.getControl(PhysicsControl.class);
		if(picBox != null){
			if(pc != null){
				if(facingRight){
					if(pc.getVelocity().getY() < -1){
						if(picBox.getRotate() < -10){
							picBox.setRotate(picBox.getRotate() + change);
						}
					}
					else if(pc.getVelocity().getY() >= -1 && pc.getVelocity().getY() < 1){
						if(picBox.getRotate() < 10){
							picBox.setRotate(picBox.getRotate() + change);
						}
					}
					else if(pc.getVelocity().getY() >= 1){
						if(picBox.getRotate() < 45){
							picBox.setRotate(picBox.getRotate() + change);
						}
					}
				}
				else{
					if(pc.getVelocity().getY() < -1){
						if(picBox.getRotate() > 10){
							picBox.setRotate(picBox.getRotate() - change);
						}
					}
					else if(pc.getVelocity().getY() >= -1 && pc.getVelocity().getY() < 1){
						if(picBox.getRotate() > -10){
							picBox.setRotate(picBox.getRotate() - change);
						}
					}
					else if(pc.getVelocity().getY() >= 1){
						if(picBox.getRotate() > -45){
							picBox.setRotate(picBox.getRotate() - change);
						}
					}
				}
			}
		}
		
		
		if (Math.abs(entity.getTranslateX() - player.getTranslateX()) >= 350) {
			entity.fireFXGLEvent(new FXGLEvent(Event.DEATH));
		}
	}

	@Override
	protected void initEntity(Entity entity) {
		PhysicsControl pc = entity.getControl(PhysicsControl.class);
		if(picBox != null && facingRight){
			picBox.setRotate(-45);
		}
		else if (picBox != null){
			picBox.setRotate(45);
		}
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
