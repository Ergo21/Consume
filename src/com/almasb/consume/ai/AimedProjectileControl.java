package com.almasb.consume.ai;

import javafx.geometry.Point2D;

import com.almasb.consume.Config.Speed;
import com.almasb.consume.Event;
import com.almasb.fxgl.entity.AbstractControl;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.FXGLEvent;

public class AimedProjectileControl extends AbstractControl {

	private Entity target;
	private double angle;

	public AimedProjectileControl(Entity source, Entity targ) {
		target = targ;
		Point2D aim = new Point2D(target.getTranslateX() + target.getWidth() / 2,
				target.getTranslateY() + target.getHeight() / 2);
		angle = Math.asin((source.getPosition().getX() - aim.getX()) / source.getPosition().distance(aim));
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
		entity.setTranslateX(entity.getTranslateX() + (Math.round(-(float) Math.sin(angle) * Speed.PROJECTILE)));
		entity.setTranslateY(entity.getTranslateY() + (Math.round((float) Math.cos(angle) * Speed.PROJECTILE)));

		if (Math.abs(entity.getTranslateX() - target.getTranslateX()) >= 350
				|| Math.abs(entity.getTranslateY() - target.getTranslateY()) >= 350) {
			entity.fireFXGLEvent(new FXGLEvent(Event.DEATH));
		}
	}

	public int getVelocityX() {
		return Math.round(-(float) Math.sin(angle) * Speed.PROJECTILE);
	}
}
