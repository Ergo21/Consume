package com.almasb.consume.ai;

import com.almasb.consume.Config;
import com.almasb.consume.Event;
import com.almasb.consume.Config.Speed;
import com.almasb.fxgl.entity.AbstractControl;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.FXGLEvent;

public class SimpleMoveControl extends AbstractControl{
	
	private Entity target;
    private int vel = 0;
    private long firstTimeSaw = -1;		//Stores when the enemy first sees the player
    private boolean notFired = true;

    public SimpleMoveControl(Entity target) {
        this.target = target;
    }

	@Override
	public void onUpdate(Entity entity, long now) {
		if(isTargetInRange() && firstTimeSaw == -1){
			firstTimeSaw = now;
			vel = 0;
		}
		else if(firstTimeSaw != -1 && now - firstTimeSaw >= Config.ENEMY_FIRE_DELAY && notFired){
			entity.fireFXGLEvent(new FXGLEvent(Event.ENEMY_SAW_PLAYER));
			vel = 0;
			notFired = false;
		}
		else if(firstTimeSaw != -1 && now - firstTimeSaw < Config.ENEMY_FIRE_DELAY*2){
			vel = 0;
		}		
		else {
			vel = -Speed.ENEMY_PATROL;
		}

		entity.setTranslateX(entity.getTranslateX() + vel);
		if (entity.getTranslateX() - target.getTranslateX() <= -400) {
			firstTimeSaw = -1;
			entity.setTranslateX(target.getTranslateX() + 400);
			notFired = true;
        }
	}

	@Override
	protected void initEntity(Entity entity) {
		// TODO Auto-generated method stub
		
	}
	
	private boolean isTargetInRange() {
        return target.getPosition().distance(entity.getPosition()) <= Config.ENEMY_FIRE_RANGE;
    }
	
	public int getVelocity() {
        return vel;
    }
	
}