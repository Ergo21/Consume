package com.almasb.consume.ai;

import javafx.geometry.Point2D;

import com.almasb.consume.Config;
import com.almasb.consume.Event;
import com.almasb.fxgl.entity.AbstractControl;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.FXGLEvent;
import com.almasb.fxgl.time.TimerManager;

public class IngestControl extends AbstractControl {

	private Entity source;
	private long created;

	public IngestControl(Entity source) {
		this.source = source;
		created = 0;
	}

	@Override
	protected void initEntity(Entity entity) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onUpdate(Entity entity, long now) {
		// PhysicsControl control = entity.getControl(PhysicsControl.class);
		if (created == 0) {
			created = now;
		}

		Point2D nPos = source.getPosition();
		if ((boolean) source.getProperty("facingRight")) {
			nPos = nPos.add(source.getWidth(), 0);
		} else {
			nPos = nPos.add(-source.getWidth() * 0.5, 0);
		}
		entity.setPosition(nPos);

		if (Math.abs(entity.getTranslateX() - source.getTranslateX()) >= 350 || now - created > TimerManager.toNanos(Config.CONSUME_DECAY)) {
			entity.fireFXGLEvent(new FXGLEvent(Event.DEATH));
		}
	}
}
