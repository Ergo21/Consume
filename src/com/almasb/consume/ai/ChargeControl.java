package com.almasb.consume.ai;

import com.almasb.consume.Config;
import com.almasb.consume.Config.Speed;
import com.almasb.consume.Event;
import com.almasb.consume.Physics;
import com.almasb.fxgl.entity.AbstractControl;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.FXGLEvent;

public class ChargeControl extends AbstractControl {

	private Entity target;
	private int vel = 0;
	private long firstTimeSaw = -1; // Stores when the enemy first sees the
									// player
	private long lastTimeSaw = -1; // Stores when the enemy last saw the player

	public ChargeControl(Entity target) {
		this.target = target;
	}

	@Override
	protected void initEntity(Entity entity) {
		entity.addFXGLEventHandler(Event.ENEMY_HIT_PLAYER, event -> {
			vel = 0;
			firstTimeSaw = -1;
		});
	}

	@Override
	public void onUpdate(Entity entity, long now) {
		if (isTargetInRange()) {
			if (firstTimeSaw == -1) {
				firstTimeSaw = now;
				lastTimeSaw = now;
				entity.fireFXGLEvent(new FXGLEvent(Event.ENEMY_SAW_PLAYER));
			} else {
				lastTimeSaw = now;
			}
		} else {
			firstTimeSaw = -1;
		}

		if (firstTimeSaw != -1 && now - firstTimeSaw >= Config.ENEMY_CHARGE_DELAY) {
			boolean right = target.getTranslateX() - entity.getTranslateX() > 0;

			vel += right ? Speed.ENEMY_SEEK_ACCEL : -Speed.ENEMY_SEEK_ACCEL;

			if (Math.abs(vel) >= Speed.ENEMY_SEEK_MAX)
				vel = (int) Math.signum(vel) * Speed.ENEMY_SEEK_MAX;

			Physics physics = entity.getProperty("physics");
			boolean canMove = physics.moveX(entity, vel);

			if (!canMove)
				vel = 0;
		} else if (now - lastTimeSaw < Config.ENEMY_CHARGE_DELAY / 2 && vel != 0) {
			boolean right = vel > 0;

			vel += right ? Speed.ENEMY_SEEK_ACCEL : -Speed.ENEMY_SEEK_ACCEL;

			if (Math.abs(vel) >= Speed.ENEMY_SEEK_MAX)
				vel = (int) Math.signum(vel) * Speed.ENEMY_SEEK_MAX;

			Physics physics = entity.getProperty("physics");
			boolean canMove = physics.moveX(entity, vel);

			if (!canMove)
				vel = 0;
		} else {
			if (vel == 0)
				return;

			if (vel > 0)
				vel -= Speed.ENEMY_SEEK_DECEL;
			else
				vel += Speed.ENEMY_SEEK_DECEL;

			Physics physics = entity.getProperty("physics");
			boolean canMove = physics.moveX(entity, vel);

			if (!canMove)
				vel = 0;
		}

	}

	private boolean isTargetInRange() {
		return target.getPosition().distance(entity.getPosition()) <= Config.ENEMY_CHARGE_RANGE
				&& Math.abs(target.getTranslateY() - entity.getTranslateY()) <= 10;
	}

	public int getVelocity() {
		return vel;
	}
}
