package com.almasb.consume.ai;

import com.almasb.consume.Config.Speed;
import com.almasb.consume.Event;
import com.almasb.fxgl.entity.AbstractControl;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.FXGLEvent;

public class BulletProjectileControl extends AbstractControl {

	private boolean facingRight;
	private Entity player;

	public BulletProjectileControl(Entity player, boolean facingRight) {
		this.facingRight = facingRight;
		this.player = player;
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
		PhysicsControl control = entity.getControl(PhysicsControl.class);
		control.moveX(facingRight ? 
				(Speed.PROJECTILE + Speed.PROJECTILE / 2) : -(Speed.PROJECTILE + Speed.PROJECTILE / 2));

		if (Math.abs(entity.getTranslateX() - player.getTranslateX()) >= 550) {
			entity.fireFXGLEvent(new FXGLEvent(Event.DEATH));
		}
	}
}
