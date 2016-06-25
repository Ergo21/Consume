package com.almasb.consume.ai;

import javafx.geometry.Point2D;

import com.almasb.consume.Config.Speed;
import com.almasb.consume.Event;
import com.almasb.fxgl.entity.AbstractControl;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.FXGLEvent;

public class AimedFireballControl extends AbstractControl {

	private Entity target;
	private double angle;
	private int movX;
	private int movY;

	public AimedFireballControl(Entity source, Entity targ) {
		target = targ;
		Point2D aim = new Point2D(target.getPosition().getX() + target.getWidth() / 2,
				target.getPosition().getY() + target.getHeight() / 2);
		if(source.getPosition().getX() >= aim.getX()){
			angle = Math.acos((source.getPosition().getX() - aim.getX()) / source.getPosition().distance(aim));
		}
		else{
			angle = Math.acos((aim.getX() - source.getPosition().getX()) / source.getPosition().distance(aim));
		}
		movX = Math.round((float)Math.cos(angle)*Speed.PROJECTILE);
		movY = Math.round((float)Math.sin(angle)*Speed.PROJECTILE);
		if(aim.getX() < source.getPosition().getX()){
			movX = -movX;
		}
		if(aim.getY() < source.getPosition().getY()){
			movY = -movY;
		}
	}

	@Override
	protected void initEntity(Entity entity) {
		// TODO Auto-generated method stub

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
		if(entity != null && entity.getControl(PhysicsControl.class) != null){
			entity.getControl(PhysicsControl.class).moveX(movX);
			entity.getControl(PhysicsControl.class).moveY(movY);
		}
		
		if (Math.abs(entity.getPosition().getX() - target.getPosition().getX()) >= 700
				|| Math.abs(entity.getPosition().getY() - target.getPosition().getY()) >= 500) {
			entity.fireFXGLEvent(new FXGLEvent(Event.DEATH));
		}
	}

	public int getVelocityX() {
		return Math.round(-(float) Math.sin(angle) * Speed.PROJECTILE);
	}
}
