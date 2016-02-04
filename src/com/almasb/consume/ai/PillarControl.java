package com.almasb.consume.ai;

import javafx.scene.shape.Rectangle;

import com.almasb.consume.Config;
import com.almasb.consume.Event;
import com.almasb.fxgl.entity.AbstractControl;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.FXGLEvent;
import com.almasb.fxgl.time.TimerManager;

public class PillarControl extends AbstractControl {

	private long created;
	private int pillarHeight;

	public PillarControl() {
		created = 0;
		pillarHeight = 5;
	}

	@Override
	protected void initEntity(Entity entity) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onUpdate(Entity entity, long now) {
		if (created == 0) {
			created = now;
		}

		if(now - created > TimerManager.toNanos(Config.PILLAR_LIFE)/3){
			entity.setCollidable(true);
			entity.setPosition(entity.getPosition().add(0, -2));
			pillarHeight += 2;
			entity.setGraphics(new Rectangle(0,0,20,pillarHeight));
		}

		if (now - created > TimerManager.toNanos(Config.PILLAR_LIFE)) {
			entity.fireFXGLEvent(new FXGLEvent(Event.DEATH));
		}
	}
}
