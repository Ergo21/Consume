package com.almasb.consume.ai;

import com.almasb.consume.Config;
import com.almasb.consume.Event;
import com.almasb.fxgl.entity.AbstractControl;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.FXGLEvent;

public class BurnerControl extends AbstractControl {

	private Entity target;
	private boolean spearThrown;
	
	public BurnerControl(Entity target) {
		this.target = target;
		spearThrown = false;
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
		if (isTargetInRange() && !spearThrown) {
			spearThrown = true;
			entity.fireFXGLEvent(new FXGLEvent(Event.ENEMY_FIRED));
		}
	}
	
	public void setSpearThrown(boolean b){
		spearThrown = b;
	}

	@Override
	protected void initEntity(Entity entity) {
		// TODO Auto-generated method stub

	}

	private boolean isTargetInRange() {
		return target.getPosition().distance(entity.getPosition()) <= Config.ENEMY_FIRE_RANGE/2;
	}
}