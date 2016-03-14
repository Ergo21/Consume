package com.almasb.consume.ai;

import com.almasb.consume.Config;
import com.almasb.consume.Event;
import com.almasb.fxgl.asset.Texture;
import com.almasb.fxgl.entity.AbstractControl;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.FXGLEvent;
import com.almasb.fxgl.time.TimerManager;

public class PillarControl extends AbstractControl {

	private long created;
	private double pillarWidth;
	private double pillarHeight;
	private Texture sand;

	public PillarControl(Texture t) {
		created = 0;
		pillarWidth = 20;
		pillarHeight = 5;
		sand = t;
		sand.setPreserveRatio(false);
		sand.setTranslateY(2);
		sand.setFitWidth(pillarWidth);
		sand.setFitHeight(pillarHeight);
	}

	@Override
	protected void initEntity(Entity entity) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onUpdate(Entity entity, long now){
			actualUpdate(entity, now);
		
	}
	
	public void actualUpdate(Entity entity, long now) {
		if (created == 0) {
			created = now;
			entity.setGraphics(sand);
		}

		if(now - created > TimerManager.toNanos(Config.PILLAR_LIFE)*2/3){
			entity.setCollidable(true);
			entity.setPosition(entity.getPosition().add(0.25, -4));
			pillarHeight += 4;
			pillarWidth -= 0.5;
			if(pillarWidth <= 0){
				pillarWidth = 1;
				entity.setPosition(entity.getPosition().add(-0.5, 0));
			}
			sand.setFitWidth(pillarWidth);
			sand.setFitHeight(pillarHeight);
			entity.setGraphics(sand);
		}

		if (now - created > TimerManager.toNanos(Config.PILLAR_LIFE)) {
			entity.fireFXGLEvent(new FXGLEvent(Event.DEATH));
		}
	}
}
