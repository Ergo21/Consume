package com.almasb.consume.ai;

import javafx.geometry.Point2D;

import com.almasb.consume.Config;
import com.almasb.consume.Event;
import com.almasb.fxgl.entity.AbstractControl;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.FXGLEvent;
import com.almasb.fxgl.time.TimerManager;

public class StabControl extends AbstractControl {

	private Entity source;
	private long created;

	public StabControl(Entity source) {
		this.source = source;
		created = 0;
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
		// PhysicsControl control = entity.getControl(PhysicsControl.class);
		if (created == 0) {
			created = now;
		}
		if(source != null && source.getPosition() != null && source.getProperty("facingRight") != null){
			Point2D nPos = source.getPosition();
			if (source.<Boolean>getProperty("facingRight")) {
				nPos = nPos.add(source.getWidth(), source.getHeight()/2 - entity.getHeight()/2);
			} else {
				nPos = nPos.add(-entity.getWidth(), source.getHeight()/2 - entity.getHeight()/2);
			}
			entity.setPosition(nPos);

			if (Math.abs(entity.getTranslateX() - source.getTranslateX()) >= 350 || now - created > TimerManager.toNanos(Config.CONSUME_DECAY)) {
				entity.fireFXGLEvent(new FXGLEvent(Event.DEATH));
			}
		}
		else{
			entity.fireFXGLEvent(new FXGLEvent(Event.DEATH));
		}
	}
}
