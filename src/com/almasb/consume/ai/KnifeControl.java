package com.almasb.consume.ai;

import javafx.geometry.Point2D;

import com.almasb.consume.Config;
import com.almasb.consume.Event;
import com.almasb.fxgl.entity.AbstractControl;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.FXGLEvent;
import com.almasb.fxgl.time.TimerManager;

public class KnifeControl extends AbstractControl {

	private Entity source;
	private long created;
	private int curStartDif = 0;

	public KnifeControl(Entity source) {
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

		if(source != null && source.getProperty("facingRight") != null && source.getPosition() != null && source.getControl(PhysicsControl.class) != null){
			Point2D nPos = source.getPosition();
			PhysicsControl pc = entity.getControl(PhysicsControl.class);
			PhysicsControl pcc = source.getControl(PhysicsControl.class);
			if ((boolean) source.getProperty("facingRight")) {
				entity.setPosition(nPos.add(curStartDif, 0));
				pc.moveX((int)pcc.getVelocity().getX() + 1);
			} else {
				entity.setPosition(nPos.add(-curStartDif, 0));
				pc.moveX((int)pcc.getVelocity().getX() - 1);
			}
			
			//pc.moveY((int)(nPos.getY() - entity.getPosition().getY()));
			if(curStartDif < 20){
				curStartDif++;
			}
			
			//entity.setPosition(nPos);
		}

		if (Math.abs(entity.getTranslateX() - source.getTranslateX()) >= 350 || now - created > TimerManager.toNanos(Config.CONSUME_DECAY)) {
			entity.fireFXGLEvent(new FXGLEvent(Event.DEATH));
		}
	}
}
