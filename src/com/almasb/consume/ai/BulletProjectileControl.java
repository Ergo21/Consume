package com.almasb.consume.ai;

import com.almasb.consume.Config.Speed;
import com.almasb.consume.Event;
import com.almasb.fxgl.entity.AbstractControl;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.FXGLEvent;

public class BulletProjectileControl extends AbstractControl {

	private boolean facingRight;
	private Entity player;

	public BulletProjectileControl(Entity player) {
		facingRight = player.getProperty("facingRight");
		this.player = player;
	}

	@Override
	protected void initEntity(Entity entity) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onUpdate(Entity entity, long now) {
		PhysicsControl control = entity.getControl(PhysicsControl.class);
		control.moveX(
				facingRight ? (Speed.PROJECTILE + Speed.PROJECTILE / 2) : -(Speed.PROJECTILE + Speed.PROJECTILE / 2));

		if (Math.abs(entity.getTranslateX() - player.getTranslateX()) >= 350) {
			entity.fireFXGLEvent(new FXGLEvent(Event.DEATH));
		}
	}
}
