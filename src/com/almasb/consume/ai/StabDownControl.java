package com.almasb.consume.ai;

import javafx.geometry.Point2D;

import com.almasb.consume.Config;
import com.almasb.consume.Event;
import com.almasb.fxgl.entity.AbstractControl;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.FXGLEvent;
import com.almasb.fxgl.time.TimerManager;

public class StabDownControl extends AbstractControl {

	private Entity source;
	private long created;

	public StabDownControl(Entity source) {
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
		nPos = nPos.add(source.getWidth()/2 - entity.getWidth()/2, source.getHeight());
		entity.setPosition(nPos);

		if (Math.abs(entity.getTranslateX() - source.getTranslateX()) >= 350 || now - created > TimerManager.toNanos(Config.STAB_DOWN_DECAY)) {
			entity.fireFXGLEvent(new FXGLEvent(Event.DEATH));
		}
	}
}
