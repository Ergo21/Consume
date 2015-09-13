package com.almasb.consume.ai;

import com.almasb.consume.Config;
import com.almasb.consume.Config.Speed;
import com.almasb.consume.Event;
import com.almasb.fxgl.entity.AbstractControl;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.FXGLEvent;
import com.almasb.fxgl.time.TimerManager;

public class ChargeControl extends AbstractControl {

	private Entity target;
	private int vel = 0;
	private long firstTimeSaw = -1; // Stores when the enemy first sees the player
	private long lastTimeSaw = -1; // Stores when the enemy last saw the player
	private boolean hitPlayer = false;
	private long firstHitPlayer = -1;

	public ChargeControl(Entity target) {
		this.target = target;
	}

	@Override
	protected void initEntity(Entity entity) {
		entity.addFXGLEventHandler(Event.ENEMY_HIT_PLAYER, event -> {
			hitPlayer = true;
		});
	}

	@Override
	public void onUpdate(Entity entity, long now) {
		if (isTargetInRange() && !hitPlayer) {
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

		vel = (int)entity.getControl(PhysicsControl.class).getVelocity().getX();

		if(firstHitPlayer == -1 && hitPlayer){
			firstHitPlayer = now;
		}
		else if(now - firstHitPlayer > TimerManager.toNanos(Config.ENEMY_CHARGE_DELAY) * 2){
			firstHitPlayer = -1;
			hitPlayer = false;
		}

		if (firstTimeSaw != -1 && now - firstTimeSaw >= TimerManager.toNanos(Config.ENEMY_CHARGE_DELAY)) {
			boolean right = target.getTranslateX() - entity.getTranslateX() > 0;

			vel += right ? Speed.ENEMY_SEEK_ACCEL : -Speed.ENEMY_SEEK_ACCEL;

			if (Math.abs(vel) >= Speed.ENEMY_SEEK_MAX)
				vel = (int) Math.signum(vel) * Speed.ENEMY_SEEK_MAX;

			entity.getControl(PhysicsControl.class).moveX(vel);

		} else if (now - lastTimeSaw < TimerManager.toNanos(Config.ENEMY_CHARGE_DELAY) / 2 && vel != 0 && !hitPlayer) {
			boolean right = vel > 0;

			vel += right ? Speed.ENEMY_SEEK_ACCEL : -Speed.ENEMY_SEEK_ACCEL;

			if (Math.abs(vel) >= Speed.ENEMY_SEEK_MAX)
				vel = (int) Math.signum(vel) * Speed.ENEMY_SEEK_MAX;

			entity.getControl(PhysicsControl.class).moveX(vel);
		} else {
			if (vel == 0)
				return;

			if (vel > 0)
				vel -= Speed.ENEMY_SEEK_DECEL;
			else
				vel += Speed.ENEMY_SEEK_DECEL;

			entity.getControl(PhysicsControl.class).moveX(vel);
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
